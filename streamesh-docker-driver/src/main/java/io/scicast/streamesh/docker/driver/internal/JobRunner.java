package io.scicast.streamesh.docker.driver.internal;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.model.Container;
import io.scicast.streamesh.core.JobDescriptor;

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

public class JobRunner {


    private static final String CONTAINER_NOT_FOUND_MSG = "Could not locate container with id %s for job %s";
    private DockerClient client;
    private JobDescriptor descriptor;
    Consumer<JobDescriptor> onStatusUpdate;

    private Logger logger = Logger.getLogger(getClass().getName());

    public JobRunner(DockerClient client, JobDescriptor descriptor, Consumer<JobDescriptor> onStatusUpdate) {
        this.client = client;
        this.descriptor = descriptor;
        this.onStatusUpdate = onStatusUpdate;
    }

    public void init() {
        Optional<Container> c = findContainer();
        if (c.isEmpty()) {
            descriptor = descriptor.withStatus(JobDescriptor.JobStatus.FAILED)
                    .withErrorMessage(
                            String.format(CONTAINER_NOT_FOUND_MSG, descriptor.getContainerId(), descriptor.getId()));
            onStatusUpdate.accept(descriptor);
            return;
        }

        StartContainerCmd start = client.startContainerCmd(descriptor.getContainerId());
        ExecutorService svc = Executors.newSingleThreadExecutor();
        Future<?> startFut = svc.submit(() -> {
            new StartObserver(descriptor, jd -> {
                onStatusUpdate.accept(jd);
            });
            try {
                start.exec();
                if(!descriptor.getStatus().equals(JobDescriptor.JobStatus.COMPLETE)) {
                    descriptor = descriptor.withStatus(JobDescriptor.JobStatus.COMPLETE);
                    onStatusUpdate.accept(descriptor);
                }
            } catch (Exception e) {
               descriptor = descriptor.withStatus(JobDescriptor.JobStatus.FAILED)
                       .withErrorMessage(e.getMessage());
               onStatusUpdate.accept(descriptor);
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
    }


    private Optional<Container> findContainer() {
        return client.listContainersCmd()
                .withShowAll(true)
                .withIdFilter(Arrays.asList(descriptor.getContainerId())).exec()
                .stream()
                .findFirst();
    }


    class StartObserver {

        private JobDescriptor descriptor;
        private Consumer<JobDescriptor> onContainerStateChange;

        StartObserver(JobDescriptor descriptor, Consumer<JobDescriptor> onContainerStateChange) {
            this.descriptor = descriptor;
            this.onContainerStateChange = onContainerStateChange;
            track();
        }

        private void track() {
            TimerTask repeatedTask = new TimerTask() {
                public void run() {
                    Optional<Container> container = findContainer();
                    if(container.isPresent()) {
                        String state = container.get().getState();
                        String ststus = container.get().getStatus();
                        if (state.equalsIgnoreCase("running") || state.equalsIgnoreCase("exited")) {
                            logger.info("Container " + descriptor.getContainerId() + " is in state " + state);
                            descriptor = descriptor.withStatus(state.equalsIgnoreCase("running") ? JobDescriptor.JobStatus.RUNNING : JobDescriptor.JobStatus.COMPLETE);
                            onContainerStateChange.accept(descriptor);
                            this.cancel();
                        }
                    }
                }
            };
            Timer timer = new Timer("Container " + descriptor.getContainerId() + " monitor");
            timer.scheduleAtFixedRate(repeatedTask, 0, 1000);
        }


    }

}
