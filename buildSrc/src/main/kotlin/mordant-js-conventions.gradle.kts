plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        // We have different code paths on browsers and node, so we run tests on both
        nodejs()
        browser()
    }
}
