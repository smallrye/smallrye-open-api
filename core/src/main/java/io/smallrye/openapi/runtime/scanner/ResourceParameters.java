package io.smallrye.openapi.runtime.scanner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;

/**
 * Result object returned to the annotation scanner. Parameters are split
 * between those that apply at the {@link PathItem} level and those that apply
 * at the {@link Operation} level, except for form parameters which only apply
 * to the operation.
 *
 * This object includes the class and method path which may have been modified
 * from the values specified by {@link javax.ws.rs.Path Path} annotations to
 * support the linkage of matrix parameters.
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class ResourceParameters {

    private static final Comparator<Parameter> PARAMETER_COMPARATOR = Comparator.comparing(Parameter::getIn)
            .thenComparing(Parameter::getName);

    private String pathItemPath;
    private List<Parameter> pathItemParameters;

    private String operationPath;
    private List<Parameter> operationParameters;

    private Content formBodyContent;

    public List<Parameter> getPathItemParameters() {
        return pathItemParameters;
    }

    public String getOperationPath() {
        return pathItemPath + operationPath;
    }

    public List<Parameter> getOperationParameters() {
        return operationParameters;
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

    public void setPathItemPath(String pathItemPath) {
        this.pathItemPath = pathItemPath;
    }

    public void setPathItemParameters(List<Parameter> pathItemParameters) {
        this.pathItemParameters = pathItemParameters;
    }

    public void setOperationPath(String operationPath) {
        this.operationPath = operationPath;
    }

    public void setOperationParameters(List<Parameter> operationParameters) {
        this.operationParameters = operationParameters;
    }

    public void setFormBodyContent(Content formBodyContent) {
        this.formBodyContent = formBodyContent;
    }

    public void sort() {
        if (pathItemParameters != null) {
            pathItemParameters.sort(PARAMETER_COMPARATOR);
        }
        if (operationParameters != null) {
            operationParameters.sort(PARAMETER_COMPARATOR);
        }
    }
}
