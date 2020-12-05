package com.github.ajalt.mordant.rendering.internal

import com.github.ajalt.mordant.toCodePoint
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.test.Test


internal class CellWidthTest {

    @Test
    fun cellWidth() = forAll(
            row(" ", 1),
            row("a", 1),
            row("\n", 0),
            row("\u0000", 0), // NUL
            row("\u001b", 0), // ESC
            row("\u1885", 0), // MONGOLIAN LETTER ALI GALI BALUDA (combining)
            row("\u007f", -1), // DEL
            row("\u0008", -1), // BS
            row("가", 2), // HANGUL SYLLABLE GA
            row("ぁ", 2), // HIRAGANA LETTER SMALL A
            row("💯", 2), // HUNDRED POINTS SYMBOL
    ) { char, width ->
        com.github.ajalt.mordant.internal.cellWidth(char.toCodePoint()) shouldBe width
    }

    @Test
    fun stringCellWidth() = forAll(
            row("", 0),
            row("a", 1),
            row("\n", 0),
            row("1\u007F1", 1),
            row("모ㄹ단ㅌ", 8),
            row("媒人", 4),
            row("🙊🙉🙈", 6),
            row("en\u0303e", 3),
    ) { str, width ->
        com.github.ajalt.mordant.internal.stringCellWidth(str) shouldBe width
    }
}
