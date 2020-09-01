package com.github.ajalt.mordant.rendering

interface Theme  {
    val markdownText get() = TextStyle()
    val markdownEmph get() = TextStyle(italic = true)
    val markdownStrong get() = TextStyle(bold = true)
    val markdownCode get() = TextStyle() // TODO
}

internal val DEFAULT_THEME = object: Theme{}
