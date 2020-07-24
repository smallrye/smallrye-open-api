package io.smallrye.openapi.api.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.servers.Server;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.info.ContactImpl;
import io.smallrye.openapi.api.models.info.LicenseImpl;
import io.smallrye.openapi.api.models.servers.ServerImpl;

/**
 * Used to configure server information and some more from config properties.
 *
 * @author eric.wittmann@gmail.com
 */
public class ConfigUtil {

    private ConfigUtil() {
    }

    public static final void applyConfig(OpenApiConfig config, OpenAPI oai) {
        // From the spec
        configureServers(config, oai);
        // Our own extension
        configureVersion(config, oai);
        configureInfo(config, oai);
    }

    protected static final void configureVersion(OpenApiConfig config, OpenAPI oai) {
        String versionInConfig = config.getOpenApiVersion();
        if (versionInConfig != null && !versionInConfig.isEmpty()) {
            oai.setOpenapi(versionInConfig);
        } else if (oai.getOpenapi() == null || oai.getOpenapi().isEmpty()) {
            oai.setOpenapi(OpenApiConstants.OPEN_API_VERSION);
        }
    }

    protected static final void configureInfo(OpenApiConfig config, OpenAPI oai) {
        if (config.getInfoTitle() != null) {
            oai.getInfo().setTitle(config.getInfoTitle());
        }
        if (config.getInfoVersion() != null) {
            oai.getInfo().setVersion(config.getInfoVersion());
        }
        if (config.getInfoDescription() != null) {
            oai.getInfo().setDescription(config.getInfoDescription());
        }
        if (config.getInfoTermsOfService() != null) {
            oai.getInfo().setTermsOfService(config.getInfoTermsOfService());
        }

        // Contact
        if (oai.getInfo().getContact() == null && (config.getInfoContactEmail() != null || config.getInfoContactName() != null
                || config.getInfoContactUrl() != null)) {
            oai.getInfo().setContact(new ContactImpl());
        }
        if (config.getInfoContactEmail() != null) {
            oai.getInfo().getContact().setEmail(config.getInfoContactEmail());
        }
        if (config.getInfoContactName() != null) {
            oai.getInfo().getContact().setName(config.getInfoContactName());
        }
        if (config.getInfoContactUrl() != null) {
            oai.getInfo().getContact().setUrl(config.getInfoContactUrl());
        }

        // License
        if (oai.getInfo().getLicense() == null && (config.getInfoLicenseName() != null || config.getInfoLicenseUrl() != null)) {
            oai.getInfo().setLicense(new LicenseImpl());
        }
        if (config.getInfoLicenseName() != null) {
            oai.getInfo().getLicense().setName(config.getInfoLicenseName());
        }
        if (config.getInfoLicenseUrl() != null) {
            oai.getInfo().getLicense().setUrl(config.getInfoLicenseUrl());
        }
    }

    protected static final void configureServers(OpenApiConfig config, OpenAPI oai) {
        // Start with the global servers.
        Set<String> servers = config.servers();
        if (servers != null && !servers.isEmpty()) {
            oai.servers(new ArrayList<>());
            for (String server : servers) {
                Server s = new ServerImpl();
                s.setUrl(server);
                oai.addServer(s);
            }
        }

        // Now the PathItem and Operation servers
        Map<String, PathItem> pathItems = oai.getPaths().getPathItems();
        if (pathItems != null) {
            pathItems.entrySet().forEach(entry -> configureServers(config, entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Configures the servers for a PathItem.
     * 
     * @param config OpenApiConfig
     * @param pathName String representing the pathName
     * @param pathItem String representing the pathItem
     */
    protected static void configureServers(OpenApiConfig config, String pathName, PathItem pathItem) {
        if (pathItem == null) {
            return;
        }

        Set<String> pathServers = config.pathServers(pathName);
        if (pathServers != null && !pathServers.isEmpty()) {
            pathItem.servers(new ArrayList<>());
            for (String pathServer : pathServers) {
                Server server = new ServerImpl();
                server.setUrl(pathServer);
                pathItem.addServer(server);
            }
        }

        configureServers(config, pathItem.getGET());
        configureServers(config, pathItem.getPUT());
        configureServers(config, pathItem.getPOST());
        configureServers(config, pathItem.getDELETE());
        configureServers(config, pathItem.getHEAD());
        configureServers(config, pathItem.getOPTIONS());
        configureServers(config, pathItem.getPATCH());
        configureServers(config, pathItem.getTRACE());
    }

    /**
     * Configures the servers for an Operation.
     * 
     * @param config OpenApiConfig
     * @param operation Operation
     */
    protected static void configureServers(OpenApiConfig config, Operation operation) {
        if (operation == null) {
            return;
        }
        if (operation.getOperationId() == null) {
            return;
        }

        Set<String> operationServers = config.operationServers(operation.getOperationId());
        if (operationServers != null && !operationServers.isEmpty()) {
            operation.servers(new ArrayList<>());
            for (String operationServer : operationServers) {
                Server server = new ServerImpl();
                server.setUrl(operationServer);
                operation.addServer(server);
            }
        }
    }

}
