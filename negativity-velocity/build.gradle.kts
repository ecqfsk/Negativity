plugins {
    id("com.gradleup.shadow")
}

dependencies {
    api(project(":negativity-proxy-common"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    implementation("org.yaml:snakeyaml:2.2")
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filesMatching("velocity-plugin.json") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveBaseName.set("Negativity-Velocity")
    archiveClassifier.set("")
    mergeServiceFiles()
    relocate("org.yaml.snakeyaml", "com.elikill58.negativity.libs.snakeyaml")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    enabled = false
}
