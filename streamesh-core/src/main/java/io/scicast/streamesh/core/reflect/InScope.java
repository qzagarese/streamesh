package io.scicast.streamesh.core.reflect;

import io.scicast.streamesh.core.reflect.impl.InScopeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@FlowGrammarMarker(handler = InScopeHandler.class)
public @interface InScope {
}
