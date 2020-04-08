package io.smallrye.openapi.api.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Util that helps with List operations
 * 
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ListUtil {

    private ListUtil() {
    }

    /**
     * Combines the lists passed into a new list, excluding any null lists given.
     * If the resulting list is empty, return null. This method is marked with
     * {@code @SafeVarargs} because the elements of the lists handled generically
     * and the input/output types match.
     * 
     * @param <T> element type of the list
     * @param lists one or more lists to combine
     * @return the combined/merged lists or null if the resulting merged list is empty
     */
    @SafeVarargs
    public static <T> List<T> mergeNullableLists(List<T>... lists) {
        List<T> result = Arrays.stream(lists)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return result.isEmpty() ? null : result;
    }
}
