package io.smallrye.openapi.model;

import java.lang.annotation.Target;

@Target({})
public @interface OASModelProperty {

    String name();

    String singularName() default "";

    String methodNameOverride() default "";

    Class<?> type();

    Class<?> valueType() default Void.class;

    String valueTypeLiteral() default "";

    boolean unwrapped() default false;

}
