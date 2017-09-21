package com.github.ajalt.termcolors.samples

import com.github.ajalt.termcolors.Ansi16ColorCode
import com.github.ajalt.termcolors.Ansi256ColorCode
import com.github.ajalt.termcolors.TermColors

fun demo() = TermColors(TermColors.Level.TRUECOLOR).run {
    val title = (white + bold)
    println(title("16 color mode\n"))
    val colors16 = ((30..37).toList() + (90..97)).map { Ansi16ColorCode(it) }
    for (fg in colors16) {
        for (bg in colors16) {
            print("${(fg on bg)(" ABC ")} ")
        }
        println()
    }

    println(title("\n\n256 color mode\n"))

    fun printRow(row: Int, space: Boolean) {
        for (i in 0..35) {
            if (i % 6 == 0) print(" ")
            val col = row + i + ((i / 6) * 30)
            val v = if (space) "   " else String.format("%03d", col)
            print((Ansi256ColorCode(242) on Ansi256ColorCode(col))(" $v "))
        }
    }
    for (row in 16..46 step 6) {
        printRow(row, true)
        printRow(row, false)
        printRow(row, true)
        println()
    }

    println(title("\n\n256 color mode grayscale\n"))
    for (i in 232..255) {
        print(Ansi256ColorCode(i).bg("   "))
    }
    println()

    println(title("\n\n${red("R")}${green("G")}${blue("B")} true color 24-bit mode\n"))

    for (g in 0..255 step (255 / 18)) {
        for (r in 0..255 step (255 / 6)) {
            print(" ")
            for (b in 0..255 step (255 / 31)) {
                print(rgb(r,g,b).bg(" "))
            }
        }
        println()
    }

    println(title("\n\ntrue color 24-bit mode grayscale\n"))

    for(i in 0..255 step 2) {
        print(rgb(i,i,i).bg(" "))
    }
    println()

}


fun main(args: Array<String>) {
    demo()
}
