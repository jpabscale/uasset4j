plugins {
    kotlin("jvm")
    application
    id("com.gradleup.shadow")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

// Emit JVM 21 bytecode (class-file version 65) while compiling with the
// installed JDK, so the jar runs on any JVM 21+ (incl. automod's Zulu 25).
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

application {
    mainClass.set("com.github.jpabscale.uasset4j.uassetcli.MainKt")
}

tasks.shadowJar {
    archiveFileName.set("uassetcli.jar")
}

dependencies {
    implementation(project(":uassetapi"))
}
