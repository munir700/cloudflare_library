plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
    implementation("com.android.tools.build:gradle:8.13.1")
    implementation("com.google.gms:google-services:4.4.4")
}

