import com.github.ajalt.mordant.components.RawRenderable
import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

abstract class Animation<T>(private val terminal: Terminal) {
    private var size: Pair<Int, Int>? = null

    protected abstract fun renderData(data: T): Renderable

    fun clear() {
        getClear(0, 0)?.let { terminal.print(RawRenderable(it)) }
    }

    private fun getClear(renderedHeight: Int, renderedWidth: Int): String? {
        val (height, width) = size ?: return null
        return terminal.cursor.getMoves {
            startOfLine()
            up(height)

            if (height > renderedHeight || width > renderedWidth) {
                clearScreenAfterCursor()
            }
        }
    }

    fun update(data: T) {
        val rendered = renderData(data).render(terminal)
        size = rendered.height to rendered.width
        val codes = getClear(rendered.height, rendered.width)
        val combined = RawRenderable(codes + terminal.render(rendered))
        terminal.print(combined)
    }
}

inline fun <T> Terminal.animation(crossinline draw: (T) -> Renderable): Animation<T> {
    return object : Animation<T>(this) {
        override fun renderData(data: T): Renderable = draw(data)
    }
}

inline fun <T> Terminal.textAnimation(
        style: TextStyle = TextStyle(),
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
        tabWidth: Int? = null,
        crossinline draw: (T) -> String,
): Animation<T> {
    return object : Animation<T>(this) {
        override fun renderData(data: T): Renderable = Text(draw(data), style, whitespace, align, overflowWrap, width, tabWidth)
    }
}
