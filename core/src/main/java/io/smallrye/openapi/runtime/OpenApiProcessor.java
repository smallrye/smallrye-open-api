package io.smallrye.openapi.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.IndexView;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.api.util.ClassLoaderUtil;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;

/**
 * Provides some core archive processing functionality.
 * 
 * @author eric.wittmann@gmail.com
 */
public class OpenApiProcessor {

    private OpenApiProcessor() {
    }

    public static OpenAPI bootstrap(IndexView index) {
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfigImpl.fromConfig(config);
        return bootstrap(openApiConfig, index);
    }

    public static OpenAPI bootstrap(OpenApiConfig config, IndexView index) {
        ClassLoader defaultClassLoader = ClassLoaderUtil.getDefaultClassLoader();
        return bootstrap(config, index, defaultClassLoader);
    }

    public static OpenAPI bootstrap(OpenApiConfig config, IndexView index, OpenApiStaticFile... staticFiles) {
        ClassLoader defaultClassLoader = ClassLoaderUtil.getDefaultClassLoader();
        return bootstrap(config, index, defaultClassLoader, staticFiles);
    }

    public static OpenAPI bootstrap(OpenApiConfig config, IndexView index, ClassLoader classLoader) {
        List<OpenApiStaticFile> staticfiles = loadOpenApiStaticFiles(classLoader);
        return bootstrap(config, index, classLoader, staticfiles.toArray(new OpenApiStaticFile[] {}));
    }

    public static OpenAPI bootstrap(OpenApiConfig config, IndexView index, ClassLoader classLoader,
            OpenApiStaticFile... staticFiles) {
        OpenApiDocument.INSTANCE.reset();

        // Set the config
        if (config != null) {
            OpenApiDocument.INSTANCE.config(config);
        }
        // Load all static files
        if (staticFiles != null && staticFiles.length > 0) {
            for (OpenApiStaticFile staticFile : staticFiles) {
                OpenApiDocument.INSTANCE.modelFromStaticFile(modelFromStaticFile(config, staticFile));
            }
        }
        // Scan annotations
        if (config != null && index != null) {
            OpenApiDocument.INSTANCE.modelFromAnnotations(modelFromAnnotations(config, classLoader, index));
        }
        // Filter and model
        if (config != null && classLoader != null) {
            OpenApiDocument.INSTANCE.modelFromReader(modelFromReader(config, classLoader));
            OpenApiDocument.INSTANCE.filter(getFilter(config, classLoader));
        }

        OpenApiDocument.INSTANCE.initialize();

        OpenAPI openAPI = OpenApiDocument.INSTANCE.get();

        OpenApiDocument.INSTANCE.reset();

        return openAPI;
    }

    /**
     * Parse the static file content and return the resulting model. Note that this
     * method does NOT close the resources in the static file. The caller is
     * responsible for that.
     * 
     * @param staticFile OpenApiStaticFile to be parsed
     * @return OpenApiImpl
     */
    public static OpenAPI modelFromStaticFile(OpenApiConfig config, OpenApiStaticFile staticFile) {
        if (staticFile == null) {
            return null;
        }
        try {
            return OpenApiParser.parse(staticFile.getContent(), staticFile.getFormat(), config.getMaximumStaticFileSize());
        } catch (IOException e) {
            throw new OpenApiRuntimeException(e);
        }
    }

    /**
     * Create an {@link OpenAPI} model by scanning the deployment for relevant JAX-RS and
     * OpenAPI annotations. If scanning is disabled, this method returns null. If scanning
     * is enabled but no relevant annotations are found, an empty OpenAPI model is returned.
     * 
     * @param config OpenApiConfig
     * @param index IndexView of Archive
     * @return OpenAPIImpl generated from annotations
     */
    public static OpenAPI modelFromAnnotations(OpenApiConfig config, IndexView index) {
        return modelFromAnnotations(config, ClassLoaderUtil.getDefaultClassLoader(), index);
    }

    /**
     * Create an {@link OpenAPI} model by scanning the deployment for relevant JAX-RS and
     * OpenAPI annotations. If scanning is disabled, this method returns null. If scanning
     * is enabled but no relevant annotations are found, an empty OpenAPI model is returned.
     * 
     * @param config OpenApiConfig
     * @param loader ClassLoader
     * @param index IndexView of Archive
     * @return OpenAPIImpl generated from annotations
     */
    public static OpenAPI modelFromAnnotations(OpenApiConfig config, ClassLoader loader, IndexView index) {
        if (config.scanDisable()) {
            return null;
        }

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, loader, index);
        return scanner.scan();
    }

    /**
     * Instantiate the configured {@link OASModelReader} and invoke it. If no reader is configured,
     * then return null. If a class is configured but there is an error either instantiating or invoking
     * it, a {@link OpenApiRuntimeException} is thrown.
     * 
     * @param config OpenApiConfig
     * @param loader ClassLoader
     * @return OpenApiImpl created from OASModelReader
     */
    public static OpenAPI modelFromReader(OpenApiConfig config, ClassLoader loader) {
        String readerClassName = config.modelReader();
        if (readerClassName == null) {
            return null;
        }
        try {
            Class<?> c = loader.loadClass(readerClassName);
            OASModelReader reader = (OASModelReader) c.getDeclaredConstructor().newInstance();
            return reader.buildModel();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new OpenApiRuntimeException(e);
        }
    }

    /**
     * Instantiate the {@link OASFilter} configured by the app.
     * 
     * @param config OpenApiConfig
     * @param loader ClassLoader
     * @return OASFilter instance retrieved from loader
     */
    public static OASFilter getFilter(OpenApiConfig config, ClassLoader loader) {
        String filterClassName = config.filter();
        if (filterClassName == null) {
            return null;
        }
        try {
            Class<?> c = loader.loadClass(filterClassName);
            return (OASFilter) c.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new OpenApiRuntimeException(e);
        }
    }

    private static List<OpenApiStaticFile> loadOpenApiStaticFiles(ClassLoader classLoader) {
        List<OpenApiStaticFile> apiStaticFiles = new ArrayList<>();

        loadOpenApiStaticFile(apiStaticFiles, classLoader, "/META-INF/openapi.yaml", Format.YAML);
        loadOpenApiStaticFile(apiStaticFiles, classLoader, "/WEB-INF/classes/META-INF/openapi.yaml", Format.YAML);
        loadOpenApiStaticFile(apiStaticFiles, classLoader, "/META-INF/openapi.yml", Format.YAML);
        loadOpenApiStaticFile(apiStaticFiles, classLoader, "/WEB-INF/classes/META-INF/openapi.yml", Format.YAML);
        loadOpenApiStaticFile(apiStaticFiles, classLoader, "/META-INF/openapi.json", Format.JSON);
        loadOpenApiStaticFile(apiStaticFiles, classLoader, "/WEB-INF/classes/META-INF/openapi.json", Format.JSON);

        return apiStaticFiles;
    }

    private static List<OpenApiStaticFile> loadOpenApiStaticFile(List<OpenApiStaticFile> apiStaticFiles,
            ClassLoader classLoader, String path, Format format) {
        InputStream staticStream = classLoader.getResourceAsStream(path);
        if (staticStream != null) {
            apiStaticFiles.add(new OpenApiStaticFile(staticStream, format));
        }
        return apiStaticFiles;
    }
}
