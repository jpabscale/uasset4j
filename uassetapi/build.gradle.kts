plugins {
    kotlin("jvm")
    `maven-publish`
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

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.qyntrax:unzstd:0.1.0")

    testImplementation(platform("org.junit:junit-bom:5.12.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // zstd-jni is test-only: it is the parity oracle for our pure-JVM Zstd seam
    // (both wrap libzstd, matching the C# ZstdSharp). Never a runtime dependency.
    testImplementation("com.github.luben:zstd-jni:1.5.7-4")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "uasset4j"
            pom {
                name.set("uasset4j")
                description.set("Kotlin/JVM port of the UAssetAPI asset library")
                url.set("https://github.com/jpabscale/uasset4j")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
    // Consumed via JitPack (https://jitpack.io), which builds the repo on demand:
    //   //> using repository https://jitpack.io
    //   //> using dep com.github.jpabscale:uasset4j:33ef77e
}

tasks.withType<Test>().configureEach {
    maxHeapSize = "4g"
}
