plugins {
    id("mordant-mpp-sample-conventions")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.squareup.okio:okio:3.9.0")
        }
    }
}
