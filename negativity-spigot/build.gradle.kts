plugins {
    id("com.gradleup.shadow")
}

dependencies {
    api(project(":negativity-common"))
    api(project(":negativity-proxy-common"))

    // Paper API (compatible with Purpur and other Paper forks)
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // Soft dependencies
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")

    // Shaded runtime deps
    implementation("org.yaml:snakeyaml:2.2")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
        expand(props)
    }
}

tasks.shadowJar {
    archiveBaseName.set("Negativity")
    archiveClassifier.set("")
    mergeServiceFiles()

    // Relocate only shaded libraries — never relocate API packages
    relocate("org.yaml.snakeyaml", "com.elikill58.negativity.libs.snakeyaml")
    relocate("org.sqlite", "com.elikill58.negativity.libs.sqlite")

    dependencies {
        exclude(dependency("io.papermc.paper:paper-api:.*"))
        exclude(dependency("com.comphenix.protocol:ProtocolLib:.*"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    // Thin jar unused — the shadow jar is the distribution artifact
    enabled = false
}
