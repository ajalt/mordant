package com.github.ajalt.mordant.input

// TODO: docs
interface InputReceiver<T> {
    sealed class Status<out T> {
        data object Continue : Status<Nothing>()
        class Finished<T>(val result: T) : Status<T>()
    }
    fun onInput(event: KeyboardEvent): Status<T>
}
