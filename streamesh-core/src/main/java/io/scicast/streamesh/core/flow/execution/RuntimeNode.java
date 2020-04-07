package io.scicast.streamesh.core.flow.execution;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class RuntimeNode implements NodeObserver {

    protected String name;

    protected List<NodeObserver> observers = new ArrayList<>();
    protected RuntimeDataValue value;

    public abstract boolean canExecute();

    public void addObserver(NodeObserver observer) {
        observers.add(observer);
    }

    public void update(RuntimeDataValue value) {
        if(!value.equals(this.value)) {
            this.value = value;
            notifyObservers();
        }

    }

    protected void notifyObservers() {
        observers.forEach(observer -> observer.notify(this));
    }

}
