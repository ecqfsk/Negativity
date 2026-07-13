plugins {
    id("com.gradleup.shadow")
}

dependencies {
    api(project(":negativity-proxy-common"))
    compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
    implementation("org.yaml:snakeyaml:2.2")
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filesMatching("bungee.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveBaseName.set("Negativity-Bungee")
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
