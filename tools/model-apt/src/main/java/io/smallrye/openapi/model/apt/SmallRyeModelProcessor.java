package io.smallrye.openapi.model.apt;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Generated;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.Extensible;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;

import io.smallrye.openapi.model.BaseExtensibleModel;
import io.smallrye.openapi.model.BaseModel;
import io.smallrye.openapi.model.OASModelType;

@SupportedAnnotationTypes({
        "io.smallrye.openapi.model.OASModelType",
        "io.smallrye.openapi.model.OASModelType.List" })
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_11)
public class SmallRyeModelProcessor extends AbstractProcessor {

    private static final String INDENT = "    ";
    private static final String PUBLIC = "public ";
    private static final String CLASS = ".class";
    private static final String NEW_VALUE_ARG = "newValue";
    private static final String RETURN_THIS = INDENT.repeat(2) + "return this;\n";

    private static final Map<String, Class<?>> CENTRALIZED_PROPERIES = Map.ofEntries(
            Map.entry("name", String.class),
            Map.entry("ref", String.class),
            Map.entry("description", String.class),
            Map.entry("summary", String.class),
            Map.entry("externalDocs", ExternalDocumentation.class));

    private static class PropertyInfo {
        String name;
        String methodName;
        String rawType;
        String type;
        String valueType;
        String getPropertyMethod;
        String setPropertyMethod;
        String singularName;
        String singularMethodName;
        boolean unwrapped;
    }

    private Map<Class<?>, String> generatedClasses = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        String rootPackage = "";

        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                PackageElement pkg = (PackageElement) element;
                if (rootPackage.isEmpty() || pkg.getQualifiedName().toString().length() < rootPackage.length()) {
                    rootPackage = pkg.getQualifiedName().toString();
                }
            }
        }

        if (rootPackage.isEmpty()) {
            // Nothing was given
            return true;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Root package: " + rootPackage);

        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                PackageElement pkg = (PackageElement) element;
                // Process each element
                processPackage(annotation, pkg, rootPackage);
            }
        }

        try {
            JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(rootPackage + ".SmallRyeOASModels");

            try (Writer writer = fileObject.openWriter()) {
                writeClassPrefix(writer, rootPackage);

                writeCodeLn(writer, 0, "public class SmallRyeOASModels {");
                writeCodeLn(writer, 1, "public interface Properties {");
                writeCodeLn(writer, 2, "DataType getPropertyType(String name);");
                writeCodeLn(writer, 1, "}");
                writeCodeLn(writer, 1,
                        "private final java.util.Map<Class<?>, Properties> models = new java.util.HashMap<>(",
                        Integer.toString(generatedClasses.size()), ");");
                writer.write("\n");
                writeCodeLn(writer, 1, "public SmallRyeOASModels() {");
                for (Map.Entry<Class<?>, String> entry : generatedClasses.entrySet()) {
                    writeCodeLn(writer, 2, "models.put(", entry.getKey().getName(), CLASS, ", new ", entry.getValue(),
                            ".Properties());");
                }
                writeCodeLn(writer, 1, "}");
                writer.write("\n");
                writeCodeLn(writer, 1, "public Properties getModel(Class<?> modelType) {");
                writeCodeLn(writer, 2, "return models.get(modelType);");
                writeCodeLn(writer, 1, "}");
                writeCodeLn(writer, 0, "}");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate class: " + e.getMessage());
        }

        return true; // No further processing of this annotation type
    }

    private void processPackage(TypeElement annotation, PackageElement pkg, String rootPackage) {
        String annotationName = annotation.getQualifiedName().toString();
        String packageName = pkg.getQualifiedName().toString();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing: " + packageName);
        Filer filer = processingEnv.getFiler();

        for (var mirror : pkg.getAnnotationMirrors()) {
            if (annotationName.equals(OASModelType.class.getName())) {
                writeType(filer, rootPackage, packageName, toMap(mirror));
            } else if (annotationName.equals(OASModelType.List.class.getName().replace('$', '.'))) {
                @SuppressWarnings("unchecked")
                List<AnnotationValue> types = (List<AnnotationValue>) toMap(mirror).get("value");

                for (var type : types) {
                    writeType(filer, rootPackage, packageName, toMap((AnnotationMirror) type.getValue()));
                }
            }
        }
    }

    private Map<String, Object> toMap(AnnotationMirror annotation) {
        return processingEnv.getElementUtils()
                .getElementValuesWithDefaults(annotation)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getSimpleName().toString(),
                        e -> e.getValue().getValue(),
                        (u1, u2) -> u2,
                        LinkedHashMap::new));
    }

    private void writeType(Filer filer, String rootPackage, String packageName, Map<String, Object> annotation) {
        String className = (String) annotation.get("name");
        String fqcn = packageName + "." + className;
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Writing: " + fqcn);

        try (Writer writer = filer.createSourceFile(fqcn).openWriter()) {
            writeClassPrefix(writer, packageName);

            Class<? extends Constructible> constructibleType = loadConstructible(annotation);

            @SuppressWarnings("unchecked")
            List<AnnotationValue> properties = (List<AnnotationValue>) annotation.get("properties");

            writeClassHeader(writer, constructibleType, annotation);

            Map<String, PropertyInfo> propertyMap = properties.stream()
                    .map(AnnotationValue::getValue)
                    .map(AnnotationMirror.class::cast)
                    .map(this::toMap)
                    .map(this::getPropertyInfo)
                    .collect(Collectors.toMap(
                            e -> e.name,
                            Function.identity(),
                            (u1, u2) -> u2,
                            LinkedHashMap::new));

            propertyMap.values().forEach(property -> {
                try {
                    writeProperty(writer, className, property);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            generatedClasses.put(constructibleType, fqcn);
            boolean extensible = Extensible.class.isAssignableFrom(constructibleType);
            writePropertyInfo(writer, rootPackage, extensible, propertyMap);

            writeCodeLn(writer, 0, "}");
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate class: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    static <C extends Constructible> Class<C> loadConstructible(Map<String, Object> annotation) {
        DeclaredType constructible = (DeclaredType) annotation.get("constructible");
        Class<C> constructibleType;

        try {
            constructibleType = (Class<C>) Class.forName(constructible.toString());
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException("Unknown Constructible: " + constructible.toString(), cnfe);
        }

        return constructibleType;
    }

    private void writeClassHeader(Writer writer, Class<? extends Constructible> constructible,
            Map<String, Object> modelAnnotation) throws IOException {
        writeCodeLn(writer, 0, "@", Generated.class.getName(), "(value = \"", getClass().getName(), "\", date = \"",
                OffsetDateTime.now().toString(), "\")");

        writer.write(PUBLIC);

        if (get(modelAnnotation, "incomplete", Boolean.class).booleanValue()) {
            writer.write("abstract ");
        }

        writer.write("class ");
        writer.write(get(modelAnnotation, "name", String.class));

        Class<?> parent;

        if (Extensible.class.isAssignableFrom(constructible)) {
            parent = BaseExtensibleModel.class;
        } else {
            parent = BaseModel.class;
        }

        writer.write('\n');
        writer.write(INDENT.repeat(2));
        writer.write("extends ");
        writer.write(parent.getName());
        writer.write('<');
        writer.write(constructible.getName());
        writer.write('>');

        writer.write('\n');
        writer.write(INDENT.repeat(2));
        writer.write("implements ");
        writer.write(constructible.getName());

        for (Object entry : get(modelAnnotation, "interfaces", List.class)) {
            AnnotationValue iface = (AnnotationValue) entry;
            writeInterface(writer, iface.getValue().toString());
        }

        writer.write(" {\n");

        try {
            String simpleName = constructible.getSimpleName();
            Method filterMethod = OASFilter.class.getMethod("filter" + simpleName, constructible);

            if (filterMethod != null) {
                writeFilter(writer, filterMethod);
            }
        } catch (NoSuchMethodException | SecurityException e) {
            // Ignore errors
        }
    }

    private void writeInterface(Writer writer, String interfaceName) {
        try {
            writer.write(",\n");
            writer.write(INDENT.repeat(2));
            writer.write(interfaceName);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isCentralizedProperty(PropertyInfo property) {
        String name = property.name;
        String type = property.type;
        return CENTRALIZED_PROPERIES.getOrDefault(name, Void.class).getName().equals(type);
    }

    private void writePropertyInfo(Writer writer, String rootPackage, boolean extensible, Map<String, PropertyInfo> propertyMap)
            throws IOException {

        writer.write("\n");
        writeCodeLn(writer, 1, "public static class Properties implements ", rootPackage, ".SmallRyeOASModels.Properties {");
        writer.write("\n");
        writeCodeLn(writer, 2,
                "private final java.util.Map<String, DataType> types = new java.util.HashMap<>(",
                Integer.toString(propertyMap.size()), ");");
        writer.write("\n");
        writeCodeLn(writer, 2, "public Properties() {");

        for (Map.Entry<String, PropertyInfo> entry : propertyMap.entrySet()) {
            String name = entry.getKey();
            PropertyInfo property = entry.getValue();
            writePropertyTypeEntry(writer, name, property);
        }

        writeCodeLn(writer, 2, "}");
        writer.write('\n');

        boolean hasUnwrappedProperty = propertyMap.values().stream().anyMatch(p -> p.unwrapped);
        final String getTypeByName = "return types.get(name);";

        writeOverride(writer);
        writeCodeLn(writer, 2, "public DataType getPropertyType(String name) {");
        if (hasUnwrappedProperty) {
            writeCodeLn(writer, 3, "if (types.containsKey(name)) {");
            writeCodeLn(writer, 4, getTypeByName);

            if (extensible) {
                writeCodeLn(writer, 3, "} else if (name.startsWith(\"x-\")) {");
                writeCodeLn(writer, 4, "return DataType.type(Object.class);");
            }

            writeCodeLn(writer, 3, "} else {");
            writeCodeLn(writer, 4, "return types.get(\"io.smallrye.openapi.internal.model.unwrapped\");");
            writeCodeLn(writer, 3, "}");
        } else {
            if (extensible) {
                writeCodeLn(writer, 3, "if (types.containsKey(name)) {");
                writeCodeLn(writer, 4, getTypeByName);
                writeCodeLn(writer, 3, "} else {");
                writeCodeLn(writer, 4, "return DataType.type(Object.class);");
                writeCodeLn(writer, 3, "}");
            } else {
                writeCodeLn(writer, 3, getTypeByName);
            }
        }
        writeCodeLn(writer, 2, "}");

        writeCodeLn(writer, 1, "}");
    }

    private void writePropertyTypeEntry(Writer writer, String name, PropertyInfo property) throws IOException {
        final String putType = "types.put(\"";

        if (Map.class.getName().equals(property.rawType)) {
            String content;

            if (property.valueType.indexOf('<') > -1) {
                String outerValueType = property.valueType.substring(0, property.valueType.indexOf('<'));
                String innerValueType = property.valueType.substring(property.valueType.indexOf('<') + 1,
                        property.valueType.lastIndexOf('>'));
                if (Map.class.getName().equals(outerValueType)) {
                    content = "DataType.mapOf(DataType.type(" + innerValueType + CLASS + "))";
                } else if (List.class.getName().equals(outerValueType)) {
                    content = "DataType.listOf(DataType.type(" + innerValueType + CLASS + "))";
                } else {
                    throw new IllegalStateException("Unexpected outer value type: " + outerValueType);
                }
            } else {
                content = "DataType.type(" + property.valueType + CLASS + ")";
            }

            if (property.unwrapped) {
                writeCodeLn(writer, 3, "types.put(\"io.smallrye.openapi.internal.model.unwrapped\", ", content, ");");
            } else {
                writeCodeLn(writer, 3, putType, name, "\", DataType.mapOf(", content, "));");
            }
        } else if (List.class.getName().equals(property.rawType)) {
            String content = "DataType.type(" + property.valueType + CLASS + ")";
            writeCodeLn(writer, 3, putType, name, "\", DataType.listOf(", content, "));");
        } else {
            writeCodeLn(writer, 3, putType, name, "\", DataType.type(", property.rawType, CLASS + "));");
        }
    }

    private PropertyInfo getPropertyInfo(Map<String, Object> values) {
        PropertyInfo property = new PropertyInfo();

        property.name = (String) values.get("name");
        property.rawType = ((TypeMirror) values.get("type")).toString();
        property.unwrapped = get(values, "unwrapped", Boolean.class).booleanValue();

        if (Map.class.getName().equals(property.rawType)) {
            property.valueType = getValueType(values, property.rawType);
            property.type = String.format("%s<String, %s>", property.rawType, property.valueType);
            property.getPropertyMethod = "getMapProperty";
            property.setPropertyMethod = "setMapProperty";
            property.singularName = getSingularName(property.name, values);
        } else if (List.class.getName().equals(property.rawType)) {
            if (property.unwrapped) {
                throw new IllegalArgumentException("unwrapped model types must have a property of type List: " + property.name);
            }

            property.valueType = getValueType(values, property.rawType);
            property.type = String.format("%s<%s>", property.rawType, property.valueType);
            property.getPropertyMethod = "getListProperty";
            property.setPropertyMethod = "setListProperty";
            property.singularName = getSingularName(property.name, values);
        } else {
            if (property.unwrapped) {
                throw new IllegalArgumentException("unwrapped model types must have a property of type Map: " + property.name);
            }

            property.valueType = null;
            property.type = property.rawType;
            property.getPropertyMethod = "getProperty";
            property.setPropertyMethod = "setProperty";
            property.singularName = null;
        }

        StringBuilder buffer = new StringBuilder();

        property.methodName = getOptional(values, "methodNameOverride", String.class)
                .filter(Predicate.not(String::isBlank))
                .orElseGet(() -> {
                    buffer.append(property.name);
                    buffer.setCharAt(0,
                            Character.toUpperCase(property.name.charAt(0)));
                    return buffer.toString();
                });

        if (property.singularName != null) {
            buffer.setLength(0);
            buffer.append(property.singularName);
            buffer.setCharAt(0, Character.toUpperCase(property.singularName.charAt(0)));
            property.singularMethodName = buffer.toString();
        }

        return property;
    }

    private String getSingularName(String propertyName, Map<String, Object> values) {
        String singularName = get(values, "singularName", String.class);

        if (singularName.isBlank()) {
            if (propertyName.endsWith("s")) {
                singularName = propertyName.substring(0, propertyName.length() - 1);
            } else {
                throw new IllegalArgumentException(
                        "singularName is required for map or list type when name does not end with 's': " + propertyName);
            }
        }

        return singularName;
    }

    private void writeProperty(Writer writer, String className, PropertyInfo property)
            throws IOException {

        if (isCentralizedProperty(property)) {
            return;
        }

        writeGet(writer, property);
        writeSet(writer, property);

        if (Map.class.getName().equals(property.rawType)) {
            writeMapAdd(writer, className, property);
            writeMapRemove(writer, property);
        } else if (List.class.getName().equals(property.rawType)) {
            writeListAdd(writer, className, property);
            writeListRemove(writer, property);
        }
    }

    /**
     *
     * @param writer
     * @param property
     * @throws IOException
     */
    private void writeGet(Writer writer, PropertyInfo property) throws IOException {
        writeJavaDoc(writer);
        writeOverride(writer);

        writeCodeLn(writer, 1, PUBLIC, property.type, " get", property.methodName, "() {");

        if (property.unwrapped) {
            String valueTypeParam;

            if (property.valueType.indexOf('<') > -1) {
                /*
                 * We only care about the raw type, but the compiler needs to be fooled to obtain
                 * the type with generic arguments to satisfy the call to getProperties, and the
                 * return type.
                 */
                String rawType = property.valueType.substring(0, property.valueType.indexOf('<'));
                writeCodeLn(writer, 2, "@SuppressWarnings(\"unchecked\")");
                writeCodeLn(writer, 2, "Class<", property.valueType, "> TYPE = (Class<", property.valueType, ">) (Object) ",
                        rawType,
                        CLASS, ";");
                valueTypeParam = "TYPE";
            } else {
                valueTypeParam = property.valueType + CLASS;
            }

            writeCodeLn(writer, 2, "return getProperties(", valueTypeParam, ");");
        } else {
            writeCodeLn(writer, 2, "return ", property.getPropertyMethod, "(\"", property.name, "\");");
        }

        writeCodeLn(writer, 1, "}");

    }

    /**
     * Write a method to set a property value. If the current property is an unwrapped property
     * where the keys are set directly in the parent object, first remove any current property
     * entries.
     */
    private void writeSet(Writer writer, PropertyInfo property) throws IOException {
        writeJavaDoc(writer);
        writeOverride(writer);

        writeCode(writer, 1, "public void set", property.methodName);
        writeArgs(writer, property.type, NEW_VALUE_ARG);

        if (property.unwrapped) {
            writeCodeLn(writer, 2, "get", property.methodName, "().keySet().forEach(this::remove", property.singularMethodName,
                    ");");
            writeCodeLn(writer, 2, "if (", NEW_VALUE_ARG, " != null) {");
            writeCodeLn(writer, 3, NEW_VALUE_ARG, ".forEach(this::add", property.singularMethodName, ");");
            writeCodeLn(writer, 2, "}");
        } else {
            writeCodeLn(writer, 2, property.setPropertyMethod, "(\"", property.name, "\", newValue);");
        }

        writer.write(INDENT);
        writer.write("}\n");
    }

    /**
     * Write a method to add an entry to a map property.
     */
    private void writeMapAdd(Writer writer, String className, PropertyInfo property) throws IOException {
        writeJavaDoc(writer);
        writeOverride(writer);

        writer.write(INDENT);
        writer.write(PUBLIC);
        writer.write(className);
        writer.write(" add");
        writer.write(property.singularMethodName);
        writeArgs(writer, "String", "newKey", property.valueType, NEW_VALUE_ARG);
        writeCodeLn(writer, 2, "java.util.Objects.requireNonNull(newKey, \"Key must not be null\");");
        writeCodeLn(writer, 2, "java.util.Objects.requireNonNull(newValue, \"Value must not be null\");");

        if (property.unwrapped) {
            writeCodeLn(writer, 2, "setProperty(newKey, newValue);");
        } else {
            writeCodeLn(writer, 2, "putMapPropertyEntry(\"", property.name, "\", newKey, newValue);");
        }

        writer.write(RETURN_THIS);
        writer.write(INDENT);
        writer.write("}\n");
    }

    /**
     * Write a method to remove an entry from a map property.
     */
    private void writeMapRemove(Writer writer, PropertyInfo property) throws IOException {
        writeJavaDoc(writer);
        writeOverride(writer);

        writer.write(INDENT);
        writer.write("public void remove");
        writer.write(property.singularMethodName);
        writeArgs(writer, "String", "key");

        if (property.unwrapped) {
            writer.write(INDENT.repeat(2));
            writer.write("setProperty(key, null);\n");
        } else {
            writer.write(INDENT.repeat(2));
            writer.write("removeMapPropertyEntry");
            writer.write("(\"");
            writer.write(property.name);
            writer.write("\", key);\n");
        }

        writer.write(INDENT);
        writer.write("}\n");
    }

    /**
     * Write a method to add an entry to a list property.
     */
    private void writeListAdd(Writer writer, String className, PropertyInfo property) throws IOException {
        writeJavaDoc(writer);
        writeOverride(writer);

        writer.write(INDENT);
        writer.write(PUBLIC);
        writer.write(className);
        writer.write(" add");
        writer.write(property.singularMethodName);
        writeArgs(writer, property.valueType, NEW_VALUE_ARG);
        writer.write(INDENT.repeat(2));
        writer.write("addListPropertyEntry");
        writer.write("(\"");
        writer.write(property.name);
        writer.write("\", newValue);\n");
        writer.write(RETURN_THIS);
        writer.write(INDENT);
        writer.write("}\n");
    }

    /**
     * Write a method to remove an entry from a list property.
     */
    private void writeListRemove(Writer writer, PropertyInfo property) throws IOException {
        writeJavaDoc(writer);
        writeOverride(writer);

        writer.write(INDENT);
        writer.write("public void remove");
        writer.write(property.singularMethodName);
        writeArgs(writer, property.valueType, "value");
        writer.write(INDENT.repeat(2));
        writer.write("removeListPropertyEntry");
        writer.write("(\"");
        writer.write(property.name);
        writer.write("\", value);\n");
        writer.write(INDENT);
        writer.write("}\n");
    }

    /**
     * Writes the variable number of method arguments, wrapped by parentheses, and terminated
     * with an opening brace to start the method body.
     */
    private void writeArgs(Writer writer, String... args) throws IOException {
        if ((args.length & 1) != 0) { // implicit nullcheck of input
            throw new IllegalArgumentException("length is odd");
        }

        writer.write('(');

        for (int i = 0; i < args.length; i += 2) {
            if (i > 0) {
                writer.write(", ");
            }
            writer.write(args[i]);
            writer.write(' ');
            writer.write(args[i + 1]);
        }

        writer.write(") {\n");
    }

    /**
     * Write code with the given indentation level, without adding a new-line
     */
    private void writeCode(Writer writer, int indent, String... fragments) throws IOException {
        writer.write(INDENT.repeat(indent));
        for (String f : fragments) {
            writer.write(f);
        }
    }

    /**
     * Write a line of code with the given indentation level
     */
    private void writeCodeLn(Writer writer, int indent, String... fragments) throws IOException {
        writeCode(writer, indent, fragments);
        writer.write('\n');
    }

    /**
     * Write a filter method for types that may be filtered by {@code OASFilter}.
     */
    private void writeFilter(Writer writer, Method filterMethod) throws IOException {
        writeJavaDoc(writer);
        writeOverride(writer);
        writer.write(INDENT);
        writer.write("protected ");
        writer.write(filterMethod.getParameters()[0].getType().getName());
        writer.write(" filter(org.eclipse.microprofile.openapi.OASFilter filter) {\n");
        writer.write(INDENT.repeat(2));
        if (void.class.equals(filterMethod.getReturnType())) {
            writer.write("filter.filter");
        } else {
            writer.write("return filter.filter");
        }
        writer.write(filterMethod.getParameters()[0].getType().getSimpleName());
        writer.write("(this);\n");
        if (void.class.equals(filterMethod.getReturnType())) {
            writer.write(RETURN_THIS);
        }
        writer.write(INDENT);
        writer.write("}\n");
    }

    private String getValueType(Map<String, Object> values, String type) {
        return getOptional(values, "valueTypeLiteral", String.class).filter(Predicate.not(String::isBlank))
                .or(() -> getOptional(values, "valueType", TypeMirror.class)
                        .map(TypeMirror::toString)
                        .filter(Predicate.not(Void.class.getName()::equals)))
                .orElseThrow(() -> new NoSuchElementException("valueType required when type is " + type));
    }

    private <T> T get(Map<String, Object> values, String key, Class<T> type) {
        return getOptional(values, key, type).orElseThrow(() -> new NoSuchElementException(key));
    }

    private <T> Optional<T> getOptional(Map<String, Object> values, String key, Class<T> type) {
        return Optional.ofNullable(values.get(key)).map(type::cast);
    }

    private void writeJavaDoc(Writer writer) throws IOException {
        writer.write('\n');
        writer.write(INDENT);
        writer.write("/** {@inheritDoc} */\n");
    }

    private void writeOverride(Writer writer) throws IOException {
        writer.write(INDENT);
        writer.write("@Override\n");
    }

    private void writeClassPrefix(Writer writer, String packageName) throws IOException {
        writer.write("package ");
        writer.write(packageName);
        writer.write(";\n\n");

        writeCodeLn(writer, 0, "import io.smallrye.openapi.model.DataType;");
        writer.write('\n');
    }
}
