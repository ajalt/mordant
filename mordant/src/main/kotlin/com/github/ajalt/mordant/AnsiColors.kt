package com.github.ajalt.mordant


private const val ESC = "\u001B"
internal const val CSI = "$ESC["
private val ansiCloseRe = Regex("""$ESC\[((?:\d{1,3};?)+)m""")

/**
 * A class representing one or more numeric ANSI codes.
 *
 * @property codes A list of pairs, with each pair being the list of opening codes and a closing code.
 */
open class AnsiCode(protected val codes: List<Pair<List<Int>, Int>>) : (String) -> String {
    companion object {
        val BLANK = AnsiCode(emptyList())
    }
    constructor(openCodes: List<Int>, closeCode: Int) : this(listOf(openCodes to closeCode))
    constructor(openCode: Int, closeCode: Int) : this(listOf(openCode), closeCode)

    val open: String get() = tag(codes.flatMap { it.first })
    val close: String get() = tag(codes.map { it.second })

    override fun toString() = open
    override fun invoke(text: String) = if (text.isEmpty()) "" else open + nest(text) + close

    open operator fun plus(other: AnsiCode) = AnsiCode(codes + other.codes)

    private fun nest(text: String) = ansiCloseRe.replace(text) {
        // Replace instances of our close codes with their corresponding opening codes. If the close
        // code is at the end of the text, omit it instead so that we don't open and immediately
        // close a command.
        val openCodesByCloseCode = HashMap<Int, List<Int>>()
        for ((o, c) in codes) openCodesByCloseCode[c] = o
        val atEnd = it.range.endInclusive == text.lastIndex
        val codes = it.groupValues[1].splitToSequence(';').flatMap {
            it.toInt().let {
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
 *
 * @property codes A list of pairs, with each pair being the list of opening codes and a closing code.
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

class Ansi16ColorCode(code: Int) : AnsiColorCode(code, 39) {
    override val bgCodes get() = codes.map { listOf(it.first[0] + 10) to 49 }
}

class Ansi256ColorCode(code: Int) : AnsiColorCode(listOf(38, 5, code), 39) {
    override val bgCodes get() = codes.map { listOf(48, 5, it.first[2]) to 49 }
}

class AnsiRGBColorCode(r: Int, g: Int, b: Int) : AnsiColorCode(listOf(38, 2, r, g, b), 39) {
    override val bgCodes get() = codes.map { (o, _) -> listOf(48, 2, o[2], o[3], o[4]) to 49 }
}
