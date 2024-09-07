package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.*
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.widgets.HorizontalRule
import com.github.ajalt.mordant.widgets.Text

/**
 * Functions for printing styled text to the terminal.
 *
 * Use functions like [print] and [println] instead of [kotlin.io.println] to automatically
 * downsample any [TextColors] and [TextStyles] you use in strings, and to render widgets like
 * [table] and [HorizontalRule].
 *
 * The default constructor for this class will detect the current terminal capabilities, but you can
 * override this detection by passing your desired value for `ansiLevel`, `hyperlinks`, and
 * `interactive`.
 */
class Terminal private constructor(
    /** The theme to use when rendering widgets */
    val theme: Theme,
    /** The number of spaces to use when printing tab characters. */
    val tabWidth: Int,
    /** The interface to use to interact with the system terminal. */
    val terminalInterface: TerminalInterface,
    private val forceWidth: Int?,
    private val forceHeight: Int?,
    private val nonInteractiveWidth: Int?,
    private val nonInteractiveHeight: Int?,
    /** The terminal capabilities that were detected or set in the constructor. */
    val info: TerminalInfo,
) {

    /**
     * @param ansiLevel The level of color support to use, or `null` to detect the level of the current terminal
     * @param theme The theme to use for widgets and styles like [success]
     * @param width The width to render widget and wrap text, or `null` to detect the current width.
     * @param height The height of the terminal to use when rendering widgets, or `null` to detect the current width.
     * @param nonInteractiveWidth The width to use when the terminal is not interactive, or `null` to use the default of 79 columns.
     * @param nonInteractiveHeight The height to use when the terminal is not interactive, or `null` to use the default of 24 rows.
     * @param hyperlinks whether to render hyperlinks using ANSI codes, or `null` to detect the capability
     * @param tabWidth The number of spaces to use for `\t` characters
     * @param interactive Set to true to always use color codes, even if stdout is redirected to a
     *   file, or `null` to detect the capability. This can be useful if you expect to call your
     *   program from some other source like gradle that forwards the output to the terminal.
     * @param terminalInterface The terminal interface to use to interact with the terminal.
     */
    constructor(
        ansiLevel: AnsiLevel? = null,
        theme: Theme = Theme.Default,
        width: Int? = null,
        height: Int? = null,
        nonInteractiveWidth: Int? = null,
        nonInteractiveHeight: Int? = null,
        hyperlinks: Boolean? = null,
        tabWidth: Int = 8,
        interactive: Boolean? = null,
        terminalInterface: TerminalInterface = STANDARD_TERM_INTERFACE,
    ) : this(
        theme = theme,
        tabWidth = tabWidth,
        terminalInterface = terminalInterface,
        forceWidth = width,
        forceHeight = height,
        nonInteractiveWidth = nonInteractiveWidth,
        nonInteractiveHeight = nonInteractiveHeight,
        info = terminalInterface.info(
            ansiLevel = ansiLevel,
            hyperlinks = hyperlinks,
            outputInteractive = interactive,
            inputInteractive = interactive,
        )
    )

    private val interceptors: MppAtomicRef<List<TerminalInterceptor>> =
        MppAtomicRef(emptyList())

    private val atomicSize: MppAtomicRef<Size> =
        MppAtomicRef(
            terminalInterface.detectSize(
                info,
                forceWidth,
                forceHeight,
                nonInteractiveWidth,
                nonInteractiveHeight
            )
        )


    /**
     * The current size of the terminal.
     *
     * This value is updated automatically whenever you print to the terminal.
     */
    val size: Size get() = atomicSize.value

    /**
     * Detect the current terminal's size and update [size].
     *
     * This is called automatically whenever you print to the terminal.
     */
    fun updateSize(): Size {
        return atomicSize.update {
            terminalInterface.detectSize(
                info,
                forceWidth,
                forceHeight,
                nonInteractiveWidth,
                nonInteractiveHeight
            )
        }.second
    }

    /**
     * Functions for controlling the terminal's cursor.
     *
     * If the terminal is not interactive, all the cursor functions are no-ops.
     */
    val cursor: TerminalCursor = when {
        info.interactive -> makePrintingTerminalCursor(this)
        else -> DisabledTerminalCursor
    }

    /**
     * Print a [message] to the terminal.
     *
     * Any contained [TextColors] and [TextStyles] will be automatically downsampled based on the
     * current terminal's [info].
     *
     * @param message The message to print as a string.
     * @param whitespace How to handle whitespace and line wrapping. By default, whitespace is
     *   printed unchanged and lines are not wrapped.
     * @param align How to align text in the terminal. By default, no alignment is performed.
     * @param overflowWrap How to wrap words longer than a single line. By default, long words are not wrapped.
     * @param width The width to wrap to if [whitespace] or [overflowWrap] are set. By default, this
     *   will use [info.width][TerminalInfo.width]
     * @param stderr Whether to print to stderr instead of stdout.
     */
    fun print(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
        stderr: Boolean = false,
    ) {
        rawPrint(render(message, whitespace, align, overflowWrap, width), stderr)
    }

    /**
     * Print a [message] to the terminal, followed by a line break.
     *
     * Any contained [TextColors] and [TextStyles] will be automatically downsampled based on the
     * current terminal's [info].
     *
     * @param message The message to print as a string.
     * @param whitespace How to handle whitespace and line wrapping. By default, whitespace is
     *   printed unchanged and lines are not wrapped.
     * @param align How to align text in the terminal. By default, no alignment is performed.
     * @param overflowWrap How to wrap words longer than a single line. By default, long words are not wrapped.
     * @param width The width to wrap to if [whitespace] or [overflowWrap] are set. By default, this
     *   will use [info.width][TerminalInfo.width]
     * @param stderr Whether to print to stderr instead of stdout.
     */
    fun println(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
        stderr: Boolean = false,
    ) {
        rawPrintln(render(message, whitespace, align, overflowWrap, width), stderr)
    }

    /**
     * Print a [widget] to the terminal.
     */
    fun print(widget: Widget, stderr: Boolean = false) {
        rawPrint(render(widget), stderr)
    }

    /**
     * Print a [widget] to the terminal, followed by a line break.
     */
    fun println(widget: Widget, stderr: Boolean = false) {
        rawPrintln(render(widget), stderr)
    }


    /**
     * Print a line break to the terminal.
     */
    fun println(stderr: Boolean = false) {
        sendPrintRequest(PrintRequest("", trailingLinebreak = true, stderr = stderr))
    }

    /**
     * Render a [message] to a string.
     *
     * Any contained [TextColors] and [TextStyles] will be automatically downsampled based on the
     * current terminal's [info].
     *
     * @param message The message to render as a string.
     * @param whitespace How to handle whitespace and line wrapping. By default, whitespace is
     *   printed unchanged and lines are not wrapped.
     * @param align How to align text in the terminal. By default, no alignment is performed.
     * @param overflowWrap How to wrap words longer than a single line. By default, long words are not wrapped.
     * @param width The width to wrap to if [whitespace] or [overflowWrap] are set. By default, this
     *   will use [info.width][TerminalInfo.width]
     */
    fun render(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ): String {
        return when (message) {
            is Lines -> renderLinesAnsi(message, info.ansiLevel, info.ansiHyperLinks)
            is Widget -> render(message)
            else -> render(Text(message.toString(), whitespace, align, overflowWrap, width))
        }
    }

    /** Render a [widget] as a string */
    fun render(widget: Widget): String {
        return renderLinesAnsi(widget.render(this), info.ansiLevel, info.ansiHyperLinks)
    }

    /**
     * Read a line of input from stdin.
     *
     * This is similar to [readlnOrNull], but is supported on NodeJS, and uses this terminal's
     * [interface][TerminalInterface.readLineOrNull], so it can be customized or mocked for testing.
     */
    fun readLineOrNull(hideInput: Boolean): String? {
        return terminalInterface.readLineOrNull(hideInput)
    }

    /**
     * Print a [message] to the terminal without parsing or rendering it.
     *
     * This is useful if you want to print ANSI escape codes manually.
     */
    fun rawPrint(message: String, stderr: Boolean = false) {
        sendPrintRequest(PrintRequest(message, false, stderr))
    }

    internal fun addInterceptor(interceptor: TerminalInterceptor) {
        interceptors.update { this + interceptor }
    }

    internal fun removeInterceptor(interceptor: TerminalInterceptor) {
        interceptors.update { filter { it != interceptor } }
    }

    private fun rawPrintln(message: String, stderr: Boolean) {
        sendPrintRequest(PrintRequest(message, true, stderr))
    }

    private fun sendPrintRequest(request: PrintRequest) {
        if (terminalInterface.shouldAutoUpdateSize()) updateSize()
        sendInterceptedPrintRequest(request, terminalInterface, interceptors.value)
    }
}
