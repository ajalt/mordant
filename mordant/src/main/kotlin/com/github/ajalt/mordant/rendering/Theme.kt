package com.github.ajalt.mordant.rendering

interface Theme  {
    val listNumber get() = TextStyle()
    val listBullet get() = TextStyle()
    val listBulletText get() = " • "
    val blockQuote get() = TextStyle()

    val markdownText get() = TextStyle()
    val markdownEmph get() = TextStyle(italic = true)
    val markdownStrong get() = TextStyle(bold = true)
    val markdownCode get() = TextStyle() // TODO
}

internal val DEFAULT_THEME = object : Theme {}
