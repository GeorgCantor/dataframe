rootProject.name = "dataframe"
enableFeaturePreview("VERSION_CATALOGS")

includeBuild("generator")
include("plugins:dataframe-gradle-plugin")
include("plugins:symbol-processor")

val kspVersion: String by settings

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("ksp", kspVersion)
            alias("ksp-gradle").to("com.google.devtools.ksp", "symbol-processing-gradle-plugin").versionRef("ksp")
            alias("ksp-api").to("com.google.devtools.ksp", "symbol-processing-api").versionRef("ksp")
        }
    }
}
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

