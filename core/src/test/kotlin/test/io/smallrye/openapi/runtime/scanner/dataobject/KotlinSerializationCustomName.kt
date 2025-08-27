package test.io.smallrye.openapi.runtime.scanner.dataobject

class KotlinSerializationCustomName {
    @kotlinx.serialization.SerialName(value = "theName")
    var name: String? = null
    var name2: String? = null
}
