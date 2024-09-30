package io.smallrye.openapi.runtime.scanner.dataobject;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.internal.models.media.SchemaSupport;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.dataobject.BeanValidationScanner.RequirementHandler;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class BeanValidationScannerTest extends IndexScannerTestBase {

    BeanValidationScanner testTarget;
    Set<String> methodsInvoked = new LinkedHashSet<>();
    Schema schema;
    ClassInfo javaxTargetClass;
    ClassInfo jakartaTargetClass;

    @BeforeEach
    void beforeEach() {
        Index javaxIndex = indexOf(test.io.smallrye.openapi.runtime.scanner.dataobject.javax.BVTestContainer.class);
        Index jakartaIndex = indexOf(test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.BVTestContainer.class);
        methodsInvoked.clear();
        schema = OASFactory.createSchema();
        javaxTargetClass = javaxIndex.getClassByName(
                componentize(test.io.smallrye.openapi.runtime.scanner.dataobject.javax.BVTestContainer.class.getName()));

        jakartaTargetClass = jakartaIndex.getClassByName(
                componentize(test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.BVTestContainer.class.getName()));

        FilteredIndexView index = new FilteredIndexView(CompositeIndex.create(javaxIndex, jakartaIndex), emptyConfig());
        AnnotationScannerContext context = new AnnotationScannerContext(index, Thread.currentThread().getContextClassLoader(),
                Collections.emptyList(),
                emptyConfig(), OASFactory.createOpenAPI());
        testTarget = new BeanValidationScanner(context);
    }

    Schema proxySchema(Schema schema, Set<String> methodsInvoked) {
        return (Schema) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { Schema.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy,
                            Method method,
                            Object[] args) throws Throwable {
                        methodsInvoked.add(method.getName());
                        return method.invoke(schema, args);
                    }
                });
    }

    RequirementHandler requirementHandler(Schema parentSchema) {
        return (target, name) -> {
            List<String> requiredProperties = parentSchema.getRequired();

            if (requiredProperties == null || !requiredProperties.contains(name)) {
                parentSchema.addRequired(name);
            }
        };
    }

    RequirementHandler requirementHandlerFail() {
        return (target, name) -> fail("Unexpected call to handler");
    }

    @Test
    void testJavaxNullSchemaIgnored() {
        testTarget.applyConstraints(javaxTargetClass,
                proxySchema(schema, methodsInvoked),
                null,
                null);

        assertArrayEquals(new String[] { "getType" },
                methodsInvoked.toArray(),
                "Unexpected methods were invoked");
    }

    @Test
    void testJakartaNullSchemaIgnored() {
        testTarget.applyConstraints(jakartaTargetClass,
                proxySchema(schema, methodsInvoked),
                null,
                null);

        assertArrayEquals(new String[] { "getType" },
                methodsInvoked.toArray(),
                "Unexpected methods were invoked");
    }

    @Test
    void testJavaxRefSchemaIgnored() {
        schema.setType(List.of(SchemaType.OBJECT));
        schema.setRef("#/components/schemas/Anything");
        testTarget.applyConstraints(javaxTargetClass,
                proxySchema(schema, methodsInvoked),
                null,
                null);

        assertArrayEquals(new String[] { "getType", "getRef" },
                methodsInvoked.toArray(),
                "Unexpected methods were invoked");
    }

    @Test
    void testJakartaRefSchemaIgnored() {
        schema.setType(List.of(SchemaType.OBJECT));
        schema.setRef("#/components/schemas/Anything");
        testTarget.applyConstraints(jakartaTargetClass,
                proxySchema(schema, methodsInvoked),
                null,
                null);

        assertArrayEquals(new String[] { "getType", "getRef" },
                methodsInvoked.toArray(),
                "Unexpected methods were invoked");
    }

    /**********************************************************************/

    @Test
    void testJavaxArrayListNotNullAndNotEmptyAndMaxItems() {
        FieldInfo targetField = javaxTargetClass.field("arrayListNotNullAndNotEmptyAndMaxItems");
        testArrayListNotNullAndNotEmptyAndMaxItems(targetField);
    }

    @Test
    void testJakartaArrayListNotNullAndNotEmptyAndMaxItems() {
        FieldInfo targetField = jakartaTargetClass.field("arrayListNotNullAndNotEmptyAndMaxItems");
        testArrayListNotNullAndNotEmptyAndMaxItems(targetField);
    }

    void testArrayListNotNullAndNotEmptyAndMaxItems(FieldInfo targetField) {
        Schema parentSchema = OASFactory.createSchema();
        String propertyKey = targetField.name();

        testTarget.notNull(targetField, propertyKey, requirementHandler(parentSchema));
        testTarget.sizeArray(targetField, schema);
        testTarget.notEmptyArray(targetField, schema, propertyKey, requirementHandler(parentSchema));

        assertNull(SchemaSupport.getNullable(schema));
        assertEquals(Integer.valueOf(1), schema.getMinItems());
        assertEquals(Integer.valueOf(20), schema.getMaxItems());
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    @Test
    void testJavaxArrayListNullableAndMinItemsAndMaxItems() {
        FieldInfo targetField = javaxTargetClass.field("arrayListNullableAndMinItemsAndMaxItems");
        testArrayListNullableAndMinItemsAndMaxItems(targetField);
    }

    @Test
    void testJakartaArrayListNullableAndMinItemsAndMaxItems() {
        FieldInfo targetField = jakartaTargetClass.field("arrayListNullableAndMinItemsAndMaxItems");
        testArrayListNullableAndMinItemsAndMaxItems(targetField);
    }

    void testArrayListNullableAndMinItemsAndMaxItems(FieldInfo targetField) {
        Schema parentSchema = OASFactory.createSchema();
        String propertyKey = targetField.name();

        testTarget.notNull(targetField, propertyKey, requirementHandlerFail());
        testTarget.sizeArray(targetField, schema);
        testTarget.notEmptyArray(targetField, schema, propertyKey, requirementHandler(parentSchema));

        assertNull(SchemaSupport.getNullable(schema));
        assertEquals(Integer.valueOf(5), schema.getMinItems());
        assertEquals(Integer.valueOf(20), schema.getMaxItems());
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    /**********************************************************************/

    @Test
    void testJavaxMapObjectNotNullAndNotEmptyAndMaxProperties() {
        FieldInfo targetField = javaxTargetClass.field("mapObjectNotNullAndNotEmptyAndMaxProperties");
        testMapObjectNotNullAndNotEmptyAndMaxProperties(targetField);
    }

    @Test
    void testJakartaMapObjectNotNullAndNotEmptyAndMaxProperties() {
        FieldInfo targetField = jakartaTargetClass.field("mapObjectNotNullAndNotEmptyAndMaxProperties");
        testMapObjectNotNullAndNotEmptyAndMaxProperties(targetField);
    }

    void testMapObjectNotNullAndNotEmptyAndMaxProperties(FieldInfo targetField) {
        schema.setAdditionalPropertiesSchema(OASFactory.createSchema().booleanSchema(Boolean.TRUE));

        Schema parentSchema = OASFactory.createSchema();
        String propertyKey = targetField.name();

        testTarget.notNull(targetField, propertyKey, requirementHandler(parentSchema));
        testTarget.sizeObject(targetField, schema);
        testTarget.notEmptyObject(targetField, schema, propertyKey, requirementHandler(parentSchema));

        assertNull(SchemaSupport.getNullable(schema));
        assertEquals(Integer.valueOf(1), schema.getMinProperties());
        assertEquals(Integer.valueOf(20), schema.getMaxProperties());
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    @Test
    void testJavaxMapObjectNullableAndMinPropertiesAndMaxProperties() {
        FieldInfo targetField = javaxTargetClass.field("mapObjectNullableAndMinPropertiesAndMaxProperties");
        testMapObjectNullableAndMinPropertiesAndMaxProperties(targetField);
    }

    @Test
    void testJakartaMapObjectNullableAndMinPropertiesAndMaxProperties() {
        FieldInfo targetField = jakartaTargetClass.field("mapObjectNullableAndMinPropertiesAndMaxProperties");
        testMapObjectNullableAndMinPropertiesAndMaxProperties(targetField);
    }

    void testMapObjectNullableAndMinPropertiesAndMaxProperties(FieldInfo targetField) {
        schema.setAdditionalPropertiesSchema(OASFactory.createSchema().booleanSchema(Boolean.TRUE));

        Schema parentSchema = OASFactory.createSchema();
        String propertyKey = targetField.name();

        testTarget.notNull(targetField, propertyKey, requirementHandlerFail());
        testTarget.sizeObject(targetField, schema);
        testTarget.notEmptyObject(targetField, schema, propertyKey, requirementHandler(parentSchema));

        assertNull(SchemaSupport.getNullable(schema));
        assertEquals(Integer.valueOf(5), schema.getMinProperties());
        assertEquals(Integer.valueOf(20), schema.getMaxProperties());
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    @Test
    void testJavaxMapObjectNullableNoAdditionalProperties() {
        FieldInfo targetField = javaxTargetClass.field("mapObjectNullableAndMinPropertiesAndMaxProperties");
        testMapObjectNullableNoAdditionalProperties(targetField);
    }

    @Test
    void testJakartaMapObjectNullableNoAdditionalProperties() {
        FieldInfo targetField = jakartaTargetClass.field("mapObjectNullableAndMinPropertiesAndMaxProperties");
        testMapObjectNullableNoAdditionalProperties(targetField);
    }

    void testMapObjectNullableNoAdditionalProperties(FieldInfo targetField) {
        Schema parentSchema = OASFactory.createSchema();
        String propertyKey = targetField.name();

        testTarget.notNull(targetField, propertyKey, requirementHandlerFail());
        testTarget.sizeObject(targetField, schema);
        testTarget.notEmptyObject(targetField, schema, propertyKey, requirementHandlerFail());

        assertNull(SchemaSupport.getNullable(schema));
        assertEquals(null, schema.getMinProperties());
        assertEquals(null, schema.getMaxProperties());
        assertNull(parentSchema.getRequired());
    }

    /**********************************************************************/

    @Test
    void testJavaxDecimalMaxPrimaryDigits() {
        FieldInfo targetField = javaxTargetClass.field("decimalMaxBigDecimalPrimaryDigits");
        testDecimalMaxPrimaryDigits(targetField);
    }

    @Test
    void testJakartaDecimalMaxPrimaryDigits() {
        FieldInfo targetField = jakartaTargetClass.field("decimalMaxBigDecimalPrimaryDigits");
        testDecimalMaxPrimaryDigits(targetField);
    }

    void testDecimalMaxPrimaryDigits(FieldInfo targetField) {
        testTarget.decimalMax(targetField, schema);
        testTarget.digits(targetField, schema);

        assertEquals(new BigDecimal("200.00"), schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
        assertEquals("^\\d{1,3}([.]\\d{1,2})?$", schema.getPattern());
    }

    @Test
    void testJavaxDecimalMaxNoConstraint() {
        testTarget.decimalMax(javaxTargetClass.field("decimalMaxBigDecimalNoConstraint"), schema);
        assertEquals(null, schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    @Test
    void testJakartaDecimalMaxNoConstraint() {
        testTarget.decimalMax(jakartaTargetClass.field("decimalMaxBigDecimalNoConstraint"), schema);
        assertEquals(null, schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    @Test
    void testJavaxDecimalMaxInvalidValue() {
        testTarget.decimalMax(javaxTargetClass.field("decimalMaxBigDecimalInvalidValue"), schema);
        assertEquals(null, schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    @Test
    void testJakartaDecimalMaxInvalidValue() {
        testTarget.decimalMax(jakartaTargetClass.field("decimalMaxBigDecimalInvalidValue"), schema);
        assertEquals(null, schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    @Test
    void testJavaxDecimalMaxExclusiveDigits() {
        FieldInfo targetField = javaxTargetClass.field("decimalMaxBigDecimalExclusiveDigits");
        testDecimalMaxExclusiveDigits(targetField);
    }

    @Test
    void testJakartaDecimalMaxExclusiveDigits() {
        FieldInfo targetField = jakartaTargetClass.field("decimalMaxBigDecimalExclusiveDigits");
        testDecimalMaxExclusiveDigits(targetField);
    }

    void testDecimalMaxExclusiveDigits(FieldInfo targetField) {
        testTarget.decimalMax(targetField, schema);
        testTarget.digits(targetField, schema);
        assertEquals(new BigDecimal("201.0"), schema.getExclusiveMaximum());
        assertEquals("^\\d{1,3}([.]\\d)?$", schema.getPattern());
    }

    @Test
    void testJavaxDecimalMaxInclusive() {
        testTarget.decimalMax(javaxTargetClass.field("decimalMaxBigDecimalInclusive"), schema);
        assertEquals(new BigDecimal("201.00"), schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    @Test
    void testJakartaDecimalMaxInclusive() {
        testTarget.decimalMax(jakartaTargetClass.field("decimalMaxBigDecimalInclusive"), schema);
        assertEquals(new BigDecimal("201.00"), schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    /**********************************************************************/

    @Test
    void testJavaxDecimalMinPrimary() {
        testTarget.decimalMin(javaxTargetClass.field("decimalMinBigDecimalPrimary"), schema);
        assertEquals(new BigDecimal("10.0"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    void testJakartaDecimalMinPrimary() {
        testTarget.decimalMin(jakartaTargetClass.field("decimalMinBigDecimalPrimary"), schema);
        assertEquals(new BigDecimal("10.0"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    void testJavaxDecimalMinNoConstraint() {
        testTarget.decimalMin(javaxTargetClass.field("decimalMinBigDecimalNoConstraint"), schema);
        assertEquals(null, schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    void testJakartaDecimalMinNoConstraint() {
        testTarget.decimalMin(jakartaTargetClass.field("decimalMinBigDecimalNoConstraint"), schema);
        assertEquals(null, schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    void testJavaxDecimalMinInvalidValue() {
        testTarget.decimalMin(javaxTargetClass.field("decimalMinBigDecimalInvalidValue"), schema);
        assertEquals(null, schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    void testJakartaDecimalMinInvalidValue() {
        testTarget.decimalMin(jakartaTargetClass.field("decimalMinBigDecimalInvalidValue"), schema);
        assertEquals(null, schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    void testJavaxDecimalMinExclusiveDigits() {
        FieldInfo targetField = javaxTargetClass.field("decimalMinBigDecimalExclusiveDigits");
        testDecimalMinExclusiveDigits(targetField);
    }

    @Test
    void testJakartaDecimalMinExclusiveDigits() {
        FieldInfo targetField = jakartaTargetClass.field("decimalMinBigDecimalExclusiveDigits");
        testDecimalMinExclusiveDigits(targetField);
    }

    void testDecimalMinExclusiveDigits(FieldInfo targetField) {
        testTarget.decimalMin(targetField, schema);
        testTarget.digits(targetField, schema);

        assertEquals(new BigDecimal("9.00"), schema.getExclusiveMinimum());
        assertEquals("^\\d([.]\\d{1,2})?$", schema.getPattern());
    }

    @Test
    void testJavaxDecimalMinInclusive() {
        testTarget.decimalMin(javaxTargetClass.field("decimalMinBigDecimalInclusive"), schema);
        assertEquals(new BigDecimal("9.00"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    void testJakartaDecimalMinInclusive() {
        testTarget.decimalMin(jakartaTargetClass.field("decimalMinBigDecimalInclusive"), schema);
        assertEquals(new BigDecimal("9.00"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    /**********************************************************************/

    @Test
    void testJavaxIntegerPositiveNotZeroMaxValue() {
        FieldInfo targetField = javaxTargetClass.field("integerPositiveNotZeroMaxValue");
        testIntegerPositiveNotZeroMaxValue(targetField);
    }

    @Test
    void testJakartaIntegerPositiveNotZeroMaxValue() {
        FieldInfo targetField = jakartaTargetClass.field("integerPositiveNotZeroMaxValue");
        testIntegerPositiveNotZeroMaxValue(targetField);
    }

    void testIntegerPositiveNotZeroMaxValue(FieldInfo targetField) {
        testTarget.max(targetField, schema);
        testTarget.positive(targetField, schema);

        assertEquals(new BigDecimal("0"), schema.getExclusiveMinimum());
        assertEquals(new BigDecimal("1000"), schema.getMaximum());
    }

    @Test
    void testJavaxIntegerPositiveOrZeroMaxValue() {
        FieldInfo targetField = javaxTargetClass.field("integerPositiveOrZeroMaxValue");
        testIntegerPositiveOrZeroMaxValue(targetField);
    }

    @Test
    void testJakartaIntegerPositiveOrZeroMaxValue() {
        FieldInfo targetField = jakartaTargetClass.field("integerPositiveOrZeroMaxValue");
        testIntegerPositiveOrZeroMaxValue(targetField);
    }

    void testIntegerPositiveOrZeroMaxValue(FieldInfo targetField) {
        testTarget.max(targetField, schema);
        testTarget.positiveOrZero(targetField, schema);

        assertEquals(new BigDecimal("0"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
        assertEquals(new BigDecimal("999"), schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    /**********************************************************************/

    @Test
    void testJavaxIntegerNegativeNotZeroMinValue() {
        FieldInfo targetField = javaxTargetClass.field("integerNegativeNotZeroMinValue");
        testIntegerNegativeNotZeroMinValue(targetField);
    }

    @Test
    void testJakartaIntegerNegativeNotZeroMinValue() {
        FieldInfo targetField = jakartaTargetClass.field("integerNegativeNotZeroMinValue");
        testIntegerNegativeNotZeroMinValue(targetField);
    }

    void testIntegerNegativeNotZeroMinValue(FieldInfo targetField) {
        testTarget.min(targetField, schema);
        testTarget.negative(targetField, schema);

        assertEquals(new BigDecimal("0"), schema.getExclusiveMaximum());
        assertEquals(new BigDecimal("-1000000"), schema.getMinimum());
    }

    @Test
    void testJavaxIntegerNegativeOrZeroMinValue() {
        FieldInfo targetField = javaxTargetClass.field("integerNegativeOrZeroMinValue");
        testIntegerNegativeOrZeroMinValue(targetField);
    }

    @Test
    void testJakartaIntegerNegativeOrZeroMinValue() {
        FieldInfo targetField = jakartaTargetClass.field("integerNegativeOrZeroMinValue");
        testIntegerNegativeOrZeroMinValue(targetField);
    }

    void testIntegerNegativeOrZeroMinValue(FieldInfo targetField) {
        testTarget.min(targetField, schema);
        testTarget.negativeOrZero(targetField, schema);

        assertEquals(new BigDecimal("0"), schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
        assertEquals(new BigDecimal("-999"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    /**********************************************************************/

    @Test
    void testJavaxStringNotBlankNotNull() {
        FieldInfo targetField = javaxTargetClass.field("stringNotBlankNotNull");
        testStringNotBlankNotNull(targetField);
    }

    @Test
    void testJakartaStringNotBlankNotNull() {
        FieldInfo targetField = jakartaTargetClass.field("stringNotBlankNotNull");
        testStringNotBlankNotNull(targetField);
    }

    void testStringNotBlankNotNull(FieldInfo targetField) {
        Schema parentSchema = OASFactory.createSchema();
        String propertyKey = targetField.name();

        testTarget.notBlank(targetField, schema, propertyKey, requirementHandler(parentSchema));
        testTarget.notNull(targetField, propertyKey, requirementHandler(parentSchema));

        assertEquals("\\S", schema.getPattern());
        assertNull(SchemaSupport.getNullable(schema));
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    @Test
    void testJavaxStringNotBlankDigits() {
        FieldInfo targetField = javaxTargetClass.field("stringNotBlankDigits");
        testStringNotBlankDigits(targetField);
    }

    @Test
    void testJakartaStringNotBlankDigits() {
        FieldInfo targetField = jakartaTargetClass.field("stringNotBlankDigits");
        testStringNotBlankDigits(targetField);
    }

    void testStringNotBlankDigits(FieldInfo targetField) {
        Schema parentSchema = OASFactory.createSchema();
        String propertyKey = targetField.name();

        testTarget.digits(targetField, schema);
        testTarget.notBlank(targetField, schema, propertyKey, requirementHandler(parentSchema));

        assertEquals("^\\d{1,8}([.]\\d{1,10})?$", schema.getPattern());
        assertNull(SchemaSupport.getNullable(schema));
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    @Test
    void testJavaxStringNotEmptyMaxSize() {
        FieldInfo targetField = javaxTargetClass.field("stringNotEmptyMaxSize");
        testStringNotEmptyMaxSize(targetField);
    }

    @Test
    void testJakartaStringNotEmptyMaxSize() {
        FieldInfo targetField = jakartaTargetClass.field("stringNotEmptyMaxSize");
        testStringNotEmptyMaxSize(targetField);
    }

    void testStringNotEmptyMaxSize(FieldInfo targetField) {
        Schema parentSchema = OASFactory.createSchema();
        String propertyKey = targetField.name();

        testTarget.sizeString(targetField, schema);
        testTarget.notEmptyString(targetField, schema, propertyKey, requirementHandler(parentSchema));

        assertEquals(Integer.valueOf(1), schema.getMinLength());
        assertEquals(Integer.valueOf(2000), schema.getMaxLength());
        assertNull(SchemaSupport.getNullable(schema));
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    @Test
    void testJavaxStringNotEmptySizeRange() {
        FieldInfo targetField = javaxTargetClass.field("stringNotEmptySizeRange");
        testStringNotEmptySizeRange(targetField);
    }

    @Test
    void testJakartaStringNotEmptySizeRange() {
        FieldInfo targetField = jakartaTargetClass.field("stringNotEmptySizeRange");
        testStringNotEmptySizeRange(targetField);
    }

    void testStringNotEmptySizeRange(FieldInfo targetField) {
        Schema parentSchema = OASFactory.createSchema();
        String propertyKey = targetField.name();

        testTarget.sizeString(targetField, schema);
        testTarget.notEmptyString(targetField, schema, propertyKey, requirementHandler(parentSchema));

        assertEquals(Integer.valueOf(100), schema.getMinLength());
        assertEquals(Integer.valueOf(2000), schema.getMaxLength());
        assertNull(SchemaSupport.getNullable(schema));
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    @Test
    void testJavaxJacksonRequiredString() {
        String propertyKey = "jacksonRequiredTrueString";
        FieldInfo targetField = javaxTargetClass.field(propertyKey);
        testJacksonRequiredString(targetField, propertyKey);
    }

    @Test
    void testJakartaJacksonRequiredString() {
        String propertyKey = "jacksonRequiredTrueString";
        FieldInfo targetField = jakartaTargetClass.field(propertyKey);
        testJacksonRequiredString(targetField, propertyKey);
    }

    void testJacksonRequiredString(FieldInfo targetField, String propertyKey) {
        Schema parentSchema = OASFactory.createSchema();

        testTarget.requiredJackson(targetField, propertyKey, (target, name) -> {
            parentSchema.addRequired(name);
        });

        assertNull(SchemaSupport.getNullable(schema));
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    @Test
    void testJavaxJacksonDefaultString() {
        String propertyKey = "jacksonDefaultString";
        FieldInfo targetField = javaxTargetClass.field(propertyKey);
        testJacksonDefaultString(targetField);
    }

    @Test
    void testJakartaJacksonDefaultString() {
        String propertyKey = "jacksonDefaultString";
        FieldInfo targetField = jakartaTargetClass.field(propertyKey);
        testJacksonDefaultString(targetField);
    }

    void testJacksonDefaultString(FieldInfo targetField) {
        String propertyKey = "jacksonDefaultString";
        Schema parentSchema = OASFactory.createSchema();

        testTarget.requiredJackson(targetField, propertyKey, (target, name) -> {
            parentSchema.addRequired(name);
        });

        assertNull(SchemaSupport.getNullable(schema));
        assertNull(parentSchema.getRequired());
    }

    @Test
    void testJavaxPatternFields() {
        testPatternFields(javaxTargetClass.field("patternFromBV"), "^something$");
    }

    @Test
    void testJakartaPatternFields() {
        testPatternFields(jakartaTargetClass.field("patternFromBV"), "^something$");
    }

    void testPatternFields(FieldInfo targetField, String expectedPattern) {
        testTarget.pattern(targetField, schema);
        assertEquals(expectedPattern, schema.getPattern());
    }
}
