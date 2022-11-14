java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.1.0"
    id("com.github.vlsi.jandex") version "1.82"
}

repositories {
    mavenCentral()
    maven {
        url = uri("file:./target/dependencies")
    }
}

dependencies {
    implementation("io.smallrye:smallrye-open-api-core:$version")
    implementation("io.smallrye:smallrye-open-api-jaxrs:$version")
    implementation("io.smallrye:smallrye-open-api-spring:$version")
    implementation("io.smallrye:smallrye-open-api-vertx:$version")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation(gradleTestKit())
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
}

gradlePlugin {
    val smallryePlugin by plugins.creating {
        id = "io.smallrye.openapi"
        implementationClass = "io.smallrye.openapi.gradleplugin.SmallryeOpenApiPlugin"
        description = "Official Smallrye Open API Gradle plugin"
    }
}

pluginBundle {
    website = "https://github.com/smallrye/smallrye-open-api/"
    vcsUrl = "https://github.com/smallrye/smallrye-open-api/"
    tags = listOf("openapi", "smallrye")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

group = "io.smallrye"
