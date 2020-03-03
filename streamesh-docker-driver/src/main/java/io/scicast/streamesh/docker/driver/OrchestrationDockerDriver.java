package io.scicast.streamesh.docker.driver;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import io.scicast.streamesh.core.TaskDescriptor;
import io.scicast.streamesh.core.OrchestrationDriver;
import io.scicast.streamesh.core.OutputMapping;
import io.scicast.streamesh.core.exception.NotFoundException;
import io.scicast.streamesh.docker.driver.internal.JobRunner;
import io.scicast.streamesh.docker.driver.internal.DockerClientProviderFactory;
import io.scicast.streamesh.docker.driver.internal.DockerPullStatusManager;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class OrchestrationDockerDriver implements OrchestrationDriver {

    public static final String TMP_DIR_PROPERTY = "java.io.tmpdir";
    public static final String STREAMESH_DIR = "streamesh";
    public static final String STREAMESH_SERVER_HOST_NAME = "streamesh-server";
    private Logger logger = Logger.getLogger(getClass().getName());
    private DockerClient client = DockerClientProviderFactory.create().getClient();

    private Map<String, List<JobOutputManager>> outputManagers = new HashMap<>();
    private String streameshServerAddress;

    public String retrieveContainerImage(String imageName) {
        CompletableFuture<String> respFut = new CompletableFuture<>();
        CompletableFuture<String> pullFut;
        Optional<Image> image = findImage(computeImageName(imageName), client);
        if (!image.isPresent()) {
            pullFut = pullImage(imageName, client);
        } else {
            pullFut = CompletableFuture.completedFuture(image.get().getId());
        }

        pullFut.exceptionally(ex -> {
            String msg = String.format("An error occurred while pulling image %s - %s",
                    imageName,
                    ex.getMessage());
            logger.warning(msg);
            respFut.completeExceptionally(new RuntimeException(msg));
            return null;
        }).thenAccept(imgId -> {
            respFut.complete(imgId);
        });
        respFut.exceptionally(ex -> {
            throw new RuntimeException("Could not retrieve the specified image", ex.getCause());
        });
        return respFut.join();
    }

    public TaskDescriptor scheduleTask(String image, String command, List<OutputMapping> outputMapping, Consumer<TaskDescriptor> onStatusUpdate) {
        TaskDescriptor descriptor = TaskDescriptor.builder()
                .id(UUID.randomUUID().toString())
                .build();
        String parentOutputDirectory = createOutputDirectory(descriptor.getId(),
                System.getProperty(TMP_DIR_PROPERTY) + File.separator +STREAMESH_DIR);

        List<JobOutputManager> managersList = new ArrayList<>();

        AtomicReference<CreateContainerCmd> create = new AtomicReference<>(client.createContainerCmd(image));
        create.set(create.get().withCmd(command.split(" ")));
        outputMapping.forEach(om -> {
            String outputDirectory = createOutputDirectory(om.getName(), parentOutputDirectory);
            create.set(setupOutputVolume(create.get(), outputDirectory, om.getOutputDir()));
            JobOutputManager manager = new JobOutputManager(om.getName(), outputDirectory + File.separator + om.getFileNamePattern());
            managersList.add(manager);
        });
        create.set(setupServerIpMapping(create.get(), streameshServerAddress));

        CreateContainerResponse createContainerResponse = create.get().exec();
        descriptor = descriptor.withContainerId(createContainerResponse.getId());

        outputManagers.put(descriptor.getId(), managersList);

        JobRunner runner = new JobRunner(client, descriptor, jd -> {
            onStatusUpdate.accept(this.handleUpdate(jd));
        });
        return runner.init();
    }

    private CreateContainerCmd setupServerIpMapping(CreateContainerCmd cmd, String streameshServerAddress) {
        List<String> extraHosts = Optional.ofNullable(cmd.getHostConfig().getExtraHosts())
                .map(Arrays::asList)
                .map(ArrayList::new)
                .orElse(new ArrayList<>());
        extraHosts.add(STREAMESH_SERVER_HOST_NAME + ":" + streameshServerAddress);
        HostConfig hc = cmd.getHostConfig().withExtraHosts(extraHosts.toArray(new String[0]));
        return cmd.withHostConfig(hc);

    }

    private TaskDescriptor handleUpdate(TaskDescriptor descriptor) {
        if (descriptor.getStatus().equals(TaskDescriptor.JobStatus.COMPLETE)) {
            descriptor = descriptor.withExited(LocalDateTime.now());
            List<JobOutputManager> managers = outputManagers.get(descriptor.getId());
            managers.forEach(m -> m.notifyTermination());
        }
        return descriptor;
    }


    private CreateContainerCmd setupOutputVolume(CreateContainerCmd cmd, String hostOutputPath, String containerOutputPath) {
        List<Bind> binds = Optional.ofNullable(cmd.getHostConfig().getBinds())
                .map(Arrays::asList)
                .map(ArrayList::new)
                .orElse(new ArrayList<>());

        binds.add(new Bind(hostOutputPath, new Volume(containerOutputPath), AccessMode.rw));
        HostConfig hc = cmd.getHostConfig().withBinds(binds);
        return cmd.withHostConfig(hc);
    }

    private String createOutputDirectory(String name, String parentDir) {
        File dir = new File(parentDir
                + File.separator
                + name + File.separator);
        boolean dirsCreated = dir.mkdirs();
        if (!dirsCreated) {
            throw new RuntimeException("Could not create output directory for job " + name);
        }

        return  dir.getAbsolutePath();
    }

    public InputStream getTaskOutput(String taskId, String outputName) {
        return outputManagers.get(taskId).stream()
                .filter(om -> om.getOutputName().equalsIgnoreCase(outputName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        String.format("No output named %s for taskId %s available.", outputName, taskId)))
                .requestStream();
    }

    public void setStreameshServerAddress(String ipAddress) {
        this.streameshServerAddress = ipAddress;
    }

    private String computeImageName(String cmdImageName) {
        return Arrays.asList(cmdImageName).stream()
                .filter(s -> s.lastIndexOf("/") > s.lastIndexOf(":"))
                .findFirst()
                .map(s -> s += ":latest")
                .orElse(cmdImageName);
    }

    private Optional<Image> findImage(String imageName, DockerClient client) {
        ListImagesCmd imagesCmd = client.listImagesCmd().withImageNameFilter(imageName);
        List<Image> imagesList = imagesCmd.exec();
        if (imagesList == null || imagesList.isEmpty()) {
            return Optional.empty();
        }

        return imagesList.stream()
                .findFirst();
    }

    private CompletableFuture<String> pullImage(String imageName, DockerClient client) {
        PullImageCmd pullImageCmd = client.pullImageCmd(imageName);
        CompletableFuture<String> pullFut = new CompletableFuture<>();
        ResultCallback<PullResponseItem> resultCallback = new ResultCallback<PullResponseItem>() {

            private Closeable closeable;
            private DockerPullStatusManager manager = new DockerPullStatusManager(imageName);

            @Override
            public void close() throws IOException {
                try {
                    closeable.close();
                } catch (IOException e) {
                    throw new RuntimeException("Cannot close closeable " + closeable, e);
                }
            }

            @Override
            public void onStart(Closeable closeable) {
                this.closeable = closeable;
            }

            @Override
            public void onNext(PullResponseItem object) {
                System.out.print(manager.update(object));
            }

            @Override
            public void onError(Throwable throwable) {
                pullFut.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                Optional<Image> image = findImage(imageName, client);
                pullFut.complete(image.get().getId());
            }

        };
        pullImageCmd.exec(resultCallback);
        return pullFut;
    }
}
