package com.github.ajalt.mordant.platform

import com.github.ajalt.mordant.internal.exitProcessMpp
import com.github.ajalt.mordant.internal.getEnv
import com.github.ajalt.mordant.internal.readFileIfExists

/**
 * Utility functions useful for multiplatform command line apps interacting with the system.
 */
object MultiplatformSystem {
    /**
     * Get the value of an environment variable.
     *
     * If the variable is not set, this function returns `null`.
     */
    fun readEnvironmentVariable(key: String): String? {
        return getEnv(key)
    }

    /**
     * Immediately exit the process with the given [status] code.
     *
     * On browsers, where it's not possible to exit the process, this function is a no-op.
     */
    fun exitProcess(status: Int) {
        exitProcessMpp(status)
    }

    /**
     * Read the contents of a file as a UTF-8 string.
     *
     * @return The file contents decoded as a UTF-8 string, or `null` if the file could not be read.
     */
    fun readFileAsUtf8(path: String): String? {
        return try {
            readFileIfExists(path)
        } catch (e: Throwable) {
            null
        }
    }
}
