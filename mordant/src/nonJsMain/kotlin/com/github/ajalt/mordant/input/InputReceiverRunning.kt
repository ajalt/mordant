package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.terminal.Terminal

/**
 * Read input from the [terminal], and feed to this [InputReceiver] until it returns a result.
 *
 * @return the result of the completed receiver, or `null` if the terminal is not interactive or the
 * input could not be read.
 */
fun <T> InputReceiver<T>.run(terminal: Terminal): T? {
    terminal.enterRawMode()?.use { rawMode ->
        while (true) {
            val event = rawMode.readKey() ?: return null
            when (val status = onInput(event)) {
                is InputReceiver.Status.Continue -> continue
                is InputReceiver.Status.Finished -> return status.result
            }
        }
    }
    return null
}
