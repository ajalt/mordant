package com.github.ajalt.mordant.rendering

import com.github.ajalt.colormath.Ansi256
import com.github.ajalt.mordant.terminal.TextColors.*

sealed class Theme(
    val styles: Map<String, TextStyle>,
    val strings: Map<String, String>,
    val flags: Map<String, Boolean>,
    val dimensions: Map<String, Int>,
) {
    companion object {
        val Default: Theme = BuiltTheme(
            mapOf(
                "success" to TextStyle(green),
                "danger" to TextStyle(red),
                "warning" to TextStyle(yellow),
                "info" to TextStyle(cyan),
                "muted" to TextStyle(dim = true),

                "list.number" to DEFAULT_STYLE,
                "list.bullet" to DEFAULT_STYLE,
                "blockquote" to TextStyle(brightYellow),
                "hr.rule" to DEFAULT_STYLE,
                "hr.title" to DEFAULT_STYLE,
                "progressbar.pending" to TextStyle(gray),
                "progressbar.complete" to TextStyle(brightBlue),
                "progressbar.separator" to DEFAULT_STYLE,
                "progressbar.finished" to TextStyle(green),

                "markdown.emph" to TextStyle(italic = true),
                "markdown.strong" to TextStyle(bold = true),
                "markdown.stikethrough" to TextStyle(strikethrough = true),
                "markdown.code.block" to TextStyle(brightRed),
                "markdown.code.span" to TextStyle(brightRed, Ansi256(236)),
                "markdown.table.header" to TextStyle(bold = true),
                "markdown.table.body" to DEFAULT_STYLE,
                "markdown.link.text" to TextStyle(brightBlue),
                "markdown.link.destination" to TextStyle(blue),
                "markdown.img.alt-text" to TextStyle(dim = true),
                "markdown.h1" to TextStyle(magenta, bold = true),
                "markdown.h2" to TextStyle(magenta, bold = true),
                "markdown.h3" to TextStyle(magenta, bold = true, underline = true),
                "markdown.h4" to TextStyle(magenta, underline = true),
                "markdown.h5" to TextStyle(magenta, italic = true),
                "markdown.h6" to TextStyle(magenta, dim = true),
            ),
            mapOf(
                "list.number.separator" to ".",
                "list.bullet.text" to "•",
                "progressbar.pending" to "━",
                "progressbar.complete" to "━",
                "progressbar.separator" to " ",

                "markdown.task.checked" to "☑",
                "markdown.task.unchecked" to "☐",
                "markdown.h1.rule" to "═",
                "markdown.h2.rule" to "─",
                "markdown.h3.rule" to " ",
                "markdown.h4.rule" to " ",
                "markdown.h5.rule" to " ",
                "markdown.h6.rule" to " ",
            ),
            mapOf(
                "markdown.code.block.border" to true,
                "markdown.table.ascii" to false,
            ),
            mapOf(
                "markdown.header.padding" to 1,
            )
        )

        val Plain: Theme = BuiltTheme(emptyMap(), Default.strings, Default.flags, Default.dimensions)

        val PlainAscii: Theme = Theme(Plain) {
            strings["list.number.separator"] = "."
            strings["list.bullet.text"] = "*"
            strings["progressbar.pending"] = " "
            strings["progressbar.complete"] = "#"
            strings["progressbar.separator"] = ">"

            strings["markdown.task.checked"] = "[x]"
            strings["markdown.task.unchecked"] = "[ ]"
            strings["markdown.h1.rule"] = "="
            strings["markdown.h2.rule"] = "-"
            flags["markdown.table.ascii"] = true
        }
    }

    /** Return a style if defined, or [default] otherwise */
    fun style(style: String, default: TextStyle = DEFAULT_STYLE): TextStyle = styles.getOrDefault(style, default)

    /** Return a style if defined, or `null` otherwise */
    fun styleOrNull(style: String): TextStyle? = styles[style]

    /** Return a flag if defined, or [default] otherwise */
    fun flag(flag: String, default: Boolean = false): Boolean = flags.getOrDefault(flag, default)

    /** Return a flag if defined, or `null` otherwise */
    fun flagOrNull(flag: String): Boolean? = flags[flag]

    /** Return a string if defined, or [default] otherwise */
    fun string(string: String, default: String = "") = strings.getOrDefault(string, default)

    /** Return a string if defined, or `null` otherwise */
    fun stringOrNull(string: String): String? = strings[string]

    /** Return a dimension if defined, or [default] otherwise */
    fun dimension(dimension: String, default: Int = 0): Int = dimensions.getOrDefault(dimension, default)

    /** Return a dimension if defined, or `null` otherwise */
    fun dimensionOrNull(dimension: String): Int? = dimensions[dimension]
}

class ThemeBuilder internal constructor(
    val styles: MutableMap<String, TextStyle>,
    val strings: MutableMap<String, String>,
    val flags: MutableMap<String, Boolean>,
    val dimensions: MutableMap<String, Int>,
) {
    internal fun build(): Theme = BuiltTheme(styles, strings, flags, dimensions)
}


@Suppress("FunctionName")
fun Theme(from: Theme = Theme.Default, init: ThemeBuilder.() -> Unit): Theme {
    return ThemeBuilder(
        from.styles.toMutableMap(),
        from.strings.toMutableMap(),
        from.flags.toMutableMap(),
        from.dimensions.toMutableMap(),
    ).apply(init).build()
}

private class BuiltTheme(
    styles: Map<String, TextStyle>,
    strings: Map<String, String>,
    flags: Map<String, Boolean>,
    dimensions: Map<String, Int>,
) : Theme(styles, strings, flags, dimensions)
