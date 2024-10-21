package io.smallrye.openapi.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.servers.Server;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.SmallRyeOASConfig;

/**
 * Used to configure server information and some more from config properties.
 *
 * @author eric.wittmann@gmail.com
 */
public class ConfigUtil {

    private ConfigUtil() {
    }

    public static final void applyConfig(OpenApiConfig config, OpenAPI oai, boolean defaultRequiredProperties) {
        // From the spec
        configureServers(config, oai);
        // Our own extension
        configureVersion(config, oai, defaultRequiredProperties);
        configureInfo(config, oai);
    }

    protected static final void configureVersion(OpenApiConfig config, OpenAPI oai, boolean defaultRequiredProperties) {
        String versionInConfig = config.getOpenApiVersion();
        if (versionInConfig != null && !versionInConfig.isEmpty()) {
            oai.setOpenapi(versionInConfig);
        } else if (defaultRequiredProperties && (oai.getOpenapi() == null || oai.getOpenapi().isEmpty())) {
            oai.setOpenapi(SmallRyeOASConfig.Defaults.VERSION);
        }
    }

    protected static final void configureInfo(OpenApiConfig config, OpenAPI oai) {
        if (!defaultIfNecessary(oai.getInfo(), OASFactory::createInfo, oai::setInfo,
                config.getInfoTitle(),
                config.getInfoVersion(),
                config.getInfoDescription(),
                config.getInfoTermsOfService(),
                config.getInfoContactName(),
                config.getInfoContactEmail(),
                config.getInfoContactUrl(),
                config.getInfoLicenseName(),
                config.getInfoLicenseUrl())) {
            // Nothing to configure
            return;
        }

        setIfPresent(config.getInfoTitle(), oai.getInfo()::setTitle);
        setIfPresent(config.getInfoVersion(), oai.getInfo()::setVersion);
        setIfPresent(config.getInfoDescription(), oai.getInfo()::setDescription);
        setIfPresent(config.getInfoSummary(), oai.getInfo()::setSummary);
        setIfPresent(config.getInfoTermsOfService(), oai.getInfo()::setTermsOfService);

        // Contact
        if (defaultIfNecessary(oai.getInfo().getContact(), OASFactory::createContact, oai.getInfo()::setContact,
                config.getInfoContactName(),
                config.getInfoContactEmail(),
                config.getInfoContactUrl())) {
            setIfPresent(config.getInfoContactName(), oai.getInfo().getContact()::setName);
            setIfPresent(config.getInfoContactEmail(), oai.getInfo().getContact()::setEmail);
            setIfPresent(config.getInfoContactUrl(), oai.getInfo().getContact()::setUrl);
        }

        // License
        if (defaultIfNecessary(oai.getInfo().getLicense(), OASFactory::createLicense, oai.getInfo()::setLicense,
                config.getInfoLicenseName(),
                config.getInfoLicenseUrl())) {
            setIfPresent(config.getInfoLicenseName(), oai.getInfo().getLicense()::setName);
            setIfPresent(config.getInfoLicenseIdentifier(), oai.getInfo().getLicense()::setIdentifier);
            setIfPresent(config.getInfoLicenseUrl(), oai.getInfo().getLicense()::setUrl);
        }
    }

    private static <T> boolean defaultIfNecessary(T value, Supplier<T> factory, Consumer<T> mutator, Object... sources) {
        if (Stream.of(sources).anyMatch(Objects::nonNull)) {
            if (value == null) {
                mutator.accept(factory.get());
            }
            return true;
        }
        return false;
    }

    private static <T> void setIfPresent(T value, Consumer<T> mutator) {
        if (value != null) {
            mutator.accept(value);
        }
    }

    protected static final void configureServers(OpenApiConfig config, OpenAPI oai) {
        // Start with the global servers.
        List<String> servers = config.servers();
        if (servers != null && !servers.isEmpty()) {
            oai.servers(new ArrayList<>());
            for (String server : servers) {
                Server s = OASFactory.createServer();
                s.setUrl(server);
                oai.addServer(s);
            }
        }

        // Now the PathItem and Operation servers
        Optional.ofNullable(oai.getPaths())
                .map(Paths::getPathItems)
                .ifPresent(pathItems -> pathItems.forEach((key, value) -> configureServers(config, key, value)));
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

        List<String> pathServers = config.pathServers(pathName);
        if (pathServers != null && !pathServers.isEmpty()) {
            pathItem.servers(new ArrayList<>());
            for (String pathServer : pathServers) {
                Server server = OASFactory.createServer();
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

        List<String> operationServers = config.operationServers(operation.getOperationId());
        if (operationServers != null && !operationServers.isEmpty()) {
            operation.servers(new ArrayList<>());
            for (String operationServer : operationServers) {
                Server server = OASFactory.createServer();
                server.setUrl(operationServer);
                operation.addServer(server);
            }
        }
    }

}
