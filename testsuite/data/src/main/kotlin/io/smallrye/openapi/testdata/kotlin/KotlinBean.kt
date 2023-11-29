package io.smallrye.openapi.testdata.kotlin

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema
data class KotlinBean (
    @field:Schema(type = SchemaType.INTEGER, implementation = Long::class, name = "id")
    val id: KotlinLongValue? = null,
    val name: String? = null,
    val description: String = "",
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
