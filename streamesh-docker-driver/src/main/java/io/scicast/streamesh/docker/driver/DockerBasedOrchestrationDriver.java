package io.scicast.streamesh.docker.driver;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.model.*;
import io.scicast.streamesh.core.*;
import io.scicast.streamesh.core.exception.NotFoundException;
import io.scicast.streamesh.docker.driver.internal.DockerClientProviderFactory;
import io.scicast.streamesh.docker.driver.internal.DockerPullStatusManager;
import io.scicast.streamesh.docker.driver.internal.TaskRunner;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DockerBasedOrchestrationDriver implements OrchestrationDriver {

    public static final String TMP_DIR_PROPERTY = "java.io.tmpdir";
    public static final String STREAMESH_DIR = "streamesh";
    public static final String STREAMESH_SERVER_HOST_NAME = "streamesh-server";
    private Logger logger = Logger.getLogger(getClass().getName());
    private DockerClient client = DockerClientProviderFactory.create().getClient();

    private Map<String, List<TaskOutputManager>> outputManagers = new HashMap<>();

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

    @Override
    public TaskDescriptor scheduleTask(TaskExecutionIntent intent,
                                       Consumer<TaskExecutionEvent<?>> onStatusUpdate,
                                       StreameshContext context) {
        TaskDescriptor descriptor = TaskDescriptor.builder()
                .id(intent.getTaskId())
                .build();
        String parentOutputDirectory = createOutputDirectory(descriptor.getId(),
                System.getProperty(TMP_DIR_PROPERTY) + File.separator +STREAMESH_DIR);

        List<TaskOutputManager> managersList = new ArrayList<>();

        AtomicReference<CreateContainerCmd> create = new AtomicReference<>(client.createContainerCmd(intent.getImage()));
        create.set(create.get().withCmd(buildCommand(intent.getTaskInput(), intent.getRuntimeInput())));
        intent.getTaskOutputs().forEach(om -> {
            String outputDirectory = createOutputDirectory(om.getName(), parentOutputDirectory);
            create.set(setupOutputVolume(create.get(), outputDirectory, om.getOutputDir()));
            TaskOutputManager manager = new TaskOutputManager(om.getName(), outputDirectory + File.separator + om.getFileNamePattern());
            managersList.add(manager);
        });
        create.set(setupServerIpMapping(create.get(), context.getServerInfo()));

        CreateContainerResponse createContainerResponse = create.get().exec();
        descriptor = descriptor.withContainerId(createContainerResponse.getId());

        outputManagers.put(descriptor.getId(), managersList);

        TaskRunner runner = new TaskRunner(client, descriptor, event -> {
            if (event.getType().equals(TaskExecutionEvent.EventType.CONTAINER_STATE_CHANGE)) {
                event = TaskExecutionEvent.builder()
                        .type(event.getType())
                        .descriptor(handleUpdate((TaskDescriptor) event.getDescriptor()))
                        .build();
            }
            onStatusUpdate.accept(event);
        });
        return runner.init();
    }

    private List<String> buildCommand(TaskInput taskInput, Map<?, ?> runtimeInput) {
        List<String> cmd = Arrays.asList(taskInput.getBaseCmd().trim().split(" "));
        // TODO perform better parsing of base cmd to detect quoted strings in string

        List<String> parameters = taskInput.getParameters().stream()
                .flatMap(p -> {
                    if (!p.isRepeatable()) {
                        String value = (String) runtimeInput.get(p.getName());
                        return Arrays.asList(p.getInternalName().trim(), value.trim()).stream();
                    } else {
                        List<String> value = (List<String>) runtimeInput.get(p.getName());
                        return value.stream()
                                .map(v -> new ArrayList<>(Arrays.asList(p.getInternalName().trim(), v.trim())))
                                .flatMap(arg -> arg.stream());
                    }
                })
                .collect(Collectors.toList());
        return Stream.concat(cmd.stream(), parameters.stream()).collect(Collectors.toList());
    }

    @Override
    public void killTask(String taskId, StreameshContext context) {
        TaskDescriptor descriptor = context.getStore().getTaskById(taskId);
        if (descriptor == null) {
            throw new NotFoundException("Cannot find the task specified by id " + taskId);
        }
        try {
            client.removeContainerCmd(descriptor.getContainerId()).withForce(true).exec();
        } catch (com.github.dockerjava.api.exception.NotFoundException e) {
            logger.info(String.format("Container %s has already been deleted.", descriptor.getContainerId()));
        }
        context.getStore().updateTask(descriptor.getServiceId(), descriptor.withStatus(TaskDescriptor.TaskStatus.KILLED));
    }

    private CreateContainerCmd setupServerIpMapping(CreateContainerCmd cmd, StreameshServerInfo serverInfo) {
        List<String> extraHosts = Optional.ofNullable(cmd.getHostConfig().getExtraHosts())
                .map(Arrays::asList)
                .map(ArrayList::new)
                .orElse(new ArrayList<>());
        extraHosts.add(serverInfo.getHost() + ":" + serverInfo.getIpAddress());
        HostConfig hc = cmd.getHostConfig().withExtraHosts(extraHosts.toArray(new String[0]));
        return cmd.withHostConfig(hc);

    }

    private TaskDescriptor handleUpdate(TaskDescriptor descriptor) {
        if (descriptor.getStatus().equals(TaskDescriptor.TaskStatus.COMPLETE)) {
            descriptor = descriptor.withExited(LocalDateTime.now());
            List<TaskOutputManager> managers = outputManagers.get(descriptor.getId());
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
