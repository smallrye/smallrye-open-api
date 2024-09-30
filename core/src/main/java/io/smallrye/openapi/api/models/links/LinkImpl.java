package io.smallrye.openapi.api.models.links;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.servers.Server;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Link} OpenAPI model interface.
 */
public class LinkImpl extends ExtensibleImpl<Link> implements Link, ModelImpl {

    private String ref;
    private String operationRef;
    private String operationId;
    private Map<String, Object> parameters;
    private Object requestBody;
    private String description;
    private Server server;

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
            ref = ReferenceType.LINK.referenceOf(ref);
        }
        this.ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getServer()
     */
    @Override
    public Server getServer() {
        return this.server;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setServer(org.eclipse.microprofile.openapi.models.servers.Server)
     */
    @Override
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getOperationRef()
     */
    @Override
    public String getOperationRef() {
        return this.operationRef;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setOperationRef(java.lang.String)
     */
    @Override
    public void setOperationRef(String operationRef) {
        this.operationRef = operationRef;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getRequestBody()
     */
    @Override
    public Object getRequestBody() {
        return this.requestBody;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setRequestBody(java.lang.Object)
     */
    @Override
    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getOperationId()
     */
    @Override
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setOperationId(java.lang.String)
     */
    @Override
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getParameters()
     */
    @Override
    public Map<String, Object> getParameters() {
        return ModelUtil.unmodifiableMap(this.parameters);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setParameters(java.util.Map)
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = ModelUtil.replace(parameters);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#addParameter(java.lang.String, java.lang.Object)
     */
    @Override
    public Link addParameter(String name, Object parameter) {
        this.parameters = ModelUtil.add(name, parameter, this.parameters);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#removeParameter(java.lang.String)
     */
    @Override
    public void removeParameter(String name) {
        ModelUtil.remove(this.parameters, name);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

}
