package io.smallrye.openapi.runtime.scanner.dataobject;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.junit.Before;
import org.junit.Test;

import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class BeanValidationScannerTest extends IndexScannerTestBase {

    BeanValidationScanner testTarget;
    Index index;
    Set<String> methodsInvoked = new LinkedHashSet<>();
    Schema schema;
    ClassInfo targetClass;

    @Before
    public void beforeEach() {
        testTarget = BeanValidationScanner.INSTANCE;

        Indexer indexer = new Indexer();
        index(indexer, "io/smallrye/openapi/runtime/scanner/dataobject/BeanValidationScannerTest.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/dataobject/BeanValidationScannerTest$BVTestContainer.class");
        index = indexer.complete();

        methodsInvoked.clear();
        schema = new SchemaImpl();
        targetClass = index.getClassByName(componentize(BVTestContainer.class.getName()));
    }

    Schema proxySchema(Schema schema, Set<String> methodsInvoked) {
        Schema schemaProxy = (Schema) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
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

        return schemaProxy;
    }

    @Test
    public void testNullSchemaIgnored() {
        BeanValidationScanner.applyConstraints(targetClass,
                                               proxySchema(schema, methodsInvoked),
                                               null,
                                               null);

        assertArrayEquals("Unexpected methods were invoked",
                          new String[] { "getType" },
                          methodsInvoked.toArray());
    }

    @Test
    public void testRefSchemaIgnored() {
        schema.setType(SchemaType.OBJECT);
        schema.setRef("#/components/schemas/Anything");
        BeanValidationScanner.applyConstraints(targetClass,
                                               proxySchema(schema, methodsInvoked),
                                               null,
                                               null);

        assertArrayEquals("Unexpected methods were invoked",
                          new String[] { "getType", "getRef" },
                          methodsInvoked.toArray());
    }

    /**********************************************************************/

    @Test
    public void testArrayListNotNullAndNotEmptyAndMaxItems() {
        FieldInfo targetField = targetClass.field("arrayListNotNullAndNotEmptyAndMaxItems");
        Schema parentSchema = new SchemaImpl();
        String propertyKey = "TESTKEY";

        testTarget.notNull(targetField, schema, propertyKey, (target, name) -> {
            parentSchema.addRequired(name);
        });
        testTarget.sizeArray(targetField, schema);
        testTarget.notEmptyArray(targetField, schema);

        assertEquals(Boolean.FALSE, schema.getNullable());
        assertEquals(Integer.valueOf(1), schema.getMinItems());
        assertEquals(Integer.valueOf(20), schema.getMaxItems());
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    @Test
    public void testArrayListNullableAndMinItemsAndMaxItems() {
        FieldInfo targetField = targetClass.field("arrayListNullableAndMinItemsAndMaxItems");
        Schema parentSchema = new SchemaImpl();
        String propertyKey = "TESTKEY";

        testTarget.notNull(targetField, schema, propertyKey, (target, name) -> {
            fail("Unexpected call to handler");
        });
        testTarget.sizeArray(targetField, schema);
        testTarget.notEmptyArray(targetField, schema);

        assertEquals(null, schema.getNullable());
        assertEquals(Integer.valueOf(5), schema.getMinItems());
        assertEquals(Integer.valueOf(20), schema.getMaxItems());
        assertNull(parentSchema.getRequired());
    }

    /**********************************************************************/

    @Test
    public void testMapObjectNotNullAndNotEmptyAndMaxProperties() {
        schema.setAdditionalPropertiesBoolean(Boolean.TRUE);

        FieldInfo targetField = targetClass.field("mapObjectNotNullAndNotEmptyAndMaxProperties");
        Schema parentSchema = new SchemaImpl();
        String propertyKey = "TESTKEY";

        testTarget.notNull(targetField, schema, propertyKey, (target, name) -> {
            parentSchema.addRequired(name);
        });
        testTarget.sizeObject(targetField, schema);
        testTarget.notEmptyObject(targetField, schema);

        assertEquals(Boolean.FALSE, schema.getNullable());
        assertEquals(Integer.valueOf(1), schema.getMinProperties());
        assertEquals(Integer.valueOf(20), schema.getMaxProperties());
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    @Test
    public void testMapObjectNullableAndMinPropertiesAndMaxProperties() {
        schema.setAdditionalPropertiesBoolean(Boolean.TRUE);

        FieldInfo targetField = targetClass.field("mapObjectNullableAndMinPropertiesAndMaxProperties");
        Schema parentSchema = new SchemaImpl();
        String propertyKey = "TESTKEY";

        testTarget.notNull(targetField, schema, propertyKey, (target, name) -> {
            fail("Unexpected call to handler");
        });
        testTarget.sizeObject(targetField, schema);
        testTarget.notEmptyObject(targetField, schema);

        assertEquals(null, schema.getNullable());
        assertEquals(Integer.valueOf(5), schema.getMinProperties());
        assertEquals(Integer.valueOf(20), schema.getMaxProperties());
        assertNull(parentSchema.getRequired());
    }

    @Test
    public void testMapObjectNullableNoAdditionalProperties() {
        FieldInfo targetField = targetClass.field("mapObjectNullableAndMinPropertiesAndMaxProperties");
        Schema parentSchema = new SchemaImpl();
        String propertyKey = "TESTKEY";

        testTarget.notNull(targetField, schema, propertyKey, (target, name) -> {
            fail("Unexpected call to handler");
        });
        testTarget.sizeObject(targetField, schema);
        testTarget.notEmptyObject(targetField, schema);

        assertEquals(null, schema.getNullable());
        assertEquals(null, schema.getMinProperties());
        assertEquals(null, schema.getMaxProperties());
        assertNull(parentSchema.getRequired());
    }

    /**********************************************************************/

    @Test
    public void testDecimalMaxPrimaryDigits() {
        FieldInfo targetField = targetClass.field("decimalMaxBigDecimalPrimaryDigits");
        testTarget.decimalMax(targetField, schema);
        testTarget.digits(targetField, schema);

        assertEquals(new BigDecimal("200.00"), schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
        assertEquals("^\\d{1,3}([.]\\d{1,2})?$", schema.getPattern());
    }

    @Test
    public void testDecimalMaxNoConstraint() {
        testTarget.decimalMax(targetClass.field("decimalMaxBigDecimalNoConstraint"), schema);
        assertEquals(null, schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    @Test
    public void testDecimalMaxInvalidValue() {
        testTarget.decimalMax(targetClass.field("decimalMaxBigDecimalInvalidValue"), schema);
        assertEquals(null, schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    @Test
    public void testDecimalMaxExclusiveDigits() {
        FieldInfo targetField = targetClass.field("decimalMaxBigDecimalExclusiveDigits");
        testTarget.decimalMax(targetField, schema);
        testTarget.digits(targetField, schema);
        assertEquals(new BigDecimal("201.0"), schema.getMaximum());
        assertEquals(Boolean.TRUE, schema.getExclusiveMaximum());
        assertEquals("^\\d{1,3}([.]\\d)?$", schema.getPattern());
    }

    @Test
    public void testDecimalMaxInclusive() {
        testTarget.decimalMax(targetClass.field("decimalMaxBigDecimalInclusive"), schema);
        assertEquals(new BigDecimal("201.00"), schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    /**********************************************************************/

    @Test
    public void testDecimalMinPrimary() {
        testTarget.decimalMin(targetClass.field("decimalMinBigDecimalPrimary"), schema);
        assertEquals(new BigDecimal("10.0"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    public void testDecimalMinNoConstraint() {
        testTarget.decimalMin(targetClass.field("decimalMinBigDecimalNoConstraint"), schema);
        assertEquals(null, schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    public void testDecimalMinInvalidValue() {
        testTarget.decimalMin(targetClass.field("decimalMinBigDecimalInvalidValue"), schema);
        assertEquals(null, schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    public void testDecimalMinExclusiveDigits() {
        FieldInfo targetField = targetClass.field("decimalMinBigDecimalExclusiveDigits");
        testTarget.decimalMin(targetField, schema);
        testTarget.digits(targetField, schema);

        assertEquals(new BigDecimal("9.00"), schema.getMinimum());
        assertEquals(Boolean.TRUE, schema.getExclusiveMinimum());
        assertEquals("^\\d([.]\\d{1,2})?$", schema.getPattern());
    }

    @Test
    public void testDecimalMinInclusive() {
        testTarget.decimalMin(targetClass.field("decimalMinBigDecimalInclusive"), schema);
        assertEquals(new BigDecimal("9.00"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    /**********************************************************************/

    @Test
    public void testIntegerPositiveNotZeroMaxValue() {
        FieldInfo targetField = targetClass.field("integerPositiveNotZeroMaxValue");
        testTarget.max(targetField, schema);
        testTarget.positive(targetField, schema);

        assertEquals(new BigDecimal("1"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
        assertEquals(new BigDecimal("1000"), schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    @Test
    public void testIntegerPositiveNotZeroMaxValueExclusive() {
        FieldInfo targetField = targetClass.field("integerPositiveNotZeroMaxValue");
        schema.setExclusiveMaximum(Boolean.TRUE);
        schema.setExclusiveMinimum(Boolean.TRUE);

        testTarget.max(targetField, schema);
        testTarget.positive(targetField, schema);

        assertEquals(new BigDecimal("0"), schema.getMinimum());
        assertEquals(Boolean.TRUE, schema.getExclusiveMinimum());
        assertEquals(new BigDecimal("1000"), schema.getMaximum());
        assertEquals(Boolean.TRUE, schema.getExclusiveMaximum());
    }

    @Test
    public void testIntegerPositiveOrZeroMaxValue() {
        FieldInfo targetField = targetClass.field("integerPositiveOrZeroMaxValue");
        testTarget.max(targetField, schema);
        testTarget.positiveOrZero(targetField, schema);

        assertEquals(new BigDecimal("0"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
        assertEquals(new BigDecimal("999"), schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
    }

    @Test
    public void testIntegerPositiveOrZeroMaxValueExclusive() {
        FieldInfo targetField = targetClass.field("integerPositiveOrZeroMaxValue");
        schema.setExclusiveMaximum(Boolean.TRUE);
        schema.setExclusiveMinimum(Boolean.TRUE);

        testTarget.max(targetField, schema);
        testTarget.positiveOrZero(targetField, schema);

        assertEquals(new BigDecimal("-1"), schema.getMinimum());
        assertEquals(Boolean.TRUE, schema.getExclusiveMinimum());
        assertEquals(new BigDecimal("999"), schema.getMaximum());
        assertEquals(Boolean.TRUE, schema.getExclusiveMaximum());
    }

    /**********************************************************************/

    @Test
    public void testIntegerNegativeNotZeroMinValue() {
        FieldInfo targetField = targetClass.field("integerNegativeNotZeroMinValue");
        testTarget.min(targetField, schema);
        testTarget.negative(targetField, schema);

        assertEquals(new BigDecimal("-1"), schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
        assertEquals(new BigDecimal("-1000000"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    public void testIntegerNegativeNotZeroMinValueExclusive() {
        FieldInfo targetField = targetClass.field("integerNegativeNotZeroMinValue");
        schema.setExclusiveMaximum(Boolean.TRUE);
        schema.setExclusiveMinimum(Boolean.TRUE);

        testTarget.min(targetField, schema);
        testTarget.negative(targetField, schema);

        assertEquals(new BigDecimal("0"), schema.getMaximum());
        assertEquals(Boolean.TRUE, schema.getExclusiveMaximum());
        assertEquals(new BigDecimal("-1000000"), schema.getMinimum());
        assertEquals(Boolean.TRUE, schema.getExclusiveMinimum());
    }

    @Test
    public void testIntegerNegativeOrZeroMinValue() {
        FieldInfo targetField = targetClass.field("integerNegativeOrZeroMinValue");
        testTarget.min(targetField, schema);
        testTarget.negativeOrZero(targetField, schema);

        assertEquals(new BigDecimal("0"), schema.getMaximum());
        assertEquals(null, schema.getExclusiveMaximum());
        assertEquals(new BigDecimal("-999"), schema.getMinimum());
        assertEquals(null, schema.getExclusiveMinimum());
    }

    @Test
    public void testIntegerNegativeOrZeroMinValueExclusive() {
        FieldInfo targetField = targetClass.field("integerNegativeOrZeroMinValue");
        schema.setExclusiveMaximum(Boolean.TRUE);
        schema.setExclusiveMinimum(Boolean.TRUE);

        testTarget.min(targetField, schema);
        testTarget.negativeOrZero(targetField, schema);

        assertEquals(new BigDecimal("1"), schema.getMaximum());
        assertEquals(Boolean.TRUE, schema.getExclusiveMaximum());
        assertEquals(new BigDecimal("-999"), schema.getMinimum());
        assertEquals(Boolean.TRUE, schema.getExclusiveMinimum());
    }

    /**********************************************************************/

    @Test
    public void testStringNotBlankNotNull() {
        FieldInfo targetField = targetClass.field("stringNotBlankNotNull");
        Schema parentSchema = new SchemaImpl();
        String propertyKey = "TESTKEY";

        testTarget.notBlank(targetField, schema);
        testTarget.notNull(targetField, schema, propertyKey, (target, name) -> {
            parentSchema.addRequired(name);
        });

        assertEquals("\\S", schema.getPattern());
        assertEquals(Boolean.FALSE, schema.getNullable());
        assertEquals(Arrays.asList(propertyKey), parentSchema.getRequired());
    }

    @Test
    public void testStringNotBlankDigits() {
        FieldInfo targetField = targetClass.field("stringNotBlankDigits");

        testTarget.digits(targetField, schema);
        testTarget.notBlank(targetField, schema);

        assertEquals("^\\d{1,8}([.]\\d{1,10})?$", schema.getPattern());
        assertEquals(Boolean.FALSE, schema.getNullable());
    }

    @Test
    public void testStringNotEmptyMaxSize() {
        FieldInfo targetField = targetClass.field("stringNotEmptyMaxSize");

        testTarget.sizeString(targetField, schema);
        testTarget.notEmptyString(targetField, schema);

        assertEquals(Integer.valueOf(1), schema.getMinLength());
        assertEquals(Integer.valueOf(2000), schema.getMaxLength());
        assertEquals(Boolean.FALSE, schema.getNullable());
    }

    @Test
    public void testStringNotEmptySizeRange() {
        FieldInfo targetField = targetClass.field("stringNotEmptySizeRange");

        testTarget.sizeString(targetField, schema);
        testTarget.notEmptyString(targetField, schema);

        assertEquals(Integer.valueOf(100), schema.getMinLength());
        assertEquals(Integer.valueOf(2000), schema.getMaxLength());
        assertEquals(Boolean.FALSE, schema.getNullable());
    }

    /**********************************************************************/

    @SuppressWarnings("unused")
    static class BVTestContainer {
        @NotNull
        @NotEmpty
        @Size(max = 20)
        List<String> arrayListNotNullAndNotEmptyAndMaxItems;

        @NotEmpty
        @Size(min = 5, max = 20)
        List<String> arrayListNullableAndMinItemsAndMaxItems;

        /**********************************************************************/

        @NotNull
        @NotEmpty
        @Size(max = 20)
        Map<String, String> mapObjectNotNullAndNotEmptyAndMaxProperties;

        @NotEmpty
        @Size(min = 5, max = 20)
        Map<String, String> mapObjectNullableAndMinPropertiesAndMaxProperties;

        /**********************************************************************/

        @DecimalMax("200.00")
        @Digits(integer = 3, fraction = 2)
        private BigDecimal decimalMaxBigDecimalPrimaryDigits;
        private BigDecimal decimalMaxBigDecimalNoConstraint;
        @DecimalMax("Invalid BigDecimal value")
        private BigDecimal decimalMaxBigDecimalInvalidValue;
        @DecimalMax(value = "201.0", inclusive = false, groups = {})
        @Digits(integer = 3, fraction = 1)
        private BigDecimal decimalMaxBigDecimalExclusiveDigits;
        @DecimalMax(value = "201.00", inclusive = true, groups = Default.class)
        private BigDecimal decimalMaxBigDecimalInclusive;

        /**********************************************************************/

        @DecimalMin("10.0")
        private BigDecimal decimalMinBigDecimalPrimary;
        private BigDecimal decimalMinBigDecimalNoConstraint;
        @DecimalMin("Invalid BigDecimal value")
        private BigDecimal decimalMinBigDecimalInvalidValue;
        @DecimalMin(value = "9.00", inclusive = false)
        @Digits(integer = 1, fraction = 2)
        private BigDecimal decimalMinBigDecimalExclusiveDigits;
        @DecimalMin(value = "9.00", inclusive = true)
        private BigDecimal decimalMinBigDecimalInclusive;

        /**********************************************************************/

        @Positive
        @Max(1000)
        private Long integerPositiveNotZeroMaxValue;

        @PositiveOrZero
        @Max(999)
        private Integer integerPositiveOrZeroMaxValue;

        @Negative
        @Min(-1_000_000)
        private Long integerNegativeNotZeroMinValue;

        @NegativeOrZero
        @Min(-999)
        private Integer integerNegativeOrZeroMinValue;

        /**********************************************************************/

        @NotNull
        @NotBlank
        private String stringNotBlankNotNull;

        @Digits(integer = 8, fraction = 10)
        @NotBlank
        private String stringNotBlankDigits;

        @NotEmpty
        @Size(max = 2000)
        private String stringNotEmptyMaxSize;

        @NotEmpty
        @Size(min = 100, max = 2000)
        private String stringNotEmptySizeRange;

        /**********************************************************************/

        @NotNull
        private boolean booleanNotNull;
    }
}
