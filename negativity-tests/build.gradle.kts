plugins {
    java
}

dependencies {
    testImplementation(project(":negativity-api"))
    testImplementation(project(":negativity-common"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
