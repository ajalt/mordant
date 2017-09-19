package com.github.ajalt.termcolors

import java.util.*


//https://github.com/Qix-/color-convert
// https://github.com/chalk/chalk
open class AnsiCode(val openCodes: IntArray, val closeCodes: IntArray) : (String) -> String {
    constructor(openCode: Int, closeCode: Int) : this(intArrayOf(openCode), intArrayOf(closeCode))

    val open: String get() = "${ESC}[${openCodes.joinToString(";")}m"
    val close: String get() = "${ESC}[${closeCodes.joinToString(";")}m"

    override fun toString() = open
    override fun invoke(text: String) = open + text + close

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

abstract class AnsiColorCode(openCodes: IntArray, closeCodes: IntArray) :
        AnsiCode(openCodes, closeCodes) {
    constructor(openCode: Int, closeCode: Int) : this(intArrayOf(openCode), intArrayOf(closeCode))

    abstract val bgOpenCodes: IntArray
    abstract val bgCloseCodes: IntArray
}

class Ansi16ColorCode(code: Int) : AnsiColorCode(code, 39) {
    override val bgOpenCodes get() = intArrayOf(openCodes[0] + 10)
    override val bgCloseCodes get() = intArrayOf(49)
}

class Ansi256ColorCode(code: Int) : AnsiColorCode(intArrayOf(38, 5, code), intArrayOf(39)) {
    override val bgOpenCodes get() = intArrayOf(48, 5, openCodes[2])
    override val bgCloseCodes get() = intArrayOf(49)
}

infix fun AnsiColorCode.on(bg: AnsiColorCode): AnsiCode {
    return AnsiCode(openCodes + bg.bgOpenCodes, closeCodes + bg.bgCloseCodes)
}

operator fun AnsiCode.plus(other: AnsiCode): AnsiCode {
    return AnsiCode(openCodes + other.openCodes, closeCodes + other.closeCodes)
}

private val ESC = (0x1b).toChar()
val reset = "${ESC}[0m"
val underline = AnsiCode(4, 24)

val black = Ansi16ColorCode(30)
val red = Ansi16ColorCode(31)
val green = Ansi16ColorCode(32)
val yellow = Ansi16ColorCode(33)
val blue = Ansi16ColorCode(34)
val purple = Ansi16ColorCode(35)
val cyan = Ansi16ColorCode(36)
val white = Ansi16ColorCode(37)

val brightBlack = Ansi16ColorCode(90)
val brightRed = Ansi16ColorCode(91)
val brightGreen = Ansi16ColorCode(92)
val brightYellow = Ansi16ColorCode(93)
val brightBlue = Ansi16ColorCode(94)
val brightPurple = Ansi16ColorCode(95)
val brightCyan = Ansi16ColorCode(96)
val brightWhite = Ansi16ColorCode(97)

/*
modifier: {
    reset: [0, 0],
    // 21 isn't widely supported and 22 does the same thing
    bold: [1, 22],
    dim: [2, 22],
    italic: [3, 23],
    underline: [4, 24],
    inverse: [7, 27],
    hidden: [8, 28],
    strikethrough: [9, 29]
},
color: {
    black: [30, 39],
    red: [31, 39],
    green: [32, 39],
    yellow: [33, 39],
    blue: [34, 39],
    magenta: [35, 39],
    cyan: [36, 39],
    white: [37, 39],
    gray: [90, 39],

    // Bright color
    redBright: [91, 39],
    greenBright: [92, 39],
    yellowBright: [93, 39],
    blueBright: [94, 39],
    magentaBright: [95, 39],
    cyanBright: [96, 39],
    whiteBright: [97, 39]
},
bgColor: {
    bgBlack: [40, 49],
    bgRed: [41, 49],
    bgGreen: [42, 49],
    bgYellow: [43, 49],
    bgBlue: [44, 49],
    bgMagenta: [45, 49],
    bgCyan: [46, 49],
    bgWhite: [47, 49],

    // Bright color
    bgBlackBright: [100, 49],
    bgRedBright: [101, 49],
    bgGreenBright: [102, 49],
    bgYellowBright: [103, 49],
    bgBlueBright: [104, 49],
    bgMagentaBright: [105, 49],
    bgCyanBright: [106, 49],
bgWhiteBright: [107, 49]
 */

class TermColors(val level: Level = Level.TRUECOLOR) {
    companion object {
        enum class Level {NONE, ANSI16, ANSI256, TRUECOLOR }
    }


}


fun main(args: Array<String>) {
    println("${red("wow")}, ${(green on blue)("that's")}, pretty, ${brightPurple("cool")}")

}
