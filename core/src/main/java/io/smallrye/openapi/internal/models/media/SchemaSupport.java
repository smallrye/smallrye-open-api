package io.smallrye.openapi.internal.models.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;

import io.smallrye.openapi.model.BaseModel;
import io.smallrye.openapi.model.Extensions;

public final class SchemaSupport {

    static final List<SchemaType> NULL_SINGLETON = Collections.singletonList(SchemaType.NULL);

    public static Schema nullSchema() {
        return OASFactory.createSchema().type(NULL_SINGLETON);
    }

    private SchemaSupport() {
        // No instances
    }

    public static int getModCount(Schema schema) {
        if (schema instanceof BaseModel) {
            return ((BaseModel<?>) schema).getModCount();
        }
        return 0;
    }

    /**
     * Implements the old logic of getNullable().
     * <p>
     * {@link #setType(Schema, SchemaType)}, {@link #setNullable(Schema, Boolean)} and {@link #getNullable(Schema)} can
     * be used together allow the type and nullability of a schema to be set separately by different parts of the
     * scanning process, even though that information is now contained in one field.
     */
    public static Boolean getNullable(Schema schema) {
        List<SchemaType> types = getTypeList(schema);
        Boolean nullable = Extensions.getPrivateExtension(schema, "nullable", Boolean.class);

        if (types != null) {
            boolean nullPermitted = types.contains(SchemaType.NULL);

            // Retain old tri-state behaviour of getNullable
            // If setNullable has not been called and null is not permitted, return null rather than false
            if (!nullPermitted && nullable == null) {
                return null; // NOSONAR
            } else {
                return nullPermitted;
            }
        } else {
            // If types is unset, return any value passed to setNullable
            return nullable;
        }
    }

    /**
     * Implements the old logic of setNullable(Boolean).
     * <p>
     * {@link #setType(Schema, SchemaType)}, {@link #setNullable(Schema, Boolean)} and {@link #getNullable(Schema)} can
     * be used together allow the type and nullability of a schema to be set separately by different parts of the
     * scanning process, even though that information is now contained in one field.
     */
    public static void setNullable(Schema schema, Boolean nullable) {
        Extensions.setPrivateExtension(schema, "nullable", nullable);

        if (nullable == Boolean.TRUE) {
            List<SchemaType> types = getTypeList(schema);

            if (types != null && !types.contains(SchemaType.NULL)) {
                schema.addType(SchemaType.NULL);
            }
        } else {
            schema.removeType(SchemaType.NULL);
        }
    }

    /**
     * Sets the array of types to a single value. Implements the old logic of setType(SchemaType).
     * <p>
     * {@link #setType(Schema, SchemaType)}, {@link #setNullable(Schema, Boolean)} and {@link #getNullable(Schema)} can
     * be used together allow the type and nullability of a schema to be set separately by different parts of the
     * scanning process, even though that information is now contained in one field.
     */
    public static void setType(Schema schema, SchemaType singletonType) {
        if (singletonType == null) {
            setTypeList(schema, null);
        } else if (getNullable(schema) == Boolean.TRUE) {
            setTypeList(schema, List.of(singletonType, SchemaType.NULL));
        } else {
            setTypeList(schema, Collections.singletonList(singletonType));
        }

        notifyTypeObservers(schema, o -> setType(o, singletonType));
    }

    /**
     * Returns the first {@link SchemaType} that is not {@link SchemaType#NULL}
     * or {@code null} if no types are set, or only {@link SchemaType#NULL} is set.
     */
    public static SchemaType getNonNullType(Schema schema) {
        List<SchemaType> types = getTypeList(schema);

        if (types == null || types.isEmpty()) {
            return null;
        }

        for (SchemaType type : types) {
            if (!type.equals(SchemaType.NULL)) {
                return type;
            }
        }

        return null;
    }

    public static void addTypeObserver(Schema observed, Schema observer) {
        List<Schema> typeObservers = Extensions.getTypeObservers(observed);
        if (typeObservers == null) {
            typeObservers = new ArrayList<>(2);
            Extensions.setTypeObservers(observed, typeObservers);
        }
        typeObservers.add(observer);
        setTypesRetainingNull(observer, getTypeList(observed));
    }

    static void notifyTypeObservers(Schema observed, Consumer<Schema> observerAction) {
        List<Schema> typeObservers = Extensions.getTypeObservers(observed);

        if (typeObservers != null) {
            typeObservers.forEach(observerAction);
        }
    }

    static void setTypesRetainingNull(Schema target, List<SchemaType> newTypes) {
        // Set types on the observer, but retain null if it was set on the observer
        List<SchemaType> oldTypes = getTypeList(target);

        if (oldTypes != null && newTypes != null
                && oldTypes.contains(SchemaType.NULL)
                && !newTypes.contains(SchemaType.NULL)) {
            newTypes = new ArrayList<>(newTypes);
            newTypes.add(SchemaType.NULL);
        }

        target.setType(newTypes);
    }

    private static List<SchemaType> getTypeList(Schema schema) {
        if (schema instanceof io.smallrye.openapi.internal.models.media.Schema) {
            return ((io.smallrye.openapi.internal.models.media.Schema) schema).getTypeList();
        }
        return schema.getType();
    }

    private static void setTypeList(Schema schema, List<SchemaType> types) {
        if (schema instanceof io.smallrye.openapi.internal.models.media.Schema) {
            ((io.smallrye.openapi.internal.models.media.Schema) schema).setTypeList(types);
        }
        schema.setType(types);
    }
}
