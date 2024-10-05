plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
