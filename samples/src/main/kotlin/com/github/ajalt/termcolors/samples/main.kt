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
            print("${(fg on bg)(" ::: ")} ")
        }
        println()
    }

    println(title("\n\n256 color mode\n"))

    for (row in (16..46 step 6)+(124..154 step 6)) {
        for (space in arrayOf(true, false, true)) {
            for (block in (row..(row + 72) step 36)) {
                for (cell in (block..(block + 5))) {
                    val v = if (space) "   " else String.format("%03d", cell)
                    print((Ansi256ColorCode(242) on Ansi256ColorCode(cell))(" $v "))
                }
                print("  ")
            }
            println()
        }
        if (row == 46) println()
    }
    println()

    println(title("\n\n256 color mode grayscale\n"))
    for (i in 232..255) {
        print(Ansi256ColorCode(i).bg("   "))
    }
    println()

    println(title("\n\n${red("R")}${green("G")}${blue("B")} true color 24-bit mode\n"))

    for (ri in (0..255 step 128)) {
        for (g in 0..255 step (255 / 18)) {
            for (r in ri..(ri + 128) step (255 / 6)) {
                print("  ")
                for (b in 0..255 step (255 / 31)) {
                    print(rgb(r, g, b).bg(" "))
                }
            }
            println()
        }
        println()
    }

    println(title("\n\ntrue color 24-bit mode grayscale\n"))

    for (i in 1..136) {
        print(gray(i / 136.0).bg(" "))
    }
    println()
}



fun main(args: Array<String>) {
    demo()
}
