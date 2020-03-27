package io.scicast.streamesh.core.internal.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphNode {

    NodeType value();

    enum NodeType {
        SOURCE, SINK, INTERNAL
    }

}
