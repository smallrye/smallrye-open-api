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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.media.Content;
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
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.constants.JaxRsConstants;
import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.constants.RestEasyConstants;
import io.smallrye.openapi.api.constants.SecurityConstants;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.api.models.OperationImpl;
import io.smallrye.openapi.api.models.PathItemImpl;
import io.smallrye.openapi.api.models.PathsImpl;
import io.smallrye.openapi.api.models.media.ContentImpl;
import io.smallrye.openapi.api.models.media.MediaTypeImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.models.parameters.RequestBodyImpl;
import io.smallrye.openapi.api.models.responses.APIResponseImpl;
import io.smallrye.openapi.api.models.security.SecurityRequirementImpl;
import io.smallrye.openapi.api.reader.CallbackReader;
import io.smallrye.openapi.api.reader.CurrentContentTypes;
import io.smallrye.openapi.api.reader.DefinitionReader;
import io.smallrye.openapi.api.reader.ExtensionReader;
import io.smallrye.openapi.api.reader.ParameterReader;
import io.smallrye.openapi.api.reader.RequestBodyReader;
import io.smallrye.openapi.api.reader.ResponseObjectReader;
import io.smallrye.openapi.api.reader.SecurityReader;
import io.smallrye.openapi.api.reader.SecuritySchemeReader;
import io.smallrye.openapi.api.reader.ServerReader;
import io.smallrye.openapi.api.reader.TagReader;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.ParameterProcessor;
import io.smallrye.openapi.runtime.scanner.PathMaker;
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

    private String currentSecurityScheme;
    private List<OAuthFlow> currentFlows;
    private String[] resourceRolesAllowed;

    @Override
    public String getName() {
        return "JAX-RS";
    }

    @Override
    public OpenAPI scan(final AnnotationScannerContext context, OpenAPI oai) {
        // Get all jax-rs applications and convert them to OAI models (and merge them into a single one)
        Collection<ClassInfo> applications = context.getIndex().getAllKnownSubclasses(JAXRS_APPLICATION);

        for (ClassInfo classInfo : applications) {
            OpenAPI jaxRsApplicationToOpenApi = jaxRsApplicationToOpenApi(context, classInfo);
            oai = MergeUtil.merge(oai, jaxRsApplicationToOpenApi);
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
    private OpenAPI jaxRsApplicationToOpenApi(final AnnotationScannerContext context, ClassInfo applicationClass) {
        OpenAPI oai = new OpenAPIImpl();
        oai.setOpenapi(OpenApiConstants.OPEN_API_VERSION);

        // Get the @ApplicationPath info and save it for later (also support @Path which seems nonstandard but common).
        ////////////////////////////////////////
        AnnotationInstance appPathAnno = JandexUtil.getClassAnnotation(applicationClass,
                JaxRsConstants.APPLICATION_PATH);
        if (appPathAnno == null || context.getConfig().applicationPathDisable()) {
            appPathAnno = JandexUtil.getClassAnnotation(applicationClass, JaxRsConstants.PATH);
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
                MPOpenApiConstants.OPEN_API_DEFINITION);
        if (openApiDefAnno != null) {
            DefinitionReader.processDefinition(context, oai, openApiDefAnno);
        }

        // Process @SecurityScheme annotations
        ////////////////////////////////////////
        List<AnnotationInstance> securitySchemeAnnotations = JandexUtil.getRepeatableAnnotation(applicationClass,
                MPOpenApiConstants.SECURITY_SCHEME, MPOpenApiConstants.SECURITY_SCHEMES);
        for (AnnotationInstance annotation : securitySchemeAnnotations) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SECURITY_SCHEME_NAME);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                SecurityScheme securityScheme = SecuritySchemeReader.readSecurityScheme(annotation);
                Components components = ModelUtil.components(oai);
                components.addSecurityScheme(name, securityScheme);
            }
        }

        // Process @Server annotations
        ///////////////////////////////////
        List<AnnotationInstance> serverAnnotations = JandexUtil.getRepeatableAnnotation(applicationClass,
                MPOpenApiConstants.SERVER, MPOpenApiConstants.SERVERS);
        for (AnnotationInstance annotation : serverAnnotations) {
            Server server = ServerReader.readServer(annotation);
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
                MPOpenApiConstants.SECURITY_SCHEME, MPOpenApiConstants.SECURITY_SCHEMES);
        for (AnnotationInstance annotation : securitySchemeAnnotations) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_SECURITY_SCHEME_NAME);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                SecurityScheme securityScheme = SecuritySchemeReader.readSecurityScheme(annotation);
                Components components = ModelUtil.components(openApi);
                components.addSecurityScheme(name, securityScheme);
            }
        }

        // Process tags (both declarations and references)
        ////////////////////////////////////////
        Set<String> tagRefs = processTags(openApi, resourceClass, false);

        addScopes(TypeUtil.getAnnotationValue(resourceClass, SecurityConstants.DECLARE_ROLES));
        resourceRolesAllowed = TypeUtil.getAnnotationValue(resourceClass, SecurityConstants.ROLES_ALLOWED);
        addScopes(resourceRolesAllowed);

        // Process exception mapper to auto generate api response based on method exceptions
        ////////////////////////////////////////
        Map<DotName, AnnotationInstance> exceptionAnnotationMap = processExceptionMappers(context);

        // Now find and process the operation methods
        ////////////////////////////////////////
        for (MethodInfo methodInfo : getResourceMethods(context, resourceClass)) {
            final AtomicInteger resourceCount = new AtomicInteger(0);

            JaxRsConstants.HTTP_METHODS
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

            if (resourceCount.get() == 0 && methodInfo.hasAnnotation(JaxRsConstants.PATH)) {
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
                .getKnownDirectImplementors(JaxRsConstants.EXCEPTION_MAPPER);

        for (ClassInfo classInfo : exceptionMappers) {
            DotName exceptionDotName = classInfo.interfaceTypes()
                    .stream()
                    .filter(it -> it.name().equals(JaxRsConstants.EXCEPTION_MAPPER))
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

            if (toResponseMethod.hasAnnotation(MPOpenApiConstants.API_RESPONSE)) {
                AnnotationInstance apiResponseAnnotation = toResponseMethod
                        .annotation(MPOpenApiConstants.API_RESPONSE);
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

            Function<AnnotationInstance, Parameter> reader = (t) -> {
                return ParameterReader.readParameter(context, t);
            };

            ParameterProcessor.ResourceParameters params = ParameterProcessor.process(context.getIndex(), resourceClass, method,
                    reader, context.getExtensions());

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
        if (method.hasAnnotation(MPOpenApiConstants.OPERATION)) {
            AnnotationInstance operationAnno = method.annotation(MPOpenApiConstants.OPERATION);
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
        String[] currentConsumes = getMediaTypes(method, JaxRsConstants.CONSUMES);
        String[] currentProduces = getMediaTypes(method, JaxRsConstants.PRODUCES);
        CurrentContentTypes.register(currentConsumes, currentProduces);

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
        Function<AnnotationInstance, Parameter> reader = (t) -> {
            return ParameterReader.readParameter(context, t);
        };

        ParameterProcessor.ResourceParameters params = ParameterProcessor.process(context.getIndex(), resourceClass, method,
                reader, context.getExtensions());

        operation.setParameters(params.getOperationParameters());
        pathItem.setParameters(mergeNullableLists(locatorPathParameters, params.getPathItemParameters()));

        // Process any @RequestBody annotation
        /////////////////////////////////////////
        // note: the @RequestBody annotation can be found on a method argument *or* on the method
        RequestBody requestBody = null;

        List<AnnotationInstance> requestBodyAnnotations = JandexUtil.getRepeatableAnnotation(method,
                MPOpenApiConstants.REQUEST_BODY, null);
        for (AnnotationInstance annotation : requestBodyAnnotations) {
            requestBody = RequestBodyReader.readRequestBody(context, annotation);
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

                    if (RestEasyConstants.MULTIPART_INPUTS.contains(requestBodyType.name())) {
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
                MPOpenApiConstants.API_RESPONSE, MPOpenApiConstants.API_RESPONSES);
        for (AnnotationInstance annotation : apiResponseAnnotations) {
            addApiReponseFromAnnotation(context, annotation, operation);
        }
        /*
         * If there is no response from annotations, try to create one from the method return value.
         * Do not generate a response if the app has used an empty @ApiResponses annotation. This
         * provides a way for the application to indicate that responses will be supplied some other
         * way (i.e. static file).
         */
        AnnotationInstance apiResponses = method.annotation(MPOpenApiConstants.API_RESPONSES);
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
                MPOpenApiConstants.SECURITY_REQUIREMENT,
                MPOpenApiConstants.SECURITY_REQUIREMENTS);
        securityRequirementAnnotations
                .addAll(JandexUtil.getRepeatableAnnotation(resourceClass, MPOpenApiConstants.SECURITY_REQUIREMENT,
                        MPOpenApiConstants.SECURITY_REQUIREMENTS));
        for (AnnotationInstance annotation : securityRequirementAnnotations) {
            SecurityRequirement requirement = SecurityReader.readSecurityRequirement(annotation);
            if (requirement != null) {
                operation.addSecurityRequirement(requirement);
            }
        }

        // Process @Callback annotations
        /////////////////////////////////////////
        List<AnnotationInstance> callbackAnnotations = JandexUtil.getRepeatableAnnotation(method,
                MPOpenApiConstants.CALLBACK, MPOpenApiConstants.CALLBACKS);
        Map<String, Callback> callbacks = new LinkedHashMap<>();
        for (AnnotationInstance annotation : callbackAnnotations) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(annotation)) {
                name = JandexUtil.nameFromRef(annotation);
            }
            if (name != null) {
                callbacks.put(name, CallbackReader.readCallback(context, annotation));
            }

            if (!callbacks.isEmpty()) {
                operation.setCallbacks(callbacks);
            }
        }

        // Process @Server annotations
        ///////////////////////////////////
        List<AnnotationInstance> serverAnnotations = JandexUtil.getRepeatableAnnotation(method,
                MPOpenApiConstants.SERVER, MPOpenApiConstants.SERVERS);
        if (serverAnnotations.isEmpty()) {
            serverAnnotations.addAll(JandexUtil.getRepeatableAnnotation(method.declaringClass(),
                    MPOpenApiConstants.SERVER, MPOpenApiConstants.SERVERS));
        }
        for (AnnotationInstance annotation : serverAnnotations) {
            Server server = ServerReader.readServer(annotation);
            operation.addServer(server);
        }

        // Process @Extension annotations
        ///////////////////////////////////
        List<AnnotationInstance> extensionAnnotations = JandexUtil.getRepeatableAnnotation(method,
                MPOpenApiConstants.EXTENSION, MPOpenApiConstants.EXTENSIONS);
        if (extensionAnnotations.isEmpty()) {
            extensionAnnotations.addAll(JandexUtil.getRepeatableAnnotation(method.declaringClass(),
                    MPOpenApiConstants.EXTENSION, MPOpenApiConstants.EXTENSIONS));
        }
        for (AnnotationInstance annotation : extensionAnnotations) {
            String name = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_NAME);
            operation.addExtension(name, ExtensionReader.readExtensionValue(context, name, annotation));
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
        APIResponse response = ResponseObjectReader.readResponse(context, apiResponseAnnotation);
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
        if (!TypeUtil.hasAnnotation(target, MPOpenApiConstants.TAG) &&
                !TypeUtil.hasAnnotation(target, MPOpenApiConstants.TAGS)) {
            return nullWhenMissing ? null : Collections.emptySet();
        }

        Set<String> tags = new LinkedHashSet<>();
        List<AnnotationInstance> tagAnnos = JandexUtil.getRepeatableAnnotation(target,
                MPOpenApiConstants.TAG,
                MPOpenApiConstants.TAGS);

        for (AnnotationInstance ta : tagAnnos) {
            if (JandexUtil.isRef(ta)) {
                tags.add(JandexUtil.value(ta, OpenApiConstants.PROP_REF));
            } else {
                Tag tag = TagReader.readTag(ta);

                if (tag.getName() != null) {
                    ModelUtil.addTag(openApi, tag);
                    tags.add(tag.getName());
                }
            }
        }

        String[] refs = TypeUtil.getAnnotationValue(target, MPOpenApiConstants.TAGS,
                OpenApiConstants.PROP_REFS);

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
                    .anyMatch(JaxRsConstants.ASYNC_RESPONSE::equals);

            if (method.hasAnnotation(JaxRsConstants.POST)) {
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
            if (!returnType.name().equals(JaxRsConstants.RESPONSE) &&
                    (ModelUtil.responses(operation).getAPIResponse(code) == null ||
                            ModelUtil.responses(operation).getAPIResponse(code).getContent() == null)) {

                Schema schema;

                if (RestEasyConstants.MULTIPART_OUTPUTS.contains(returnType.name())) {
                    schema = new SchemaImpl();
                    schema.setType(Schema.SchemaType.OBJECT);
                } else {
                    schema = SchemaFactory.typeToSchema(context.getIndex(), returnType, context.getExtensions());
                }

                ContentImpl content = new ContentImpl();
                String[] produces = CurrentContentTypes.getCurrentProduces();

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
            String[] rolesAllowed = TypeUtil.getAnnotationValue(method, SecurityConstants.ROLES_ALLOWED);

            if (rolesAllowed != null) {
                addScopes(rolesAllowed);
                addRolesAllowed(operation, rolesAllowed);
            } else if (this.resourceRolesAllowed != null) {
                boolean denyAll = TypeUtil.getAnnotation(method, SecurityConstants.DENY_ALL) != null;
                boolean permitAll = TypeUtil.getAnnotation(method, SecurityConstants.PERMIT_ALL) != null;

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

    private static final DotName JAXRS_APPLICATION = DotName.createSimple(Application.class.getName());

}
