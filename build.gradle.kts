plugins {
    java
    id("com.gradleup.shadow") version "8.3.5" apply false
}

allprojects {
    group = providers.gradleProperty("group").orElse("com.elikill58.negativity").get()
    version = providers.gradleProperty("version").orElse("2.0.0-SNAPSHOT").get()

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.viaversion.com")
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "java-library")

    val javaVersion = providers.gradleProperty("javaVersion").orElse("21").get().toInt()

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(javaVersion)
    }

    tasks.withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    tasks.withType<ProcessResources>().configureEach {
        filteringCharset = "UTF-8"
    }

    dependencies {
        "compileOnly"("org.jetbrains:annotations:24.1.0")
    }

    tasks.test {
        useJUnitPlatform()
    }
}
