package io.scicast.streamesh.docker.driver;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import io.scicast.streamesh.core.JobDescriptor;
import io.scicast.streamesh.core.OrchestrationDriver;
import io.scicast.streamesh.core.OutputMapping;
import io.scicast.streamesh.docker.driver.internal.ContainerTracker;
import io.scicast.streamesh.docker.driver.internal.DockerClientProviderFactory;
import io.scicast.streamesh.docker.driver.internal.DockerPullStatusManager;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class OrchestrationDockerDriver implements OrchestrationDriver {

    public static final String TMP_DIR_PROPERTY = "java.io.tmpdir";
    public static final String STREAMESH_DIR = "streamesh";
    public static final String OUTPUT_FILE_NAME = "output";
    private Logger logger = Logger.getLogger(getClass().getName());
    private DockerClient client = DockerClientProviderFactory.create().getClient();

    private Map<String, JobDescriptor> jobs = new HashMap<>();
    private Map<String, ContainerTracker> containers = new HashMap<>();
    private Map<String, Consumer<JobDescriptor>> updateListeners = new HashMap<>();

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

    public JobDescriptor scheduleJob(String image, String command, OutputMapping outputMapping, Consumer<JobDescriptor> onStatusUpdate) {
        JobDescriptor descriptor = JobDescriptor.builder()
                .id(UUID.randomUUID().toString())
                .build();
        updateListeners.put(descriptor.getId(), onStatusUpdate);


        String hostOutputFilePath = createOutputFile(descriptor.getId());


        String redirectedContainerOutputPath = "/tmp/" + OUTPUT_FILE_NAME;
        if(outputMapping.getLocationType().equals(OutputMapping.OutputLocationType.STDOUT)) {
            command += " > " + redirectedContainerOutputPath;
        }

        CreateContainerCmd create = client.createContainerCmd(image);
        create = create.withCmd(command.split(" "));
        String containerOutputPath = outputMapping.getLocationType().equals(OutputMapping.OutputLocationType.STDOUT)
                ? redirectedContainerOutputPath
                : outputMapping.getOutputDir() + File.separator + outputMapping.getOutputFileName();
        create = setupOutputVolume(create, hostOutputFilePath, containerOutputPath);
        CreateContainerResponse createContainerResponse = create.exec();
        descriptor = descriptor.withContainerId(createContainerResponse.getId());
        StartContainerCmd start = client.startContainerCmd(createContainerResponse.getId());
        start.exec();


        return descriptor;
    }

    private CreateContainerCmd setupOutputVolume(CreateContainerCmd cmd, String hostOutputPath, String containerOutputPath) {
        List<Bind> binds = Optional.ofNullable(cmd.getHostConfig().getBinds())
                .map(Arrays::asList)
                .map(ArrayList::new)
                .orElse(new ArrayList<>());

        binds.add(new Bind(hostOutputPath, new Volume(containerOutputPath), AccessMode.fromBoolean(true)));
        HostConfig hc = cmd.getHostConfig().withBinds(binds);
        return cmd.withHostConfig(hc);
    }

    private String createOutputFile(String jobId) {
        File dir = new File(System.getProperty(TMP_DIR_PROPERTY)
                + File.separator
                + STREAMESH_DIR
                + File.separator
                + jobId + File.separator);
        boolean dirsCreated = dir.mkdirs();
        if (!dirsCreated) {
            throw new RuntimeException("Could not create output directory for job " + jobId);
        }

        File outputFile = new File(dir, OUTPUT_FILE_NAME);
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Could not create output file for job " + jobId);
        }
        return  outputFile.getAbsolutePath();
    }

    public InputStream getJobOutput(String jobId) {
        return null;
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
