/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.io.smallrye.openapi.tck;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.jandex.IndexView;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiDocument;
// import io.smallrye.openapi.api.util.ArchiveUtil;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;

/**
 * A Junit 4 test runner used to quickly run the OpenAPI tck tests directly against the
 * {@link OpenApiDocument} without spinning up an MP compliant server. This is not
 * a replacement for running the full OpenAPI TCK using Arquillian. However, it runs
 * much faster and does *most* of what we need for coverage.
 *
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("rawtypes")
public class TckTestRunner extends ParentRunner<ProxiedTckTest> {

    private Class<?> testClass;
    private Class<? extends Arquillian> tckTestClass;

    public static Map<Class, OpenAPI> OPEN_API_DOCS = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param testClass
     * @throws InitializationError
     */
    public TckTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.testClass = testClass;
        this.tckTestClass = determineTckTestClass(testClass);

        // The Archive (shrinkwrap deployment)
        Archive archive = archive();
        // MPConfig
        OpenApiConfig config = ArchiveUtil.archiveToConfig(archive);

        try {
            IndexView index = ArchiveUtil.archiveToIndex(config, archive);
            OpenApiStaticFile staticFile = ArchiveUtil.archiveToStaticFile(archive);

            OpenAPI openAPI = OpenApiProcessor.bootstrap(config, index, getContextClassLoader(), staticFile);

            Assert.assertNotNull("Generated OAI document must not be null.", openAPI);

            OPEN_API_DOCS.put(testClass, openAPI);

            // Output the /openapi content to a file for debugging purposes
            File parent = new File("target", "TckTestRunner");
            if (!parent.exists()) {
                parent.mkdir();
            }
            File file = new File(parent, testClass.getName() + ".json");
            String content = OpenApiSerializer.serialize(OpenApiDocument.INSTANCE.get(), Format.JSON);
            try (FileWriter writer = new FileWriter(file)) {
                IOUtils.write(content, writer);
            }
        } catch (Exception e) {
            throw new InitializationError(e);
        }
    }

    /**
     * Creates and returns the shrinkwrap archive for this test.
     */
    private Archive archive() throws InitializationError {
        try {
            Method[] methods = tckTestClass.getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Deployment.class)) {
                    Archive archive = (Archive) method.invoke(null);
                    return archive;
                }
            }
            throw TckMessages.msg.missingDeploymentArchive();
        } catch (Exception e) {
            throw new InitializationError(e);
        }
    }

    /**
     * Figures out what TCK test is being run.
     * 
     * @throws InitializationError
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Arquillian> determineTckTestClass(Class<?> testClass) throws InitializationError {
        ParameterizedType ptype = (ParameterizedType) testClass.getGenericSuperclass();
        Class cc = (Class) ptype.getActualTypeArguments()[0];
        return cc;
    }

    /**
     * @see org.junit.runners.ParentRunner#getChildren()
     */
    @Override
    protected List<ProxiedTckTest> getChildren() {
        List<ProxiedTckTest> children = new ArrayList<>();
        Method[] methods = tckTestClass.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Test.class)) {
                try {
                    Object theTestObj = this.testClass.newInstance();
                    Arquillian delegate = createDelegate(theTestObj);
                    Test testAnnotation = method.getAnnotation(Test.class);
                    String providerMethodName = testAnnotation.dataProvider();
                    Method providerMethod = null;

                    for (Method m : tckTestClass.getMethods()) {
                        if (m.isAnnotationPresent(DataProvider.class)) {
                            DataProvider provider = m.getAnnotation(DataProvider.class);
                            if (provider.name().equals(providerMethodName)) {
                                providerMethod = m;
                                break;
                            }
                        }
                    }

                    if (method.getParameterCount() > 0 && providerMethod != null) {
                        Object[][] args = (Object[][]) providerMethod.invoke(delegate);

                        for (Object[] arg : args) {
                            children.add(ProxiedTckTest.create(delegate, theTestObj, method, arg));
                        }
                    } else {
                        children.add(ProxiedTckTest.create(delegate, theTestObj, method, new Object[0]));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        children.sort(new Comparator<ProxiedTckTest>() {
            @Override
            public int compare(ProxiedTckTest o1, ProxiedTckTest o2) {
                return o1.getTestMethod().getName().compareTo(o2.getTestMethod().getName());
            }
        });
        return children;
    }

    /**
     * Creates the delegate test instance. This is done by instantiating the test itself
     * and calling its "getDelegate()" method. If no such method exists then an error
     * is thrown.
     */
    private Arquillian createDelegate(Object testObj) throws Exception {
        Object delegate = testObj.getClass().getMethod("getDelegate").invoke(testObj);
        return (Arquillian) delegate;
    }

    /**
     * @see org.junit.runners.ParentRunner#describeChild(java.lang.Object)
     */
    @Override
    protected Description describeChild(ProxiedTckTest child) {
        StringBuilder name = new StringBuilder(child.getTestMethod().getName());

        if (child.getArguments().length > 0) {
            name.append(' ');
            name.append(Arrays.stream(child.getArguments()).map(Object::toString).collect(Collectors.joining(",")));
        }

        return Description.createTestDescription(tckTestClass, name.toString());
    }

    /**
     * @see org.junit.runners.ParentRunner#runChild(java.lang.Object, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(final ProxiedTckTest child, final RunNotifier notifier) {
        OpenApiDocument.INSTANCE.set(TckTestRunner.OPEN_API_DOCS.get(child.getTest().getClass()));

        Description description = describeChild(child);
        if (isIgnored(child)) {
            notifier.fireTestIgnored(description);
        } else {
            Statement statement = new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        Method testMethod = child.getTestMethod();
                        testMethod.invoke(child.getDelegate(), child.getArguments());
                    } catch (InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        Test testAnno = child.getTestMethod().getAnnotation(Test.class);
                        Class[] expectedExceptions = testAnno.expectedExceptions();
                        if (expectedExceptions != null && expectedExceptions.length > 0) {
                            Class expectedException = expectedExceptions[0];
                            Assert.assertEquals(expectedException, cause.getClass());
                        } else {
                            throw cause;
                        }
                    }
                }
            };
            runLeaf(statement, description, notifier);
        }

    }

    /**
     * @see org.junit.runners.ParentRunner#isIgnored(java.lang.Object)
     */
    @Override
    protected boolean isIgnored(ProxiedTckTest child) {
        Method testMethod = child.getTestMethod();

        if (testMethod.isAnnotationPresent(Ignore.class)) {
            return true;
        }

        Method testMethodOverride;

        try {
            testMethodOverride = testClass.getMethod(testMethod.getName(), testMethod.getParameterTypes());
            return testMethodOverride.isAnnotationPresent(Ignore.class);
        } catch (NoSuchMethodException | SecurityException e) {
            // Ignore, no override has been specified in the BaseTckTest subclass
        }

        return false;
    }

    private static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return AccessController
                .doPrivileged((PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
    }

}
