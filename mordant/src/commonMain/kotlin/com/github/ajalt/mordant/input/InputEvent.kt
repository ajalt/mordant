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
    val buttons: Int,
    val ctrl: Boolean = false,
    val alt: Boolean = false,
    val shift: Boolean = false,
): InputEvent()

val MouseEvent.leftPressed: Boolean get() = buttons and 1 != 0
val MouseEvent.rightPressed: Boolean get() = buttons and 2 != 0
val MouseEvent.middlePressed: Boolean get() = buttons and 4 != 0
val MouseEvent.mouse4Pressed: Boolean get() = buttons and 8 != 0
val MouseEvent.mouse5Pressed: Boolean get() = buttons and 16 != 0

