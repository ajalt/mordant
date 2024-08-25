package com.github.ajalt.mordant.rendering

enum class TextAlign {
    /** Align text to the left side of the widget, and include trailing whitespace so that all lines are the lame length. */
    LEFT,

    /** Align text to the right side of the widget */
    RIGHT,

    /** Align text to the center of the widget */
    CENTER,

    /**
     * Align text to both sides of the widget, adding spaces between words.
     *
     * For Widgets other than `Text`, this is treated the same as [CENTER].
     */
    JUSTIFY,

    /** Align text to the left side of the widget, and don't include trailing whitespace */
    NONE
}

enum class VerticalAlign {
    /** Align widgets vertically to the top of the layout */
    TOP,

    /** Align widgets vertically to the middle of the layout */
    MIDDLE,

    /** Align widgets vertically to the bottom of the layout */
    BOTTOM
}
