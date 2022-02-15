package test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings(value = "unused")
public class BVTestContainer {

    @NotNull
    @NotEmpty
    @Size(max = 20)
    List<String> arrayListNotNullAndNotEmptyAndMaxItems;
    @NotEmpty
    @Size(min = 5, max = 20)
    List<String> arrayListNullableAndMinItemsAndMaxItems;
    /**********************************************************************/
    @NotNull
    @NotEmpty
    @Size(max = 20)
    Map<String, String> mapObjectNotNullAndNotEmptyAndMaxProperties;
    @NotEmpty
    @Size(min = 5, max = 20)
    Map<String, @NotNull String> mapObjectNullableAndMinPropertiesAndMaxProperties;
    /**********************************************************************/
    @DecimalMax(value = "200.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal decimalMaxBigDecimalPrimaryDigits;
    private BigDecimal decimalMaxBigDecimalNoConstraint;
    @DecimalMax(value = "Invalid BigDecimal value")
    private BigDecimal decimalMaxBigDecimalInvalidValue;
    @DecimalMax(value = "201.0", inclusive = false, groups = {})
    @Digits(integer = 3, fraction = 1)
    private BigDecimal decimalMaxBigDecimalExclusiveDigits;
    @DecimalMax(value = "201.00", inclusive = true, groups = Default.class)
    private BigDecimal decimalMaxBigDecimalInclusive;
    /**********************************************************************/
    @DecimalMin(value = "10.0")
    private BigDecimal decimalMinBigDecimalPrimary;
    private BigDecimal decimalMinBigDecimalNoConstraint;
    @DecimalMin(value = "Invalid BigDecimal value")
    private BigDecimal decimalMinBigDecimalInvalidValue;
    @DecimalMin(value = "9.00", inclusive = false)
    @Digits(integer = 1, fraction = 2)
    private BigDecimal decimalMinBigDecimalExclusiveDigits;
    @DecimalMin(value = "9.00", inclusive = true)
    private BigDecimal decimalMinBigDecimalInclusive;
    /**********************************************************************/
    @Positive
    @Max(value = 1000)
    private Long integerPositiveNotZeroMaxValue;
    @PositiveOrZero
    @Max(value = 999)
    private Integer integerPositiveOrZeroMaxValue;
    @Negative
    @Min(value = -1_000_000)
    private Long integerNegativeNotZeroMinValue;
    @NegativeOrZero
    @Min(value = -999)
    private Integer integerNegativeOrZeroMinValue;
    /**********************************************************************/
    @NotNull
    @NotBlank
    private String stringNotBlankNotNull;
    @Digits(integer = 8, fraction = 10)
    @NotBlank
    private String stringNotBlankDigits;
    @NotEmpty
    @Size(max = 2000)
    private String stringNotEmptyMaxSize;
    @NotEmpty
    @Size(min = 100, max = 2000)
    private String stringNotEmptySizeRange;
    /**********************************************************************/
    @NotNull
    private boolean booleanNotNull;
    @JsonProperty(required = true)
    private String jacksonRequiredTrueString;
    @JsonProperty
    private String jacksonDefaultString;

}
