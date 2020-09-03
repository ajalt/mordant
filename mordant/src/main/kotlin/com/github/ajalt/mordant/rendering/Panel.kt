package com.github.ajalt.mordant.rendering

//import com.github.ajalt.mordant.Terminal
//import com.github.ajalt.mordant.rendering.internal.setWidth
//
//class Panel(
//        content: Renderable,
//        private val borders: Borders = Borders.SQUARE,
//        private val expand: Boolean = true,
//        private val borderStyle: TextStyle = TextStyle(),
//        padding: Padding = Padding()
//) : Renderable {
//    private val content: Renderable = Padded.get(content, padding)
//
//    override fun render(t: Terminal, width: Int): Lines {
//        val maxContentWidth = width - 2
//        val measurement = content.measure(t, maxContentWidth)
//        val lines = content.render(t, maxContentWidth).toLines()
//
//        val contentWidth = when {
//            expand -> maxContentWidth
//            else -> measurement.first.coerceAtMost(maxContentWidth)
//        }
//
//        lines.setWidth(contentWidth)
//
//        lines.add(0, mutableListOf(borders.renderTop(listOf(width))))
//
//        for (line in lines)
//    }
//
//    override fun measure(t: Terminal, width: Int): IntRange {
//        val measurement = content.measure(t, width - 2)
//
//        return if (expand) {
//            (measurement.last + 1)..(measurement.last + 1)
//        } else {
//            (measurement.first + 1)..(measurement.last + 1)
//        }
//    }
//}
