package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal

internal sealed class ThemeStyle {
    companion object {
        fun of(key: String, explicit: TextStyle?, default: TextStyle = DEFAULT_STYLE): ThemeStyle {
            return when (explicit) {
                null -> Default(key, default)
                else -> Explicit(explicit)
            }
        }
    }

    abstract operator fun get(theme: Theme): TextStyle
    operator fun get(terminal: Terminal): TextStyle = get(terminal.theme)

    class Default(private val key: String, private val default: TextStyle) : ThemeStyle() {
        override fun get(theme: Theme): TextStyle = theme.style(key, default)
    }

    class Explicit(private val style: TextStyle) : ThemeStyle() {
        override fun get(theme: Theme): TextStyle = style
    }
}

internal sealed class ThemeString {
    companion object {
        fun of(key: String, explicit: String?, default: String = ""): ThemeString {
            return when (explicit) {
                null -> Default(key, default)
                else -> Explicit(explicit)
            }
        }
    }

    abstract operator fun get(theme: Theme): String
    operator fun get(terminal: Terminal) = get(terminal.theme)

    class Default(private val key: String, private val default: String) : ThemeString() {
        override fun get(theme: Theme): String = theme.string(key, default)
    }

    class Explicit(private val style: String) : ThemeString() {
        override fun get(theme: Theme): String = style
    }
}

internal sealed class ThemeFlag {
    companion object {
        fun of(key: String, explicit: Boolean?, default: Boolean = false): ThemeFlag {
            return when (explicit) {
                null -> Default(key, default)
                else -> Explicit(explicit)
            }
        }
    }

    abstract operator fun get(theme: Theme): Boolean
    operator fun get(terminal: Terminal) = get(terminal.theme)

    class Default(private val key: String, private val default: Boolean) : ThemeFlag() {
        override fun get(theme: Theme): Boolean = theme.flag(key, default)
    }

    class Explicit(private val style: Boolean) : ThemeFlag() {
        override fun get(theme: Theme): Boolean = style
    }
}

internal sealed class ThemeDimension {
    companion object {
        fun of(key: String, explicit: Int?, default: Int = 0): ThemeDimension {
            return when (explicit) {
                null -> Default(key, default)
                else -> Explicit(explicit)
            }
        }
    }

    abstract operator fun get(theme: Theme): Int
    operator fun get(terminal: Terminal) = get(terminal.theme)

    class Default(private val key: String, private val default: Int) : ThemeDimension() {
        override fun get(theme: Theme): Int = theme.dimension(key, default)
    }

    class Explicit(private val style: Int) : ThemeDimension() {
        override fun get(theme: Theme): Int = style
    }
}
