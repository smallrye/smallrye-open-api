package io.smallrye.openapi.internal.models;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.Operation;

/**
 * An implementation of the {@link PathItem} OpenAPI model interface.
 */
public class PathItem extends AbstractPathItem {

    private static final Map<String, HttpMethod> OPERATIONS = Arrays.stream(HttpMethod.values())
            .collect(Collectors.toMap(m -> m.name().toLowerCase(), Function.identity()));

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<HttpMethod, Operation> getOperations() {
        Map<HttpMethod, Operation> ops = new LinkedHashMap<>(OPERATIONS.size());

        for (Map.Entry<String, Operation> property : getProperties(Operation.class).entrySet()) {
            String name = property.getKey();

            if (OPERATIONS.containsKey(name)) {
                ops.put(OPERATIONS.get(name), property.getValue());
            }
        }

        return ops;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOperation(HttpMethod httpMethod, Operation operation) {
        setProperty(httpMethod.name().toLowerCase(), operation);
    }

}
