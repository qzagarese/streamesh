package io.scicast.streamesh.core.internal;

import io.scicast.streamesh.core.*;
import io.scicast.streamesh.core.crypto.CryptoUtil;
import io.scicast.streamesh.core.exception.InvalidCmdParameterException;
import io.scicast.streamesh.core.exception.MissingParameterException;
import io.scicast.streamesh.core.exception.NotFoundException;
import io.scicast.streamesh.core.flow.FlowDefinition;
import io.scicast.streamesh.core.flow.FlowInstance;
import io.scicast.streamesh.core.flow.execution.FlowExecutionEvent;
import io.scicast.streamesh.core.flow.execution.LocalFlowExecutor;
import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowGraphBuilder;
import io.scicast.streamesh.core.internal.reflect.Scope;
import io.scicast.streamesh.core.internal.reflect.ScopeFactory;

import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultStreameshOrchestrator implements StreameshOrchestrator {

    private final StreameshStore streameshStore = new InMemoryStreameshStore();
    private Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private final StreameshContext context;
    private final ScopeFactory scopeFactory;

    private OrchestrationDriver driver;

    public DefaultStreameshOrchestrator(String serverIpAddress) {
        ServiceLoader<OrchestrationDriver> loader = ServiceLoader.load(OrchestrationDriver.class);

       driver = StreamSupport.stream(loader.spliterator(), false)
                .peek(impl -> logger.info(
                        "Found orchestration driver of type " + impl.getClass().getSimpleName()))
                .findFirst()
                .map(impl -> {
                    logger.info("Using orchestration driver " + impl.getClass().getSimpleName());
                    return impl;
                })
                .orElseThrow(() -> new RuntimeException("No orchestration driver. Booting sequence aborted."));

        context = StreameshContext.builder()
                .orchestrationDriver(driver)
                .store(streameshStore)
                .orchestrator(this)
                .streameshServerAddress(serverIpAddress)
                .build();

        scopeFactory = ScopeFactory.builder()
                .streameshContext(context)
                .build();
    }

    public String applyDefinition(Definition definition) {
        if (definition instanceof MicroPipe) {
            return applyMicroPipe((MicroPipe) definition);
        } else if (definition instanceof FlowDefinition){
            return applyFlowDefinition((FlowDefinition) definition);
        } else {
            throw new NotFoundException("Unrecognized definition type " + definition.getType());
        }
    }

    private String applyFlowDefinition(FlowDefinition definition) {
        String definitionId = UUID.randomUUID().toString();
        Scope scope = scopeFactory.create(definition);
        FlowGraph graph = new FlowGraphBuilder().build(scope);

        streameshStore.storeDefinition(definition.withId(definitionId)
            .withGraph(graph)
            .withScope(scope));
        return definitionId;
    }



    private String applyMicroPipe(MicroPipe micropipe) {
        String imageId = driver.retrieveContainerImage(micropipe.getImage());
        String definitionId = UUID.randomUUID().toString();
        streameshStore.storeDefinition(micropipe.withImageId(imageId)
                .withId(definitionId));
        return definitionId;
    }

    public Definition getDefinition(String id) {
        Definition definition = streameshStore.getDefinitionById(id);
        if(definition == null) {
            throw new NotFoundException(String.format("No definition with id %s found", id));
        }
        return definition;
    }

    public Definition getDefinitionByName(String name) {
        Definition definition = streameshStore.getDefinitionByName(name);
        if(definition == null) {
            throw new NotFoundException(String.format("No definition found for name %s", name));
        }
        return definition;
    }

    public void removeDefinition(String id) {
        streameshStore.removeDefinition(id);
    }

    public Set<Definition> getDefinitions() {
        return streameshStore.getAllDefinitions();
    }

    public Set<TaskDescriptor> getAllTasks() {
        return streameshStore.getAllTasks();
    }

    public Set<TaskDescriptor> getTasksByDefinition(String definitionId) {
        return streameshStore.getTasksByDefinition(definitionId);
    }

    public TaskDescriptor scheduleTask(String definitionId, Map<?, ?> input) {
        return scheduleTask(definitionId, input, event -> {});
    }

    public TaskDescriptor scheduleTask(String definitionId, Map<?, ?> input, Consumer<TaskExecutionEvent<?>> eventHandler) {
        Definition definition = getDefinition(definitionId);
        if (!(definition instanceof MicroPipe)) {
            throw new IllegalArgumentException("Cannot schedule tasks for definitions of type " + definition.getType());
        }
        MicroPipe pipe = (MicroPipe) definition;
        TaskDescriptor descriptor = driver.scheduleTask(
                TaskExecutionIntent.builder()
                    .image(pipe.getImage())
                    .command(buildCommand(pipe, input))
                    .taskOutputs(pipe.getOutputMapping())
                    .build(),
                event -> {
                    updateState(pipe, event);
                    eventHandler.accept(event);
                },
                context)
                .withServiceName(definition.getName())
                .withServiceId(definition.getId());
        updateIndexes(pipe, descriptor);
        return descriptor;
    }

    public FlowInstance scheduleFlow(String definitionId, Map<?, ?> input) {
        return scheduleFlow(definitionId, input, event -> {});
    }

    public FlowInstance scheduleFlow(String definitionId, Map<?, ?> input, Consumer<FlowExecutionEvent<?>> eventHandler) {
        Definition definition = getDefinition(definitionId);
        if (!(definition instanceof FlowDefinition)) {
            throw new IllegalArgumentException("Cannot schedule flows for definitions of type " + definition.getType());
        }
        return new LocalFlowExecutor(context).execute((FlowDefinition) definition, input, eventHandler);
    }

    public TaskDescriptor scheduleSecureTask(String definitionId, Map<?, ?> input, String publicKey) {
        CryptoUtil.WrappedAesGCMKey wrappedKey = CryptoUtil.createWrappedKey(publicKey);
        TaskDescriptor descriptor = scheduleTask(definitionId, input);
        descriptor.setKey(wrappedKey);
        return descriptor;
    }

    public TaskDescriptor getTask(String taskId) {
        TaskDescriptor job = streameshStore.getTaskById(taskId);
        if (job == null) {
            throw new NotFoundException(String.format("No job found for id %s", taskId));
        }
        return job;
    }

    private void updateState(MicroPipe definition, TaskExecutionEvent<?> event) {
        if (event.getType().equals(TaskExecutionEvent.EventType.CONTAINER_STATE_CHANGE)) {
            TaskDescriptor descriptor = (TaskDescriptor) event.getDescriptor();
            updateIndexes(definition, descriptor);
        }
    }

    private void updateIndexes(MicroPipe definition, TaskDescriptor descriptor) {
        streameshStore.updateTask(definition.getId(),
                descriptor.withServiceId(definition.getId())
                        .withServiceName(definition.getName()));
    }

    private String buildCommand(MicroPipe definition, Map<?, ?> input) {
        TaskInput inputMapping = definition.getInputMapping();
        String params = inputMapping.getParameters().stream()
                .map(p -> {
                    Object o = input.get(p.getName());
                    if (!p.isOptional() && o == null) {
                        throw new MissingParameterException(String.format("Parameter %s is mandatory.", p.getName()));
                    }
                    if (p.isRepeatable() && (!List.class.isAssignableFrom(o.getClass()))) {
                        throw new InvalidCmdParameterException(String.format("Parameter %s must be provided as an array", p.getName()));
                    }
                    if (!p.isRepeatable()) {
                        String value = (String) o;
                        return p.getInternalName().trim() + " " + value.trim();
                    } else {
                        List<String> value = (List<String>) o;
                        return value.stream()
                                .map(v -> p.getInternalName().trim() + " " + v.trim())
                                .collect(Collectors.joining(" "));
                    }
                }).collect(Collectors.joining(" "));
        return (inputMapping.getBaseCmd().trim() + " " + params.trim()).trim();
    }

    public InputStream getTaskOutput(String taskDescriptorId, String outputName) {
        TaskDescriptor job = getTask(taskDescriptorId);
        InputStream stream = driver.getTaskOutput(taskDescriptorId, outputName);
        if (job.getKey() != null) {
            stream = CryptoUtil.getCipherInputStream(stream, job.getKey());
        }
        return stream;
    }
}
