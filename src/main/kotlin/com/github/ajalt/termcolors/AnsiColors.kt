package com.github.ajalt.termcolors

import java.util.*


private const val ESC = (0x1b).toChar()
private val ansiCloseRe = Regex("$ESC\\[((?:39|49|22|23|24|27|28|29))+m")

open class AnsiCode(protected val openCodes: IntArray,
                    protected val closeCodes: IntArray) : (String) -> String {
    constructor(openCode: Int, closeCode: Int) : this(intArrayOf(openCode), intArrayOf(closeCode))

    val open: String get() = tag(openCodes)
    val close: String get() = tag(closeCodes)

    override fun toString() = open
    override fun invoke(text: String) = if (text.isEmpty()) "" else open + nest(text) + close

    operator open fun plus(other: AnsiCode): AnsiCode {
        return AnsiCode(openCodes + other.openCodes, closeCodes + other.closeCodes)
    }

    private fun nest(text: String) = ansiCloseRe.replace(text) {
        val codes = it.groupValues[1].splitToSequence(';').map { it.toInt() }
                .filter { it !in closeCodes }.toList().toIntArray()
        val atEnd = it.range.endInclusive == text.lastIndex
        if (atEnd && codes.isEmpty()) ""
        else AnsiCode(if (atEnd) codes else codes + openCodes, intArrayOf()).open
    }

    private fun tag(c: IntArray) = if (c.isEmpty()) "" else "$ESC[${c.joinToString(";")}m"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnsiCode

        if (!Arrays.equals(openCodes, other.openCodes)) return false
        if (!Arrays.equals(closeCodes, other.closeCodes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(openCodes)
        result = 31 * result + Arrays.hashCode(closeCodes)
        return result
    }
}

internal object DisabledAnsiCode : AnsiCode(intArrayOf(), intArrayOf()) {
    override fun plus(other: AnsiCode): AnsiCode = this
}

abstract class AnsiColorCode(openCodes: IntArray, closeCodes: IntArray) :
        AnsiCode(openCodes, closeCodes) {
    constructor(openCode: Int, closeCode: Int) : this(intArrayOf(openCode), intArrayOf(closeCode))

    /**
     * Get a color for background only.
     *
     * Note that if you want to specify both a background and foreground color, use [on] instead of
     * this property.
     */
    val bg: AnsiCode get() = AnsiCode(bgOpenCodes, bgCloseCodes)

    open infix fun on(bg: AnsiColorCode): AnsiCode {
        return AnsiCode(openCodes + bg.bgOpenCodes, closeCodes + bg.bgCloseCodes)
    }

    protected abstract val bgOpenCodes: IntArray
    protected abstract val bgCloseCodes: IntArray
}

internal object DisabledAnsiColorCode : AnsiColorCode(intArrayOf(), intArrayOf()) {
    override val bgOpenCodes: IntArray get() = intArrayOf()
    override val bgCloseCodes: IntArray get() = intArrayOf()
    override fun plus(other: AnsiCode): AnsiCode = this
    override fun on(bg: AnsiColorCode): AnsiCode = DisabledAnsiCode
}

class Ansi16ColorCode(code: Int) : AnsiColorCode(code, 39) {
    override val bgOpenCodes get() = intArrayOf(openCodes[0] + 10)
    override val bgCloseCodes get() = intArrayOf(49)
}

class Ansi256ColorCode(code: Int) : AnsiColorCode(intArrayOf(38, 5, code), intArrayOf(39)) {
    override val bgOpenCodes get() = intArrayOf(48, 5, openCodes[2])
    override val bgCloseCodes get() = intArrayOf(49)
}

class AnsiRGBColorCode(r: Int, g: Int, b: Int) : AnsiColorCode(intArrayOf(38, 2, r, g, b), intArrayOf(39)) {
    override val bgOpenCodes get() = intArrayOf(48, 2, openCodes[2], openCodes[3], openCodes[4])
    override val bgCloseCodes get() = intArrayOf(49)
}
