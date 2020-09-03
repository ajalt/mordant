package com.github.ajalt.mordant

import com.github.ajalt.mordant.rendering.*

class Terminal(
        val colors: TermColors,
        val theme: Theme = DEFAULT_THEME,
        val width: Int = System.getenv("COLUMNS")?.toInt() ?: 79
) {


    fun print(text: String) {
        kotlin.io.print(render(text))
    }

    fun print(renderable: Renderable) {
        print(render(renderable))
    }

    fun render(text: String): String {
        return render(Text(text, whitespace = Whitespace.PRE_WRAP))
    }


    fun render(renderable: Renderable): String {
        return render(renderable.render(this))
    }

   private fun render(lines: Lines): String = buildString {
        for ((i, line) in lines.lines.withIndex()) {
            if (i > 0) append("\n") // TODO: line separator

            for (span in line) {
                val ansi = span.style.toAnsi(this@Terminal)
                append(ansi.invoke(span.text))
            }
        }
    }
}

fun main() {
    val t = Terminal(TermColors(TermColors.Level.TRUECOLOR))
    t.print(Text("""
    line 1



    line 2
    """.trimIndent(), whitespace = Whitespace.NORMAL))

}
