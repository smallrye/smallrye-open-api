package io.smallrye.openapi.runtime.scanner.dataobject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;

import io.smallrye.openapi.api.constants.KotlinConstants;
import io.smallrye.openapi.internal.models.media.SchemaSupport;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import kotlinx.metadata.Attributes;
import kotlinx.metadata.KmClass;
import kotlinx.metadata.KmConstructor;
import kotlinx.metadata.KmFunction;
import kotlinx.metadata.KmProperty;
import kotlinx.metadata.KmValueParameter;
import kotlinx.metadata.jvm.JvmExtensionsKt;
import kotlinx.metadata.jvm.JvmMethodSignature;
import kotlinx.metadata.jvm.KotlinClassMetadata;

public class KotlinMetadataScanner {

    private final AnnotationScannerContext context;
    private final Exception initException;
    private final AtomicBoolean exceptionLogged = new AtomicBoolean(false);
    private final Map<ClassInfo, KmClass> cache;
    private final IndexView metaIndex;

    public KotlinMetadataScanner(AnnotationScannerContext context) {
        this.context = context;
        IndexView singletonIndex = null;
        Exception iniException = null;

        try {
            Class<?> metadataAnnoClass = Class.forName("kotlin.Metadata");
            Class.forName("kotlinx.metadata.Attributes");
            singletonIndex = Index.of(metadataAnnoClass);
        } catch (ClassNotFoundException | IOException e) {
            iniException = e;
        }

        this.initException = iniException;
        this.metaIndex = singletonIndex;
        this.cache = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<ClassInfo, KmClass> eldest) {
                return size() > 10;
            }
        };
    }

    public void applyMetadata(AnnotationTarget target,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {

        ClassInfo declaringClass = JandexUtil.declaringClass(context, target);

        if (declaringClass == null || !declaringClass.hasAnnotation(KotlinConstants.METADATA)) {
            return;
        }

        if (initException != null) {
            if (!exceptionLogged.getAndSet(true)) {
                DataObjectLogging.logger.exceptionInitializingKotlinMetadataScanner(initException);
            }

            return;
        }

        try {
            KmClass kmClass = cache.computeIfAbsent(declaringClass, this::kotlinClassMetadata);

            if (kmClass == null) {
                return;
            }

            if (target.kind() == Kind.FIELD) {
                applyPropertyMetadata(target.asField(), kmClass, schema, propertyKey, handler);
            } else if (target.kind() == Kind.METHOD_PARAMETER) {
                applyParameterMetadata(target.asMethodParameter(), kmClass, schema, propertyKey, handler);
            }
        } catch (Exception e) {
            DataObjectLogging.logger.exceptionScanningKotlinMetadata(e);
        }
    }

    private void applyPropertyMetadata(FieldInfo field,
            KmClass kmClass,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {

        for (KmConstructor constructor : kmClass.getConstructors()) {
            if (!Attributes.isSecondary(constructor)) {
                for (KmValueParameter param : constructor.getValueParameters()) {
                    if (param.getName().equals(field.name())) {
                        applyMetadata(field, param, schema, propertyKey, handler);
                        return;
                    }
                }
            }
        }

        for (KmProperty property : kmClass.getProperties()) {
            if (property.getName().equals(field.name())) {
                KmValueParameter param = property.getSetterParameter();
                applyMetadata(field, param, schema, propertyKey, handler);
                return;
            }
        }
    }

    private void applyParameterMetadata(MethodParameterInfo methodParam,
            KmClass kmClass,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {
        MethodInfo method = methodParam.method();
        String methodName = method.name();
        String methodDescriptor = method.descriptor();

        for (KmFunction function : kmClass.getFunctions()) {
            if (function.getName().equals(methodName)) {
                JvmMethodSignature signature = JvmExtensionsKt.getSignature(function);

                if (signature.getDescriptor().equals(methodDescriptor)) {
                    KmValueParameter valueParam = function.getValueParameters().get(methodParam.position());
                    applyMetadata(methodParam, valueParam, schema, propertyKey, handler);
                    return;
                }
            }
        }
    }

    private void applyMetadata(AnnotationTarget target,
            KmValueParameter param,
            Schema schema,
            String propertyKey,
            RequirementHandler handler) {
        if (!Attributes.getDeclaresDefaultValue(param)) {
            handler.setRequired(target, propertyKey);
        }

        if (Attributes.isNullable(param.getType()) && SchemaSupport.getNullable(schema) == null) {
            SchemaSupport.setNullable(schema, Boolean.TRUE);
        }
    }

    private KmClass kotlinClassMetadata(ClassInfo declaringClass) {
        AnnotationInstance annotation = declaringClass.declaredAnnotation(KotlinConstants.METADATA);

        var kotlinMeta = KotlinClassMetadata.readLenient(
                KotlinMetadataProxy.create(metaIndex, annotation));

        if (kotlinMeta instanceof KotlinClassMetadata.Class) {
            return ((KotlinClassMetadata.Class) kotlinMeta).getKmClass();
        }

        return null;
    }

    private static class KotlinMetadataProxy {
        static kotlin.Metadata create(IndexView metaIndex, AnnotationInstance annotation) {
            return new kotlin.Metadata() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return kotlin.Metadata.class;
                }

                @Override
                public int[] bv() {
                    return annotation.valueWithDefault(metaIndex, "bv").asIntArray();
                }

                @Override
                public String[] d1() {
                    return annotation.valueWithDefault(metaIndex, "d1").asStringArray();
                }

                @Override
                public String[] d2() {
                    return annotation.valueWithDefault(metaIndex, "d2").asStringArray();
                }

                @Override
                public int k() {
                    return annotation.valueWithDefault(metaIndex, "k").asInt();
                }

                @Override
                public int[] mv() {
                    return annotation.valueWithDefault(metaIndex, "mv").asIntArray();
                }

                @Override
                public String pn() {
                    return annotation.valueWithDefault(metaIndex, "pn").asString();
                }

                @Override
                public int xi() {
                    return annotation.valueWithDefault(metaIndex, "xi").asInt();
                }

                @Override
                public String xs() {
                    return annotation.valueWithDefault(metaIndex, "xs").asString();
                }
            };
        }
    }
}
