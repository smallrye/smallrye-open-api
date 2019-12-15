package io.smallrye.openapi.api.util;

import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.OASConfig;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;

public class ArchiveUtilTest {

    @Test
    public void givenAnExcludedPackage_whenArchiveToIndex_thenTheExcludedPackageIsOmittedFromIndex() {
        // GIVEN
        Archive<?> archive = ShrinkWrap.create(JavaArchive.class, "dummy.jar");
        archive.add((Asset) () -> getClass().getClassLoader()
                .getResourceAsStream("io/smallrye/openapi/api/util/ArchiveUtil.class"), new ArchivePath() {
                    @Override
                    public String get() {
                        return "/io/smallrye/openapi/api/util/ArchiveUtil.class";
                    }

                    @Override
                    public ArchivePath getParent() {
                        return null;
                    }

                    @Override
                    public int compareTo(ArchivePath o) {
                        return 0;
                    }
                });

        OpenApiConfig openApiConfig = customConfig();

        // WHEN
        IndexView indexView = ArchiveUtil.archiveToIndex(openApiConfig, archive);

        // THEN
        assertNull(indexView.getClassByName(DotName.createSimple("io.smallrye.openapi.api.util.ArchiveUtil")));
    }

    public static OpenApiConfig customConfig() {
        return new OpenApiConfigImpl(new Config() {
            @Override
            public <T> T getValue(String propertyName, Class<T> propertyType) {
                return null;
            }

            @Override
            public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
                if (OASConfig.SCAN_EXCLUDE_PACKAGES.equals(propertyName) && String.class.equals(propertyType)) {
                    return (Optional<T>) Optional.of("io.smallrye.openapi.api.util");
                }
                return Optional.empty();
            }

            @Override
            public Iterable<String> getPropertyNames() {
                return Collections.emptyList();
            }

            @Override
            public Iterable<ConfigSource> getConfigSources() {
                return Collections.emptyList();
            }
        });
    }
}
