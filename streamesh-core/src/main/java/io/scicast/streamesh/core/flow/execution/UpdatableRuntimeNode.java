package io.scicast.streamesh.core.flow.execution;

public abstract class UpdatableRuntimeNode extends RuntimeNode {

    public abstract void update(RuntimeDataValue value);

}
