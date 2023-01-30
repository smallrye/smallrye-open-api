package io.smallrye.openapi.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to include a class to the path section of the generated OpenAPI specification, with the specified value as the
 * path.
 * <p>
 * Currently, this annotation is only supported by the JAX-RS extension.
 * </p>
 * 
 * @author Anar Sultanov (anar@sultanov.dev)
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SmallRyeOpenApiPath {

    String value();
}
