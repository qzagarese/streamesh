package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.internal.reflect.handler.FieldValueInstanceFactory;
import io.scicast.streamesh.core.internal.reflect.handler.LocallyScopedHandler;
import io.scicast.streamesh.core.internal.reflect.handler.ScopedInstanceFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@FlowGrammarMarker(handler = LocallyScopedHandler.class)
public @interface LocallyScoped {

    String as() default "";
    String using() default "";
    Class<? extends ScopedInstanceFactory> factory() default FieldValueInstanceFactory.class;

}
