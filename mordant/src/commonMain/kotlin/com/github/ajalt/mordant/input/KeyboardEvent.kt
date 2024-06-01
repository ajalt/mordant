package com.github.ajalt.mordant.input

// TODO: docs
// https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent
data class KeyboardEvent(
    val key: String,
    val ctrl: Boolean,
    val alt: Boolean, // `Option ⌥` key on mac
    val shift: Boolean,
    // maybe add a `data` field for escape sequences?
)
