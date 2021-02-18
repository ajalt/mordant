package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.makePrintingTerminalCursor
import com.github.ajalt.mordant.internal.renderLinesAnsi
import com.github.ajalt.mordant.internal.sendInterceptedPrintRequest
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.widgets.Text

@OptIn(ExperimentalTerminalApi::class)
class Terminal(
    val theme: Theme = Theme.Default,
    val tabWidth: Int = 8,
    private val terminalInterface: TerminalInterface = StdoutTerminalInterface(null, null, null, null, null),
) {
    constructor(
        ansiLevel: AnsiLevel? = null,
        theme: Theme = Theme.Default,
        width: Int? = null,
        height: Int? = null,
        hyperlinks: Boolean? = null,
        tabWidth: Int = 8,
        interactive: Boolean? = null,
    ) : this(theme, tabWidth, StdoutTerminalInterface(ansiLevel, width, height, hyperlinks, interactive))

    val info: TerminalInfo = terminalInterface.info
    val colors: TerminalColors = TerminalColors(info)
    val cursor: TerminalCursor = if (info.interactive) makePrintingTerminalCursor(this) else DisabledTerminalCursor
    private val interceptors: MutableList<TerminalInterceptor> = mutableListOf()
    private val lock = Any()

    fun success(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        println(message, theme.style("success"), whitespace, align, overflowWrap, width)
    }

    fun danger(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        println(message, theme.style("danger"), whitespace, align, overflowWrap, width)
    }

    fun warning(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        println(message, theme.style("warning"), whitespace, align, overflowWrap, width)
    }

    fun renderWarning(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ): String {
        return render(message, theme.style("warning"), whitespace, align, overflowWrap, width)
    }

    fun info(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        println(message, theme.style("info"), whitespace, align, overflowWrap, width)
    }

    fun muted(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        println(message, theme.style("muted"), whitespace, align, overflowWrap, width)
    }

    fun print(
        message: Any?,
        style: TextStyle = DEFAULT_STYLE,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        rawPrint(render(message, style, whitespace, align, overflowWrap, width))
    }

    fun println(
        message: Any?,
        style: TextStyle = DEFAULT_STYLE,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        rawPrintln(render(message, style, whitespace, align, overflowWrap, width))
    }

    fun print(widget: Widget) {
        rawPrint(render(widget))
    }

    fun println(widget: Widget) {
        rawPrintln(render(widget))
    }

    fun renderSuccess(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ): String {
        return render(message, theme.style("success"), whitespace, align, overflowWrap, width)
    }

    fun renderDanger(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ): String {
        return render(message, theme.style("danger"), whitespace, align, overflowWrap, width)
    }

    fun renderInfo(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ): String {
        return render(message, theme.style("info"), whitespace, align, overflowWrap, width)
    }

    fun renderMuted(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ): String {
        return render(message, theme.style("muted"), whitespace, align, overflowWrap, width)
    }

    fun render(
        message: Any?,
        style: TextStyle = DEFAULT_STYLE,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ): String {
        return when (message) {
            is Lines -> renderLinesAnsi(message, info.ansiLevel, info.ansiHyperLinks)
            is Widget -> render(message)
            else -> render(Text(message.toString(), style, whitespace, align, overflowWrap, width))
        }
    }

    fun render(widget: Widget): String {
        return renderLinesAnsi(widget.render(this), info.ansiLevel, info.ansiHyperLinks)
    }

    fun println() {
        sendPrintRequest(PrintRequest("", true))
    }

    internal fun addInterceptor(interceptor: TerminalInterceptor) {
        interceptors += interceptor
    }

    internal fun removeInterceptor(interceptor: TerminalInterceptor) {
        interceptors.remove(interceptor)
    }

    private fun rawPrintln(message: String) {
        sendPrintRequest(PrintRequest(message, true))
    }

    private fun rawPrint(message: String) {
        sendPrintRequest(PrintRequest(message, false))
    }

    private fun sendPrintRequest(request: PrintRequest) {
        sendInterceptedPrintRequest(request, terminalInterface, interceptors, lock)
    }
}
