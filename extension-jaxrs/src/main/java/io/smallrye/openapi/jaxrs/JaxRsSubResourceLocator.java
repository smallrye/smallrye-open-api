package io.smallrye.openapi.jaxrs;

import java.util.Objects;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.MethodInfo;

class JaxRsSubResourceLocator {
    final ClassInfo clazz;
    final MethodInfo method;

    JaxRsSubResourceLocator(ClassInfo clazz, MethodInfo method) {
        this.clazz = clazz;
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof JaxRsSubResourceLocator) {
            JaxRsSubResourceLocator other = (JaxRsSubResourceLocator) o;
            return Objects.equals(this.clazz, other.clazz) && Objects.equals(this.method, other.method);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, method);
    }
}
