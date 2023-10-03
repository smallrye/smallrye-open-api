package io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.OpenApiAnnotationScanner;

class EnumNamingTest extends IndexScannerTestBase {

    static void test(Class<?>... classes) throws Exception {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.enum-naming.json", result);
    }

    @Test
    void testEnumNamingDefault() throws Exception {
        @Schema(name = "Bean")
        class Bean {
            @SuppressWarnings("unused")
            DaysOfWeekDefault days;
        }

        test(Bean.class, DaysOfWeekDefault.class);
    }

    @Test
    void testEnumNamingValueMethod() throws Exception {
        @Schema(name = "Bean")
        class Bean {
            @SuppressWarnings("unused")
            DaysOfWeekValue days;
        }

        test(Bean.class, DaysOfWeekValue.class);
    }

    @Test
    void testEnumNamingStrategy() throws Exception {
        @Schema(name = "Bean")
        class Bean {
            @SuppressWarnings("unused")
            DaysOfWeekStrategy days;
        }

        test(Bean.class, DaysOfWeekStrategy.class);
    }

    @Test
    void testEnumNamingProperty() throws Exception {
        @Schema(name = "Bean")
        class Bean {
            @SuppressWarnings("unused")
            DaysOfWeekProperty days;
        }

        test(Bean.class, DaysOfWeekProperty.class);
    }

    @Schema(name = "DaysOfWeek")
    enum DaysOfWeekDefault {
        Monday,
        Tuesday,
        Wednesday,
        Thursday,
        Friday,
        Saturday,
        Sunday
    }

    @Schema(name = "DaysOfWeek")
    enum DaysOfWeekValue {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY;

        @com.fasterxml.jackson.annotation.JsonValue
        public String toValue() {
            String name = name();
            return name.charAt(0) + name.substring(1).toLowerCase();
        }
    }

    @Schema(name = "DaysOfWeek")
    @com.fasterxml.jackson.databind.annotation.EnumNaming(DaysOfWeekShortNaming.class)
    enum DaysOfWeekStrategy {
        MON("Monday"),
        TUE("Tuesday"),
        WED("Wednesday"),
        THU("Thursday"),
        FRI("Friday"),
        SAT("Saturday"),
        SUN("Sunday");

        final String displayName;

        private DaysOfWeekStrategy(String displayName) {
            this.displayName = displayName;
        }
    }

    public static class DaysOfWeekShortNaming implements com.fasterxml.jackson.databind.EnumNamingStrategy {
        @Override
        public String convertEnumToExternalName(String enumName) {
            return DaysOfWeekStrategy.valueOf(enumName).displayName;
        }
    }

    @Schema(name = "DaysOfWeek")
    enum DaysOfWeekProperty {
        @com.fasterxml.jackson.annotation.JsonProperty("Monday")
        MONDAY,
        @com.fasterxml.jackson.annotation.JsonProperty("Tuesday")
        TUESDAY,
        @com.fasterxml.jackson.annotation.JsonProperty("Wednesday")
        WEDNESDAY,
        @com.fasterxml.jackson.annotation.JsonProperty("Thursday")
        THURSDAY,
        @com.fasterxml.jackson.annotation.JsonProperty("Friday")
        FRIDAY,
        @com.fasterxml.jackson.annotation.JsonProperty("Saturday")
        SATURDAY,
        @com.fasterxml.jackson.annotation.JsonProperty("Sunday")
        SUNDAY
    }

}
