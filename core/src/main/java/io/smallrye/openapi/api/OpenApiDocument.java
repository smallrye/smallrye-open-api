package io.smallrye.openapi.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;

import io.smallrye.openapi.api.util.ConfigUtil;
import io.smallrye.openapi.api.util.FilterUtil;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.api.util.UnusedSchemaFilter;

/**
 * Holds the final OpenAPI document produced during the startup of the app.
 *
 * <p>
 * Note that the model must be initialized first!
 * </p>
 *
 * @author Martin Kouba
 *
 * @deprecated use the {@link io.smallrye.openapi.api.SmallRyeOpenAPI
 *             SmallRyeOpenAPI} builder API instead. This class may be moved,
 *             have reduced visibility, or be removed in a future release.
 */
@Deprecated
public class OpenApiDocument {

    public static final OpenApiDocument INSTANCE = new OpenApiDocument();

    // These are used during init only
    private transient OpenApiConfig config;
    private transient OpenAPI annotationsModel;
    private transient OpenAPI readerModel;
    private transient OpenAPI staticFileModel;
    private transient Map<String, OASFilter> filters = new LinkedHashMap<>();
    private transient boolean defaultRequiredProperties = true;
    private transient String archiveName;
    private transient String version;

    private transient OpenAPI model;

    private OpenApiDocument() {
    }

    public static OpenApiDocument newInstance() {
        return new OpenApiDocument();
    }

    /**
     *
     * @return the final OpenAPI document produced during the startup of the app
     * @throws IllegalStateException If the final model is not initialized yet
     */
    public synchronized OpenAPI get() {
        if (model == null) {
            throw ApiMessages.msg.modelNotInitialized();
        }
        return model;
    }

    /**
     * Set the final OpenAPI document. This method should only be used for testing.
     *
     * @param model OpenAPI model instance
     */
    public synchronized void set(OpenAPI model) {
        this.model = model;
    }

    /**
     * Reset the holder.
     */
    public synchronized void reset() {
        model = null;
        clear();
    }

    /**
     * @return {@code true} if model initialized
     */
    public synchronized boolean isSet() {
        return model != null;
    }

    public synchronized void config(OpenApiConfig config) {
        set(() -> this.config = config);
    }

    public void modelFromAnnotations(OpenAPI model) {
        set(() -> this.annotationsModel = model);
    }

    public void modelFromReader(OpenAPI model) {
        set(() -> this.readerModel = model);
    }

    public void modelFromStaticFile(OpenAPI model) {
        set(() -> this.staticFileModel = model);
    }

    public void filter(OASFilter filter) {
        if (filter != null) {
            set(() -> this.filters.putIfAbsent(filter.getClass().getName(), filter));
        }
    }

    public void defaultRequiredProperties(boolean defaultRequiredProperties) {
        set(() -> this.defaultRequiredProperties = defaultRequiredProperties);
    }

    public void archiveName(String archiveName) {
        set(() -> this.archiveName = archiveName);
    }

    public void version(String version) {
        set(() -> this.version = version);
    }

    public synchronized void initialize() {
        if (model != null) {
            modelAlreadyInitialized();
        }
        // Check all the required parts are set
        if (config == null) {
            throw ApiMessages.msg.configMustBeSet();
        }

        // Phase 1: Use OASModelReader
        OpenAPI merged = readerModel;

        // Phase 2: Merge any static OpenAPI file packaged in the app
        merged = MergeUtil.mergeObjects(merged, staticFileModel);

        // Phase 3: Merge annotations
        merged = MergeUtil.mergeObjects(merged, annotationsModel);

        // Phase 4: Filter model via OASFilter
        merged = filterModel(merged);

        // Phase 5: Default empty document if model == null
        if (merged == null) {
            merged = OASFactory.createOpenAPI();
            merged.setOpenapi(SmallRyeOASConfig.Defaults.VERSION);
        }

        // Phase 6: Provide missing required elements using defaults
        if (defaultRequiredProperties) {
            if (merged.getPaths() == null) {
                merged.setPaths(OASFactory.createPaths());
            }
            if (merged.getInfo() == null) {
                merged.setInfo(OASFactory.createInfo());
            }
            if (merged.getInfo().getTitle() == null) {
                merged.getInfo().setTitle((archiveName == null ? "Generated" : archiveName) + " API");
            }
            if (merged.getInfo().getVersion() == null) {
                merged.getInfo().setVersion((version == null ? "1.0" : version));
            }
        }

        // Phase 7: Use Config values to add Servers (global, pathItem, operation)
        ConfigUtil.applyConfig(config, merged, defaultRequiredProperties);

        model = merged;
        clear();
    }

    /**
     * Filter the final model using a {@link OASFilter} configured by the app. If no filter has been configured, this will
     * simply return the model unchanged.
     *
     * @param model
     */
    private OpenAPI filterModel(OpenAPI model) {
        if (model == null) {
            return model;
        }
        if (config.removeUnusedSchemas()) {
            model = FilterUtil.applyFilter(new UnusedSchemaFilter(), model);
        }
        for (OASFilter filter : filters.values()) {
            model = FilterUtil.applyFilter(filter, model);
        }
        return model;
    }

    private synchronized void set(Runnable action) {
        if (model != null) {
            modelAlreadyInitialized();
        }
        action.run();
    }

    private void modelAlreadyInitialized() {
        throw ApiMessages.msg.modelAlreadyInitialized();
    }

    private void clear() {
        config = null;
        annotationsModel = null;
        readerModel = null;
        staticFileModel = null;
        filters.clear();
        archiveName = null;
        defaultRequiredProperties = true;
    }

}
