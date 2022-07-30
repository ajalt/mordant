package com.github.ajalt.mordant.rendering.internal

import com.github.ajalt.mordant.internal.cellWidth
import com.github.ajalt.mordant.internal.codepointSequence
import com.github.ajalt.mordant.internal.stringCellWidth
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
        cellWidth(codepointSequence(char).single()) shouldBe width
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
        row("👍🏿", 2),
        row("🇩🇪", 2),
        row("\uD83D\uDC68\uD83C\uDFFE\u200D\uD83E\uDDB1", 2), // MAN, FITZPATRICK TYPE-5, ZWJ, CURLY HAIR
        row("\uD83D\uDC69\u200D\uD83D\uDC67", 2), // Emoji_ZWJ_Sequence ; family: woman, girl (👩‍👧)
        row("\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66", 2), //Emoji_ZWJ_Sequence ; family: woman, girl, boy (👩‍👧‍👦)
        row("\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC66\u200D\uD83D\uDC66", 2), // Emoji_ZWJ_Sequence ; family: woman, woman, boy, boy (👩‍👩‍👦‍👦)

    ) { str, width ->
        stringCellWidth(str) shouldBe width
    }
}
