java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.1.0"
    id("com.github.vlsi.jandex") version "1.86"
}

repositories {
    mavenCentral()
    maven {
        url = uri("file:./target/dependencies")
    }
}

dependencies {
    val versionJackson: String by project
    val versionJunit5: String by project

    implementation("io.smallrye:smallrye-open-api-core:$version")
    implementation("io.smallrye:smallrye-open-api-jaxrs:$version")
    implementation("io.smallrye:smallrye-open-api-spring:$version")
    implementation("io.smallrye:smallrye-open-api-vertx:$version")

    testImplementation("org.junit.jupiter:junit-jupiter:${versionJunit5}")
    testImplementation(gradleTestKit())
    testImplementation("org.assertj:assertj-core:3.24.1")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:${versionJackson}")
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

tasks.named("pluginUnderTestMetadata") {
    dependsOn("processJandexIndex")
}

group = "io.smallrye"
