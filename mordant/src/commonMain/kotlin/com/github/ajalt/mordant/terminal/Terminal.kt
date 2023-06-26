package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.makePrintingTerminalCursor
import com.github.ajalt.mordant.internal.renderLinesAnsi
import com.github.ajalt.mordant.internal.sendInterceptedPrintRequest
import com.github.ajalt.mordant.internal.synchronizeJvm
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
    val theme: Theme,
    val tabWidth: Int,
    private val terminalInterface: TerminalInterface,
    private val interceptors: MutableList<TerminalInterceptor>,
    private val lock: Any,
) {
    /**
     * @param ansiLevel The level of color support to use, or `null` to detect the level of the current terminal
     * @param theme The theme to use for widgets and styles like [success]
     * @param width The width to render widget and wrap text, or `null` to detect the current width.
     *   On JVM, you'll need to call [info.updateTerminalSize][TerminalInfo.updateTerminalSize] to
     *   detect the size correctly.
     * @param height The height of th terminal to use when rendering widgets, or `null` to detect the current width.
     *   On JVM, you'll need to call [info.updateTerminalSize][TerminalInfo.updateTerminalSize] to
     *   detect the size correctly.
     * @param hyperlinks whether to render hyperlinks using ANSI codes, or `null` to detect the capability
     * @param tabWidth The number of spaces to use for `\t` characters
     * @param interactive Set to true to always use color codes, even if stdout is redirected to a
     *   file, or `null` to detect the capability. This can be useful if you expect to call your
     *   program from some other source like gradle that forwards the output to the terminal.
     */
    constructor(
        ansiLevel: AnsiLevel? = null,
        theme: Theme = Theme.Default,
        width: Int? = null,
        height: Int? = null,
        hyperlinks: Boolean? = null,
        tabWidth: Int = 8,
        interactive: Boolean? = null,
    ) : this(
        theme,
        tabWidth,
        StdoutTerminalInterface(ansiLevel, width, height, hyperlinks, interactive)
    )

    /**
     * @param theme The theme to use for widgets and styles like [success]
     * @param tabWidth The number of spaces to use for `\t` characters
     * @param terminalInterface The [TerminalInterface] to use to read and write
     */
    constructor(
        theme: Theme,
        tabWidth: Int,
        terminalInterface: TerminalInterface,
    ) : this(theme, tabWidth, terminalInterface, mutableListOf(), Any())

    /**
     * @param theme The theme to use for widgets and styles like [success]
     * @param terminalInterface The [TerminalInterface] to use to read and write
     */
    constructor(
        theme: Theme,
        terminalInterface: TerminalInterface,
    ) : this(theme, 8, terminalInterface, mutableListOf(), Any())

    /**
     * @param terminalInterface The [TerminalInterface] to use to read and write
     */
    constructor(
        terminalInterface: TerminalInterface,
    ) : this(Theme.Default, 8, terminalInterface, mutableListOf(), Any())

    /**
     * The terminal capabilities that were detected or set in the constructor.
     */
    val info: TerminalInfo = terminalInterface.info

    /**
     * Colors and styles that are downsampled based on the current terminal [info].
     *
     * This can be used if you want to style a string that is going to be printed with
     * [kotlin.io.print]. If you're using the terminal's [print], you can use [TextColors] and
     * [TextStyles] directly instead.
     */
    val colors: TerminalColors = TerminalColors(info, theme)

    /**
     * Functions for controlling the terminal's cursor.
     *
     * If the terminal is not interactive, all the cursor functions are no-ops.
     */
    val cursor: TerminalCursor =
        if (info.interactive) makePrintingTerminalCursor(this) else DisabledTerminalCursor

    /**
     * Print a line styled with the theme's [success][Theme.success] style.
     */
    fun success(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        println(theme.success(message.toString()), whitespace, align, overflowWrap, width)
    }

    /**
     * Print a line styled with the theme's [danger][Theme.danger] style.
     */
    fun danger(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        println(theme.danger(message.toString()), whitespace, align, overflowWrap, width)
    }

    /**
     * Print a line styled with the theme's [warning][Theme.warning] style.
     */
    fun warning(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        println(theme.warning(message.toString()), whitespace, align, overflowWrap, width)
    }

    /**
     * Print a line styled with the theme's [info][Theme.info] style.
     */
    fun info(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        println(theme.info(message.toString()), whitespace, align, overflowWrap, width)
    }

    /**
     * Print a line styled with the theme's [muted][Theme.muted] style.
     */
    fun muted(
        message: Any?,
        whitespace: Whitespace = Whitespace.PRE,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null,
    ) {
        println(theme.muted(message.toString()), whitespace, align, overflowWrap, width)
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
    fun println() {
        sendPrintRequest(PrintRequest("", trailingLinebreak = true, stderr = false))
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

    inline fun <T> prompt(
        prompt: String,
        default: T? = null,
        showDefault: Boolean = true,
        showChoices: Boolean = true,
        hideInput: Boolean = false,
        choices: Collection<T> = emptyList(),
        promptSuffix: String = ": ",
        invalidChoiceMessage: String = "Invalid value, choose from ",
        crossinline convert: (String) -> ConversionResult<T>,
    ): T? {
        return object : Prompt<T>(
            prompt = prompt,
            terminal = this,
            default = default,
            showDefault = showDefault,
            showChoices = showChoices,
            hideInput = hideInput,
            choices = choices,
            promptSuffix = promptSuffix,
            invalidChoiceMessage = invalidChoiceMessage,
        ) {
            override fun convert(input: String): ConversionResult<T> {
                return convert(input)
            }
        }.ask()
    }

    /**
     * Print a [prompt] to the user and return the value they enter.
     *
     * To customize the prompt behavior, see the [Prompt] class.
     *
     * @param prompt The message asking for input to show the user
     * @param default The value to return if the user enters an empty line, or `null` to require a value
     * @param showDefault If true and a [default] is specified, add the default value to the prompt
     * @param hideInput If true, the user's input will be treated like a password and hidden from
     *   the screen. This value will be ignored on platforms where it is not supported.
     * @param choices The set of values that the user must choose from.
     * @param promptSuffix A string to append after [prompt] when showing the user the prompt
     * @param invalidChoiceMessage The message to show the user if [choices] is specified,
     *   and they enter a value that isn't one of the choices.
     */
    fun prompt(
        prompt: String,
        default: String? = null,
        showDefault: Boolean = true,
        showChoices: Boolean = true,
        hideInput: Boolean = false,
        choices: Collection<String> = emptyList(),
        promptSuffix: String = ": ",
        invalidChoiceMessage: String = "Invalid value, choose from ",
    ): String? = prompt(
        prompt,
        default,
        showDefault,
        showChoices,
        hideInput,
        choices,
        promptSuffix,
        invalidChoiceMessage
    ) { ConversionResult.Valid(it) }

    /**
     * Print a [message] to the terminal without parsing or rendering it.
     *
     * This is useful if you want to print ANSI escape codes manually.
     */
    fun rawPrint(message: String, stderr: Boolean = false) {
        sendPrintRequest(PrintRequest(message, false, stderr))
    }

    internal fun addInterceptor(interceptor: TerminalInterceptor) {
        synchronizeJvm(lock) {
            interceptors += interceptor
        }
    }

    internal fun removeInterceptor(interceptor: TerminalInterceptor) {
        synchronizeJvm(lock) {
            interceptors.remove(interceptor)
        }
    }

    private fun rawPrintln(message: String, stderr: Boolean) {
        sendPrintRequest(PrintRequest(message, true, stderr))
    }

    private fun sendPrintRequest(request: PrintRequest) {
        synchronizeJvm(lock) {
            sendInterceptedPrintRequest(request, terminalInterface, interceptors)
        }
    }
}
