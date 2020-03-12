package io.smallrye.openapi.api.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.servers.Server;

import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link PathItem} OpenAPI model interface.
 */
public class PathItemImpl extends ExtensibleImpl<PathItem> implements PathItem, ModelImpl {

    private String $ref;
    private String summary;
    private String description;
    private Operation get;
    private Operation put;
    private Operation post;
    private Operation delete;
    private Operation options;
    private Operation head;
    private Operation patch;
    private Operation trace;
    private List<Parameter> parameters;
    private List<Server> servers;

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#getRef()
     */
    @Override
    public String getRef() {
        return $ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(java.lang.String)
     */
    @Override
    public void setRef(String ref) {
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getSummary()
     */
    @Override
    public String getSummary() {
        return this.summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setSummary(java.lang.String)
     */
    @Override
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#summary(java.lang.String)
     */
    @Override
    public PathItem summary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getGET()
     */
    @Override
    public Operation getGET() {
        return this.get;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setGET(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setGET(Operation get) {
        this.get = get;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getPUT()
     */
    @Override
    public Operation getPUT() {
        return this.put;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setPUT(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setPUT(Operation put) {
        this.put = put;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getPOST()
     */
    @Override
    public Operation getPOST() {
        return this.post;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setPOST(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setPOST(Operation post) {
        this.post = post;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getDELETE()
     */
    @Override
    public Operation getDELETE() {
        return this.delete;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setDELETE(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setDELETE(Operation delete) {
        this.delete = delete;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getOPTIONS()
     */
    @Override
    public Operation getOPTIONS() {
        return this.options;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setOPTIONS(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setOPTIONS(Operation options) {
        this.options = options;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getHEAD()
     */
    @Override
    public Operation getHEAD() {
        return this.head;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setHEAD(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setHEAD(Operation head) {
        this.head = head;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getPATCH()
     */
    @Override
    public Operation getPATCH() {
        return this.patch;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setPATCH(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setPATCH(Operation patch) {
        this.patch = patch;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getTRACE()
     */
    @Override
    public Operation getTRACE() {
        return this.trace;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setTRACE(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setTRACE(Operation trace) {
        this.trace = trace;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getOperations()
     */
    @Override
    public Map<HttpMethod, Operation> getOperations() {
        Map<HttpMethod, Operation> ops = new LinkedHashMap<>();
        addOperationToMap(HttpMethod.GET, this.get, ops);
        addOperationToMap(HttpMethod.PUT, this.put, ops);
        addOperationToMap(HttpMethod.POST, this.post, ops);
        addOperationToMap(HttpMethod.DELETE, this.delete, ops);
        addOperationToMap(HttpMethod.OPTIONS, this.options, ops);
        addOperationToMap(HttpMethod.HEAD, this.head, ops);
        addOperationToMap(HttpMethod.PATCH, this.patch, ops);
        addOperationToMap(HttpMethod.TRACE, this.trace, ops);
        return ops;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getServers()
     */
    @Override
    public List<Server> getServers() {
        return ModelUtil.unmodifiableList(this.servers);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setServers(java.util.List)
     */
    @Override
    public void setServers(List<Server> servers) {
        this.servers = ModelUtil.replace(servers, ArrayList<Server>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#addServer(org.eclipse.microprofile.openapi.models.servers.Server)
     */
    @Override
    public PathItem addServer(Server server) {
        this.servers = ModelUtil.add(server, this.servers, ArrayList<Server>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#removeServer(org.eclipse.microprofile.openapi.models.servers.Server)
     */
    @Override
    public void removeServer(Server server) {
        ModelUtil.remove(this.servers, server);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        return ModelUtil.unmodifiableList(this.parameters);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setParameters(java.util.List)
     */
    @Override
    public void setParameters(List<Parameter> parameters) {
        this.parameters = ModelUtil.replace(parameters, ArrayList<Parameter>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#addParameter(org.eclipse.microprofile.openapi.models.parameters.Parameter)
     */
    @Override
    public PathItem addParameter(Parameter parameter) {
        this.parameters = ModelUtil.add(parameter, this.parameters, ArrayList<Parameter>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#removeParameter(org.eclipse.microprofile.openapi.models.parameters.Parameter)
     */
    @Override
    public void removeParameter(Parameter parameter) {
        ModelUtil.remove(this.parameters, parameter);
    }

    /**
     * Adds the given operation to the given map only if the operation is not null.
     * 
     * @param method
     * @param operation
     * @param operationMap
     */
    private void addOperationToMap(HttpMethod method, Operation operation, Map<HttpMethod, Operation> operationMap) {
        if (operation != null) {
            operationMap.put(method, operation);
        }
    }

}