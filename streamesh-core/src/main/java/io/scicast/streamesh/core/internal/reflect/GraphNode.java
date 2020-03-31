package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.internal.reflect.handler.BaseGraphNodeHandler;
import io.scicast.streamesh.core.internal.reflect.handler.GraphNodeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphNode {

    NodeType value();
    Class<? extends GraphNodeHandler> handler() default BaseGraphNodeHandler.class;

    enum NodeType {
        SOURCE, SINK, INTERNAL
    }

}
