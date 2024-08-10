package com.github.ajalt.mordant.terminal

/**
 * A provider for a [TerminalInterface].
 *
 * Implementations of this interface are loaded via the `ServiceLoader` mechanism, and should be
 * declared in the `META-INF/services/com.github.ajalt.mordant.terminal.TerminalInterfaceProvider`
 * file.
 */
interface TerminalInterfaceProvider {
    /**
     * Load the terminal interface.
     *
     * @return The terminal interface, or null if it could not be loaded.
     */
    fun load(): TerminalInterface?
}

