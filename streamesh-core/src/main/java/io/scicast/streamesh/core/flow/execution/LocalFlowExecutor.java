package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.*;
import io.scicast.streamesh.core.exception.InvalidCmdParameterException;
import io.scicast.streamesh.core.exception.MissingParameterException;
import io.scicast.streamesh.core.flow.FlowDefinition;
import io.scicast.streamesh.core.flow.FlowInstance;
import io.scicast.streamesh.core.flow.FlowParameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public class LocalFlowExecutor implements FlowExecutor {

    private final StreameshContext context;
    private String flowInstanceId;
    private Consumer<FlowExecutionEvent<?>> upstreamFlowHandler;

    @Override
    public FlowInstance execute(FlowDefinition flow, String flowInstanceId, Map<?, ?> input, Consumer<FlowExecutionEvent<?>> eventHandler) {
        ExecutionGraph runtimeGraph = new ExecutionGraph(flow.getGraph());
        this.upstreamFlowHandler = eventHandler;
        this.flowInstanceId = flowInstanceId;

        FlowInstance instance = FlowInstance.builder()
            .definitionId(flow.getId())
            .flowName(flow.getName())
            .id(flowInstanceId)
            .executionGraph(runtimeGraph)
            .started(LocalDateTime.now())
            .status(FlowInstance.FlowInstanceStatus.LAUNCHING)
            .build();
        context.getStore().storeFlowInstance(instance);
        init(runtimeGraph, input);
        instance = instance.withStatus(FlowInstance.FlowInstanceStatus.RUNNING);
        if (allDone(instance)) {
            instance = instance.withStatus(FlowInstance.FlowInstanceStatus.COMPLETE)
                    .withCompleted(LocalDateTime.now());

        }
        context.getStore().storeFlowInstance(instance);
        return instance;
    }

    private boolean allDone(FlowInstance instance) {
        return instance.getExecutionGraph().getOutputNodes().stream()
                .allMatch(node -> node.getValue() != null && node.getValue().getParts().stream()
                        .allMatch(p -> p.getState().equals(RuntimeDataValue.DataState.COMPLETE)));
    }

    private void init(ExecutionGraph runtimeGraph, Map<?, ?> input) {
        runtimeGraph.getPipeInputNodes().forEach(node -> {
            if (node.isStaticallyInitialised()) {
                node.notifyObservers();
            }
        });

        Set<FlowParameterRuntimeNode> inputNodes = runtimeGraph.getInputNodes();
        inputNodes.forEach(node -> {
            FlowParameter parameterSpec = (FlowParameter) node.getStaticGraphNode().getValue();
            Object o = input.get(parameterSpec.getName());
            node.update(buildRuntimeDataValue(parameterSpec, o));
        });

        executeNodes(runtimeGraph.getExecutableNodes());
        checkFlowOutput(runtimeGraph.getOutputNodes());
    }

    private void checkFlowOutput(Set<FlowOutputRuntimeNode> outputNodes) {
        outputNodes.forEach(node -> {
            if (node.getValue() != null && !node.isOutputAlreadyConsumed()) {
                FlowExecutionEvent<?> event = FlowExecutionEvent.builder()
                        .type(FlowExecutionEvent.EventType.OUTPUT_AVAILABILITY)
                        .descriptor(OutputAvailabilityDescriptor.builder()
                            .flowInstanceId(flowInstanceId)
                            .nodeName(node.getName())
                            .runtimeDataValue(node.getValue())
                            .build())
                        .build();
                upstreamFlowHandler.accept(event);
            }
        });
    }

    private void executeNodes(Set<ExecutablePipeRuntimeNode> executableNodes) {
        StreameshOrchestrator orchestrator = context.getOrchestrator();

        executableNodes.forEach(node -> {
            node.setRunning(true);
            String executableId = node.getName() + "-" + UUID.randomUUID().toString();
            if (node instanceof MicroPipeRuntimeNode) {
                ((MicroPipeRuntimeNode) node).setTaskId(executableId);
                orchestrator.scheduleTask(node.getDefinitionId(),executableId, node.getPipeInput(), this::onTaskExecutionEvent);
            } else if (node instanceof FlowReferenceRuntimeNode){
                ((FlowReferenceRuntimeNode) node).setInstanceId(executableId);
                orchestrator.scheduleFlow(node.getDefinitionId(), executableId, node.getPipeInput(), this::onFlowExecutionEvent);
            }
        });
    }

    private void onFlowExecutionEvent(FlowExecutionEvent<?> event) {

    }

    private void onTaskExecutionEvent(TaskExecutionEvent<?> event) {
        boolean stateUpdated = false;
        FlowInstance instance = context.getStore().getFlowInstance(flowInstanceId);
        if (event.getType().equals(TaskExecutionEvent.EventType.CONTAINER_STATE_CHANGE)) {
            TaskDescriptor descriptor = (TaskDescriptor) event.getDescriptor();
            if (descriptor.getStatus().equals(TaskDescriptor.TaskStatus.COMPLETE)) {
                MicroPipeRuntimeNode targetNode = getTargetNode(instance, descriptor);
                updateTargetNode(descriptor, targetNode);
                stateUpdated = true;
            }
        }

        if (stateUpdated) {
            executeNodes(instance.getExecutionGraph().getExecutableNodes());
            checkFlowOutput(instance.getExecutionGraph().getOutputNodes());
            if (allDone(instance)) {
                instance = instance.withStatus(FlowInstance.FlowInstanceStatus.COMPLETE)
                        .withCompleted(LocalDateTime.now());
            }
            context.getStore().storeFlowInstance(instance);
        }

    }

    private void updateTargetNode(TaskDescriptor descriptor, MicroPipeRuntimeNode targetNode) {
        Set<RuntimeDataValue.RuntimeDataValuePart> parts = ((MicroPipe) targetNode.getStaticGraphNode().getValue()).getOutputMapping().stream()
                .map(taskOutput -> RuntimeDataValue.RuntimeDataValuePart.builder()
                        .refName(taskOutput.getName())
                        .state(RuntimeDataValue.DataState.COMPLETE)
                        .value(context.getServerInfo().getBaseUrl() + "/tasks/" + descriptor.getId() + "/" + taskOutput.getName())
                        .build())
                .collect(Collectors.toSet());

        targetNode.update(RuntimeDataValue.builder()
            .parts(parts)
            .build());
    }

    private MicroPipeRuntimeNode getTargetNode(FlowInstance instance, TaskDescriptor descriptor) {
        return instance.getExecutionGraph().getNodes().stream()
            .filter(n -> n instanceof MicroPipeRuntimeNode)
            .map(n -> (MicroPipeRuntimeNode) n)
            .filter(n -> descriptor.getId().equals(n.getTaskId()))
            .findFirst()
            .orElse(null);
}

    private RuntimeDataValue buildRuntimeDataValue(FlowParameter parameterSpec, Object o) {
        if (!parameterSpec.isOptional() && o == null) {
            throw new MissingParameterException(String.format("Parameter %s is mandatory.", parameterSpec.getName()));
        }
        if (parameterSpec.isRepeatable() && (!List.class.isAssignableFrom(o.getClass()))) {
            throw new InvalidCmdParameterException(String.format("Parameter %s must be provided as an array", parameterSpec.getName()));
        }

        RuntimeDataValue.RuntimeDataValueBuilder builder = RuntimeDataValue.builder();
        Set<RuntimeDataValue.RuntimeDataValuePart> parts;
        if (!parameterSpec.isRepeatable()) {
            parts = Stream.of(RuntimeDataValue.RuntimeDataValuePart.builder()
                    .state(RuntimeDataValue.DataState.COMPLETE)
                    .refName(parameterSpec.getName())
                    .value((String) o)
                    .build())
                .collect(Collectors.toSet());
        } else {
            parts = ((List<String>) o).stream()
                    .map(s -> RuntimeDataValue.RuntimeDataValuePart.builder()
                        .value(s)
                        .refName(parameterSpec.getName())
                        .state(RuntimeDataValue.DataState.COMPLETE)
                        .build())
                    .collect(Collectors.toSet());
        }
        return builder.parts(parts)
                .build();
    }

}
