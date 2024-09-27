package io.smallrye.openapi.api.models;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.tags.Tag;

import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link OpenAPI} OpenAPI model interface.
 */
public class OpenAPIImpl extends ExtensibleImpl<OpenAPI> implements OpenAPI, ModelImpl {

    private String openapi;
    private Info info;
    private ExternalDocumentation externalDocs;
    private List<Server> servers;
    private List<SecurityRequirement> security;
    private List<Tag> tags;
    private Paths paths;
    private Components components;

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#getOpenapi()
     */
    @Override
    public String getOpenapi() {
        return this.openapi;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#setOpenapi(java.lang.String)
     */
    @Override
    public void setOpenapi(String openapi) {
        this.openapi = openapi;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#getInfo()
     */
    @Override
    public Info getInfo() {
        return this.info;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#setInfo(org.eclipse.microprofile.openapi.models.info.Info)
     */
    @Override
    public void setInfo(Info info) {
        this.info = info;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#getExternalDocs()
     */
    @Override
    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#setExternalDocs(org.eclipse.microprofile.openapi.models.ExternalDocumentation)
     */
    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#getServers()
     */
    @Override
    public List<Server> getServers() {
        return ModelUtil.unmodifiableList(this.servers);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#setServers(java.util.List)
     */
    @Override
    public void setServers(List<Server> servers) {
        this.servers = ModelUtil.replace(servers);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#addServer(org.eclipse.microprofile.openapi.models.servers.Server)
     */
    @Override
    public OpenAPI addServer(Server server) {
        this.servers = ModelUtil.add(server, this.servers);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#removeServer(org.eclipse.microprofile.openapi.models.servers.Server)
     */
    @Override
    public void removeServer(Server server) {
        ModelUtil.remove(this.servers, server);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#getSecurity()
     */
    @Override
    public List<SecurityRequirement> getSecurity() {
        return ModelUtil.unmodifiableList(this.security);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#setSecurity(java.util.List)
     */
    @Override
    public void setSecurity(List<SecurityRequirement> security) {
        this.security = ModelUtil.replace(security);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#addSecurityRequirement(org.eclipse.microprofile.openapi.models.security.SecurityRequirement)
     */
    @Override
    public OpenAPI addSecurityRequirement(SecurityRequirement securityRequirement) {
        this.security = ModelUtil.add(securityRequirement, this.security);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#removeSecurityRequirement(org.eclipse.microprofile.openapi.models.security.SecurityRequirement)
     */
    @Override
    public void removeSecurityRequirement(SecurityRequirement securityRequirement) {
        ModelUtil.remove(this.security, securityRequirement);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#getTags()
     */
    @Override
    public List<Tag> getTags() {
        return ModelUtil.unmodifiableList(this.tags);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#setTags(java.util.List)
     */
    @Override
    public void setTags(List<Tag> tags) {
        this.tags = ModelUtil.replace(tags);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#addTag(org.eclipse.microprofile.openapi.models.tags.Tag)
     */
    @Override
    public OpenAPI addTag(Tag tag) {
        if (tag == null) {
            return this;
        }
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (!this.hasTag(tag.getName())) {
            this.tags.add(tag);
        }
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#removeTag(org.eclipse.microprofile.openapi.models.tags.Tag)
     */
    @Override
    public void removeTag(Tag tag) {
        ModelUtil.remove(this.tags, tag);
    }

    /**
     * Returns true if the tag already exists in the OpenAPI document.
     *
     * @param name
     */
    private boolean hasTag(String name) {
        if (this.tags == null || name == null) {
            return false;
        }
        return this.tags.stream().anyMatch(tag -> tag.getName().equals(name));
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#getPaths()
     */
    @Override
    public Paths getPaths() {
        return this.paths;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#setPaths(org.eclipse.microprofile.openapi.models.Paths)
     */
    @Override
    public void setPaths(Paths paths) {
        this.paths = paths;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#getComponents()
     */
    @Override
    public Components getComponents() {
        return this.components;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.OpenAPI#setComponents(org.eclipse.microprofile.openapi.models.Components)
     */
    @Override
    public void setComponents(Components components) {
        this.components = components;
    }
}
