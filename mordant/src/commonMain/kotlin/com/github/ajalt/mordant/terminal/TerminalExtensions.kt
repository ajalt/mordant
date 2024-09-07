package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.OverflowWrap
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Whitespace

/**
 * Print a line styled with the theme's [danger][Theme.danger] style.
 */
fun Terminal.danger(
    message: Any?,
    whitespace: Whitespace = Whitespace.PRE,
    align: TextAlign = TextAlign.NONE,
    overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
    width: Int? = null,
    stderr: Boolean = false,
) {
    println(theme.danger(message.toString()), whitespace, align, overflowWrap, width, stderr)
}

/**
 * Print a line styled with the theme's [warning][Theme.warning] style.
 */
fun Terminal.warning(
    message: Any?,
    whitespace: Whitespace = Whitespace.PRE,
    align: TextAlign = TextAlign.NONE,
    overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
    width: Int? = null,
    stderr: Boolean = false,
) {
    println(theme.warning(message.toString()), whitespace, align, overflowWrap, width, stderr)
}

/**
 * Print a line styled with the theme's [info][Theme.info] style.
 */
fun Terminal.info(
    message: Any?,
    whitespace: Whitespace = Whitespace.PRE,
    align: TextAlign = TextAlign.NONE,
    overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
    width: Int? = null,
    stderr: Boolean = false,
) {
    println(theme.info(message.toString()), whitespace, align, overflowWrap, width, stderr)
}

/**
 * Print a line styled with the theme's [muted][Theme.muted] style.
 */
fun Terminal.muted(
    message: Any?,
    whitespace: Whitespace = Whitespace.PRE,
    align: TextAlign = TextAlign.NONE,
    overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
    width: Int? = null,
    stderr: Boolean = false,
) {
    println(theme.muted(message.toString()), whitespace, align, overflowWrap, width, stderr)
}

/**
 * Print a line styled with the theme's [success][Theme.success] style.
 */
fun Terminal.success(
    message: Any?,
    whitespace: Whitespace = Whitespace.PRE,
    align: TextAlign = TextAlign.NONE,
    overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
    width: Int? = null,
    stderr: Boolean = false,
) {
    println(theme.success(message.toString()), whitespace, align, overflowWrap, width, stderr)
}
