import org.evoleq.publish.mppPublications

plugins {
    kotlin("multiplatform") version Config.Versions.kotlin
    id ("com.github.hierynomus.license") version "0.15.0"
    `maven-publish`
    id ("com.jfrog.artifactory") version "4.28.3"
    id("org.evoleq.publish") version "0.0.0"
    id("org.jetbrains.dokka") version "0.9.17"
}

group = Config.Module.MathcatReader.group
version = Config.Module.MathcatReader.version//+"-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    /* Targets configuration omitted.
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */
    jvm().compilations["main"].defaultSourceSet {
        dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation(kotlin("reflect"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Config.Versions.coroutines}")
        }
    }
    // JVM-specific tests and their dependencies:
    jvm().compilations["test"].defaultSourceSet {
        dependencies {
            implementation(kotlin("test-junit"))
        }
    }
    
    js().compilations["main"].defaultSourceSet  {
        dependencies {
            //implementation(kotlin("js"))
            implementation(kotlin("reflect"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${Config.Versions.coroutines}")
        }/*  */
    }
    js().compilations["test"].defaultSourceSet {/* ... */ }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Config.Versions.coroutines}")
                implementation(project(":mathcat-core"))
                implementation(project(":mathcat-morphism"))
                implementation(project(":mathcat-structure"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

tasks{
    val licenseFormatJvmMain by creating(com.hierynomus.gradle.license.tasks.LicenseFormat::class) {
        source = fileTree("$projectDir/src/jvmMain/kotlin") {
        }
        group = "license"
    }
    val licenseFormatJsMain by creating(com.hierynomus.gradle.license.tasks.LicenseFormat::class) {
        source = fileTree("$projectDir/src/jsMain/kotlin") {
        }
        group = "license"
    }
    val licenseFormatCommonMain by creating(com.hierynomus.gradle.license.tasks.LicenseFormat::class) {
        source = fileTree("$projectDir/src/commonMain/kotlin") {
        }
        group = "license"
    }
    licenseFormat {
        finalizedBy(licenseFormatJsMain, licenseFormatCommonMain, licenseFormatJvmMain)
    }
}


val jfrogUser: String by project
val jfrogMathcatPw: String by project

val jfrogEvoleqContextUrl: String by project
val jfrogReleaseLibs: String by project

artifactory{
    setContextUrl (jfrogEvoleqContextUrl)
    publish {
        repository {
            setRepoKey ( jfrogReleaseLibs ) // The Artifactory repository key to publish to
            setUsername ( jfrogUser ) // The publisher user name
            setPassword ( jfrogMathcatPw ) // The publisher password
        }
        defaults {
            // Reference to Gradle publications defined in the build script.
            // This is how we tell the Artifactory Plugin which artifacts should be
            // published to Artifactory.
            publications(
                *mppPublications()
            )
            setPublishArtifacts ( true )
            // Properties to be attached to the published artifacts.
            // setProperties (mapOf("qa.level" to "basic", "dev.team" to "core"))
            // Publish generated POM files to Artifactory (true by default)
            setPublishPom (true)
        }
    }
}