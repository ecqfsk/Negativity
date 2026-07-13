plugins {
    id("com.gradleup.shadow")
}

dependencies {
    api(project(":negativity-api"))

    // Config / YAML — use published artifacts instead of vendored sources
    implementation("org.yaml:snakeyaml:2.2")

    // SQLite for async violation logs (driver shaded into platform jars)
    compileOnly("org.xerial:sqlite-jdbc:3.45.3.0")

    // Testing helpers live in negativity-tests; keep common free of test frameworks
}

tasks.jar {
    archiveBaseName.set("negativity-common")
}

tasks.shadowJar {
    archiveBaseName.set("negativity-common")
    archiveClassifier.set("all")
    mergeServiceFiles()
}
