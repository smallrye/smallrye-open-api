/*
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
package io.smallrye.openapi.runtime.scanner.dataobject;

import java.util.Collection;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * IndexView augmented with additional methods for common operations
 * used throughout the data object scanning code.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class AugmentedIndexView implements IndexView {

    private final IndexView index;

    public AugmentedIndexView(IndexView index) {
        validateInput(index);
        this.index = index;
    }

    public ClassInfo getClass(Type type) {
        validateInput(type);
        return index.getClassByName(TypeUtil.getName(type));
    }

    public boolean containsClass(Type type) {
        validateInput(type);
        return getClass(type) != null;
    }

    public ClassInfo getClass(Class<?> klazz) {
        validateInput(klazz);
        return index.getClassByName(DotName.createSimple(klazz.getName()));
    }

    @Override
    public Collection<ClassInfo> getKnownClasses() {
        return index.getKnownClasses();
    }

    @Override
    public ClassInfo getClassByName(DotName className) {
        validateInput(className);
        return index.getClassByName(className);
    }

    @Override
    public Collection<ClassInfo> getKnownDirectSubclasses(DotName className) {
        validateInput(className);
        return index.getKnownDirectSubclasses(className);
    }

    @Override
    public Collection<ClassInfo> getAllKnownSubclasses(DotName className) {
        validateInput(className);
        return index.getAllKnownSubclasses(className);
    }

    @Override
    public Collection<ClassInfo> getKnownDirectImplementors(DotName className) {
        validateInput(className);
        return index.getKnownDirectSubclasses(className);
    }

    @Override
    public Collection<ClassInfo> getAllKnownImplementors(DotName interfaceName) {
        validateInput(interfaceName);
        return index.getAllKnownImplementors(interfaceName);
    }

    @Override
    public Collection<AnnotationInstance> getAnnotations(DotName annotationName) {
        validateInput(annotationName);
        return index.getAnnotations(annotationName);
    }

    private <T> void validateInput(T input) {
        if (input == null)
            throw new RuntimeException("Input parameter can not be null");
    }
}
