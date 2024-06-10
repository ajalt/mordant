package com.github.ajalt.mordant.input

// https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h2-Mouse-Tracking
enum class MouseTracking {
    /**
     * Disable mouse tracking
     */
    Off,

    /**
     * Normal tracking mode sends an escape sequence on both button press and
     * release.  Modifier key (shift, ctrl, meta) information is also sent.
     */
    Normal,

    /**
     * Button-event tracking is essentially the same as normal tracking, but
     * xterm also reports button-motion events. Motion events are reported
     * only if the mouse pointer has moved to a different character cell.
     */
    Button,

    /**
     * Any-event mode is the same as button-event mode, except that all motion
     * events are reported, even if no mouse button is down.
     */
    Any
}
