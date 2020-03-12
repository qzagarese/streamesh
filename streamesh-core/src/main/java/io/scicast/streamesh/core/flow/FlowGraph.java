package io.scicast.streamesh.core.flow;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class FlowGraph {

    private Set<FlowNode> nodes = new HashSet<>();

    public void createNode(String name, boolean executable) {
        FlowNode node = FlowNode.builder()
                .name(name)
                .executable(executable)
                .build();
        if (nodes.contains(node)) {
            throw new IllegalArgumentException("Duplicate nodes named " + name);
        }
        nodes.add(node);
    }

    public void connect(String downStream, String upStream, String downStreamInput, String upStreamOutput) {
        FlowNode from = getNode(upStream);
        FlowNode to = getNode(downStream);

        FlowEdge edge = FlowEdge.builder()
                .destination(to)
                .source(from)
                .destinationInputName(downStreamInput)
                .sourceOutputName(upStreamOutput)
                .build();

        if (cycleDetected(from, to)) {
            throw new IllegalArgumentException(
                    String.format("Your flow contains cycles including nodes %s ans %s", from.getName(), to.getName()));
        }
        to.addIncomingLink(edge);
        from.addOutgoingLink(edge);

    }

    private boolean cycleDetected(FlowNode from, FlowNode to) {
        return directlyConnectedTo(to, from)
                || from.getIncomingLinks().stream()
                    .anyMatch(edge -> cycleDetected(edge.getSource(), to));
    }

    private boolean directlyConnectedTo(FlowNode to, FlowNode from) {
        return from.getIncomingLinks().stream()
                .anyMatch(edge -> edge.getSource().equals(to));
    }

    private FlowNode getNode(String name) {
        return nodes.stream()
                .filter(n -> n.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find node " + name));
    }

    @Getter
    @Builder
    @EqualsAndHashCode(of = "name")
    public static class FlowNode {

        private String name;
        private boolean executable;
        private Set<FlowEdge> incomingLinks = new HashSet<>();
        private Set<FlowEdge> outgoingLinks = new HashSet<>();

        public void addIncomingLink(FlowEdge edge) {
            incomingLinks.add(edge);
        }

        public void addOutgoingLink(FlowEdge edge) {
            outgoingLinks.add(edge);
        }

    }

    @Getter
    @Builder
    @EqualsAndHashCode
    public static class FlowEdge {

        private FlowNode source;
        private String sourceOutputName;
        private FlowNode destination;
        private String destinationInputName;

    }
}
