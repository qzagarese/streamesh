package io.scicast.streamesh.docker.driver.internal;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.model.Container;
import io.scicast.streamesh.core.TaskDescriptor;
import io.scicast.streamesh.core.TaskExecutionEvent;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class TaskRunner {


    private static final String CONTAINER_NOT_FOUND_MSG = "Could not locate container with id %s for job %s";
    private DockerClient client;
    private TaskDescriptor descriptor;
    Consumer<TaskExecutionEvent<?>> onStatusUpdate;

    private Logger logger = Logger.getLogger(getClass().getName());

    public TaskRunner(DockerClient client, TaskDescriptor descriptor, Consumer<TaskExecutionEvent<?>> onStatusUpdate) {
        this.client = client;
        this.descriptor = descriptor;
        this.onStatusUpdate = onStatusUpdate;
    }

    public TaskDescriptor init() {
        Optional<Container> c = findContainer();
        if (c.isEmpty()) {
            descriptor = descriptor.withStatus(TaskDescriptor.TaskStatus.FAILED)
                    .withErrorMessage(
                            String.format(CONTAINER_NOT_FOUND_MSG, descriptor.getContainerId(), descriptor.getId()));
            onStatusUpdate.accept(TaskExecutionEvent.builder()
                    .type(TaskExecutionEvent.EventType.CONTAINER_STATE_CHANGE)
                    .descriptor(descriptor)
                    .build());
            return descriptor;
        }

        StartContainerCmd start = client.startContainerCmd(descriptor.getContainerId());
        ExecutorService svc = Executors.newSingleThreadExecutor();
        Future<?> startFut = svc.submit(() -> {
            new StartObserver(jd -> {
                onStatusUpdate.accept(jd);
            });
            try {
                start.exec();
                descriptor = descriptor.withStatus(TaskDescriptor.TaskStatus.RUNNING);
                descriptor = descriptor.withStarted(LocalDateTime.now());
                onStatusUpdate.accept(TaskExecutionEvent.builder()
                    .type(TaskExecutionEvent.EventType.CONTAINER_STATE_CHANGE)
                    .descriptor(descriptor)
                    .build());
            } catch (Exception e) {
               descriptor = descriptor.withStatus(TaskDescriptor.TaskStatus.FAILED)
                    .withErrorMessage(e.getMessage());
               onStatusUpdate.accept(TaskExecutionEvent.builder()
                    .type(TaskExecutionEvent.EventType.CONTAINER_STATE_CHANGE)
                    .descriptor(descriptor)
                    .build());
            }
        });
        try {
            startFut.get();
            svc.shutdown();
        } catch (InterruptedException e) {
            logger.warning(e.getMessage());
        } catch (ExecutionException e) {
            logger.warning(e.getMessage());
        }
        return descriptor;
    }


    private Optional<Container> findContainer() {
        return client.listContainersCmd()
                .withShowAll(true)
                .withIdFilter(Arrays.asList(descriptor.getContainerId())).exec()
                .stream()
                .findFirst();
    }


    class StartObserver {

        private Consumer<TaskExecutionEvent<?>> onContainerStateChange;

        StartObserver(Consumer<TaskExecutionEvent<?>> onContainerStateChange) {
            this.onContainerStateChange = onContainerStateChange;
            track();
        }

        private void track() {
            TimerTask repeatedTask = new TimerTask() {
                public void run() {
                    Optional<Container> container = findContainer();
                    if(container.isPresent()) {
                        String state = container.get().getState();
                        if (state.equalsIgnoreCase("running") || state.equalsIgnoreCase("exited")) {
                            logger.finest("Container " + descriptor.getContainerId() + " is in state " + state);
                            descriptor = descriptor.withStatus(state.equalsIgnoreCase("running") ? TaskDescriptor.TaskStatus.RUNNING : TaskDescriptor.TaskStatus.COMPLETE);
                            onContainerStateChange.accept(TaskExecutionEvent.builder()
                                .type(TaskExecutionEvent.EventType.CONTAINER_STATE_CHANGE)
                                .descriptor(descriptor)
                                .build());
                            if (descriptor.getStatus().equals(TaskDescriptor.TaskStatus.COMPLETE)) {
                                this.cancel();
                            }
                        }
                    }
                }
            };
            Timer timer = new Timer("Container " + descriptor.getContainerId() + " monitor");
            timer.scheduleAtFixedRate(repeatedTask, 0, 1000);
        }


    }

}
