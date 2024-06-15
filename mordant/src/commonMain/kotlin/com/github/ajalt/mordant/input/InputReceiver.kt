package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.terminal.Terminal

/**
 * An object that can receive input events.
 */
interface InputReceiver<T> {
    sealed class Status<out T> {
        companion object {
            val Finished = Finished(Unit)
        }
        data object Continue : Status<Nothing>()
        data class Finished<T>(val result: T) : Status<T>()
    }
    val terminal: Terminal
    fun receiveEvent(event: InputEvent): Status<T> = Status.Continue
    fun cancel() {}
}
