package com.github.ajalt.mordant

import com.github.ajalt.mordant.rendering.Renderable
import com.github.ajalt.mordant.rendering.Span
import com.github.ajalt.mordant.rendering.Text
import com.github.ajalt.mordant.rendering.toAnsi

class Terminal(
        val colors: TermColors,
        val width: Int = System.getenv("COLUMNS")?.toInt() ?: 79
) {


    fun print(text: String) {
        print(render(text))
    }

    fun print(renderable: Renderable) {
        print(render(renderable))
    }

    fun render(text: String): String {
        return render(Text(text))
    }


    fun render(renderable: Renderable): String {
        return render(renderable.render(this))
    }

    fun render(text: List<Span>): String = buildString {
        for (t in text) {
            val ansi = t.styles.map { it.toAnsi(this@Terminal) }.fold(AnsiCode.BLANK, AnsiCode::plus)
            append(ansi.invoke(t.text))
        }
    }
}
