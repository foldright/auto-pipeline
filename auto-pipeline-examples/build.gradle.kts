import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.7.0"
    kotlin("jvm") version kotlinVersion
}

group = "com.foldright.auto-pipeline"
version = "0.3.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")

    /*
     * annotation processor dependencies
     */
    compileOnly("com.foldright.auto-pipeline:auto-pipeline-annotations:$version")
    annotationProcessor("com.foldright.auto-pipeline:auto-pipeline-processor:$version")

    /*
     * testing dependencies
     */
    testImplementation(kotlin("test"))
    // https://kotest.io/docs/quickstart
    val kotestVersion = "5.3.1"
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")

    /*
     * dependency constraint by using bom
     */
    val kotlinVersion = project.getKotlinPluginVersion()
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:${kotlinVersion}"))
    implementation(platform("org.junit:junit-bom:5.8.2"))
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.17.2"))
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = java.sourceCompatibility.toString()
}

// https://kotest.io/docs/quickstart
tasks.test<Test> {
    useJUnitPlatform()
}
