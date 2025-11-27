package io.smallrye.openapi.runtime.scanner.dataobject

class KotlinResource {
    fun greet0(
      p1: String,             // required = true;  nullable = false
      p2: String = "default", // required = false; nullable = false
      p3: String?,            // required = true;  nullable = true
      p4: String? = null,     // required = false; nullable = true
    ): String {
        return "Hello" + p1
    }

    fun greet1(): String {
        return "Hello World"
    }

    fun greet1(
      p1: String,             // required = true;  nullable = false
      p2: String = "default", // required = false; nullable = false
      p3: String?,            // required = true;  nullable = true
      p4: String? = null,     // required = false; nullable = true
    ): String {
        return "Hello" + p1 + ", " + p2
    }
}
