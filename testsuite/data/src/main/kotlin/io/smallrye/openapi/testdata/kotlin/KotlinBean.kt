package io.smallrye.openapi.testdata.kotlin

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema
data class KotlinBean (
    @field:Schema(type = SchemaType.INTEGER, implementation = Long::class, name = "id")
    val id: KotlinLongValue? = null,
    val name: String? = null,
    val description: String = ""
)
