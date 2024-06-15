package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.animation.StoppableAnimation
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

    /**
     * The terminal that this receiver is reading input from.
     */
    val terminal: Terminal

    /**
     * Receive an input event.
     *
     * @param event The input event to process
     * @return [Status.Continue] to continue receiving events, or [Status.Finished] to stop.
     */
    fun receiveEvent(event: InputEvent): Status<T>
}

/**
 * An [InputReceiver] that is also an [StoppableAnimation].
 */
interface InputReceiverAnimation<T> : InputReceiver<T>, StoppableAnimation
