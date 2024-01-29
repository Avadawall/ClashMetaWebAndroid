import com.github.kr328.golang.GolangBuildTask
import com.github.kr328.golang.GolangPlugin
import java.io.FileOutputStream
import java.net.URL
import java.time.Duration

plugins {
    kotlin("android")
    id("com.android.library")
    id("kotlinx-serialization")
    id("golang-android")
}

val geoipDatabaseUrl =
    "https://github.com/Dreamacro/maxmind-geoip/releases/latest/download/Country.mmdb"
val geoipInvalidate = Duration.ofDays(7)!!
val geoipOutput = buildDir.resolve("intermediates/golang_blob")
val golangSource = file("src/main/golang/native")

golang {
    sourceSets {
        create("meta") {
            tags.set(listOf("foss", "with_gvisor"))
            srcDir.set(file("src/foss/golang"))
        }
        create("meta-alpha") {
            tags.set(listOf("premium", "with_gvisor"))
            srcDir.set(file("src/premium/golang"))
        }

        all {
            fileName.set("libclash.so")
            packageName.set("cfa/native")
        }
    }
}

android {
    productFlavors {
        all {
            externalNativeBuild {
                cmake {
                    arguments("-DGO_SOURCE:STRING=${golangSource}")
                    arguments("-DGO_OUTPUT:STRING=${GolangPlugin.outputDirOf(project, null, null)}")
                    arguments("-DFLAVOR_NAME:STRING=$name")
                }
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
}

dependencies {
    implementation(project(":common"))

    implementation(libs.androidx.core)
    implementation(libs.kotlin.coroutine)
    implementation(libs.kotlin.serialization.json)
}

afterEvaluate {
    tasks.withType(GolangBuildTask::class.java).forEach {
        it.inputs.dir(golangSource)
    }
}

tasks.register<Copy>("copyWebui") {
    from("$projectDir/webui")
    into("$geoipOutput/webui")
}

task("downloadGeoipDatabase") {
    val databaseFile = geoipOutput.resolve("Country.mmdb")
    val moduleFile = geoipOutput.resolve("go.mod")
    val sourceFile = geoipOutput.resolve("blob.go")

    val moduleContent = """
        module "cfa/blob"
    """.trimIndent()

    val sourceContent = """
        package blob
        
        import (
            _ "embed"
            "embed"
        )
        
        //go:embed Country.mmdb
        var GeoipDatabase []byte
        
        //go:embed webui/*
        var EmbedUI embed.FS
    """.trimIndent()

    outputs.dir(geoipOutput)
    onlyIf {
        System.currentTimeMillis() - databaseFile.lastModified() > geoipInvalidate.toMillis()
    }

    doLast {

        geoipOutput.mkdirs()

        moduleFile.writeText(moduleContent)
        sourceFile.writeText(sourceContent)

        URL(geoipDatabaseUrl).openConnection().getInputStream().use { input ->
            FileOutputStream(databaseFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}

afterEvaluate {
    val downloadTask = tasks["downloadGeoipDatabase"]
    val copyWebui = tasks["copyWebui"]

    tasks.forEach {
        if (it.name.startsWith("externalGolangBuild")) {
            it.dependsOn(downloadTask)
            it.dependsOn(copyWebui)
        }
    }
}