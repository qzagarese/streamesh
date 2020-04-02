package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.internal.reflect.GraphNode;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

public class FlowGraph {

    @Getter
    private Set<FlowNode> nodes = new HashSet<>();

    public void createNode(String name, Object value, GraphNode.NodeType nodeType) {
        FlowNode node = FlowNode.builder()
                .name(name)
                .value(value)
                .type(nodeType)
                .build();
        if (nodes.contains(node)) {
            throw new IllegalArgumentException("Duplicate nodes named " + name);
        }
        nodes.add(node);
    }

    public void connect(String source, String destination) {
        connect(source, destination, "", "");
    }

    public void connect(String source, String destination, String sourceLabel, String destinationLabel) {
        FlowNode from = getNode(source);
        FlowNode to = getNode(destination);

        FlowEdge edge = FlowEdge.builder()
                .destination(to)
                .source(from)
                .sourceLabel(sourceLabel)
                .destinationLabel(destinationLabel)
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

    public String toDot() {
        Map<String, String> varToName = new HashMap<>();
        Map<String, String> nameToVar = new HashMap<>();
        Map<String, FlowNode> nameToNode = new HashMap<>();
         List<FlowNode> nodeList = nodes.stream().collect(Collectors.toList());
        for (int i = 0; i < nodeList.size(); i++) {
            varToName.put("node" + i, nodeList.get(i).getName());
            nameToVar.put(nodeList.get(i).getName(), "node" + i);
            nameToNode.put(nodeList.get(i).getName(), nodeList.get(i));
        }
        StringBuffer buf = new StringBuffer("digraph {\n");
        varToName.entrySet().forEach(entry -> {
            buf.append("\t" + entry.getKey() + "[label=\"" + entry.getValue() + "\"];\n");
        });
        varToName.entrySet().forEach(entry -> {
            FlowNode node = nameToNode.get(entry.getValue());
            node.getOutgoingLinks().forEach(edge -> {
                buf.append("\t" + nameToVar.get(node.getName()) + " -> "
                        + nameToVar.get(edge.getDestination().getName()));
                if (!edge.getSourceLabel().isBlank() || !edge.getDestinationLabel().isBlank()) {
                    buf.append("[label=\"");
                    buf.append(edge.getSourceLabel());
                    buf.append(edge.getSourceLabel().isBlank() || edge.getDestinationLabel().isBlank() ? "" : " - ");
                    buf.append(edge.getDestinationLabel());
                    buf.append("\"]");
                }
                buf.append(";\n");
            });
        });
        buf.append("}\n");
        return buf.toString();
    }


    @Getter
    @Builder
    @EqualsAndHashCode(of = "name")
    @ToString(of = {"name", "value", "type"})
    public static class FlowNode {

        private String name;
        private Object value;
        private GraphNode.NodeType type;

        @Builder.Default
        private Set<FlowEdge> incomingLinks = new HashSet<>();

        @Builder.Default
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
        private String sourceLabel;
        private FlowNode destination;
        private String destinationLabel;

    }
}
