package com.github.ajalt.mordant.rendering

//import com.github.ajalt.mordant.Terminal
//
//data class Padding(val top: Int = 0, val right: Int = 0, val bottom: Int = 0, val left: Int = 0) {
//    init {
//        require(top >= 0) { "Invalid negative top padding"}
//        require(right >= 0) { "Invalid negative right padding"}
//        require(bottom >= 0) { "Invalid negative bottom padding"}
//        require(left >= 0) { "Invalid negative left padding"}
//    }
//
//    companion object {
//        fun all(padding: Int): Padding = Padding(padding, padding, padding, padding)
//        fun symmetrical(vertical: Int = 0, horizontal: Int = 0): Padding = Padding(vertical, horizontal, vertical, horizontal)
//    }
//
//    val isEmpty = top == 0 && right == 0 && bottom == 0 && left == 0
//}
//
//internal class Padded(private val content: Renderable, private val padding: Padding) : Renderable {
//    companion object {
//        fun get(content: Renderable, padding: Padding) =  if (padding.isEmpty) content else Padded(content, padding)
//    }
//    override fun render(t: Terminal, width: Int): Lines {
//        val renderedContent = content.render(t, width)
//        val lines = renderedContent.toLines()
//
//        val blank = Span.line()
//        val output = ArrayList<Span>(renderedContent.size + lines.size * padding.left.coerceAtMost(1) + lines.size * padding.right.coerceAtMost(1) + padding.bottom)
//        val left = if (padding.left > 0) Span.word(" ".repeat(padding.left)) else null
//        val right = if (padding.right > 0) Span.word(" ".repeat(padding.right)) else null
//
//        repeat(padding.top) { output.add(blank) }
//
//        for (line in lines) {
//            if (left != null) output += left
//            output += line.subList(0, line.lastIndex)
//            if (right != null) output += right
//            output += line.last()
//        }
//
//        repeat(padding.bottom) { output.add(blank) }
//
//        return output
//    }
//
//    override fun measure(t: Terminal, width: Int): IntRange {
//        val paddingWidth = padding.left + padding.right
//        val measurement = content.measure(t, width - paddingWidth)
//        return (measurement.first + paddingWidth)..(measurement.last + paddingWidth)
//    }
//}
