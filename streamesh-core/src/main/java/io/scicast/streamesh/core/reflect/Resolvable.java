package io.scicast.streamesh.core.reflect;

import io.scicast.streamesh.core.reflect.impl.ResolvableHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@FlowGrammarMarker(handler = ResolvableHandler.class)
public @interface Resolvable {

    String scope();

}
