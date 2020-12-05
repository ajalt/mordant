package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.terminal.TextColors.*

interface Theme {
    val success: TextStyle get() = TextStyle(green)
    val danger: TextStyle get() = TextStyle(red)
    val warning: TextStyle get() = TextStyle(yellow)
    val info: TextStyle get() = TextStyle(cyan)
    val muted: TextStyle get() = TextStyle(dim = true)

    val listNumber: TextStyle get() = DEFAULT_STYLE
    val listNumberSeparator: String get() = "."
    val listBullet: TextStyle get() = DEFAULT_STYLE
    val listBulletText: String get() = "•"
    val blockQuote: TextStyle get() = TextStyle(brightYellow)
    val horizontalRule: TextStyle get() = DEFAULT_STYLE
    val horizontalRuleTitle: TextStyle get() = DEFAULT_STYLE

    val markdownText: TextStyle get() = DEFAULT_STYLE
    val markdownEmph: TextStyle get() = TextStyle(italic = true)
    val markdownStrong: TextStyle get() = TextStyle(bold = true)
    val markdownStikethrough: TextStyle get() = TextStyle(strikethrough = true)
    val markdownCodeBlock: TextStyle get() = TextStyle(brightRed)
    val markdownCodeBlockBorder: Boolean get() = true
    val markdownCodeSpan: TextStyle get() = TextStyle(brightRed, gray, dim = true)
    val markdownHeaderPadding: Int get() = 1
    val markdownTableHeader: TextStyle get() = TextStyle(bold = true)
    val markdownTableBody: TextStyle get() = DEFAULT_STYLE
    val markdownLinkText: TextStyle get() = TextStyle(brightBlue)
    val markdownLinkDestination: TextStyle get() = TextStyle(blue)
    val markdownH1: TextStyle get() = TextStyle(magenta, bold = true)
    val markdownH2: TextStyle get() = TextStyle(magenta, bold = true)
    val markdownH3: TextStyle get() = TextStyle(magenta, bold = true, underline = true)
    val markdownH4: TextStyle get() = TextStyle(magenta, underline = true)
    val markdownH5: TextStyle get() = TextStyle(magenta, italic = true)
    val markdownH6: TextStyle get() = TextStyle(magenta, dim = true)
    val markdownTaskChecked: String get() = "☑"
    val markdownTaskUnchecked: String get() = "☐"

    val markdownH1Rule: String get() = "═"
    val markdownH2Rule: String get() = "─"
    val markdownH3Rule: String get() = " "
    val markdownH4Rule: String get() = " "
    val markdownH5Rule: String get() = " "
    val markdownH6Rule: String get() = " "

    companion object {
        val ASCII = object : Theme {
            override val listBulletText: String get() = "*"
            override val markdownH1Rule: String get() = "="
            override val markdownH2Rule: String get() = "-"
            override val markdownTaskChecked: String get() = "[x]"
            override val markdownTaskUnchecked: String get() = "[ ]"
        }
    }
}

internal val DEFAULT_THEME = object : Theme {}
