package io.smallrye.openapi.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

import org.eclipse.microprofile.openapi.models.Constructible;

@Target({ ElementType.TYPE, ElementType.PACKAGE })
@Repeatable(OASModelType.List.class)
public @interface OASModelType {

    String name();

    Class<? extends Constructible> constructible();

    Class<?>[] interfaces() default {};

    OASModelProperty[] properties();

    boolean incomplete() default false;

    @Target({ ElementType.TYPE, ElementType.PACKAGE })
    public static @interface List {
        OASModelType[] value();
    }
}
