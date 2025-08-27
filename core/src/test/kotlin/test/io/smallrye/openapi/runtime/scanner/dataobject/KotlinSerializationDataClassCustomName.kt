package test.io.smallrye.openapi.runtime.scanner.dataobject

data class KotlinSerializationDataClassCustomName(
    @kotlinx.serialization.SerialName(value = "theName")
    var name: String? = null,
    var name2: String? = null,
)
