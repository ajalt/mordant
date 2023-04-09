package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.rendering.Line
import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.Span
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.widgets.Padding

// All top-level vals are defined in this file to avoid initialization order issues on native.

internal val DEFAULT_STYLE = TextStyle(
    color = null,
    bgColor = null,
    bold = false,
    italic = false,
    underline = false,
    dim = false,
    inverse = false,
    strikethrough = false,
    hyperlink = null,
)

internal val EMPTY_LINES = Lines(emptyList())
internal val EMPTY_LINE: Line = Line(emptyList(), DEFAULT_STYLE)
internal val SINGLE_SPACE = Span.space(1)
internal val DEFAULT_PADDING = Padding(0)

@Suppress("RegExpRedundantEscape") // JS requires escaping the lone `]` at the beginning of the pattern, so we can't use $OSC
internal val ANSI_RE = Regex("""$ESC\][^$ESC]*$ESC\\|$ESC(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])""")
internal const val HYPERLINK_RESET = "__mordant_reset__"
