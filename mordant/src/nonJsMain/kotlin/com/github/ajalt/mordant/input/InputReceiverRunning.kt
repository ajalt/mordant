package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.terminal.Terminal

/**
 * Enter raw mode, read input from the [terminal] for this [InputReceiver] until it returns a
 * result, then exit raw mode.
 *
 * @return the result of the completed receiver, or `null` if the terminal is not interactive or the
 * input could not be read.
 */
fun <T> InputReceiver<T>.receiveInput(
    terminal: Terminal,
    mouseTracking: MouseTracking = MouseTracking.Off,
): T? {
    terminal.enterRawMode(mouseTracking)?.use { rawMode ->
        while (true) {
            val event = rawMode.readEvent() ?: return null
            when (val status = onEvent(event)) {
                is InputReceiver.Status.Continue -> continue
                is InputReceiver.Status.Finished -> return status.result
            }
        }
    }
    return null
}

/**
 * Enter raw mode, read input and pass any [KeyboardEvent]s to [block] until it returns a
 * result, then exit raw mode.
 *
 * @return the result of the completed receiver, or `null` if the terminal is not interactive or the
 * input could not be read.
 */
inline fun <T> Terminal.receiveKeyEvents(
    crossinline block: (KeyboardEvent) -> InputReceiver.Status<T>,
): T? {
    return receiveEvents(MouseTracking.Off) { event ->
        when (event) {
            is KeyboardEvent -> block(event)
            else -> InputReceiver.Status.Continue
        }
    }
}

/**
 * Enter raw mode, read input and pass any [MouseEvent]s to [block] until it returns a
 * result, then exit raw mode.
 *
 * @param mouseTracking The type of mouse tracking to enable.
 * @return the result of the completed receiver, or `null` if the terminal is not interactive or the
 * input could not be read.
 */
inline fun <T> Terminal.receiveMouseEvents(
    mouseTracking: MouseTracking = MouseTracking.Normal,
    crossinline block: (MouseEvent) -> InputReceiver.Status<T>,
): T? {
    require(mouseTracking != MouseTracking.Off) {
        "Mouse tracking must be enabled to receive mouse events"
    }
    return receiveEvents(mouseTracking) { event ->
        when (event) {
            is MouseEvent -> block(event)
            else -> InputReceiver.Status.Continue
        }
    }
}

/**
 * Enter raw mode, read input and pass any [InputEvent]s to [block] until it returns a
 * result, then exit raw mode.
 *
 * @param mouseTracking The type of mouse tracking to enable.
 * @return the result of the completed receiver, or `null` if the terminal is not interactive or the
 * input could not be read.
 */
inline fun <T> Terminal.receiveEvents(
    mouseTracking: MouseTracking = MouseTracking.Normal,
    crossinline block: (InputEvent) -> InputReceiver.Status<T>,
): T? {
    return object : InputReceiver<T> {
        override fun onEvent(event: InputEvent): InputReceiver.Status<T> {
            return block(event)
        }
    }.receiveInput(this, mouseTracking)
}
