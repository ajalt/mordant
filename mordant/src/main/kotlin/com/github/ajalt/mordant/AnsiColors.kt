package com.github.ajalt.mordant

import com.github.ajalt.mordant.AnsiCodes.bgColorReset
import com.github.ajalt.mordant.AnsiCodes.bgColorSelector
import com.github.ajalt.mordant.AnsiCodes.fgBgOffset
import com.github.ajalt.mordant.AnsiCodes.fgColorReset
import com.github.ajalt.mordant.AnsiCodes.fgColorSelector
import com.github.ajalt.mordant.AnsiCodes.selector256
import com.github.ajalt.mordant.AnsiCodes.selectorRgb


internal const val ESC = "\u001B"
internal const val CSI = "$ESC["
private val ansiCloseRe = Regex("""$ESC\[((?:\d{1,3};?)+)m""")

internal object AnsiCodes {
    val reset = 0
    val boldOpen = 1
    val boldClose = 22
    val dimOpen = 2
    val dimClose = 22
    val italicOpen = 3
    val italicClose = 23
    val underlineOpen = 4
    val underlineClose = 24
    val inverseOpen = 7
    val inverseClose = 27
    val hiddenOpen = 8
    val hiddenClose = 28
    val strikethroughOpen = 9
    val strikethroughClose = 29
    val plain = AnsiCode(emptyList())

    val fg16Range = 30..37
    val fg16BrightRange = 90..97
    const val fgColorSelector = 38
    const val fgColorReset = 39

    const val fgBgOffset = 10

    val bg16Range = 40..47
    val bg16BrightRange = 100..107
    const val bgColorSelector = 48
    const val bgColorReset = 49

    const val selector256 = 5
    const val selectorRgb = 2

    const val underlineColorSelector = 58
}

/**
 * A class representing one or more numeric ANSI codes.
 *
 * @property codes A list of pairs, with each pair being the list of opening codes and a closing code.
 */
open class AnsiCode(protected val codes: List<Pair<List<Int>, Int>>) : (String) -> String {
    constructor(openCodes: List<Int>, closeCode: Int) : this(listOf(openCodes to closeCode))
    constructor(openCode: Int, closeCode: Int) : this(listOf(openCode), closeCode)

    val open: String get() = tag(codes.flatMap { it.first })
    val close: String get() = tag(codes.map { it.second })

    override fun toString() = open
    override fun invoke(text: String) = if (text.isEmpty()) "" else open + nest(text) + close

    open operator fun plus(other: AnsiCode) = AnsiCode(codes + other.codes)

    private fun nest(text: String) = ansiCloseRe.replace(text) { match ->
        // Replace instances of our close codes with their corresponding opening codes. If the close
        // code is at the end of the text, omit it instead so that we don't open and immediately
        // close a command.
        val openCodesByCloseCode = HashMap<Int, List<Int>>()
        for ((o, c) in codes) openCodesByCloseCode[c] = o
        val atEnd = match.range.last == text.lastIndex
        val codes = match.groupValues[1].splitToSequence(';').flatMap { code ->
            code.toInt().let {
                if (atEnd && it in openCodesByCloseCode) emptySequence()
                else (openCodesByCloseCode[it]?.asSequence() ?: sequenceOf(it))
            }
        }

        tag(codes.toList())
    }

    private fun tag(c: List<Int>) = if (c.isEmpty()) "" else "$CSI${c.joinToString(";")}m"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AnsiCode
        return codes == other.codes
    }

    override fun hashCode() = codes.hashCode()
}

internal object DisabledAnsiCode : AnsiCode(emptyList()) {
    override fun plus(other: AnsiCode): AnsiCode = this
}

/**
 * A class representing one or more ANSI color codes.
 */
abstract class AnsiColorCode internal constructor(
        codes: List<Pair<List<Int>, Int>>
) : AnsiCode(codes) {
    constructor(openCodes: List<Int>, closeCode: Int) : this(listOf(openCodes to closeCode))
    constructor(openCode: Int, closeCode: Int) : this(listOf(openCode), closeCode)

    /**
     * Get a color for background only.
     *
     * Note that if you want to specify both a background and foreground color, use [on] instead of
     * this property.
     */
    val bg: AnsiCode get() = AnsiCode(bgCodes)

    open infix fun on(bg: AnsiColorCode): AnsiCode {
        return AnsiCode(codes + bg.bgCodes)
    }

    protected abstract val bgCodes: List<Pair<List<Int>, Int>>
}

internal object DisabledAnsiColorCode : AnsiColorCode(emptyList()) {
    override val bgCodes: List<Pair<List<Int>, Int>> get() = emptyList()
    override fun plus(other: AnsiCode): AnsiCode = this
    override fun on(bg: AnsiColorCode): AnsiCode = DisabledAnsiCode
}

class Ansi16ColorCode(code: Int) : AnsiColorCode(code, fgColorReset) {
    override val bgCodes get() = codes.map { listOf(it.first[0] + fgBgOffset) to bgColorReset }
}

class Ansi256ColorCode(code: Int) : AnsiColorCode(listOf(fgColorSelector, selector256, code), fgColorReset) {
    override val bgCodes get() = codes.map { listOf(bgColorSelector, selector256, it.first[2]) to bgColorReset }
}

class AnsiRGBColorCode(r: Int, g: Int, b: Int) : AnsiColorCode(listOf(fgColorSelector, selectorRgb, r, g, b), fgColorReset) {
    override val bgCodes get() = codes.map { (o, _) -> listOf(bgColorSelector, selectorRgb, o[2], o[3], o[4]) to bgColorReset }
}
