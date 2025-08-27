package io.smallrye.openapi.testdata.kotlin

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema
data class KotlinBean (
    @field:Schema(type = SchemaType.INTEGER, implementation = Long::class, name = "id")
    val id: KotlinLongValue? = null,
    val name: String? = null,
    val description: String = "",
    val nestedCollection: Map<String, Set<Double>>,
    @SerialName("actualCustomName")
    val customName: String = "",
    @Required
    val requiredValue: String = ""
)

@Deprecated("1.0.0")
@Schema(name = "DeprecatedKotlinBean")
data class DeprecatedKotlinBean(
    val id: String
)

@java.lang.Deprecated(since = "1.0.0")
@Schema(name = "DeprecatedKotlinBean")
data class JavaDeprecatedKotlinBean(
    val id: String
)
