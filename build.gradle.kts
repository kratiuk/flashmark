plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
}

tasks.register("debugUpdate") {
    dependsOn(":app:debugUpdate")
}

tasks.register("debugReinstall") {
    dependsOn(":app:debugReinstall")
}
