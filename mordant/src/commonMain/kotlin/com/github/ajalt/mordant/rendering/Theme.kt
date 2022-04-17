package com.github.ajalt.mordant.rendering

import com.github.ajalt.colormath.model.RGB
import com.github.ajalt.mordant.internal.DEFAULT_STYLE

sealed class Theme(
    val styles: Map<String, TextStyle>,
    val strings: Map<String, String>,
    val flags: Map<String, Boolean>,
    val dimensions: Map<String, Int>,
) {
    companion object {
        private val DEFAULT_HEADER = RGB("#c678dd")
        private val DEFAULT_HIGHLIGHT = RGB("#61afef")
        private val DEFAULT_GRAY = RGB("#5c6370")
        private val DEFAULT_RED = RGB("#e06c75")
        private val DEFAULT_YELLOW = RGB("#e5c07b")
        private val DEFAULT_GREEN = RGB("#98c379")

        val Default: Theme = BuiltTheme(
            mapOf(
                "success" to TextStyle(DEFAULT_GREEN),
                "danger" to TextStyle(DEFAULT_RED),
                "warning" to TextStyle(DEFAULT_YELLOW),
                "info" to TextStyle(DEFAULT_HIGHLIGHT),
                "muted" to TextStyle(dim = true),

                "list.number" to DEFAULT_STYLE,
                "list.bullet" to DEFAULT_STYLE,
                "hr.rule" to DEFAULT_STYLE,
                "panel.border" to DEFAULT_STYLE,

                "prompt.prompt" to DEFAULT_STYLE,
                "prompt.default" to TextStyle(DEFAULT_HIGHLIGHT, bold = true),
                "prompt.choices" to TextStyle(DEFAULT_HIGHLIGHT, bold = true),
                "prompt.choices.invalid" to TextStyle(DEFAULT_RED),

                "progressbar.pending" to TextStyle(DEFAULT_GRAY),
                "progressbar.complete" to TextStyle(DEFAULT_HIGHLIGHT),
                "progressbar.indeterminate" to TextStyle(DEFAULT_HIGHLIGHT),
                "progressbar.separator" to DEFAULT_STYLE,
                "progressbar.finished" to TextStyle(DEFAULT_GREEN),

                "markdown.blockquote" to TextStyle(DEFAULT_YELLOW),
                "markdown.emph" to TextStyle(italic = true),
                "markdown.strong" to TextStyle(bold = true),
                "markdown.stikethrough" to TextStyle(strikethrough = true),
                "markdown.code.block" to TextStyle(DEFAULT_HIGHLIGHT),
                "markdown.code.span" to TextStyle(DEFAULT_HIGHLIGHT, DEFAULT_GRAY),
                "markdown.table.header" to TextStyle(bold = true),
                "markdown.table.body" to DEFAULT_STYLE,
                "markdown.link.text" to TextStyle(DEFAULT_HIGHLIGHT),
                "markdown.link.destination" to TextStyle(DEFAULT_HIGHLIGHT, dim = true),
                "markdown.img.alt-text" to TextStyle(dim = true),
                "markdown.h1" to TextStyle(DEFAULT_HEADER, bold = true),
                "markdown.h2" to TextStyle(DEFAULT_HEADER, bold = true),
                "markdown.h3" to TextStyle(DEFAULT_HEADER, bold = true, underline = true),
                "markdown.h4" to TextStyle(DEFAULT_HEADER, underline = true),
                "markdown.h5" to TextStyle(DEFAULT_HEADER, italic = true),
                "markdown.h6" to TextStyle(DEFAULT_HEADER, dim = true),
            ),
            mapOf(
                "list.number.separator" to ".",
                "list.bullet.text" to "•",
                "progressbar.pending" to "━",
                "progressbar.complete" to "━",
                "progressbar.separator" to " ",
                "hr.rule" to "─",

                "markdown.task.checked" to "☑",
                "markdown.task.unchecked" to "☐",
                "markdown.h1.rule" to "═",
                "markdown.h2.rule" to "─",
                "markdown.h3.rule" to " ",
                "markdown.h4.rule" to " ",
                "markdown.h5.rule" to " ",
                "markdown.h6.rule" to " ",
                "markdown.blockquote.bar" to "▎",
            ),
            mapOf(
                "progressbar.pulse" to true,

                "markdown.code.block.border" to true,
                "markdown.table.ascii" to false,
            ),
            mapOf(
                "hr.title.padding" to 1,
                "panel.title.padding" to 1,

                "markdown.header.padding" to 1,
            )
        )

        /** A theme that uses the same unicode characters as [Default], but uses no text colors or styles. */
        val Plain: Theme = Theme(
            BuiltTheme(emptyMap(), Default.strings, Default.flags, Default.dimensions)
        ) {
            flags["progressbar.pulse"] = false
        }

        /** A theme that uses no text colors or styles, and uses only ASCII characters */
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
            strings["markdown.blockquote.bar"] = "|"

            flags["markdown.table.ascii"] = true
        }
    }

    val success get(): TextStyle = style("success")
    val danger get(): TextStyle = style("danger")
    val warning get(): TextStyle = style("warning")
    val info get(): TextStyle = style("info")
    val muted: TextStyle get() = style("muted")

    /** Return a style if defined, or [default] otherwise */
    fun style(style: String, default: TextStyle = DEFAULT_STYLE): TextStyle = styles.getOrElse(style) { default }

    /** Return a style if defined, or `null` otherwise */
    fun styleOrNull(style: String): TextStyle? = styles[style]

    /** Return a flag if defined, or [default] otherwise */
    fun flag(flag: String, default: Boolean = false): Boolean = flags.getOrElse(flag) { default }

    /** Return a flag if defined, or `null` otherwise */
    fun flagOrNull(flag: String): Boolean? = flags[flag]

    /** Return a string if defined, or [default] otherwise */
    fun string(string: String, default: String = "") = strings.getOrElse(string) { default }

    /** Return a string if defined, or `null` otherwise */
    fun stringOrNull(string: String): String? = strings[string]

    /** Return a dimension if defined, or [default] otherwise */
    fun dimension(dimension: String, default: Int = 0): Int = dimensions.getOrElse(dimension) { default }

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
