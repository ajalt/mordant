package com.github.ajalt.mordant.input

sealed class InputEvent

// TODO: docs
// https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent
data class KeyboardEvent(
    val key: String,
    val ctrl: Boolean = false,
    val alt: Boolean = false, // `Option ⌥` key on mac
    val shift: Boolean = false,
    // maybe add a `data` field for escape sequences?
): InputEvent()

val KeyboardEvent.isCtrlC: Boolean
    get() = key == "c" && ctrl && !alt && !shift


// TODO: docs
// https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent
data class MouseEvent(
    val x: Int,
    val y: Int,
    val left: Boolean = false,
    val right: Boolean = false,
    val middle: Boolean = false,
    val mouse4: Boolean = false,
    val mouse5: Boolean = false,
    val wheelUp: Boolean = false,
    val wheelDown: Boolean = false,
    val wheelLeft: Boolean = false,
    val wheelRight: Boolean = false,
    val ctrl: Boolean = false,
    val alt: Boolean = false,
    val shift: Boolean = false,
): InputEvent()
