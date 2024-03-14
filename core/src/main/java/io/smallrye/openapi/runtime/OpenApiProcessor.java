package io.smallrye.openapi.runtime;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.api.util.ClassLoaderUtil;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScanner;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerFactory;

/**
 * Provides some core archive processing functionality.
 *
 * @author eric.wittmann@gmail.com
 */
public class OpenApiProcessor {

    static final IndexView EMPTY_INDEX = new Indexer().complete();

    private OpenApiProcessor() {
    }

    public static OpenAPI bootstrap(IndexView index) {
        Config config = ConfigProvider.getConfig();
        OpenApiConfig openApiConfig = OpenApiConfig.fromConfig(config);
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
            OpenApiDocument.INSTANCE.modelFromReader(modelFromReader(config, classLoader, index));
            OpenApiDocument.INSTANCE.filter(getFilter(config, classLoader, index));
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
     * @param config configuration used while reading the static file
     * @param staticFile OpenApiStaticFile to be parsed
     * @return OpenApiImpl
     */
    public static OpenAPI modelFromStaticFile(OpenApiConfig config, OpenApiStaticFile staticFile) {
        if (staticFile == null) {
            return null;
        }
        try {
            return OpenApiParser.parse(staticFile.getContent(), staticFile.getFormat(), config);
        } catch (Exception e) {
            throw new OpenApiRuntimeException(e);
        }
    }

    /**
     * Parse the static file content and return the resulting model. Note that this
     * method does NOT close the resources in the static file. The caller is
     * responsible for that.
     *
     * @param staticFile OpenApiStaticFile to be parsed
     * @return OpenAPI model from the file
     */
    public static OpenAPI modelFromStaticFile(OpenApiStaticFile staticFile) {
        return modelFromStaticFile(null, staticFile);
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
     * Create an {@link OpenAPI} model by scanning the deployment for relevant
     * JAX-RS and OpenAPI annotations. If scanning is disabled, this method
     * returns null. If scanning is enabled but no relevant annotations are
     * found, an empty OpenAPI model is returned.
     *
     * @param config
     *        OpenApiConfig
     * @param loader
     *        ClassLoader to discover AnnotationScanner services (via
     *        ServiceLoader) as well as loading application classes
     * @param index
     *        IndexView of Archive
     * @return OpenAPIImpl generated from annotations
     */
    public static OpenAPI modelFromAnnotations(OpenApiConfig config, ClassLoader loader, IndexView index) {
        return modelFromAnnotations(config, loader, index, new AnnotationScannerFactory(loader));
    }

    /**
     * Create an {@link OpenAPI} model by scanning the deployment for relevant
     * JAX-RS and OpenAPI annotations. If scanning is disabled, this method
     * returns null. If scanning is enabled but no relevant annotations are
     * found, an empty OpenAPI model is returned.
     *
     * @param config
     *        OpenApiConfig
     * @param loader
     *        ClassLoader to load application classes
     * @param index
     *        IndexView of Archive
     * @param scannerSupplier
     *        supplier of AnnotationScanner instances to use to generate the
     *        OpenAPI model for the application
     * @return OpenAPI generated from annotations
     */
    public static OpenAPI modelFromAnnotations(OpenApiConfig config, ClassLoader loader, IndexView index,
            Supplier<Iterable<AnnotationScanner>> scannerSupplier) {
        if (config.scanDisable()) {
            return null;
        }

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, loader, index, scannerSupplier);
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
     *
     * @deprecated use {@linkplain #modelFromReader(OpenApiConfig, ClassLoader, IndexView)} instead
     */
    @Deprecated
    public static OpenAPI modelFromReader(OpenApiConfig config, ClassLoader loader) {
        return modelFromReader(config, loader, EMPTY_INDEX);
    }

    /**
     * Instantiate the configured {@link OASModelReader} and invoke it. If no reader is configured,
     * then return null. If a class is configured but there is an error either instantiating or invoking
     * it, a {@link OpenApiRuntimeException} is thrown.
     *
     * @param config OpenApiConfig
     * @param loader ClassLoader
     * @param index an IndexView to be provided to the filter when accepted via its constructor
     * @return OpenApiImpl created from OASModelReader
     */
    public static OpenAPI modelFromReader(OpenApiConfig config, ClassLoader loader, IndexView index) {
        OASModelReader reader = newInstance(config.modelReader(), loader, index);
        return reader != null ? reader.buildModel() : null;
    }

    /**
     * Instantiate the {@link OASFilter} configured by the app.
     *
     * @param config OpenApiConfig
     * @param loader ClassLoader
     * @return OASFilter instance retrieved from loader
     *
     * @deprecated use {@linkplain #getFilter(OpenApiConfig, ClassLoader, IndexView)} instead
     */
    @Deprecated
    public static OASFilter getFilter(OpenApiConfig config, ClassLoader loader) {
        return getFilter(config, loader, EMPTY_INDEX);
    }

    /**
     * Instantiate the {@link OASFilter} configured by the application.
     *
     * @param config OpenApiConfig
     * @param loader ClassLoader
     * @param index an IndexView to be provided to the filter when accepted via its constructor
     * @return OASFilter instance retrieved from loader
     */
    public static OASFilter getFilter(OpenApiConfig config, ClassLoader loader, IndexView index) {
        return getFilter(config.filter(), loader, index);
    }

    /**
     * Instantiate the {@link OASFilter} from a class name.
     *
     * @param className the filter impl class name
     * @param loader ClassLoader
     * @param index an IndexView to be provided to the filter when accepted via its constructor
     * @return OASFilter instance retrieved from loader
     */
    public static OASFilter getFilter(String className, ClassLoader loader, IndexView index) {
        return newInstance(className, loader, index);
    }

    @SuppressWarnings("unchecked")
    static <T> T newInstance(String className, ClassLoader loader, IndexView index) {
        if (className == null) {
            return null;
        }

        Class<T> klazz = uncheckedCall(() -> (Class<T>) loader.loadClass(className));

        return Arrays.stream(klazz.getDeclaredConstructors())
                .filter(OpenApiProcessor::acceptsIndexView)
                .findFirst()
                .map(ctor -> uncheckedCall(() -> (T) ctor.newInstance(index)))
                .orElseGet(() -> uncheckedCall(() -> klazz.getDeclaredConstructor().newInstance()));
    }

    private static boolean acceptsIndexView(Constructor<?> ctor) {
        return ctor.getParameterCount() == 1 && IndexView.class.isAssignableFrom(ctor.getParameterTypes()[0]);
    }

    private static <T> T uncheckedCall(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
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
