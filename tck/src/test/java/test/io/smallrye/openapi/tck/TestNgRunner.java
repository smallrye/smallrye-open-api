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

import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * A Junit 4 test runner used to bridge beteen Junit and TestNG. Simply extends the
 * standard JUnit 4 runner but uses TestNG annotations instead of JUnit annotations.
 *
 * @author eric.wittmann@gmail.com
 */
public class TestNgRunner extends BlockJUnit4ClassRunner {

    /**
     * Constructor.
     * 
     * @param klass
     * @throws InitializationError
     */
    public TestNgRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        return getTestClass().getAnnotatedMethods(org.testng.annotations.Test.class);
    }

}
