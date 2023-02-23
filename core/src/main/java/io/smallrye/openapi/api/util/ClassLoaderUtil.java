package io.smallrye.openapi.api.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Help to get a default classLoader
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ClassLoaderUtil {

    private ClassLoaderUtil() {
    }

    public static final ClassLoader getDefaultClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return AccessController
                .doPrivileged((PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
    }

}
