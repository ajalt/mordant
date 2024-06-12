package com.github.ajalt.mordant.input

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
    fun onEvent(event: InputEvent): Status<T> = Status.Continue
    fun cancel() {}
}
