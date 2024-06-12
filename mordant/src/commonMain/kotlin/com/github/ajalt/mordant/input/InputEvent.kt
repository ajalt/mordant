package com.github.ajalt.mordant.input

sealed class InputEvent

/**
 * An event representing a single key press, including modifiers keys.
 *
 * This class uses the same format as the web
 * [KeyboardEvent](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent).
 *
 * Keep in mind that the not all modifier combinations or special keys are reported by all
 * terminals.
 */
// https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent
data class KeyboardEvent(
    /**
     * A string describing the key pressed.
     *
     * - If the key is a printable character, this will be the character itself.
     * - If the key is a special key, this will be a string describing the key, like `"ArrowLeft"`.
     *   The full list of possible values is available at
     *   [MDN](https://developer.mozilla.org/en-US/docs/Web/API/UI_Events/Keyboard_event_key_values), but
     *   not all terminals will report all keys.
     * - If the key cannot be identified, the value is `"Unidentified"`.
     *
     */
    val key: String,
    /** Whether the `Control` key is pressed */
    val ctrl: Boolean = false,
    /** Whether the Alt key (`Option ⌥` on macOS) is pressed */
    val alt: Boolean = false, // `Option ⌥` key on mac
    /** Whether the Shift key is pressed */
    val shift: Boolean = false,
) : InputEvent()

/** Whether this event represents a `Ctrl+C` key press. */
val KeyboardEvent.isCtrlC: Boolean
    get() = key == "c" && ctrl && !alt && !shift


/**
 * An event representing a single mouse event.
 *
 * This includes button press, release, and movement events.
 */
data class MouseEvent(
    /**
     * The x coordinate of the mouse event, where `0` is the leftmost column.
     */
    val x: Int,
    /**
     * The y coordinate of the mouse event, where `0` is the top row.
     */
    val y: Int,
    /** `true` if the left mouse button is pressed */
    val left: Boolean = false,
    /** `true` if the right mouse button is pressed */
    val right: Boolean = false,
    /** `true` if the middle mouse button (usually clicking the mouse wheel) is pressed */
    val middle: Boolean = false,
    /**
     * `true` if the fourth mouse button is pressed. This is often the "browse forward" button, and
     * isn't reported on all platforms.
     */
    val mouse4: Boolean = false,
    /**
     * `true` if the fifth mouse button is pressed. This is often the "browse backward" button, and
     * isn't reported on all platforms.
     */
    val mouse5: Boolean = false,
    /**
     * `true` if the mouse wheel moved up (away from the user).
     */
    val wheelUp: Boolean = false,
    /**
     * `true` if the mouse wheel moved down (towards the user).
     */
    val wheelDown: Boolean = false,
    /**
     * `true` if the horizontal mouse wheel moved left. This is not reported on all platforms.
     */
    val wheelLeft: Boolean = false,
    /**
     * `true` if the horizontal mouse wheel moved right. This is not reported on all platforms.
     */
    val wheelRight: Boolean = false,
    /** `true` if the `Control` key is pressed */
    val ctrl: Boolean = false,
    /** `true` if the Alt key (`Option ⌥` on macOS) is pressed */
    val alt: Boolean = false,
    /** `true` if the Shift key is pressed */
    val shift: Boolean = false,
) : InputEvent()
