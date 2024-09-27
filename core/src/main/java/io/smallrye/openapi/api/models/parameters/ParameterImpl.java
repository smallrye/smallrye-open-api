package io.smallrye.openapi.api.models.parameters;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Parameter} OpenAPI model interface.
 */
public class ParameterImpl extends ExtensibleImpl<Parameter> implements Parameter, ModelImpl {

    private String ref;
    private String name;
    private In in;
    private String description;
    private Boolean required;
    private Schema schema;
    private Boolean allowEmptyValue;
    private Boolean deprecated;
    private Style style;
    private Boolean explode;
    private Boolean allowReserved;
    private Object example;
    private Map<String, Example> examples;
    private Content content;

    private String paramRef;

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#getRef()
     */
    @Override
    public String getRef() {
        return this.ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(java.lang.String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = ReferenceType.PARAMETER.referenceOf(ref);
        }
        this.ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getRequired()
     */
    @Override
    public Boolean getRequired() {
        return this.required;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setRequired(java.lang.Boolean)
     */
    @Override
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setDeprecated(java.lang.Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getAllowEmptyValue()
     */
    @Override
    public Boolean getAllowEmptyValue() {
        return this.allowEmptyValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setAllowEmptyValue(java.lang.Boolean)
     */
    @Override
    public void setAllowEmptyValue(Boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getStyle()
     */
    @Override
    public Style getStyle() {
        return this.style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setStyle(Style)
     */
    @Override
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getExplode()
     */
    @Override
    public Boolean getExplode() {
        return this.explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setExplode(java.lang.Boolean)
     */
    @Override
    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getAllowReserved()
     */
    @Override
    public Boolean getAllowReserved() {
        return this.allowReserved;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setAllowReserved(java.lang.Boolean)
     */
    @Override
    public void setAllowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getSchema()
     */
    @Override
    public Schema getSchema() {
        return this.schema;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setSchema(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getExamples()
     */
    @Override
    public Map<String, Example> getExamples() {
        return ModelUtil.unmodifiableMap(this.examples);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setExamples(java.util.Map)
     */
    @Override
    public void setExamples(Map<String, Example> examples) {
        this.examples = ModelUtil.replace(examples);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#addExample(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.examples.Example)
     */
    @Override
    public Parameter addExample(String key, Example example) {
        this.examples = ModelUtil.add(key, example, this.examples);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#removeExample(java.lang.String)
     */
    @Override
    public void removeExample(String key) {
        ModelUtil.remove(this.examples, key);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getExample()
     */
    @Override
    public Object getExample() {
        return this.example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setExample(java.lang.Object)
     */
    @Override
    public void setExample(Object example) {
        this.example = example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getContent()
     */
    @Override
    public Content getContent() {
        return this.content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setContent(org.eclipse.microprofile.openapi.models.media.Content)
     */
    @Override
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getIn()
     */
    @Override
    public In getIn() {
        return in;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setIn(org.eclipse.microprofile.openapi.models.parameters.Parameter.In)
     */
    @Override
    public void setIn(In in) {
        if (in == In.PATH) {
            this.required = true;
        }
        this.in = in;
    }

    /**
     * Implementation specific, set a reference to the Java method parameter, so that we can bind back to it later if needed
     *
     * @return reference to the method parameter that we scanned this on
     */
    public String getParamRef() {
        return paramRef;
    }

    public void setParamRef(String paramRef) {
        this.paramRef = paramRef;
    }

    static public String getParamRef(Parameter parameter) {
        return (parameter instanceof ParameterImpl) ? ((ParameterImpl) parameter).getParamRef() : null;
    }

    public static boolean isHidden(Parameter parameter) {
        return parameter != null
                && parameter.getExtensions() != null
                && !parameter.getExtensions().isEmpty()
                && parameter.getExtensions().containsKey(HIDDEN)
                && parameter.getExtensions().get(HIDDEN) != null
                && parameter.getExtensions().get(HIDDEN).equals(true);
    }

    public static void setHidden(Parameter parameter, boolean hidden) {
        if (parameter != null) {
            parameter.addExtension(HIDDEN, hidden);
        }
    }

    public static final String HIDDEN = "smallrye.internal.hidden";
}
