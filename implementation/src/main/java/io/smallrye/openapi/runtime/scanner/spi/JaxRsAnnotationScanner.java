/**
 * Copyright 2020 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.openapi.runtime.scanner.spi;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.ComponentsImpl;
import io.smallrye.openapi.api.models.ExternalDocumentationImpl;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.api.models.PathItemImpl;
import io.smallrye.openapi.api.models.PathsImpl;
import io.smallrye.openapi.api.models.callbacks.CallbackImpl;
import io.smallrye.openapi.api.models.examples.ExampleImpl;
import io.smallrye.openapi.api.models.headers.HeaderImpl;
import io.smallrye.openapi.api.models.info.ContactImpl;
import io.smallrye.openapi.api.models.info.InfoImpl;
import io.smallrye.openapi.api.models.info.LicenseImpl;
import io.smallrye.openapi.api.models.links.LinkImpl;
import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.EncodingImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.parameters.ParameterImpl;
import io.smallrye.openapi.api.models.parameters.RequestBodyImpl;
import io.smallrye.openapi.api.models.responses.APIResponseImpl;
import io.smallrye.openapi.api.models.responses.APIResponsesImpl;
import io.smallrye.openapi.api.models.security.OAuthFlowImpl;
import io.smallrye.openapi.api.models.security.OAuthFlowsImpl;
import io.smallrye.openapi.api.models.security.SecurityRequirementImpl;
import io.smallrye.openapi.api.models.security.SecuritySchemeImpl;
import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.api.models.servers.ServerVariableImpl;
import io.smallrye.openapi.api.models.tags.TagImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.CustomSchemaRegistry;
import io.smallrye.openapi.runtime.scanner.ParameterProcessor;
import io.smallrye.openapi.runtime.scanner.PathMaker;
import io.smallrye.openapi.runtime.scanner.SchemaRegistry;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.ModelUtil;
import io.smallrye.openapi.runtime.util.SchemaFactory;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Scanner that scan Jax-Rs entry points.
 * This is also the default, as it's part of the spec.
 * 
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class JaxRsAnnotationScanner implements AnnotationScanner {
    private static final Logger LOG = Logger.getLogger(JaxRsAnnotationScanner.class);

    private String currentAppPath = "";
    private String[] currentConsumes;
    private String[] currentProduces;

    private String currentSecurityScheme;
    private List<OAuthFlow> currentFlows;
    private String[] resourceRolesAllowed;

    @Override
    public String getName() {
        return "JAX-RS";
    }

    @Override
    public OpenAPI scan(final AnnotationScannerContext context) {

        // Initialize a new OAI document.  Even if nothing is found, this will be returned.
        OpenAPI oai = new OpenAPIImpl();
        oai.setOpenapi(OpenApiConstants.OPEN_API_VERSION);

        // Creating a new instance of a registry which will be set on the thread context.
        SchemaRegistry schemaRegistry = SchemaRegistry.newInstance(context.getConfig(), oai, context.getIndex());

        // Register custom schemas if available
        getCustomSchemaRegistry(context.getConfig()).registerCustomSchemas(schemaRegistry);

        // Find all OpenAPIDefinition annotations at the package level
        processPackageOpenAPIDefinitions(context, oai);

        // Get all jax-rs applications and convert them to OAI models (and merge them into a single one)
        Collection<ClassInfo> applications = context.getIndex()
                .getAllKnownSubclasses(DotName.createSimple(Application.class.getName()));
        for (ClassInfo classInfo : applications) {
            oai = MergeUtil.merge(oai, jaxRsApplicationToOpenApi(context, classInfo));
        }

        boolean tagsDefined = oai.getTags() != null && !oai.getTags().isEmpty();

        // this can be a useful extension point to set/override the application path
        for (AnnotationScannerExtension extension : context.getExtensions()) {
            extension.processJaxRsApplications(this, applications);
        }

        checkSecurityScheme(oai);

        // Now find all jax-rs endpoints
        Collection<ClassInfo> resourceClasses = JandexUtil.getJaxRsResourceClasses(context.getIndex());
        for (ClassInfo resourceClass : resourceClasses) {
            processJaxRsResourceClass(context, oai, resourceClass, null);
        }

        // Sort the tags unless the application has defined the order in OpenAPIDefinition annotation(s)
        if (!tagsDefined && oai.getTags() != null) {
            oai.setTags(oai.getTags()
                    .stream()
                    .sorted(Comparator.comparing(Tag::getName))
                    .collect(Collectors.toList()));
        }

        // Now that all paths have been created, sort them (we don't have a better way to organize them).
        Paths paths = oai.getPaths();
        if (paths != null) {
            Paths sortedPaths = new PathsImpl();
            TreeSet<String> sortedKeys = new TreeSet<>(paths.getPathItems().keySet());
            for (String pathKey : sortedKeys) {
                PathItem pathItem = paths.getPathItem(pathKey);
                sortedPaths.addPathItem(pathKey, pathItem);
            }
            sortedPaths.setExtensions(paths.getExtensions());
            oai.setPaths(sortedPaths);
        }

        return oai;
    }

    /**
     * Scans all <code>@OpenAPIDefinition</code> annotations present on <code>package-info</code>
     * classes known to the scanner's index.
     * 
     * @param oai the current OpenAPI result
     */
    private void processPackageOpenAPIDefinitions(final AnnotationScannerContext context, OpenAPI oai) {
        List<AnnotationInstance> packageDefs = context.getIndex().getAnnotations(OpenApiConstants.DOTNAME_OPEN_API_DEFINITION)
                .stream()
                .filter(annotation -> annotation.target().kind() == AnnotationTarget.Kind.CLASS)
                .filter(annotation -> annotation.target().asClass().name().withoutPackagePrefix().equals("package-info"))
                .collect(Collectors.toList());

        for (AnnotationInstance packageDef : packageDefs) {
            OpenAPIImpl packageOai = new OpenAPIImpl();
            processDefinition(context, packageOai, packageDef);
            oai = MergeUtil.merge(oai, packageOai);
        }
    }

    /**
     * If there is a single security scheme defined by the <code>@OpenAPIDefinition</code>
     * annotations and the scheme is OAuth2 or OpenIdConnect, any of the flows
     * where no scopes have yet been provided are eligible to have scopes
     * filled by <code>@DeclareRoles</code>/<code>@RolesAllowed</code> annotations.
     * 
     * @param oai the current OpenAPI result
     */
    private void checkSecurityScheme(OpenAPI oai) {
        if (oai.getComponents() == null) {
            return;
        }

        Map<String, SecurityScheme> schemes = oai.getComponents().getSecuritySchemes();

        if (schemes != null && schemes.size() == 1) {
            Map.Entry<String, SecurityScheme> scheme = schemes.entrySet().iterator().next();
            SecurityScheme.Type schemeType = scheme.getValue().getType();

            if (schemeType != null) {
                switch (schemeType) {
                    case OAUTH2:
                    case OPENIDCONNECT:
                        saveSecurityScheme(scheme.getKey(), scheme.getValue());
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Saves the name of the SecurityScheme and references to any flows
     * that did not have scopes defined by the application via a component
     * defined in <code>@OpenAPIDefinition</code> annotations. The saved
     * flows may have scopes added by values discovered in <code>@RolesAllowed</code>
     * annotations during scanning.
     * 
     * @param scheme the scheme to save for further role processing.
     */
    private void saveSecurityScheme(String schemeName, SecurityScheme scheme) {
        this.currentSecurityScheme = schemeName;
        this.currentFlows = new ArrayList<>();

        OAuthFlows flows = scheme.getFlows();

        if (flows != null) {
            saveFlow(flows.getAuthorizationCode());
            saveFlow(flows.getClientCredentials());
            saveFlow(flows.getImplicit());
            saveFlow(flows.getPassword());
        }
    }

    /**
     * Saves an {@link OAuthFlow} object in the list of flows for further processing.
     * Only saved if no scopes were defined by the application using annotations.
     * 
     * @param flow
     */
    private void saveFlow(OAuthFlow flow) {
        if (flow != null && flow.getScopes() == null) {
            currentFlows.add(flow);
        }
    }

    /**
     * Processes a JAX-RS {@link Application} and creates an {@link OpenAPI} model. Performs
     * annotation scanning and other processing. Returns a model unique to that single JAX-RS
     * app.
     * 
     * @param applicationClass
     */
    private OpenAPIImpl jaxRsApplicationToOpenApi(final AnnotationScannerContext context, ClassInfo applicationClass) {
        OpenAPIImpl oai = new OpenAPIImpl();
        oai.setOpenapi(OpenApiConstants.OPEN_API_VERSION);

        // Get the @ApplicationPath info and save it for later (also support @Path which seems nonstandard but common).
        ////////////////////////////////////////
        AnnotationInstance appPathAnno = JandexUtil.getClassAnnotation(applicationClass,
                OpenApiConstants.DOTNAME_APPLICATION_PATH);
        if (appPathAnno == null || context.getConfig().applicationPathDisable()) {
            appPathAnno = JandexUtil.getClassAnnotation(applicationClass, OpenApiConstants.DOTNAME_PATH);
        }
        // TODO: Add support for Application selection when there are more than one
        if (appPathAnno != null) {
            this.currentAppPath = appPathAnno.value().asString();
        } else {
            this.currentAppPath = "/";
        }

        // Get the @OpenAPIDefinition annotation and process it.
        ////////////////////////////////////////
        AnnotationInstance openApiDefAnno = JandexUtil.getClassAnnotation(applicationClass,
                OpenApiConstants.DOTNAME_OPEN_API_DEFINITION);
        if (openApiDefAnno != null) {
            processDefinition(context, oai, openApiDefAnno);
        }

        // Process @SecurityScheme annotations
        ////////////////////////////////////////
        List<AnnotationInstance> securitySchemeAnnotations = JandexUtil.getRepeatableAnnotation(applicationClass,
                OpenApiConstants.DOTNAME_SECURITY_SCHEME, OpenApiConstants.DOTNAME_SECURITY_SCHEMES);
        for (AnnotationInstance annotation : securitySchemeAnnotations) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SECURITY_SCHEME_NAME);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                SecurityScheme securityScheme = readSecurityScheme(annotation);
                Components components = ModelUtil.components(oai);
                components.addSecurityScheme(name, securityScheme);
            }
        }

        // Process @Server annotations
        ///////////////////////////////////
        List<AnnotationInstance> serverAnnotations = JandexUtil.getRepeatableAnnotation(applicationClass,
                OpenApiConstants.DOTNAME_SERVER, OpenApiConstants.DOTNAME_SERVERS);
        for (AnnotationInstance annotation : serverAnnotations) {
            Server server = readServer(annotation);
            oai.addServer(server);
        }

        return oai;
    }

    /**
     * Processing a single JAX-RS resource class (annotated with @Path).
     * 
     * @param openApi
     * @param resourceClass
     * @param locatorPathParameters
     */
    private void processJaxRsResourceClass(final AnnotationScannerContext context,
            OpenAPI openApi,
            ClassInfo resourceClass,
            List<Parameter> locatorPathParameters) {
        LOG.debug("Processing a JAX-RS resource class: " + resourceClass.simpleName());

        // Process @SecurityScheme annotations
        ////////////////////////////////////////
        List<AnnotationInstance> securitySchemeAnnotations = JandexUtil.getRepeatableAnnotation(resourceClass,
                OpenApiConstants.DOTNAME_SECURITY_SCHEME, OpenApiConstants.DOTNAME_SECURITY_SCHEMES);
        for (AnnotationInstance annotation : securitySchemeAnnotations) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SECURITY_SCHEME_NAME);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                SecurityScheme securityScheme = readSecurityScheme(annotation);
                Components components = ModelUtil.components(openApi);
                components.addSecurityScheme(name, securityScheme);
            }
        }

        // Process tags (both declarations and references)
        ////////////////////////////////////////
        Set<String> tagRefs = processTags(openApi, resourceClass, false);

        addScopes(TypeUtil.getAnnotationValue(resourceClass, OpenApiConstants.DOTNAME_DECLARE_ROLES));
        resourceRolesAllowed = TypeUtil.getAnnotationValue(resourceClass, OpenApiConstants.DOTNAME_ROLES_ALLOWED);
        addScopes(resourceRolesAllowed);

        // Process exception mapper to auto generate api response based on method exceptions
        ////////////////////////////////////////
        Map<DotName, AnnotationInstance> exceptionAnnotationMap = processExceptionMappers(context);

        // Now find and process the operation methods
        ////////////////////////////////////////
        for (MethodInfo methodInfo : getResourceMethods(context, resourceClass)) {
            final AtomicInteger resourceCount = new AtomicInteger(0);

            OpenApiConstants.DOTNAME_JAXRS_HTTP_METHODS
                    .stream()
                    .filter(methodInfo::hasAnnotation)
                    .map(DotName::withoutPackagePrefix)
                    .map(PathItem.HttpMethod::valueOf)
                    .forEach(httpMethod -> {
                        resourceCount.incrementAndGet();
                        processJaxRsMethod(context, openApi, resourceClass, methodInfo, httpMethod, tagRefs,
                                locatorPathParameters,
                                exceptionAnnotationMap);
                    });

            if (resourceCount.get() == 0 && methodInfo.hasAnnotation(OpenApiConstants.DOTNAME_PATH)) {
                processJaxRsSubResource(context, openApi, locatorPathParameters, resourceClass, methodInfo);
            }
        }
    }

    /**
     * Build a map between exception class name and its corresponding @ApiResponse annotation in the jax-rs exception mapper
     *
     */
    private Map<DotName, AnnotationInstance> processExceptionMappers(final AnnotationScannerContext context) {
        Map<DotName, AnnotationInstance> exceptionHandlerMap = new HashMap<>();
        Collection<ClassInfo> exceptionMappers = context.getIndex()
                .getKnownDirectImplementors(OpenApiConstants.DOTNAME_EXCEPTION_MAPPER);

        for (ClassInfo classInfo : exceptionMappers) {
            DotName exceptionDotName = classInfo.interfaceTypes()
                    .stream()
                    .filter(it -> it.name().equals(OpenApiConstants.DOTNAME_EXCEPTION_MAPPER))
                    .filter(it -> it.kind() == Type.Kind.PARAMETERIZED_TYPE)
                    .map(Type::asParameterizedType)
                    .map(type -> type.arguments().get(0)) // ExceptionMapper<?> has a single type argument
                    .map(Type::name)
                    .findFirst()
                    .orElse(null);

            if (exceptionDotName == null) {
                continue;
            }

            MethodInfo toResponseMethod = classInfo.method(OpenApiConstants.TO_RESPONSE_METHOD_NAME,
                    Type.create(exceptionDotName, Type.Kind.CLASS));

            if (toResponseMethod.hasAnnotation(OpenApiConstants.DOTNAME_API_RESPONSE)) {
                AnnotationInstance apiResponseAnnotation = toResponseMethod.annotation(OpenApiConstants.DOTNAME_API_RESPONSE);
                if (apiResponseAnnotation.value(OpenApiConstants.PROP_RESPONSE_CODE) != null) {
                    exceptionHandlerMap.put(exceptionDotName, apiResponseAnnotation);
                }
            }
        }

        return exceptionHandlerMap;
    }

    /**
     * Adds the array of roles as scopes to each of the OAuth2 flows stored previously.
     * The flows are those declared by the application in components/securitySchemes
     * using annotations where the scopes were not defined. The description of the scope
     * will be set to the role name plus the string " role".
     *
     * @param roles array of roles from either <code>@DeclareRoles</code> or
     *        <code>@RolesAllowed</code>
     */
    private void addScopes(String[] roles) {
        if (roles == null || this.currentFlows == null) {
            return;
        }

        this.currentFlows.forEach(flow -> {
            if (flow.getScopes() == null) {
                flow.setScopes(new LinkedHashMap<>());
            }
            Arrays.stream(roles).forEach(role -> flow.addScope(role, role + " role"));
        });
    }

    /**
     * Extracts all methods from the provided class and its ancestors that are known to the instance's index
     * 
     * @param resource
     * @return all methods from the provided class and its ancestors
     */
    private List<MethodInfo> getResourceMethods(final AnnotationScannerContext context, ClassInfo resource) {
        Type resourceType = Type.create(resource.name(), Type.Kind.CLASS);
        Map<ClassInfo, Type> chain = JandexUtil.inheritanceChain(context.getIndex(), resource, resourceType);
        List<MethodInfo> methods = new ArrayList<>();

        for (ClassInfo classInfo : chain.keySet()) {
            methods.addAll(classInfo.methods());

            classInfo.interfaceTypes()
                    .stream()
                    .map(iface -> context.getIndex().getClassByName(TypeUtil.getName(iface)))
                    .filter(Objects::nonNull)
                    .flatMap(iface -> iface.methods().stream())
                    .forEach(methods::add);
        }

        return methods;
    }

    /**
     * Scans a sub-resource locator method's return type as a resource class. The list of locator path parameters
     * will be expanded with any parameters that apply to the resource sub-locator method (both path and operation
     * parameters).
     * 
     * @param openApi current OAI result
     * @param locatorPathParameters the parent resource's list of path parameters, may be null
     * @param resourceClass the JAX-RS resource class being processed. May be a sub-class of the class which declares method
     * @param method sub-resource locator JAX-RS method
     */
    private void processJaxRsSubResource(final AnnotationScannerContext context,
            OpenAPI openApi,
            List<Parameter> locatorPathParameters,
            ClassInfo resourceClass,
            MethodInfo method) {
        final Type methodReturnType = method.returnType();

        if (Type.Kind.VOID.equals(methodReturnType.kind())) {
            // Can sub-resource locators return a CompletionStage?
            return;
        }

        ClassInfo subResourceClass = context.getIndex().getClassByName(methodReturnType.name());

        if (subResourceClass != null) {
            final String originalAppPath = this.currentAppPath;
            ParameterProcessor.ResourceParameters params = ParameterProcessor.process(context.getIndex(), resourceClass, method,
                    (t) -> {
                        return readParameter(context, t);
                    },
                    context.getExtensions());

            this.currentAppPath = PathMaker.makePath(this.currentAppPath, params.getOperationPath());

            /*
             * Combine parameters passed previously with all of those from the current resource class and
             * method that apply to this Path. The full list will be used as PATH-LEVEL parameters for
             * sub-resource methods deeper in the scan.
             */
            processJaxRsResourceClass(context, openApi, subResourceClass,
                    mergeNullableLists(locatorPathParameters,
                            params.getPathItemParameters(),
                            params.getOperationParameters()));

            this.currentAppPath = originalAppPath;
        }
    }

    /**
     * Process a single JAX-RS method to produce an OpenAPI Operation.
     * 
     * @param openApi
     * @param resourceClass
     * @param method
     * @param methodType
     * @param resourceTags
     * @param locatorPathParameters
     */
    private void processJaxRsMethod(final AnnotationScannerContext context,
            OpenAPI openApi,
            ClassInfo resourceClass,
            MethodInfo method,
            PathItem.HttpMethod methodType,
            Set<String> resourceTags,
            List<Parameter> locatorPathParameters,
            Map<DotName, AnnotationInstance> exceptionAnnotationMap) {
        LOG.debugf("Processing jax-rs method: {0}", method.toString());

        final Operation operation;

        // Process any @Operation annotation
        /////////////////////////////////////////
        if (method.hasAnnotation(OpenApiConstants.DOTNAME_OPERATION)) {
            AnnotationInstance operationAnno = method.annotation(OpenApiConstants.DOTNAME_OPERATION);
            // If the operation is marked as hidden, just bail here because we don't want it as part of the model.
            if (operationAnno.value(OpenApiConstants.PROP_HIDDEN) != null
                    && operationAnno.value(OpenApiConstants.PROP_HIDDEN).asBoolean()) {
                return;
            }

            operation = new OperationImpl();
            // Otherwise, set various bits of meta-data from the values in the @Operation annotation
            operation.setSummary(JandexUtil.stringValue(operationAnno, OpenApiConstants.PROP_SUMMARY));
            operation.setDescription(JandexUtil.stringValue(operationAnno, OpenApiConstants.PROP_DESCRIPTION));
            operation.setOperationId(JandexUtil.stringValue(operationAnno, OpenApiConstants.PROP_OPERATION_ID));
            operation.setDeprecated(JandexUtil.booleanValue(operationAnno, OpenApiConstants.PROP_DEPRECATED));
        } else {
            operation = new OperationImpl();
        }

        PathItem pathItem = new PathItemImpl();

        // Figure out the current @Produces and @Consumes (if any)
        currentConsumes = getMediaTypes(method, OpenApiConstants.DOTNAME_CONSUMES);
        currentProduces = getMediaTypes(method, OpenApiConstants.DOTNAME_PRODUCES);

        // Process tags - @Tag and @Tags annotations combines with the resource tags we've already found (passed in)
        /////////////////////////////////////////
        Set<String> tags = processTags(openApi, method, true);

        if (tags == null) {
            if (!resourceTags.isEmpty()) {
                operation.setTags(new ArrayList<>(resourceTags));
            }
        } else if (!tags.isEmpty()) {
            operation.setTags(new ArrayList<>(tags));
        }

        // Process @Parameter annotations
        /////////////////////////////////////////
        ParameterProcessor.ResourceParameters params = ParameterProcessor.process(context.getIndex(), resourceClass, method,
                (t) -> {
                    return readParameter(context, t);
                }, context.getExtensions());

        operation.setParameters(params.getOperationParameters());
        pathItem.setParameters(mergeNullableLists(locatorPathParameters, params.getPathItemParameters()));

        // Process any @RequestBody annotation
        /////////////////////////////////////////
        // note: the @RequestBody annotation can be found on a method argument *or* on the method
        RequestBody requestBody = null;

        List<AnnotationInstance> requestBodyAnnotations = JandexUtil.getRepeatableAnnotation(method,
                OpenApiConstants.DOTNAME_REQUEST_BODY, null);
        for (AnnotationInstance annotation : requestBodyAnnotations) {
            requestBody = readRequestBody(context, annotation);
            Content formBodyContent = params.getFormBodyContent();

            if (formBodyContent != null) {
                // If form parameters were present, overlay RequestBody onto the generated form content
                requestBody.setContent((Content) MergeUtil.mergeObjects(formBodyContent, requestBody.getContent()));
            }

            // TODO if the method argument type is Request, don't generate a Schema!

            Type requestBodyType = null;
            if (annotation.target().kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
                requestBodyType = JandexUtil.getMethodParameterType(method,
                        annotation.target().asMethodParameter().position());
            } else if (annotation.target().kind() == AnnotationTarget.Kind.METHOD) {
                requestBodyType = JandexUtil.getRequestBodyParameterClassType(method, context.getExtensions());
            }

            // Only generate the request body schema if the @RequestBody is not a reference and no schema is yet specified
            if (requestBodyType != null && requestBody.getRef() == null) {
                if (!ModelUtil.requestBodyHasSchema(requestBody)) {
                    Schema schema = SchemaFactory.typeToSchema(context.getIndex(), requestBodyType, context.getExtensions());

                    if (schema != null) {
                        ModelUtil.setRequestBodySchema(requestBody, schema, currentConsumes);
                    }
                }

                if (requestBody.getRequired() == null && TypeUtil.isOptional(requestBodyType)) {
                    requestBody.setRequired(Boolean.FALSE);
                }
            }
        }

        // If the request body is null, figure it out from the parameters.  Only if the
        // method declares that it @Consumes data
        if ((requestBody == null || (requestBody.getContent() == null && requestBody.getRef() == null))
                && currentConsumes != null) {
            if (params.getFormBodySchema() != null) {
                if (requestBody == null) {
                    requestBody = new RequestBodyImpl();
                }
                Schema schema = params.getFormBodySchema();
                ModelUtil.setRequestBodySchema(requestBody, schema, currentConsumes);
            } else {
                Type requestBodyType = JandexUtil.getRequestBodyParameterClassType(method, context.getExtensions());

                if (requestBodyType != null) {
                    Schema schema = null;

                    if (OpenApiConstants.DOTNAME_RESTEASY_MULTIPART_INPUTS.contains(requestBodyType.name())) {
                        schema = new SchemaImpl();
                        schema.setType(Schema.SchemaType.OBJECT);
                    } else {
                        schema = SchemaFactory.typeToSchema(context.getIndex(), requestBodyType, context.getExtensions());
                    }

                    if (requestBody == null) {
                        requestBody = new RequestBodyImpl();
                    }

                    if (schema != null) {
                        ModelUtil.setRequestBodySchema(requestBody, schema, currentConsumes);
                    }

                    if (requestBody.getRequired() == null && TypeUtil.isOptional(requestBodyType)) {
                        requestBody.setRequired(Boolean.FALSE);
                    }
                }
            }
        }

        if (requestBody != null) {
            operation.setRequestBody(requestBody);
        }

        // Process @APIResponse annotations
        /////////////////////////////////////////
        List<AnnotationInstance> apiResponseAnnotations = JandexUtil.getRepeatableAnnotation(method,
                OpenApiConstants.DOTNAME_API_RESPONSE, OpenApiConstants.DOTNAME_API_RESPONSES);
        for (AnnotationInstance annotation : apiResponseAnnotations) {
            addApiReponseFromAnnotation(context, annotation, operation);
        }
        /*
         * If there is no response from annotations, try to create one from the method return value.
         * Do not generate a response if the app has used an empty @ApiResponses annotation. This
         * provides a way for the application to indicate that responses will be supplied some other
         * way (i.e. static file).
         */
        AnnotationInstance apiResponses = method.annotation(OpenApiConstants.DOTNAME_API_RESPONSES);
        if (apiResponses == null || !JandexUtil.isEmpty(apiResponses)) {
            createResponseFromJaxRsMethod(context, method, operation);
        }

        //Add api response using list of exceptions in the methods and exception mappers
        List<Type> methodExceptions = method.exceptions();

        for (Type type : methodExceptions) {
            DotName exceptionDotName = type.name();
            if (exceptionAnnotationMap.keySet().contains(exceptionDotName)) {
                AnnotationInstance exMapperApiResponseAnnotation = exceptionAnnotationMap.get(exceptionDotName);
                if (!this.responseCodeExistInMethodAnnotations(exMapperApiResponseAnnotation, apiResponseAnnotations)) {
                    addApiReponseFromAnnotation(context, exMapperApiResponseAnnotation, operation);
                }
            }
        }

        // Process @SecurityRequirement annotations
        ///////////////////////////////////////////
        List<AnnotationInstance> securityRequirementAnnotations = JandexUtil.getRepeatableAnnotation(method,
                OpenApiConstants.DOTNAME_SECURITY_REQUIREMENT, OpenApiConstants.DOTNAME_SECURITY_REQUIREMENTS);
        securityRequirementAnnotations.addAll(
                JandexUtil.getRepeatableAnnotation(resourceClass, OpenApiConstants.DOTNAME_SECURITY_REQUIREMENT,
                        OpenApiConstants.DOTNAME_SECURITY_REQUIREMENTS));
        for (AnnotationInstance annotation : securityRequirementAnnotations) {
            SecurityRequirement requirement = readSecurityRequirement(annotation);
            if (requirement != null) {
                operation.addSecurityRequirement(requirement);
            }
        }

        // Process @Callback annotations
        /////////////////////////////////////////
        List<AnnotationInstance> callbackAnnotations = JandexUtil.getRepeatableAnnotation(method,
                OpenApiConstants.DOTNAME_CALLBACK, OpenApiConstants.DOTNAME_CALLBACKS);
        Map<String, Callback> callbacks = new LinkedHashMap<>();
        for (AnnotationInstance annotation : callbackAnnotations) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                callbacks.put(name, readCallback(context, annotation));
            }

            if (!callbacks.isEmpty()) {
                operation.setCallbacks(callbacks);
            }
        }

        // Process @Server annotations
        ///////////////////////////////////
        List<AnnotationInstance> serverAnnotations = JandexUtil.getRepeatableAnnotation(method,
                OpenApiConstants.DOTNAME_SERVER, OpenApiConstants.DOTNAME_SERVERS);
        if (serverAnnotations.isEmpty()) {
            serverAnnotations.addAll(JandexUtil.getRepeatableAnnotation(method.declaringClass(),
                    OpenApiConstants.DOTNAME_SERVER, OpenApiConstants.DOTNAME_SERVERS));
        }
        for (AnnotationInstance annotation : serverAnnotations) {
            Server server = readServer(annotation);
            operation.addServer(server);
        }

        // Process @Extension annotations
        ///////////////////////////////////
        List<AnnotationInstance> extensionAnnotations = JandexUtil.getRepeatableAnnotation(method,
                OpenApiConstants.DOTNAME_EXTENSION, OpenApiConstants.DOTNAME_EXTENSIONS);
        if (extensionAnnotations.isEmpty()) {
            extensionAnnotations.addAll(JandexUtil.getRepeatableAnnotation(method.declaringClass(),
                    OpenApiConstants.DOTNAME_EXTENSION, OpenApiConstants.DOTNAME_EXTENSIONS));
        }
        for (AnnotationInstance annotation : extensionAnnotations) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            operation.addExtension(name, readExtensionValue(context, name, annotation));
        }

        processSecurityRoles(method, operation);

        // Now set the operation on the PathItem as appropriate based on the Http method type
        ///////////////////////////////////////////
        switch (methodType) {
            case DELETE:
                pathItem.setDELETE(operation);
                break;
            case GET:
                pathItem.setGET(operation);
                break;
            case HEAD:
                pathItem.setHEAD(operation);
                break;
            case OPTIONS:
                pathItem.setOPTIONS(operation);
                break;
            case PATCH:
                pathItem.setPATCH(operation);
                break;
            case POST:
                pathItem.setPOST(operation);
                break;
            case PUT:
                pathItem.setPUT(operation);
                break;
            case TRACE:
                pathItem.setTRACE(operation);
                break;
            default:
                break;
        }

        // Figure out the path for the operation.  This is a combination of the App, Resource, and Method @Path annotations
        String path = PathMaker.makePath(this.currentAppPath, params.getOperationPath());

        // Get or create a PathItem to hold the operation
        PathItem existingPath = ModelUtil.paths(openApi).getPathItem(path);

        if (existingPath == null) {
            ModelUtil.paths(openApi).addPathItem(path, pathItem);
        } else {
            // Changes applied to 'existingPath', no need to re-assign or add to OAI.
            MergeUtil.mergeObjects(existingPath, pathItem);
        }
    }

    /**
     * Check if the response code declared in the ExceptionMapper already defined in one of the ApiReponse annotations of the
     * method.
     * If the response code already exists then ignore the exception mapper annotation.
     *
     * @param exMapperApiResponseAnnotation ApiResponse annotation declared in the exception mapper
     * @param methodApiResponseAnnotations List of ApiResponse annotations declared in the jax-rs method.
     * @return response code exist or not
     */
    private boolean responseCodeExistInMethodAnnotations(AnnotationInstance exMapperApiResponseAnnotation,
            List<AnnotationInstance> methodApiResponseAnnotations) {
        AnnotationValue exMapperResponseCode = exMapperApiResponseAnnotation.value(OpenApiConstants.PROP_RESPONSE_CODE);
        Optional<AnnotationInstance> apiResponseWithSameCode = methodApiResponseAnnotations.stream()
                .filter(annotationInstance -> {
                    AnnotationValue methodAnnotationValue = annotationInstance.value(OpenApiConstants.PROP_RESPONSE_CODE);
                    return (methodAnnotationValue != null && methodAnnotationValue.equals(exMapperResponseCode));
                }).findFirst();

        return apiResponseWithSameCode.isPresent();
    }

    /**
     * Add api response to api responses using the annotation information
     *
     * @param apiResponseAnnotation The api response annotation
     * @param operation the method operation
     */
    private void addApiReponseFromAnnotation(final AnnotationScannerContext context, AnnotationInstance apiResponseAnnotation,
            Operation operation) {
        String responseCode = JandexUtil.stringValue(apiResponseAnnotation, OpenApiConstants.PROP_RESPONSE_CODE);
        if (responseCode == null) {
            responseCode = APIResponses.DEFAULT;
        }
        APIResponse response = readResponse(context, apiResponseAnnotation);
        APIResponses responses = ModelUtil.responses(operation);
        responses.addAPIResponse(responseCode, response);
    }

    /**
     * Processes any {@link org.eclipse.microprofile.openapi.annotations.tags.Tag} or
     * {@link org.eclipse.microprofile.openapi.annotations.tags.Tags} annotations present on
     * the annotation target and adds them to the OpenAPI model. The set of tag names found
     * (with iteration order preserved) is returned.
     * 
     * @param openApi OpenAPI model
     * @param target a MethodInfo or ClassInfo to read for tag annotations
     * @param nullWhenMissing determines if an empty set or a null value is returned when no annotations are found.
     * @return the set of tag names found
     */
    private Set<String> processTags(OpenAPI openApi, AnnotationTarget target, boolean nullWhenMissing) {
        if (!TypeUtil.hasAnnotation(target, OpenApiConstants.DOTNAME_TAG) &&
                !TypeUtil.hasAnnotation(target, OpenApiConstants.DOTNAME_TAGS)) {
            return nullWhenMissing ? null : Collections.emptySet();
        }

        Set<String> tags = new LinkedHashSet<>();
        List<AnnotationInstance> tagAnnos = JandexUtil.getRepeatableAnnotation(target,
                OpenApiConstants.DOTNAME_TAG,
                OpenApiConstants.DOTNAME_TAGS);

        for (AnnotationInstance ta : tagAnnos) {
            if (JandexUtil.isRef(ta)) {
                tags.add(JandexUtil.value(ta, OpenApiConstants.PROP_REF));
            } else {
                Tag tag = readTag(ta);

                if (tag.getName() != null) {
                    ModelUtil.addTag(openApi, tag);
                    tags.add(tag.getName());
                }
            }
        }

        String[] refs = TypeUtil.getAnnotationValue(target, OpenApiConstants.DOTNAME_TAGS, OpenApiConstants.PROP_REFS);

        if (refs != null) {
            Arrays.stream(refs).forEach(tags::add);
        }

        return tags;
    }

    static String[] getMediaTypes(MethodInfo resourceMethod, DotName annotationName) {
        AnnotationInstance annotation = resourceMethod.annotation(annotationName);

        if (annotation == null) {
            annotation = JandexUtil.getClassAnnotation(resourceMethod.declaringClass(), annotationName);
        }

        if (annotation != null) {
            AnnotationValue annotationValue = annotation.value();

            if (annotationValue != null) {
                return annotationValue.asStringArray();
            }

            return OpenApiConstants.DEFAULT_MEDIA_TYPES.get();
        }

        return null;
    }

    /**
     * Called when a jax-rs method's APIResponse annotations have all been processed but
     * no response was actually created for the operation. This method will create a response
     * from the method information and add it to the given operation. It will try to do this
     * by examining the method's return value and the type of operation (GET, PUT, POST, DELETE).
     *
     * If there is a return value of some kind (a non-void return type) then the response code
     * is assumed to be 200.
     *
     * If there not a return value (void return type) then either a 201 or 204 is returned,
     * depending on the type of request.
     *
     * TODO generate responses for each checked exception?
     * 
     * @param method
     * @param operation
     */
    private void createResponseFromJaxRsMethod(final AnnotationScannerContext context,
            MethodInfo method,
            Operation operation) {
        Type returnType = method.returnType();
        APIResponse response = null;
        String code = "200";
        String description = "OK";

        if (returnType.kind() == Type.Kind.VOID) {
            boolean asyncResponse = method.parameters()
                    .stream()
                    .map(Type::name)
                    .anyMatch(OpenApiConstants.DOTNAME_ASYNC_RESPONSE::equals);

            if (method.hasAnnotation(OpenApiConstants.DOTNAME_POST)) {
                code = "201";
                description = "Created";
            } else if (!asyncResponse) {
                code = "204";
                description = "No Content";
            }

            if (generateResponse(code, operation)) {
                response = new APIResponseImpl().description(description);
            }
        } else if (generateResponse(code, operation)) {
            response = new APIResponseImpl().description(description);

            /*
             * Only generate content if not already supplied in annotations and the
             * method does not return an opaque JAX-RS Response
             */
            if (!returnType.name().equals(OpenApiConstants.DOTNAME_RESPONSE) &&
                    (ModelUtil.responses(operation).getAPIResponse(code) == null ||
                            ModelUtil.responses(operation).getAPIResponse(code).getContent() == null)) {

                Schema schema;

                if (OpenApiConstants.DOTNAME_RESTEASY_MULTIPART_OUTPUTS.contains(returnType.name())) {
                    schema = new SchemaImpl();
                    schema.setType(Schema.SchemaType.OBJECT);
                } else {
                    schema = SchemaFactory.typeToSchema(context.getIndex(), returnType, context.getExtensions());
                }

                ContentImpl content = new ContentImpl();
                String[] produces = this.currentProduces;

                if (produces == null || produces.length == 0) {
                    produces = OpenApiConstants.DEFAULT_MEDIA_TYPES.get();
                }

                if (schema != null && schema.getNullable() == null && TypeUtil.isOptional(returnType)) {
                    schema.setNullable(Boolean.TRUE);
                }

                for (String producesType : produces) {
                    MediaType mt = new MediaTypeImpl();
                    mt.setSchema(schema);
                    content.addMediaType(producesType, mt);
                }

                response.setContent(content);
            }
        }

        if (response != null) {
            APIResponses responses = ModelUtil.responses(operation);

            if (responses.hasAPIResponse(code)) {
                APIResponse responseFromAnnotations = responses.getAPIResponse(code);
                responses.removeAPIResponse(code);

                // Overlay the information from the annotations (2nd arg) onto the generated details (1st)
                response = MergeUtil.mergeObjects(response, responseFromAnnotations);
            }

            responses.addAPIResponse(code, response);
        }
    }

    /**
     * Determine if the default response information should be generated.
     * It should be done when no responses have been declared or if the default
     * response already exists and is missing information (e.g. content).
     *
     * @param status the status determined to be the generated default
     * @param operation current operation
     * @return true if a default response should be generated, otherwise false.
     */
    private boolean generateResponse(String status, Operation operation) {
        APIResponses responses = operation.getResponses();
        return responses == null || responses.getAPIResponse(status) != null;
    }

    /**
     * Add method-level or resource-level <code>RolesAllowed</code> values as
     * scopes to the current operation.
     * 
     * <ul>
     * <li>If a <code>DenyAll</code> annotation is present (and a method-level
     * <code>RolesAllowed</code> is not), the roles allowed will be set to an
     * empty array.
     * 
     * <li>If none of a <code>PermitAll</code>, a <code>DenyAll</code>, and a
     * <code>RolesAllowed</code> annotation is present at the method-level, the
     * roles allowed will be set to the resource's <code>RolesAllowed</code>.
     * 
     * @param method the current JAX-RS method
     * @param operation the OpenAPI Operation
     */
    private void processSecurityRoles(MethodInfo method, Operation operation) {
        if (this.currentSecurityScheme != null) {
            String[] rolesAllowed = TypeUtil.getAnnotationValue(method, OpenApiConstants.DOTNAME_ROLES_ALLOWED);

            if (rolesAllowed != null) {
                addScopes(rolesAllowed);
                addRolesAllowed(operation, rolesAllowed);
            } else if (this.resourceRolesAllowed != null) {
                boolean denyAll = TypeUtil.getAnnotation(method, OpenApiConstants.DOTNAME_DENY_ALL) != null;
                boolean permitAll = TypeUtil.getAnnotation(method, OpenApiConstants.DOTNAME_PERMIT_ALL) != null;

                if (denyAll) {
                    addRolesAllowed(operation, new String[0]);
                } else if (!permitAll) {
                    addRolesAllowed(operation, this.resourceRolesAllowed);
                }
            }
        }
    }

    /**
     * Add an array of roles to the operation's security requirements.
     * 
     * If no security requirements yet exists, one is created with the name of the
     * single OAUTH/OPENIDCONNECT previously defined in the OpenAPI's Components
     * section.
     * 
     * Otherwise, the roles are added to only a single existing requirement
     * where the name of the requirement's scheme matches the name of the
     * single OAUTH/OPENIDCONNECT previously defined in the OpenAPI's Components
     * section.
     * 
     * @param operation the OpenAPI Operation
     * @param roles a list of JAX-RS roles to use as scopes
     */
    private void addRolesAllowed(Operation operation, String[] roles) {
        List<SecurityRequirement> requirements = operation.getSecurity();

        if (requirements == null) {
            SecurityRequirement requirement = new SecurityRequirementImpl();
            requirement.addScheme(currentSecurityScheme, new ArrayList<>(Arrays.asList(roles)));
            operation.setSecurity(new ArrayList<>(Arrays.asList(requirement)));
        } else if (requirements.size() == 1) {
            SecurityRequirement requirement = requirements.get(0);

            if (requirement.hasScheme(currentSecurityScheme)) {
                // The name of the declared requirement must match the scheme's name
                List<String> scopes = requirement.getScheme(currentSecurityScheme);
                for (String role : roles) {
                    if (!scopes.contains(role)) {
                        scopes.add(role);
                    }
                }
            }
        }
    }

    /**
     * Reads a OpenAPIDefinition annotation.
     * 
     * @param openApi OpenAPIImpl
     * @param definitionAnno AnnotationInstance
     */
    private void processDefinition(final AnnotationScannerContext context, OpenAPIImpl openApi,
            AnnotationInstance definitionAnno) {
        LOG.debug("Processing an @OpenAPIDefinition annotation.");
        openApi.setInfo(readInfo(definitionAnno.value(OpenApiConstants.PROP_INFO)));
        openApi.setTags(readTags(definitionAnno.value(OpenApiConstants.PROP_TAGS)));
        openApi.setServers(readServers(definitionAnno.value(OpenApiConstants.PROP_SERVERS)));
        openApi.setSecurity(readSecurity(definitionAnno.value(OpenApiConstants.PROP_SECURITY)));
        openApi.setExternalDocs(readExternalDocs(definitionAnno.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        openApi.setComponents(readComponents(context, definitionAnno.value(OpenApiConstants.PROP_COMPONENTS)));
    }

    /**
     * Reads an Info annotation.
     * 
     * @param infoAnno
     */
    private Info readInfo(AnnotationValue infoAnno) {
        if (infoAnno == null) {
            return null;
        }
        LOG.debug("Processing an @Info annotation.");
        AnnotationInstance nested = infoAnno.asNested();
        InfoImpl info = new InfoImpl();
        info.setTitle(JandexUtil.stringValue(nested, OpenApiConstants.PROP_TITLE));
        info.setDescription(JandexUtil.stringValue(nested, OpenApiConstants.PROP_DESCRIPTION));
        info.setTermsOfService(JandexUtil.stringValue(nested, OpenApiConstants.PROP_TERMS_OF_SERVICE));
        info.setContact(readContact(nested.value(OpenApiConstants.PROP_CONTACT)));
        info.setLicense(readLicense(nested.value(OpenApiConstants.PROP_LICENSE)));
        info.setVersion(JandexUtil.stringValue(nested, OpenApiConstants.PROP_VERSION));
        return info;
    }

    /**
     * Reads an Contact annotation.
     * 
     * @param contactAnno
     */
    private Contact readContact(AnnotationValue contactAnno) {
        if (contactAnno == null) {
            return null;
        }
        LOG.debug("Processing an @Contact annotation.");
        AnnotationInstance nested = contactAnno.asNested();
        ContactImpl contact = new ContactImpl();
        contact.setName(JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME));
        contact.setUrl(JandexUtil.stringValue(nested, OpenApiConstants.PROP_URL));
        contact.setEmail(JandexUtil.stringValue(nested, OpenApiConstants.PROP_EMAIL));
        return contact;
    }

    /**
     * Reads an License annotation.
     * 
     * @param licenseAnno
     */
    private License readLicense(AnnotationValue licenseAnno) {
        if (licenseAnno == null) {
            return null;
        }
        LOG.debug("Processing an @License annotation.");
        AnnotationInstance nested = licenseAnno.asNested();
        LicenseImpl license = new LicenseImpl();
        license.setName(JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME));
        license.setUrl(JandexUtil.stringValue(nested, OpenApiConstants.PROP_URL));
        return license;
    }

    /**
     * Reads any Tag annotations. The annotation
     * value is an array of Tag annotations.
     * 
     * @param tagAnnos
     */
    private List<Tag> readTags(AnnotationValue tagAnnos) {
        if (tagAnnos == null) {
            return null;
        }
        LOG.debug("Processing an array of @Tag annotations.");
        AnnotationInstance[] nestedArray = tagAnnos.asNestedArray();
        List<Tag> tags = new ArrayList<>();
        for (AnnotationInstance tagAnno : nestedArray) {
            if (!JandexUtil.isRef(tagAnno)) {
                tags.add(readTag(tagAnno));
            }
        }
        return tags;
    }

    /**
     * Reads a single Tag annotation.
     * 
     * @param tagAnno tag annotation, must not be null
     */
    private Tag readTag(AnnotationInstance tagAnno) {
        Objects.requireNonNull(tagAnno, "Tag annotation must not be null");
        LOG.debug("Processing a single @Tag annotation.");
        TagImpl tag = new TagImpl();
        tag.setName(JandexUtil.stringValue(tagAnno, OpenApiConstants.PROP_NAME));
        tag.setDescription(JandexUtil.stringValue(tagAnno, OpenApiConstants.PROP_DESCRIPTION));
        tag.setExternalDocs(readExternalDocs(tagAnno.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        return tag;
    }

    /**
     * Reads any Server annotations. The annotation value is an array of Server annotations.
     * 
     * @param serverAnnos
     */
    private List<Server> readServers(AnnotationValue serverAnnos) {
        if (serverAnnos == null) {
            return null;
        }
        LOG.debug("Processing an array of @Server annotations.");
        AnnotationInstance[] nestedArray = serverAnnos.asNestedArray();
        List<Server> servers = new ArrayList<>();
        for (AnnotationInstance serverAnno : nestedArray) {
            servers.add(readServer(serverAnno));
        }
        return servers;
    }

    /**
     * Reads a single Server annotation.
     * 
     * @param serverAnno
     */
    private Server readServer(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        return readServer(value.asNested());
    }

    /**
     * Reads a single Server annotation.
     * 
     * @param serverAnno
     */
    private Server readServer(AnnotationInstance serverAnno) {
        if (serverAnno == null) {
            return null;
        }
        LOG.debug("Processing a single @Server annotation.");
        ServerImpl server = new ServerImpl();
        server.setUrl(JandexUtil.stringValue(serverAnno, OpenApiConstants.PROP_URL));
        server.setDescription(JandexUtil.stringValue(serverAnno, OpenApiConstants.PROP_DESCRIPTION));
        server.setVariables(readServerVariables(serverAnno.value(OpenApiConstants.PROP_VARIABLES)));
        return server;
    }

    /**
     * Reads an array of ServerVariable annotations, returning a new {@link ServerVariables} model. The
     * annotation value is an array of ServerVariable annotations.
     * 
     * @param value
     * @return
     */
    private Map<String, ServerVariable> readServerVariables(AnnotationValue serverVariableAnnos) {
        if (serverVariableAnnos == null) {
            return null;
        }
        LOG.debug("Processing an array of @ServerVariable annotations.");
        AnnotationInstance[] nestedArray = serverVariableAnnos.asNestedArray();
        Map<String, ServerVariable> variables = new LinkedHashMap<>();
        for (AnnotationInstance serverVariableAnno : nestedArray) {
            String name = JandexUtil.stringValue(serverVariableAnno, OpenApiConstants.PROP_NAME);
            if (name != null) {
                variables.put(name, readServerVariable(serverVariableAnno));
            }
        }
        return variables;
    }

    /**
     * Reads a single ServerVariable annotation.
     * 
     * @param serverVariableAnno
     */
    private ServerVariable readServerVariable(AnnotationInstance serverVariableAnno) {
        if (serverVariableAnno == null) {
            return null;
        }
        LOG.debug("Processing a single @ServerVariable annotation.");
        ServerVariable variable = new ServerVariableImpl();
        variable.setDescription(JandexUtil.stringValue(serverVariableAnno, OpenApiConstants.PROP_DESCRIPTION));
        variable.setEnumeration(JandexUtil.stringListValue(serverVariableAnno, OpenApiConstants.PROP_ENUMERATION));
        variable.setDefaultValue(JandexUtil.stringValue(serverVariableAnno, OpenApiConstants.PROP_DEFAULT_VALUE));
        return variable;
    }

    /**
     * Reads any SecurityRequirement annotations. The annotation value is an array of
     * SecurityRequirement annotations.
     * 
     * @param value
     */
    private List<SecurityRequirement> readSecurity(AnnotationValue securityRequirementAnnos) {
        if (securityRequirementAnnos == null) {
            return null;
        }
        LOG.debug("Processing an array of @SecurityRequirement annotations.");
        AnnotationInstance[] nestedArray = securityRequirementAnnos.asNestedArray();
        List<SecurityRequirement> requirements = new ArrayList<>();
        for (AnnotationInstance requirementAnno : nestedArray) {
            SecurityRequirement requirement = readSecurityRequirement(requirementAnno);
            if (requirement != null) {
                requirements.add(requirement);
            }
        }
        return requirements;
    }

    /**
     * Reads a single SecurityRequirement annotation.
     * 
     * @param annotation
     */
    private SecurityRequirement readSecurityRequirement(AnnotationInstance annotation) {
        String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
        if (name != null) {
            List<String> scopes = JandexUtil.stringListValue(annotation, OpenApiConstants.PROP_SCOPES);
            SecurityRequirement requirement = new SecurityRequirementImpl();
            if (scopes == null) {
                requirement.addScheme(name);
            } else {
                requirement.addScheme(name, scopes);
            }
            return requirement;
        }
        return null;
    }

    /**
     * Reads an ExternalDocumentation annotation.
     * 
     * @param externalDocAnno
     */
    private ExternalDocumentation readExternalDocs(AnnotationValue externalDocAnno) {
        if (externalDocAnno == null) {
            return null;
        }
        LOG.debug("Processing an @ExternalDocumentation annotation.");
        AnnotationInstance nested = externalDocAnno.asNested();
        ExternalDocumentation externalDoc = new ExternalDocumentationImpl();
        externalDoc.setDescription(JandexUtil.stringValue(nested, OpenApiConstants.PROP_DESCRIPTION));
        externalDoc.setUrl(JandexUtil.stringValue(nested, OpenApiConstants.PROP_URL));
        return externalDoc;
    }

    /**
     * Reads any Components annotations.
     * 
     * @param componentsAnno
     */
    private Components readComponents(final AnnotationScannerContext context, AnnotationValue componentsAnno) {
        if (componentsAnno == null) {
            return null;
        }
        LOG.debug("Processing an @Components annotation.");
        AnnotationInstance nested = componentsAnno.asNested();
        Components components = new ComponentsImpl();
        // TODO for EVERY item below, handle the case where the annotation is ref-only.  then strip the ref path and use the final segment as the name
        components.setCallbacks(readCallbacks(context, nested.value(OpenApiConstants.PROP_CALLBACKS)));
        components.setExamples(readExamples(nested.value(OpenApiConstants.PROP_EXAMPLES)));
        components.setHeaders(readHeaders(context, nested.value(OpenApiConstants.PROP_HEADERS)));
        components.setLinks(readLinks(nested.value(OpenApiConstants.PROP_LINKS)));
        components.setParameters(readParameters(context, nested.value(OpenApiConstants.PROP_PARAMETERS)));
        components.setRequestBodies(readRequestBodies(context, nested.value(OpenApiConstants.PROP_REQUEST_BODIES)));
        components.setResponses(readResponses(context, nested.value(OpenApiConstants.PROP_RESPONSES)));
        components.setSchemas(readSchemas(context, nested.value(OpenApiConstants.PROP_SCHEMAS)));
        components.setSecuritySchemes(readSecuritySchemes(nested.value(OpenApiConstants.PROP_SECURITY_SCHEMES)));
        return components;
    }

    /**
     * Reads a map of Callback annotations.
     * 
     * @param value
     */
    private Map<String, Callback> readCallbacks(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Callback annotations.");
        Map<String, Callback> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readCallback(context, nested));
            }
        }
        return map;
    }

    /**
     * Reads a Callback annotation into a model.
     * 
     * @param annotation
     */
    private Callback readCallback(final AnnotationScannerContext context, AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Callback annotation.");
        Callback callback = new CallbackImpl();
        callback.setRef(JandexUtil.refValue(annotation, JandexUtil.RefType.Callback));
        String expression = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_CALLBACK_URL_EXPRESSION);
        callback.addPathItem(expression,
                readCallbackOperations(context, annotation.value(OpenApiConstants.PROP_OPERATIONS)));
        return callback;
    }

    /**
     * Reads the CallbackOperation annotations as a PathItem. The annotation value
     * in this case is an array of CallbackOperation annotations.
     * 
     * @param value
     */
    private PathItem readCallbackOperations(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing an array of @CallbackOperation annotations.");
        AnnotationInstance[] nestedArray = value.asNestedArray();
        PathItem pathItem = new PathItemImpl();
        for (AnnotationInstance operationAnno : nestedArray) {
            String method = JandexUtil.stringValue(operationAnno, OpenApiConstants.PROP_METHOD);
            Operation operation = readCallbackOperation(context, operationAnno);
            if (method == null) {
                continue;
            }
            try {
                PropertyDescriptor descriptor = new PropertyDescriptor(method.toUpperCase(), pathItem.getClass());
                Method mutator = descriptor.getWriteMethod();
                mutator.invoke(pathItem, operation);
            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOG.error("Error reading a CallbackOperation annotation.", e);
            }
        }
        return pathItem;
    }

    /**
     * Reads a single CallbackOperation annotation.
     * 
     * @param operationAnno
     * @return
     */
    private Operation readCallbackOperation(final AnnotationScannerContext context, AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @CallbackOperation annotation.");
        Operation operation = new OperationImpl();
        operation.setSummary(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SUMMARY));
        operation.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        operation.setExternalDocs(readExternalDocs(annotation.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        operation.setParameters(readCallbackOperationParameters(context, annotation.value(OpenApiConstants.PROP_PARAMETERS)));
        operation.setRequestBody(readRequestBody(context, annotation.value(OpenApiConstants.PROP_REQUEST_BODY)));
        operation.setResponses(readCallbackOperationResponses(context, annotation.value(OpenApiConstants.PROP_RESPONSES)));
        operation.setSecurity(readSecurity(annotation.value(OpenApiConstants.PROP_SECURITY)));
        operation.setExtensions(readExtensions(context, annotation.value(OpenApiConstants.PROP_EXTENSIONS)));
        return operation;
    }

    /**
     * Reads an array of Parameter annotations into a list.
     * 
     * @param value
     */
    private List<Parameter> readCallbackOperationParameters(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a list of @Parameter annotations.");
        List<Parameter> parameters = new ArrayList<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            ParameterImpl parameter = readParameter(context, nested);
            if (parameter != null && !parameter.isHidden()) {
                parameters.add(parameter);
            }
        }
        return parameters;
    }

    /**
     * Reads an array of APIResponse annotations into an {@link APIResponses} model.
     * 
     * @param value
     */
    private APIResponses readCallbackOperationResponses(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a list of @APIResponse annotations into an APIResponses model.");
        APIResponses responses = new APIResponsesImpl();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String responseCode = JandexUtil.stringValue(nested, OpenApiConstants.PROP_RESPONSE_CODE);
            if (responseCode != null) {
                responses.addAPIResponse(responseCode, readResponse(context, nested));
            }
        }
        return responses;
    }

    /**
     * Reads a map of Example annotations.
     * 
     * @param value
     */
    private Map<String, Example> readExamples(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @ExampleObject annotations.");
        Map<String, Example> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readExample(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Example annotation into a model.
     * 
     * @param annotation
     */
    private Example readExample(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @ExampleObject annotation.");
        Example example = new ExampleImpl();
        example.setSummary(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SUMMARY));
        example.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        example.setValue(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_VALUE));
        example.setExternalValue(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_EXTERNAL_VALUE));
        example.setRef(JandexUtil.refValue(annotation, JandexUtil.RefType.Example));
        return example;
    }

    /**
     * Reads a map of Header annotations.
     * 
     * @param value
     */
    private Map<String, Header> readHeaders(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Header annotations.");
        Map<String, Header> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readHeader(context, nested));
            }
        }
        return map;
    }

    /**
     * Reads a Header annotation into a model.
     * 
     * @param annotation
     */
    private Header readHeader(final AnnotationScannerContext context, AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Header annotation.");
        Header header = new HeaderImpl();
        header.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        header.setSchema(SchemaFactory.readSchema(context.getIndex(), annotation.value(OpenApiConstants.PROP_SCHEMA)));
        header.setRequired(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_REQUIRED));
        header.setDeprecated(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_DEPRECATED));
        header.setAllowEmptyValue(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_ALLOW_EMPTY_VALUE));
        header.setRef(JandexUtil.refValue(annotation, JandexUtil.RefType.Header));
        return header;
    }

    /**
     * Reads a map of Link annotations.
     * 
     * @param value
     */
    private Map<String, Link> readLinks(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Link annotations.");
        Map<String, Link> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readLink(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Link annotation into a model.
     * 
     * @param annotation
     */
    private Link readLink(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Link annotation.");
        Link link = new LinkImpl();
        link.setOperationRef(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_OPERATION_REF));
        link.setOperationId(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_OPERATION_ID));
        link.setParameters(readLinkParameters(annotation.value(OpenApiConstants.PROP_PARAMETERS)));
        link.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        link.setRequestBody(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_REQUEST_BODY));
        link.setServer(readServer(annotation.value(OpenApiConstants.PROP_SERVER)));
        link.setRef(JandexUtil.refValue(annotation, JandexUtil.RefType.Link));
        return link;
    }

    /**
     * Reads an array of LinkParameter annotations into a map.
     * 
     * @param value
     */
    private Map<String, Object> readLinkParameters(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        AnnotationInstance[] nestedArray = value.asNestedArray();
        Map<String, Object> linkParams = new LinkedHashMap<>();
        for (AnnotationInstance annotation : nestedArray) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            if (name != null) {
                String expression = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_EXPRESSION);
                linkParams.put(name, expression);
            }
        }
        return linkParams;
    }

    /**
     * Reads a map of Parameter annotations.
     * 
     * @param value
     */
    private Map<String, Parameter> readParameters(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Parameter annotations.");
        Map<String, Parameter> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                ParameterImpl parameter = readParameter(context, nested);
                if (parameter != null && !parameter.isHidden()) {
                    map.put(name, parameter);
                }
            }
        }
        return map;
    }

    /**
     * Reads a Parameter annotation into a model.
     * 
     * @param annotation
     */
    private ParameterImpl readParameter(final AnnotationScannerContext context, AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Link annotation.");

        ParameterImpl parameter = new ParameterImpl();
        parameter.setName(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME));
        parameter.setIn(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_IN,
                org.eclipse.microprofile.openapi.models.parameters.Parameter.In.class));

        // Params can be hidden. Skip if that's the case.
        Boolean isHidden = JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_HIDDEN);

        if (Boolean.TRUE.equals(isHidden)) {
            parameter.setHidden(true);
            return parameter;
        }

        parameter.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        parameter.setRequired(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_REQUIRED));
        parameter.setDeprecated(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_DEPRECATED));
        parameter.setAllowEmptyValue(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_ALLOW_EMPTY_VALUE));
        parameter.setStyle(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_STYLE,
                org.eclipse.microprofile.openapi.models.parameters.Parameter.Style.class));
        parameter.setExplode(readExplode(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_EXPLODE,
                org.eclipse.microprofile.openapi.annotations.enums.Explode.class)));
        parameter.setAllowReserved(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_ALLOW_RESERVED));
        parameter.setSchema(SchemaFactory.readSchema(context.getIndex(), annotation.value(OpenApiConstants.PROP_SCHEMA)));
        parameter.setContent(readContent(context, annotation.value(OpenApiConstants.PROP_CONTENT), ContentDirection.Parameter));
        parameter.setExamples(readExamples(annotation.value(OpenApiConstants.PROP_EXAMPLES)));
        parameter.setExample(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_EXAMPLE));
        parameter.setRef(JandexUtil.refValue(annotation, JandexUtil.RefType.Parameter));
        return parameter;
    }

    /**
     * Converts from an Explode enum to a true/false/null.
     * 
     * @param enumValue
     */
    private Boolean readExplode(Explode enumValue) {
        if (enumValue == Explode.TRUE) {
            return Boolean.TRUE;
        }
        if (enumValue == Explode.FALSE) {
            return Boolean.FALSE;
        }
        return null;
    }

    /**
     * Reads a single Content annotation into a model. The value in this case is an array of
     * Content annotations.
     * 
     * @param value
     */
    private Content readContent(final AnnotationScannerContext context, AnnotationValue value, ContentDirection direction) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a single @Content annotation.");
        Content content = new ContentImpl();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String contentType = JandexUtil.stringValue(nested, OpenApiConstants.PROP_MEDIA_TYPE);
            MediaType mediaTypeModel = readMediaType(context, nested);
            if (contentType == null) {
                // If the content type is not provided in the @Content annotation, then
                // we assume it applies to all the jax-rs method's @Consumes or @Produces
                String[] mimeTypes = {};
                if (direction == ContentDirection.Input && currentConsumes != null) {
                    mimeTypes = currentConsumes;
                }
                if (direction == ContentDirection.Output && currentProduces != null) {
                    mimeTypes = currentProduces;
                }
                if (direction == ContentDirection.Parameter) {
                    mimeTypes = OpenApiConstants.DEFAULT_MEDIA_TYPES.get();
                }
                for (String mimeType : mimeTypes) {
                    content.addMediaType(mimeType, mediaTypeModel);
                }
            } else {
                content.addMediaType(contentType, mediaTypeModel);
            }
        }
        return content;
    }

    /**
     * Reads a single Content annotation into a {@link MediaType} model.
     * 
     * @param nested
     */
    private MediaType readMediaType(final AnnotationScannerContext context, AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Content annotation as a MediaType.");
        MediaType mediaType = new MediaTypeImpl();
        mediaType.setExamples(readExamples(annotation.value(OpenApiConstants.PROP_EXAMPLES)));
        mediaType.setExample(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_EXAMPLE));
        mediaType.setSchema(SchemaFactory.readSchema(context.getIndex(), annotation.value(OpenApiConstants.PROP_SCHEMA)));
        mediaType.setEncoding(readEncodings(context, annotation.value(OpenApiConstants.PROP_ENCODING)));
        return mediaType;
    }

    /**
     * Reads an array of Encoding annotations as a Map.
     * 
     * @param value
     */
    private Map<String, Encoding> readEncodings(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Encoding annotations.");
        Map<String, Encoding> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance annotation : nestedArray) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            if (name != null) {
                map.put(name, readEncoding(context, annotation));
            }
        }
        return map;
    }

    /**
     * Reads a single Encoding annotation into a model.
     * 
     * @param annotation
     */
    private Encoding readEncoding(final AnnotationScannerContext context, AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Encoding annotation.");
        Encoding encoding = new EncodingImpl();
        encoding.setContentType(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_CONTENT_TYPE));
        encoding.setStyle(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_STYLE,
                org.eclipse.microprofile.openapi.models.media.Encoding.Style.class));
        encoding.setExplode(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_EXPLODE));
        encoding.setAllowReserved(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_ALLOW_RESERVED));
        encoding.setHeaders(readHeaders(context, annotation.value(OpenApiConstants.PROP_HEADERS)));
        return encoding;
    }

    /**
     * Reads a map of RequestBody annotations.
     * 
     * @param value
     */
    private Map<String, RequestBody> readRequestBodies(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @RequestBody annotations.");
        Map<String, RequestBody> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readRequestBody(context, nested));
            }
        }
        return map;
    }

    /**
     * Reads a RequestBody annotation into a model.
     * 
     * @param value
     */
    private RequestBody readRequestBody(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        return readRequestBody(context, value.asNested());
    }

    /**
     * Reads a RequestBody annotation into a model.
     * 
     * @param annotation
     */
    private RequestBody readRequestBody(final AnnotationScannerContext context, AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @RequestBody annotation.");
        RequestBody requestBody = new RequestBodyImpl();
        requestBody.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        requestBody.setContent(readContent(context, annotation.value(OpenApiConstants.PROP_CONTENT), ContentDirection.Input));
        requestBody.setRequired(JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_REQUIRED));
        requestBody.setRef(JandexUtil.refValue(annotation, JandexUtil.RefType.RequestBody));
        return requestBody;
    }

    /**
     * Reads a map of APIResponse annotations.
     * 
     * @param value
     */
    private Map<String, APIResponse> readResponses(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @APIResponse annotations.");
        Map<String, APIResponse> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readResponse(context, nested));
            }
        }
        return map;
    }

    /**
     * Reads a APIResponse annotation into a model.
     * 
     * @param annotation
     */
    private APIResponse readResponse(final AnnotationScannerContext context, AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @Response annotation.");
        APIResponse response = new APIResponseImpl();
        response.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        response.setHeaders(readHeaders(context, annotation.value(OpenApiConstants.PROP_HEADERS)));
        response.setLinks(readLinks(annotation.value(OpenApiConstants.PROP_LINKS)));
        response.setContent(readContent(context, annotation.value(OpenApiConstants.PROP_CONTENT), ContentDirection.Output));
        response.setRef(JandexUtil.refValue(annotation, JandexUtil.RefType.Response));
        return response;
    }

    /**
     * Reads a map of Schema annotations.
     * 
     * @param value
     */
    private Map<String, Schema> readSchemas(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @Schema annotations.");
        Map<String, Schema> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);

            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }

            /*
             * The name is REQUIRED when the schema is defined within
             * {@link org.eclipse.microprofile.openapi.annotations.Components}.
             */
            if (name != null) {
                map.put(name, SchemaFactory.readSchema(context.getIndex(), nested));
            } /*-
              //For consideration - be more lenient and attempt to use the name from the implementation's @Schema?
              else {
                if (JandexUtil.isSimpleClassSchema(nested)) {
                    Schema schema = SchemaFactory.readClassSchema(index, nested.value(OpenApiConstants.PROP_IMPLEMENTATION), false);
              
                    if (schema instanceof SchemaImpl) {
                        name = ((SchemaImpl) schema).getName();
              
                        if (name != null) {
                            map.put(name, schema);
                        }
                    }
                }
              }*/
        }
        return map;
    }

    /**
     * Reads a map of SecurityScheme annotations.
     * 
     * @param value
     */
    private Map<String, SecurityScheme> readSecuritySchemes(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a map of @SecurityScheme annotations.");
        Map<String, SecurityScheme> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_SECURITY_SCHEME_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readSecurityScheme(nested));
            }
        }
        return map;
    }

    /**
     * Reads a SecurityScheme annotation into a model.
     * 
     * @param annotation
     */
    private SecurityScheme readSecurityScheme(AnnotationInstance annotation) {
        if (annotation == null) {
            return null;
        }
        LOG.debug("Processing a single @SecurityScheme annotation.");
        SecurityScheme securityScheme = new SecuritySchemeImpl();
        securityScheme.setType(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_TYPE,
                org.eclipse.microprofile.openapi.models.security.SecurityScheme.Type.class));
        securityScheme.setDescription(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_DESCRIPTION));
        securityScheme.setName(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_API_KEY_NAME));
        securityScheme.setIn(JandexUtil.enumValue(annotation, OpenApiConstants.PROP_IN,
                org.eclipse.microprofile.openapi.models.security.SecurityScheme.In.class));
        securityScheme.setScheme(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SCHEME));
        securityScheme.setBearerFormat(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_BEARER_FORMAT));
        securityScheme.setFlows(readOAuthFlows(annotation.value(OpenApiConstants.PROP_FLOWS)));
        securityScheme.setOpenIdConnectUrl(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_OPEN_ID_CONNECT_URL));
        securityScheme.setRef(JandexUtil.refValue(annotation, JandexUtil.RefType.SecurityScheme));
        return securityScheme;
    }

    /**
     * Reads an OAuthFlows annotation into a model.
     * 
     * @param value
     */
    private OAuthFlows readOAuthFlows(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a single @OAuthFlows annotation.");
        AnnotationInstance annotation = value.asNested();
        OAuthFlows flows = new OAuthFlowsImpl();
        flows.setImplicit(readOAuthFlow(annotation.value(OpenApiConstants.PROP_IMPLICIT)));
        flows.setPassword(readOAuthFlow(annotation.value(OpenApiConstants.PROP_PASSWORD)));
        flows.setClientCredentials(readOAuthFlow(annotation.value(OpenApiConstants.PROP_CLIENT_CREDENTIALS)));
        flows.setAuthorizationCode(readOAuthFlow(annotation.value(OpenApiConstants.PROP_AUTHORIZATION_CODE)));
        return flows;
    }

    /**
     * Reads a single OAuthFlow annotation into a model.
     * 
     * @param value
     */
    private OAuthFlow readOAuthFlow(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a single @OAuthFlow annotation.");
        AnnotationInstance annotation = value.asNested();
        OAuthFlow flow = new OAuthFlowImpl();
        flow.setAuthorizationUrl(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_AUTHORIZATION_URL));
        flow.setTokenUrl(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_TOKEN_URL));
        flow.setRefreshUrl(JandexUtil.stringValue(annotation, OpenApiConstants.PROP_REFRESH_URL));
        flow.setScopes(readOAuthScopes(annotation.value(OpenApiConstants.PROP_SCOPES)));
        return flow;
    }

    /**
     * Reads an array of OAuthScope annotations into a Scopes model.
     * 
     * @param value
     */
    private Map<String, String> readOAuthScopes(AnnotationValue value) {
        if (value == null) {
            return null;
        }
        LOG.debug("Processing a list of @OAuthScope annotations.");
        AnnotationInstance[] nestedArray = value.asNestedArray();
        Map<String, String> scopes = new LinkedHashMap<>();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name != null) {
                String description = JandexUtil.stringValue(nested, OpenApiConstants.PROP_DESCRIPTION);
                scopes.put(name, description);
            }
        }
        return scopes;
    }

    /**
     * Reads an array of Extension annotations. The AnnotationValue in this case is
     * an array of Extension annotations. These must be read and converted into a Map.
     * 
     * @param value
     */
    private Map<String, Object> readExtensions(final AnnotationScannerContext context, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> e = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = value.asNestedArray();
        for (AnnotationInstance annotation : nestedArray) {
            String extName = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            e.put(extName, readExtensionValue(context, extName, annotation));
        }
        return e;
    }

    /**
     * Reads a single Extension annotation. If the value must be parsed (as indicated by the
     * 'parseValue' attribute of the annotation), the parsing is delegated to the extensions
     * currently set in the scanner. The default value will parse the string using Jackson.
     *
     * @param annotation Extension annotation
     * @return a Java representation of the 'value' property, either a String or parsed value
     * 
     */
    private Object readExtensionValue(final AnnotationScannerContext context, String name, AnnotationInstance annotation) {
        String extValue = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_VALUE);
        boolean parseValue = JandexUtil.booleanValueWithDefault(annotation, OpenApiConstants.PROP_PARSE_VALUE);
        Object parsedValue = extValue;
        if (parseValue) {
            for (AnnotationScannerExtension e : context.getExtensions()) {
                parsedValue = e.parseExtension(name, extValue);
                if (parsedValue != null) {
                    break;
                }
            }
        }
        return parsedValue;
    }

    private CustomSchemaRegistry getCustomSchemaRegistry(final OpenApiConfig config) {
        if (config == null || config.customSchemaRegistryClass() == null) {
            // Provide default implementation that does nothing
            return (type) -> {
            };
        } else {
            try {
                return (CustomSchemaRegistry) Class.forName(config.customSchemaRegistryClass(), true, getContextClassLoader())
                        .newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                throw new RuntimeException("Failed to create instance of custom schema registry: "
                        + config.customSchemaRegistryClass(), ex);
            }
        }
    }

    private static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return AccessController
                .doPrivileged((PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
    }

    /**
     * Simple enum to indicate whether an @Content annotation being processed is
     * an input or an output.
     * 
     * @author eric.wittmann@gmail.com
     */
    private static enum ContentDirection {
        Input,
        Output,
        Parameter
    }

    private void setCurrentAppPath(String path) {
        this.currentAppPath = path;
    }

    /**
     * Combines the lists passed into a new list, excluding any null lists given.
     * If the resulting list is empty, return null. This method is marked with
     * {@code @SafeVarargs} because the elements of the lists handled generically
     * and the input/output types match.
     * 
     * @param <T> element type of the list
     * @param lists one or more lists to combine
     * @return the combined/merged lists or null if the resulting merged list is empty
     */
    @SafeVarargs
    private static <T> List<T> mergeNullableLists(List<T>... lists) {
        List<T> result = (List<T>) Arrays.stream(lists)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return result.isEmpty() ? null : result;
    }
}
