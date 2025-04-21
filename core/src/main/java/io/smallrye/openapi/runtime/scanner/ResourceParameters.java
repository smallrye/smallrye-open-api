package io.smallrye.openapi.runtime.scanner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;

/**
 * Result object returned to the annotation scanner. Parameters are split
 * between those that apply at the PathItem level and those that apply
 * at the Operation level, except for form parameters which only apply
 * to the operation.
 *
 * This object includes the class and method path which may have been modified
 * from the values specified by JAX-RS Path or Spring Mapping annotations to
 * support the linkage of matrix parameters.
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class ResourceParameters {

    private static int position(List<Parameter> preferredOrder, Parameter p) {
        int pos = preferredOrder.indexOf(p);
        return pos >= 0 ? pos : Integer.MAX_VALUE;
    }

    public static Comparator<Parameter> parameterComparator(List<Parameter> preferredOrder) {
        Comparator<Parameter> primaryComparator;

        if (preferredOrder != null) {
            Comparator<Parameter> preferredComparator = (p1, p2) -> Integer.compare(position(preferredOrder, p1),
                    position(preferredOrder, p2));

            primaryComparator = preferredComparator
                    .thenComparing(Parameter::getRef, Comparator.nullsFirst(Comparator.naturalOrder()));
        } else {
            primaryComparator = Comparator
                    .comparing(Parameter::getRef, Comparator.nullsFirst(Comparator.naturalOrder()));
        }

        return primaryComparator
                .thenComparing(Parameter::getIn, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Parameter::getName, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    static final Pattern TEMPLATE_PARAM_PATTERN = Pattern.compile("\\{(\\w[\\w\\.-]*)\\}");

    private List<String> pathItemPaths;
    private List<Parameter> pathItemParameters;

    private List<String> operationPaths;
    private List<Parameter> operationParameters;

    private Content formBodyContent;

    public List<Parameter> getPathItemParameters() {
        return pathItemParameters;
    }

    public List<String> getOperationPaths() {
        return operationPaths;
    }

    public List<String> getFullOperationPaths() {
        return pathItemPaths.stream()
                .flatMap(pathItemPath -> operationPaths.stream().map(operationPath -> pathItemPath + operationPath))
                .collect(Collectors.toList());
    }

    public List<Parameter> getOperationParameters() {
        return operationParameters;
    }

    public void addOperationParameter(Parameter parameter) {
        if (this.operationParameters == null) {
            this.operationParameters = new ArrayList<>();
        }

        this.operationParameters.add(parameter);
    }

    public Content getFormBodyContent() {
        return formBodyContent;
    }

    public Schema getFormBodySchema() {
        if (formBodyContent != null) {
            return formBodyContent.getMediaTypes().values().iterator().next().getSchema();
        }
        return null;
    }

    public List<Parameter> getAllParameters() {
        List<Parameter> all = new ArrayList<>();

        if (pathItemParameters != null) {
            all.addAll(pathItemParameters);
        }

        if (operationParameters != null) {
            all.addAll(operationParameters);
        }

        return all;
    }

    public void setPathItemPaths(List<String> pathItemPaths) {
        this.pathItemPaths = pathItemPaths;
    }

    public void setPathItemParameters(List<Parameter> pathItemParameters) {
        this.pathItemParameters = pathItemParameters;
    }

    public void setOperationPaths(List<String> operationPaths) {
        this.operationPaths = operationPaths;
    }

    public void setOperationParameters(List<Parameter> operationParameters) {
        this.operationParameters = operationParameters;
    }

    public void setFormBodyContent(Content formBodyContent) {
        this.formBodyContent = formBodyContent;
    }

    public void sort(Comparator<Parameter> comparator) {
        if (pathItemParameters != null) {
            pathItemParameters.sort(comparator);
        }
        if (operationParameters != null) {
            operationParameters.sort(comparator);
        }
    }

    public List<String> getPathParameterTemplateNames() {
        return pathItemPaths.stream()
                .flatMap(pathItemPath -> operationPaths.stream()
                        .flatMap(operationPath -> getPathParameterTemplateName(pathItemPath, operationPath).stream()))
                .distinct()
                .collect(Collectors.toList());
    }

    private static List<String> getPathParameterTemplateName(String... paths) {
        List<String> templateNames = new ArrayList<>();

        for (String path : paths) {
            Matcher m = TEMPLATE_PARAM_PATTERN.matcher(path);

            while (m.find()) {
                templateNames.add(m.group(1));
            }
        }

        return templateNames;
    }
}
