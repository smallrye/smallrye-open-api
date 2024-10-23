package io.smallrye.openapi.runtime.scanner.dataobject;

import static org.jboss.jandex.DotName.createComponentized;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.constants.JacksonConstants;
import io.smallrye.openapi.api.constants.KotlinConstants;
import io.smallrye.openapi.internal.models.media.SchemaSupport;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class BeanValidationScanner {

    public interface RequirementHandler {
        void setRequired(AnnotationTarget target, String propertyKey);
    }

    static final Set<DotName> CONSTRAINTS = new HashSet<>();
    private static final String VALUE = "value";
    private static final String INCLUSIVE = "inclusive";

    static final BigDecimal NEGATIVE_ONE = BigDecimal.ZERO.subtract(BigDecimal.ONE);

    static final DotName BV_JAVAX = createComponentized(null, "javax");
    static final DotName BV_JAKARTA = createComponentized(null, "jakarta");

    static final DotName BV_JAVAX_BASE = createComponentized(BV_JAVAX, "validation");
    static final DotName BV_JAKARTA_BASE = createComponentized(BV_JAKARTA, "validation");

    static final DotName BV_JAVAX_GROUPS = createComponentized(BV_JAVAX_BASE, "groups");
    static final DotName BV_JAKARTA_GROUPS = createComponentized(BV_JAKARTA_BASE, "groups");

    static final DotName BV_JAVAX_DEFAULT_GROUP = createComponentized(BV_JAVAX_GROUPS, "Default");
    static final DotName BV_JAKARTA_DEFAULT_GROUP = createComponentized(BV_JAKARTA_GROUPS, "Default");

    static final DotName BV_JAVAX_CONTRAINTS = createComponentized(BV_JAVAX_BASE, "constraints");
    static final DotName BV_JAKARTA_CONTRAINTS = createComponentized(BV_JAKARTA_BASE, "constraints");

    // Bean Validation Constraints
    static final List<DotName> BV_DECIMAL_MAX = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "DecimalMax"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "DecimalMax"));
    static final List<DotName> BV_DECIMAL_MIN = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "DecimalMin"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "DecimalMin"));
    static final List<DotName> BV_DIGITS = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "Digits"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "Digits"));
    static final List<DotName> BV_MAX = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "Max"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "Max"));
    static final List<DotName> BV_MIN = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "Min"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "Min"));
    static final List<DotName> BV_NEGATIVE = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "Negative"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "Negative"));
    static final List<DotName> BV_NEGATIVE_OR_ZERO = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "NegativeOrZero"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "NegativeOrZero"));
    static final List<DotName> BV_NOT_BLANK = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "NotBlank"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "NotBlank"));
    static final List<DotName> BV_NOT_EMPTY = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "NotEmpty"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "NotEmpty"));
    static final List<DotName> BV_NOT_NULL = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "NotNull"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "NotNull"));
    static final List<DotName> BV_PATTERN = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "Pattern"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "Pattern"));
    static final List<DotName> BV_POSITIVE = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "Positive"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "Positive"));
    static final List<DotName> BV_POSITIVE_OR_ZERO = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "PositiveOrZero"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "PositiveOrZero"));
    static final List<DotName> BV_SIZE = Arrays.asList(
            createConstraintName(BV_JAVAX_CONTRAINTS, "Size"),
            createConstraintName(BV_JAKARTA_CONTRAINTS, "Size"));

    // Jackson Constraints
    static final DotName JACKSON_JSONPROPERTY = createConstraintName(JacksonConstants.JSON_PROPERTY);

    // Kotlin Constraints
    static final DotName KOTLIN_NULLABLE = createConstraintName(KotlinConstants.JETBRAINS_NULLABLE);
    static final DotName KOTLIN_NOT_NULL = createConstraintName(KotlinConstants.JETBRAINS_NOT_NULL);

    static DotName createConstraintName(DotName packageName, String className) {
        return createConstraintName(createComponentized(packageName, className));
    }

    static DotName createConstraintName(DotName constraintName) {
        CONSTRAINTS.add(constraintName);
        return constraintName;
    }

    private final AnnotationScannerContext context;

    public BeanValidationScanner(AnnotationScannerContext context) {
        this.context = context;
    }

    /**
     * Scan the annotation target to determine whether any annotations
     * from the Bean Validation package (<code>javax.validation.constraints</code>) are
     * present.
     *
     * @param target the annotation target to scan
     * @return true if annotations from the Bean Validation package are present, otherwise false.
     */
    public boolean hasConstraints(AnnotationTarget target) {
        return context.annotations().hasAnnotation(target, CONSTRAINTS);
    }

    /**
     * Determine if any Java Bean Validation constraint annotations are present
     * on the {@link AnnotationTarget} that are applicable to the schema. This
     * method will apply the constraints to the schema only if no value has
     * previously been set.
     *
     * If the schema's type attribute has not been previously set or the schema
     * contains a reference, this method will not apply any changes to the
     * schema.
     *
     * Each of the constraints (defined in javax.validation.constraints) will
     * apply to the schema based on the schema's type.
     *
     * When a bean validation @NotNull constraint
     * applies to the schema, the provided {@link RequirementHandler} will be
     * called in order for the component calling this method to determine if and
     * how to apply the requirement. E.g. a required Schema is communicated
     * differently for a parent schema and for a parameter described by the
     * schema.
     *
     * @param target
     *        the object from which to retrieve the constraint annotations
     * @param schema
     *        the schema to which the constraints will be applied
     * @param propertyKey
     *        the name of the property in parentSchema that refers to the
     *        schema
     * @param handler
     *        the handler to be called when a
     *        bean validation @NotNull constraint is encountered.
     */
    public void applyConstraints(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {

        List<SchemaType> schemaTypes = schema.getType();

        /*
         * The type be set. Attributes set in this function are not application
         * to $ref type schemas.
         */
        if (schemaTypes == null || schemaTypes.isEmpty() || schema.getRef() != null) {
            return;
        }

        for (SchemaType schemaType : new ArrayList<>(schemaTypes)) {
            switch (schemaType) {
                case ARRAY:
                    applyArrayConstraints(target, schema, propertyKey, handler);
                    break;
                case BOOLEAN:
                    applyBooleanConstraints(target, schema, propertyKey, handler);
                    break;
                case INTEGER:
                    applyNumberConstraints(target, schema, propertyKey, handler);
                    break;
                case NUMBER:
                    applyNumberConstraints(target, schema, propertyKey, handler);
                    break;
                case OBJECT:
                    applyObjectConstraints(target, schema, propertyKey, handler);
                    break;
                case STRING:
                    applyStringConstraints(target, schema, propertyKey, handler);
                    break;
                default:
                    break;
            }
        }
    }

    private void applyStringConstraints(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {
        decimalMax(target, schema);
        decimalMin(target, schema);
        pattern(target, schema);
        digits(target, schema);
        notBlank(target, schema, propertyKey, handler);
        notNull(target, propertyKey, handler);
        notNullKotlin(target, propertyKey, handler);
        nullableKotlin(target, schema);
        requiredJackson(target, propertyKey, handler);
        sizeString(target, schema);
        notEmptyString(target, schema, propertyKey, handler);
    }

    private void applyObjectConstraints(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {
        notNull(target, propertyKey, handler);
        notNullKotlin(target, propertyKey, handler);
        nullableKotlin(target, schema);
        requiredJackson(target, propertyKey, handler);
        sizeObject(target, schema);
        notEmptyObject(target, schema, propertyKey, handler);
    }

    private void applyArrayConstraints(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {
        notNull(target, propertyKey, handler);
        notNullKotlin(target, propertyKey, handler);
        nullableKotlin(target, schema);
        requiredJackson(target, propertyKey, handler);
        sizeArray(target, schema);
        notEmptyArray(target, schema, propertyKey, handler);
    }

    private void applyNumberConstraints(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {
        decimalMax(target, schema);
        decimalMin(target, schema);
        digits(target, schema);
        max(target, schema);
        min(target, schema);
        negative(target, schema);
        negativeOrZero(target, schema);
        notNull(target, propertyKey, handler);
        notNullKotlin(target, propertyKey, handler);
        nullableKotlin(target, schema);
        requiredJackson(target, propertyKey, handler);
        positive(target, schema);
        positiveOrZero(target, schema);
    }

    private void applyBooleanConstraints(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {
        notNull(target, propertyKey, handler);
        notNullKotlin(target, propertyKey, handler);
        nullableKotlin(target, schema);
        requiredJackson(target, propertyKey, handler);
    }

    void decimalMax(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_DECIMAL_MAX);

        if (constraint != null && schema.getMaximum() == null && schema.getExclusiveMaximum() == null) {
            String decimalValue = context.annotations().value(constraint, VALUE);
            Boolean inclusive = context.annotations().value(constraint, INCLUSIVE);
            try {
                BigDecimal decimal = new BigDecimal(decimalValue);

                if (Boolean.FALSE.equals(inclusive)) {
                    schema.setExclusiveMaximum(decimal);
                } else {
                    schema.setMaximum(decimal);
                }
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                DataObjectLogging.logger.invalidAnnotationFormat(decimalValue);
            }
        }
    }

    void decimalMin(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_DECIMAL_MIN);

        if (constraint != null && schema.getMinimum() == null && schema.getExclusiveMinimum() == null) {
            String decimalValue = context.annotations().value(constraint, VALUE);
            Boolean inclusive = context.annotations().value(constraint, INCLUSIVE);
            try {
                BigDecimal decimal = new BigDecimal(decimalValue);

                if (Boolean.FALSE.equals(inclusive)) {
                    schema.setExclusiveMinimum(decimal);
                } else {
                    schema.setMinimum(decimal);
                }
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                DataObjectLogging.logger.invalidAnnotationFormat(decimalValue);
            }
        }

    }

    void digits(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_DIGITS);

        if (constraint != null && schema.getPattern() == null) {
            // Both attributes are required - safe to use primitives.
            final int integerPart = context.annotations().value(constraint, "integer");
            final int fractionPart = context.annotations().value(constraint, "fraction");
            final StringBuilder pattern = new StringBuilder(50);

            pattern.append('^');

            if (integerPart > 0) {
                pattern.append("\\d");

                if (integerPart > 1) {
                    pattern.append("{1,").append(integerPart).append('}');
                }
            }

            if (fractionPart > 0) {
                pattern.append("([.]\\d");

                if (fractionPart > 1) {
                    pattern.append("{1,").append(fractionPart).append("}");
                }

                pattern.append(")?");
            }

            pattern.append('$');
            schema.setPattern(pattern.toString());
        }
    }

    void max(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_MAX);

        if (constraint != null && schema.getMaximum() == null) {
            AnnotationValue value = constraint.value(VALUE);
            schema.setMaximum(new BigDecimal(value.asLong()));
        }
    }

    void min(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_MIN);

        if (constraint != null && schema.getMinimum() == null) {
            AnnotationValue value = constraint.value(VALUE);
            schema.setMinimum(new BigDecimal(value.asLong()));
        }
    }

    void negative(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_NEGATIVE);

        if (constraint != null && schema.getMaximum() == null && schema.getExclusiveMaximum() == null) {
            schema.setExclusiveMaximum(BigDecimal.ZERO);
        }
    }

    void negativeOrZero(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_NEGATIVE_OR_ZERO);

        if (constraint != null && schema.getMaximum() == null && schema.getExclusiveMaximum() == null) {
            schema.setMaximum(BigDecimal.ZERO);
        }
    }

    void notBlank(AnnotationTarget target, Schema schema, String propertyKey, RequirementHandler handler) {
        AnnotationInstance constraint = getConstraint(target, BV_NOT_BLANK);

        if (constraint != null) {
            if (schema.getPattern() == null) {
                schema.setPattern("\\S");
            }

            handler.setRequired(target, propertyKey);
        }
    }

    void notEmptyArray(AnnotationTarget target, Schema schema, String propertyKey, RequirementHandler handler) {
        AnnotationInstance constraint = getConstraint(target, BV_NOT_EMPTY);

        if (constraint != null) {
            if (schema.getMinItems() == null) {
                schema.setMinItems(1);
            }

            handler.setRequired(target, propertyKey);
        }
    }

    void notEmptyObject(AnnotationTarget target, Schema schema, String propertyKey, RequirementHandler handler) {
        if (!allowsAdditionalProperties(schema)) {
            return;
        }

        AnnotationInstance constraint = getConstraint(target, BV_NOT_EMPTY);

        if (constraint != null) {
            if (schema.getMinProperties() == null) {
                schema.setMinProperties(1);
            }

            handler.setRequired(target, propertyKey);
        }
    }

    void notEmptyString(AnnotationTarget target, Schema schema, String propertyKey, RequirementHandler handler) {
        AnnotationInstance constraint = getConstraint(target, BV_NOT_EMPTY);

        if (constraint != null) {
            if (schema.getMinLength() == null) {
                schema.setMinLength(1);
            }

            handler.setRequired(target, propertyKey);
        }
    }

    void notNull(AnnotationTarget target, String propertyKey, RequirementHandler handler) {
        AnnotationInstance constraint = getConstraint(target, BV_NOT_NULL);

        if (constraint != null) {
            handler.setRequired(target, propertyKey);
        }
    }

    void notNullKotlin(AnnotationTarget target, String propertyKey, RequirementHandler handler) {
        if (context.annotations().hasAnnotation(target, KOTLIN_NOT_NULL)) {
            handler.setRequired(target, propertyKey);
        }
    }

    void nullableKotlin(AnnotationTarget target, Schema schema) {
        if (context.annotations().hasAnnotation(target, KOTLIN_NULLABLE) && SchemaSupport.getNullable(schema) == null) {
            SchemaSupport.setNullable(schema, Boolean.TRUE);
        }
    }

    void pattern(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_PATTERN);

        if (constraint != null && schema.getPattern() == null) {
            schema.setPattern(context.annotations().value(constraint, "regexp"));
        }
    }

    void positive(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_POSITIVE);

        if (constraint != null && schema.getMinimum() == null && schema.getExclusiveMinimum() == null) {
            schema.setExclusiveMinimum(BigDecimal.ZERO);
        }
    }

    void positiveOrZero(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_POSITIVE_OR_ZERO);

        if (constraint != null && schema.getMinimum() == null && schema.getExclusiveMinimum() == null) {
            schema.setMinimum(BigDecimal.ZERO);
        }
    }

    void requiredJackson(AnnotationTarget target, String propertyKey, RequirementHandler handler) {
        Boolean required = context.annotations().getAnnotationValue(target, JACKSON_JSONPROPERTY, "required");

        if (Boolean.TRUE.equals(required)) {
            handler.setRequired(target, propertyKey);
        }
    }

    void sizeArray(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_SIZE);

        if (constraint != null) {
            Integer min = context.annotations().value(constraint, "min");
            Integer max = context.annotations().value(constraint, "max");

            if (min != null && schema.getMinItems() == null) {
                schema.setMinItems(min);
            }

            if (max != null && schema.getMaxItems() == null) {
                schema.setMaxItems(max);
            }
        }
    }

    void sizeObject(AnnotationTarget target, Schema schema) {
        if (!allowsAdditionalProperties(schema)) {
            return;
        }

        AnnotationInstance constraint = getConstraint(target, BV_SIZE);

        if (constraint != null) {
            Integer min = context.annotations().value(constraint, "min");
            Integer max = context.annotations().value(constraint, "max");

            if (min != null && schema.getMinProperties() == null) {
                schema.setMinProperties(min);
            }

            if (max != null && schema.getMaxProperties() == null) {
                schema.setMaxProperties(max);
            }
        }
    }

    void sizeString(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_SIZE);

        if (constraint != null) {
            Integer min = context.annotations().value(constraint, "min");
            Integer max = context.annotations().value(constraint, "max");

            if (min != null && schema.getMinLength() == null) {
                schema.setMinLength(min);
            }

            if (max != null && schema.getMaxLength() == null) {
                schema.setMaxLength(max);
            }
        }
    }

    boolean allowsAdditionalProperties(Schema schema) {
        return schema.getAdditionalPropertiesSchema() != null;
    }

    /**
     * Retrieves a constraint {@link AnnotationInstance} from the current
     * target. If the annotation is found and does not apply to the {@link Default}
     * group, returns null.
     *
     * @param target
     *        the object from which to retrieve the constraint annotation
     * @param annotationName
     *        name of the annotation
     * @return the first occurrence of the named constraint if it applies to the
     *         {@link Default} group, otherwise null
     */
    AnnotationInstance getConstraint(AnnotationTarget target, List<DotName> annotationName) {
        AnnotationInstance constraint = context.annotations().getAnnotation(target, annotationName);

        if (constraint != null && JandexUtil.equals(constraint.target(), target)) {
            AnnotationValue groupValue = constraint.value("groups");

            if (groupValue == null) {
                return constraint;
            }

            Type[] groups = groupValue.asClassArray();

            if (groups.length == 0) {
                return constraint;
            }

            for (Type group : groups) {
                if (group.name().equals(BV_JAVAX_DEFAULT_GROUP) || group.name().equals(BV_JAKARTA_DEFAULT_GROUP)) {
                    return constraint;
                }
            }
        }

        return null;
    }

}
