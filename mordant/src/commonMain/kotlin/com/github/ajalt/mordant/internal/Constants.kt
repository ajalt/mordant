package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.rendering.Line
import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.Span
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.widgets.Padding
import kotlin.native.concurrent.SharedImmutable

// All top-level vals are defined in this file to avoid initialization order issues on native.

@SharedImmutable
internal val DEFAULT_STYLE = TextStyle()

@SharedImmutable
internal val EMPTY_LINES = Lines(emptyList())

@SharedImmutable
internal val EMPTY_LINE: Line = Line(emptyList(), DEFAULT_STYLE)

@SharedImmutable
internal val SINGLE_SPACE = Span.space(1)

@SharedImmutable
internal val DEFAULT_PADDING = Padding.none()

@Suppress("RegExpRedundantEscape") // JS requires escaping the lone `]` at the beginning of the pattern, so we can't use $OSC
@SharedImmutable
internal val ANSI_RE = Regex("""$ESC\][^$ESC]*$ESC\\|$ESC(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])""")
