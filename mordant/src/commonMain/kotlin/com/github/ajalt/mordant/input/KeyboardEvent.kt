package com.github.ajalt.mordant.input

// TODO: docs
// https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent
data class KeyboardEvent(
    val key: String,
    val ctrl: Boolean = false,
    val alt: Boolean = false, // `Option ⌥` key on mac
    val shift: Boolean = false,
    // maybe add a `data` field for escape sequences?
)

fun KeyboardEvent.isCtrlC(): Boolean {
    return key == "c" && ctrl && !alt && !shift
}
