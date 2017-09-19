package com.github.ajalt.termcolors



fun main(args: Array<String>) {
    val t = TermColors()
    with(t) {
        //        println("${red("wow")}, ${(green on blue)("that's")} pretty ${rgb("#916262")("cool")}")
//        for (i in 0..100) {
//            print(gray(i * 0.01).bg(" "))
//        }
//        println()
        println((white on green)("outer with ${(black on gray)("some inner text")}") + "that is nested")
    }
}
