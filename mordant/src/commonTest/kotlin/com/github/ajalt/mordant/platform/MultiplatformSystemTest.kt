package com.github.ajalt.mordant.platform

import com.github.ajalt.mordant.internal.cwd
import com.github.ajalt.mordant.internal.hasFileSystem
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlin.test.Test


class MultiplatformSystemTest {
    @Test
    fun readEnvironmentVariable() {
        // The kotlin.test plugin doesn't provide a way to set environment variables that works on
        // all targets, so just pick a common one that should exist everywhere.
        val actual = MultiplatformSystem.readEnvironmentVariable("PATH")
        if (!hasFileSystem()) return
        actual.shouldNotBeNull().shouldNotBeEmpty()
    }

    @Test
    fun readFileAsUtf8() {
        val actual = MultiplatformSystem.readFileAsUtf8(
            // Most targets have a cwd of $moduleDir
            "src/commonTest/resources/multiplatform_system_test.txt"
        ) ?: MultiplatformSystem.readFileAsUtf8(
            // js targets have a cwd of $projectDir/build/js/packages/mordant-mordant-test
            "../../../../mordant/src/commonTest/resources/multiplatform_system_test.txt"
        ) ?: cwd()
        if (!hasFileSystem()) return
        actual.trim() shouldBe "pass"
    }
}
