package io.smallrye.openapi.runtime.scanner.dataobject;

import static io.smallrye.openapi.runtime.util.JandexUtil.booleanValue;
import static io.smallrye.openapi.runtime.util.JandexUtil.intValue;
import static io.smallrye.openapi.runtime.util.JandexUtil.stringValue;
import static io.smallrye.openapi.runtime.util.TypeUtil.getAnnotation;
import static org.jboss.jandex.DotName.createComponentized;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.constants.JacksonConstants;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class BeanValidationScanner {

    public interface RequirementHandler {
        void setRequired(AnnotationTarget target, String propertyKey);
    }

    static final BeanValidationScanner INSTANCE = new BeanValidationScanner();
    static final Set<DotName> CONSTRAINTS = new HashSet<>();

    static final BigDecimal NEGATIVE_ONE = BigDecimal.ZERO.subtract(BigDecimal.ONE);

    static final DotName BV_JAVAX = createComponentized(null, "javax");
    static final DotName BV_BASE = createComponentized(BV_JAVAX, "validation");

    static final DotName BV_GROUPS = createComponentized(BV_BASE, "groups");
    static final DotName BV_DEFAULT_GROUP = createComponentized(BV_GROUPS, "Default");

    static final DotName BV_CONTRAINTS = createComponentized(BV_BASE, "constraints");

    // Bean Validation Constraints
    static final DotName BV_DECIMAL_MAX = createConstraintName(BV_CONTRAINTS, "DecimalMax");
    static final DotName BV_DECIMAL_MIN = createConstraintName(BV_CONTRAINTS, "DecimalMin");
    static final DotName BV_DIGITS = createConstraintName(BV_CONTRAINTS, "Digits");
    static final DotName BV_MAX = createConstraintName(BV_CONTRAINTS, "Max");
    static final DotName BV_MIN = createConstraintName(BV_CONTRAINTS, "Min");
    static final DotName BV_NEGATIVE = createConstraintName(BV_CONTRAINTS, "Negative");
    static final DotName BV_NEGATIVE_OR_ZERO = createConstraintName(BV_CONTRAINTS, "NegativeOrZero");
    static final DotName BV_NOT_BLANK = createConstraintName(BV_CONTRAINTS, "NotBlank");
    static final DotName BV_NOT_EMPTY = createConstraintName(BV_CONTRAINTS, "NotEmpty");
    static final DotName BV_NOT_NULL = createConstraintName(BV_CONTRAINTS, "NotNull");
    static final DotName BV_POSITIVE = createConstraintName(BV_CONTRAINTS, "Positive");
    static final DotName BV_POSITIVE_OR_ZERO = createConstraintName(BV_CONTRAINTS, "PositiveOrZero");
    static final DotName BV_SIZE = createConstraintName(BV_CONTRAINTS, "Size");

    // Jackson Constraints
    static final DotName JACKSON_JSONPROPERTY = createConstraintName(JacksonConstants.JSON_PROPERTY);

    static DotName createConstraintName(DotName packageName, String className) {
        return createConstraintName(createComponentized(packageName, className));
    }

    static DotName createConstraintName(DotName constraintName) {
        CONSTRAINTS.add(constraintName);
        return constraintName;
    }

    /**
     * Scan the annotation target to determine whether any annotations
     * from the Bean Validation package (<code>javax.validation.constraints</code>) are
     * present.
     * 
     * @param target the annotation target to scan
     * @return true if annotations from the Bean Validation package are present, otherwise false.
     */
    public static boolean hasConstraints(AnnotationTarget target) {
        return TypeUtil.getAnnotations(target)
                .stream()
                .map(AnnotationInstance::name)
                .anyMatch(CONSTRAINTS::contains);
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
    public static void applyConstraints(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {

        SchemaType schemaType = schema.getType();

        /*
         * The type be set. Attributes set in this function are not application
         * to $ref type schemas.
         */
        if (schemaType == null || schema.getRef() != null) {
            return;
        }

        switch (schemaType) {
            case ARRAY:
                applyArrayConstraints(target, schema, propertyKey, handler);
                break;
            case BOOLEAN:
                INSTANCE.notNull(target, schema, propertyKey, handler);
                INSTANCE.requiredJackson(target, propertyKey, handler);
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
        }
    }

    private static void applyStringConstraints(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {
        INSTANCE.decimalMax(target, schema);
        INSTANCE.decimalMin(target, schema);
        INSTANCE.digits(target, schema);
        INSTANCE.notBlank(target, schema);
        INSTANCE.notNull(target, schema, propertyKey, handler);
        INSTANCE.requiredJackson(target, propertyKey, handler);
        INSTANCE.sizeString(target, schema);
        INSTANCE.notEmptyString(target, schema);
    }

    private static void applyObjectConstraints(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {
        INSTANCE.notNull(target, schema, propertyKey, handler);
        INSTANCE.requiredJackson(target, propertyKey, handler);
        INSTANCE.sizeObject(target, schema);
        INSTANCE.notEmptyObject(target, schema);
    }

    private static void applyArrayConstraints(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {
        INSTANCE.notNull(target, schema, propertyKey, handler);
        INSTANCE.requiredJackson(target, propertyKey, handler);
        INSTANCE.sizeArray(target, schema);
        INSTANCE.notEmptyArray(target, schema);
    }

    private static void applyNumberConstraints(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {
        INSTANCE.decimalMax(target, schema);
        INSTANCE.decimalMin(target, schema);
        INSTANCE.digits(target, schema);
        INSTANCE.max(target, schema);
        INSTANCE.min(target, schema);
        INSTANCE.negative(target, schema);
        INSTANCE.negativeOrZero(target, schema);
        INSTANCE.notNull(target, schema, propertyKey, handler);
        INSTANCE.requiredJackson(target, propertyKey, handler);
        INSTANCE.positive(target, schema);
        INSTANCE.positiveOrZero(target, schema);
    }

    void decimalMax(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_DECIMAL_MAX);

        if (constraint != null && schema.getMaximum() == null) {
            String decimalValue = stringValue(constraint, VALUE);
            try {
                BigDecimal decimal = new BigDecimal(decimalValue);
                schema.setMaximum(decimal);

                Optional<Boolean> inclusive = booleanValue(constraint, INCLUSIVE);

                if (schema.getExclusiveMaximum() == null && inclusive.isPresent() && !inclusive.get().booleanValue()) {
                    schema.setExclusiveMaximum(Boolean.TRUE);
                }
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                DataObjectLogging.logger.invalidAnnotationFormat(decimalValue);
            }
        }
    }

    void decimalMin(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_DECIMAL_MIN);

        if (constraint != null && schema.getMinimum() == null) {
            String decimalValue = stringValue(constraint, VALUE);
            try {
                BigDecimal decimal = new BigDecimal(decimalValue);
                schema.setMinimum(decimal);
                Optional<Boolean> inclusive = booleanValue(constraint, INCLUSIVE);

                if (schema.getExclusiveMinimum() == null && inclusive.isPresent() && !inclusive.get().booleanValue()) {
                    schema.setExclusiveMinimum(Boolean.TRUE);
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
            final int integerPart = intValue(constraint, "integer");
            final int fractionPart = intValue(constraint, "fraction");
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

        if (constraint != null && schema.getMaximum() == null) {
            Boolean exclusive = schema.getExclusiveMaximum();

            if (exclusive != null && exclusive) {
                schema.setMaximum(BigDecimal.ZERO);
            } else {
                schema.setMaximum(NEGATIVE_ONE);
            }
        }
    }

    void negativeOrZero(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_NEGATIVE_OR_ZERO);

        if (constraint != null && schema.getMaximum() == null) {
            Boolean exclusive = schema.getExclusiveMaximum();

            if (exclusive != null && exclusive) {
                schema.setMaximum(BigDecimal.ONE);
            } else {
                schema.setMaximum(BigDecimal.ZERO);
            }
        }
    }

    void notBlank(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_NOT_BLANK);

        if (constraint != null) {
            if (schema.getNullable() == null) {
                schema.setNullable(Boolean.FALSE);
            }
            if (schema.getPattern() == null) {
                schema.setPattern("\\S");
            }
        }
    }

    void notEmptyArray(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_NOT_EMPTY);

        if (constraint != null && schema.getMinItems() == null) {
            schema.setMinItems(1);
        }
    }

    void notEmptyObject(AnnotationTarget target, Schema schema) {
        if (!allowsAdditionalProperties(schema)) {
            return;
        }

        AnnotationInstance constraint = getConstraint(target, BV_NOT_EMPTY);

        if (constraint != null && schema.getMinProperties() == null) {
            schema.setMinProperties(1);
        }
    }

    void notEmptyString(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_NOT_EMPTY);

        if (constraint != null) {
            if (schema.getNullable() == null) {
                schema.setNullable(Boolean.FALSE);
            }

            if (schema.getMinLength() == null) {
                schema.setMinLength(1);
            }
        }
    }

    void notNull(AnnotationTarget target, Schema schema, String propertyKey, RequirementHandler handler) {
        AnnotationInstance constraint = getConstraint(target, BV_NOT_NULL);

        if (constraint != null) {
            if (schema.getNullable() == null) {
                schema.setNullable(Boolean.FALSE);
            }

            handler.setRequired(target, propertyKey);
        }
    }

    void positive(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_POSITIVE);

        if (constraint != null && schema.getMinimum() == null) {
            Boolean exclusive = schema.getExclusiveMinimum();

            if (exclusive != null && exclusive) {
                schema.setMinimum(BigDecimal.ZERO);
            } else {
                schema.setMinimum(BigDecimal.ONE);
            }
        }
    }

    void positiveOrZero(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_POSITIVE_OR_ZERO);

        if (constraint != null && schema.getMinimum() == null) {
            Boolean exclusive = schema.getExclusiveMinimum();

            if (exclusive != null && exclusive) {
                schema.setMinimum(NEGATIVE_ONE);
            } else {
                schema.setMinimum(BigDecimal.ZERO);
            }
        }
    }

    void requiredJackson(AnnotationTarget target, String propertyKey, RequirementHandler handler) {
        Boolean required = TypeUtil.getAnnotationValue(target, JACKSON_JSONPROPERTY, "required");

        if (Boolean.TRUE.equals(required)) {
            handler.setRequired(target, propertyKey);
        }
    }

    void sizeArray(AnnotationTarget target, Schema schema) {
        AnnotationInstance constraint = getConstraint(target, BV_SIZE);

        if (constraint != null) {
            Integer min = intValue(constraint, "min");
            Integer max = intValue(constraint, "max");

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
            Integer min = intValue(constraint, "min");
            Integer max = intValue(constraint, "max");

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
            Integer min = intValue(constraint, "min");
            Integer max = intValue(constraint, "max");

            if (min != null && schema.getMinLength() == null) {
                schema.setMinLength(min);
            }

            if (max != null && schema.getMaxLength() == null) {
                schema.setMaxLength(max);
            }
        }
    }

    boolean allowsAdditionalProperties(Schema schema) {
        Boolean additionalProperties = schema.getAdditionalPropertiesBoolean();

        if (additionalProperties != null) {
            return additionalProperties;
        }

        return schema.getAdditionalPropertiesSchema() != null;
    }

    /**
     * Retrieves a constraint {@link AnnotationInstance} from the current
     * target. If the annotation is found and applies to multiple bean
     * validation groups or to a single group other than the {@link Default},
     * returns null.
     *
     * @param target
     *        the object from which to retrieve the constraint annotation
     * @param annotationName
     *        name of the annotation
     * @return the first occurrence of the named constraint if no groups or only
     *         the {@link Default} group is specified, or null
     */
    AnnotationInstance getConstraint(AnnotationTarget target, DotName annotationName) {
        AnnotationInstance constraint = getAnnotation(target, annotationName);

        if (constraint != null) {
            AnnotationValue groupValue = constraint.value("groups");

            if (groupValue == null) {
                return constraint;
            }

            Type[] groups = groupValue.asClassArray();

            switch (groups.length) {
                case 0:
                    return constraint;
                case 1:
                    if (groups[0].name().equals(BV_DEFAULT_GROUP)) {
                        return constraint;
                    }
                    break;
                default:
                    break;
            }
        }

        return null;
    }

    private static final String VALUE = "value";
    private static final String INCLUSIVE = "inclusive";
}
