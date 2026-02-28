// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

tasks.register("ciCheck") {
    dependsOn(
        ":app:ktlintCheck",
        ":app:detekt",
        ":app:test",
        ":app:assembleDebug"
    )
}

tasks.register("localFix") {
    dependsOn(
        ":app:ktlintFormat",
        ":app:ktlintCheck",
        ":app:detekt",
        ":app:test",
        ":app:assembleDebug"
    )
}
