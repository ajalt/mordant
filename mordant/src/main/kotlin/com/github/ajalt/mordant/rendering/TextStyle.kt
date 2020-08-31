package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.AnsiCode
import com.github.ajalt.mordant.TermColors
import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.TextStyle.*

enum class TextStyle {
    BOLD,
    ITALIC,
    UNDERLINE,
    // TODO
//    DIM,
//    INVERSE,
//    STRIKETHROUGH,
//    HIDDEN,
//    CODE
}

fun TextStyle.toAnsi(t: Terminal): AnsiCode {
    return when(this) {
        BOLD -> t.colors.bold
        ITALIC -> t.colors.italic
        UNDERLINE -> t.colors.underline
    }
}
