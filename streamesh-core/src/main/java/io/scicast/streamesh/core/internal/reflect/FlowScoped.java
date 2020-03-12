package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.internal.reflect.handler.FlowScopedHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@FlowGrammarMarker(handler = FlowScopedHandler.class)
public @interface FlowScoped {

    String as() default "";
    String using() default "";

}
