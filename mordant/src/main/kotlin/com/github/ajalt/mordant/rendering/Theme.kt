package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.AnsiColor.*

interface Theme  {
    val listNumber: TextStyle get() = DEFAULT_STYLE
    val listBullet: TextStyle get() = DEFAULT_STYLE
    val listBulletText: String get() = " • "
    val blockQuote: TextStyle get() = DEFAULT_STYLE
    val horizontalRule: TextStyle get() = DEFAULT_STYLE
    val horizontalRuleTitle: TextStyle get() = DEFAULT_STYLE

    val markdownText: TextStyle get() = DEFAULT_STYLE
    val markdownEmph: TextStyle get() = TextStyle(italic = true)
    val markdownStrong: TextStyle get() = TextStyle(bold = true)
    val markdownCodeBlock: TextStyle get() = TextStyle(brightWhite, black)
    val markdownCodeSpan: TextStyle get() = TextStyle(brightWhite, black)
    val markdownH1: TextStyle get() = DEFAULT_STYLE
    val markdownH2: TextStyle get() = DEFAULT_STYLE
    val markdownH3: TextStyle get() = DEFAULT_STYLE
    val markdownH4: TextStyle get() = TextStyle(italic = true)
    val markdownH5: TextStyle get() = TextStyle(italic = true)
    val markdownH6: TextStyle get() = TextStyle(italic = true, dim = true)
}

internal val DEFAULT_THEME = object : Theme {}
