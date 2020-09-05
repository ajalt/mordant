package com.github.ajalt.mordant.rendering

interface Theme  {
    val listNumber get() = TextStyle()
    val listBullet get() = TextStyle()
    val listBulletText get() = " • "
    val blockQuote get() = TextStyle()
    val horizontalRule get() = TextStyle()
    val horizontalRuleTitle get() = TextStyle()

    val markdownText get() = TextStyle()
    val markdownEmph get() = TextStyle(italic = true)
    val markdownStrong get() = TextStyle(bold = true)
    val markdownCode get() = TextStyle() // TODO
    val markdownH1 get() = TextStyle()
    val markdownH2 get() = TextStyle()
    val markdownH3 get() = TextStyle()
    val markdownH4 get() = TextStyle(italic = true)
    val markdownH5 get() = TextStyle(italic = true)
    val markdownH6 get() = TextStyle(italic = true, dim = true)
}

internal val DEFAULT_THEME = object : Theme {}
