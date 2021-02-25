import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Paths

plugins {
    kotlin("jvm") version "1.4.30"
    id("gg.jte.gradle") version("1.8.0-SNAPSHOT")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.junit.jupiter:junit-jupiter:5.4.2")
    implementation("gg.jte:jte-runtime:1.8.0-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

tasks.generateJte {
    sourceDirectory = Paths.get(project.projectDir.absolutePath, "src", "main", "jte")
    contentType = gg.jte.ContentType.Html
}

sourceSets {
    main {
        java.srcDirs(tasks.generateJte.get().targetDirectory)
    }
}

tasks.compileJava {
    dependsOn(tasks.generateJte)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}