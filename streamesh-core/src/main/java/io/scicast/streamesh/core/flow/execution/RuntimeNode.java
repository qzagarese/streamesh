package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class RuntimeNode implements NodeObserver {

    protected String name;
    protected FlowGraph.FlowNode staticGraphNode;

    protected List<NodeObserver> observers = new ArrayList<>();
    protected RuntimeDataValue value;

    public void addObserver(NodeObserver observer) {
        observers.add(observer);
    }

    public boolean removeObserver(NodeObserver observer) {
        return observers.remove(observer);
    }

    public void notifyObservers() {
        observers.forEach(observer -> observer.notify(this));
    }

}
