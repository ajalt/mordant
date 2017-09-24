package com.github.ajalt.termcolors.samples

import com.github.ajalt.termcolors.Ansi16ColorCode
import com.github.ajalt.termcolors.Ansi256ColorCode
import com.github.ajalt.termcolors.TermColors

fun demo() = TermColors(TermColors.Level.TRUECOLOR).run {
    val title = (bold + underline)

    println(title("text styles\n"))
    println("${bold("bold")} ${dim("dim")} ${italic("italic")} ${underline("underline")} " +
            "${inverse("inverse")} ${strikethrough("strikethrough")}")


    println(title("\n\n16 color mode\n"))
    val colors16 = ((30..37) + (90..97)).map { Ansi16ColorCode(it) }
    for (fg in colors16) {
        for (bg in colors16) {
            print("${(fg on bg)(" ::: ")} ")
        }
        println()
    }


    println(title("\n\n256 color mode\n"))
    for (row in (16..46 step 6) + (124..154 step 6)) {
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
    for (v in 0..100 step 3) {
        for (h in 0..360 step 3) {
            print(hsv(h, 100, 100 - v).bg(" "))
        }
        println()
    }


    println(title("\n\ntrue color 24-bit mode grayscale\n"))
    for (i in 1..120) {
        print(gray(i / 120.0).bg(" "))
    }
    println()
}

infix fun Int.positiveMod(mod: Int): Int {
    var x = this
    while (x < 0) x += mod
    return x % mod
}

fun rainbow() = TermColors(TermColors.Level.TRUECOLOR).run {
    val lines = """
                                  :M              MM
                                O?                    MM
                               M                        ,N8
                             N                              M
                            M          M         ,            I
                           ,       M  M 7     M  M             M
                          : MMNI  7MM M M     ~M  M             8
                         ~                ,NM  M,, M        =     :
                         MMMMN                 M ,MMM              ?
                        MMM  NMMM      M    ,MMMMMM   Z     ,
                        M                MMMM?     M   M    M
                       , MMMMMMM       M            M    M  :        M
                       :   MMMM,   M   M MMMMMM=        M  M M        M
                     +,      N~   M Z      N   MM      ,     MM        =
                    O         M,   M  M      MM 8D     M               M
                    M       N       MM                 M                M
                   MM          MM                      M                N
                  M M ,    MMMMMMM,                    M                 M
                 M  DM    N         M                  M                 M
                M    ~   =MMMMMMM    M
               M            MMMMMMMM= M                 M     M        M  M
              M     M             NMMMMN     M      M M M     M        D  8
                    M     MMMM                     M    ,     M         8
            ,,     ?                         :           M              M  M
            M      D                         M           M     M         M M
           M      M        M                             ,                ~M
                  M       M                               M     M         ZN
          M N     M       M                               M                M
            M                                             ?      M          M
         D  M      M        ,NMD                        =        M          M
         M  M       M                                  M         8
         M  M                           M             M   I            M     N
         M  :  M     :                     MMMMDD8MM      M      M     M     M
             Z M                                          M      M     M     O
          M  M  M   M                                    M      ~      M
              7                                                 =      M
           M  N  7 M                                   M      ?
            M  M                                   MMM      ,M        M
             M  M M                                    NMMN          M       O
               M  N                                      :          M        M
               +M                                         M       M:         ~
            ,N                                              MMM~            M
          M      N                                          M M            D
        M        M                                          M   M         M
      M          M                                          M     M      M
    M            M                                          N      M  MM
              M= M                                          :       M
         M NM    M                                          :        M
 _______    ___      .______    __    __   __        ______    __    __       _______.
|   ____|  /   \     |   _  \  |  |  |  | |  |      /  __  \  |  |  |  |     /       |
|  |__    /  ^  \    |  |_)  | |  |  |  | |  |     |  |  |  | |  |  |  |    |   (----`
|   __|  /  /_\  \   |   _  <  |  |  |  | |  |     |  |  |  | |  |  |  |     \   \
|  |    /  _____  \  |  |_)  | |  `--'  | |  `----.|  `--'  | |  `--'  | .----)   |
|__|   /__/     \__\ |______/   \______/  |_______| \______/   \______/  |_______/
""".lines()
    val ticksPerSecond = 20
    val seconds = 6
    print("\u001b[?25l") // hide cursor
    for (tick in 0..(ticksPerSecond * seconds)) {
        if (tick != 0) print("\u001b[${lines.size}A") // move cursor up
        for ((lineno, line) in lines.withIndex()) {
            for ((i, c) in line.iterator().withIndex()) {
                print(hsv((tick * -10 + lineno * -10 + (i * 5)) positiveMod 360, 100, 100)(c.toString()))
            }
            println()
            print("\u001b[${line.length + 1}D") // move cursor to start of line
        }
        System.out.flush()
        Thread.sleep((1000.0 / ticksPerSecond).toLong())
    }
    print("\u001b[?25h") // show cursor
}


fun main(args: Array<String>) {
    if (args.size > 1 && args[1] == "rainbow") {
        rainbow()
    } else {
        demo()
    }
}
