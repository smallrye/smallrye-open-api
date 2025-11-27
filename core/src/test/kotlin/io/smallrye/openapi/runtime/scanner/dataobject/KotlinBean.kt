package io.smallrye.openapi.runtime.scanner.dataobject

class KotlinBean @JvmOverloads constructor(
   val nonnullableNoDefault: String,
   val nonnullableWithDefault: String = "default",
   val nullableNoDefault: String?,
   val nullableWithDefault: String? = null,
) {

    constructor (nonnullableNoDefault: String) : this(nonnullableNoDefault, "default", null, null) {
    }

    var classBodyNullableWithDefault: String? = null
        set(value) {
            field = value
        }

    companion object {
        val nonPropertyField = "hello"
    }
}
