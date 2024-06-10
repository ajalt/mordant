package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.terminal.Terminal

/**
 * Read input from the [terminal], and feed to this [InputReceiver] until it returns a result.
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
