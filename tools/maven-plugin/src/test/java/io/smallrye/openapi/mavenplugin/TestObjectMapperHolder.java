package io.smallrye.openapi.mavenplugin;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.eclipse.microprofile.openapi.models.servers.Server;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.api.models.PathsImpl;
import io.smallrye.openapi.api.models.info.ContactImpl;
import io.smallrye.openapi.api.models.info.InfoImpl;
import io.smallrye.openapi.api.models.info.LicenseImpl;
import io.smallrye.openapi.api.models.servers.ServerImpl;

public class TestObjectMapperHolder {
    private static ObjectMapper json;

    private static ObjectMapper yaml;

    public static ObjectMapper json() {
        if (json != null) {
            return json;
        }

        ObjectMapper mapper = new ObjectMapper();

        configure(mapper);

        json = mapper;

        return mapper;
    }

    public static ObjectMapper yaml() {
        if (yaml != null) {
            return yaml;
        }

        YAMLFactory factory = new YAMLFactory();
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        factory.enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS);
        ObjectMapper mapper = new ObjectMapper(factory);

        configure(mapper);

        yaml = mapper;

        return mapper;
    }

    private static void configure(ObjectMapper mapper) {
        {
            SimpleModule module = new SimpleModule("AbstractTypeMapping", Version.unknownVersion());

            SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
            resolver.addMapping(OpenAPI.class, OpenAPIImpl.class);
            resolver.addMapping(Info.class, InfoImpl.class);
            resolver.addMapping(Contact.class, ContactImpl.class);
            resolver.addMapping(License.class, LicenseImpl.class);
            resolver.addMapping(Server.class, ServerImpl.class);
            resolver.addMapping(Paths.class, PathsImpl.class);

            module.setAbstractTypes(resolver);

            mapper.registerModule(module);
        }
    }
}
