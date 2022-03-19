plugins {
    `java-library`
    `maven-publish`

    id("xyz.jpenilla.run-paper") version "1.0.6"
}

group = "tk.booky"
version = "1.0.0"

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://jitpack.io/")
}

dependencies {
    api("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    api("dev.jorel.CommandAPI:commandapi-core:6.5.4")
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        vendor.set(JvmVendorSpec.ADOPTIUM)
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.toLowerCase()
        from(components["java"])
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    runServer {
        minecraftVersion.set("1.18.2")
    }
}
