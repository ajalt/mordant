package com.github.ajalt.mordant.internal.gen


internal fun couldStartEmojiSeq(codepoint: Int): Boolean {
    return codepoint in 0x261d..0x2764 || codepoint in 0x1f385..0x1faf6
}

internal data class IntTrie(val children: MutableMap<Int, IntTrie>, val values: MutableSet<Int> = mutableSetOf()) {
    constructor(vararg children: Pair<Int, IntTrie>, values: MutableSet<Int> = mutableSetOf())
            : this(mutableMapOf(*children), values)
}

internal val EMOJI_SEQUENCES: IntTrie = buildSeqTrie()

private fun buildSeqTrie(): IntTrie {
    val sequences = arrayOf(
        intArrayOf(0x261d, 0x1f3fb), // index pointing up: light skin tone (☝🏻)
        intArrayOf(0x261d, 0x1f3fc), // index pointing up: medium-light skin tone (☝🏼)
        intArrayOf(0x261d, 0x1f3fd), // index pointing up: medium skin tone (☝🏽)
        intArrayOf(0x261d, 0x1f3fe), // index pointing up: medium-dark skin tone (☝🏾)
        intArrayOf(0x261d, 0x1f3ff), // index pointing up: dark skin tone (☝🏿)
        intArrayOf(0x26f9, 0xfe0f, 0x200d, 0x2640, 0xfe0f), // woman bouncing ball (⛹️‍♀️)
        intArrayOf(0x26f9, 0xfe0f, 0x200d, 0x2642, 0xfe0f), // man bouncing ball (⛹️‍♂️)
        intArrayOf(0x26f9, 0x1f3fb), // person bouncing ball: light skin tone (⛹🏻)
        intArrayOf(0x26f9, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman bouncing ball: light skin tone (⛹🏻‍♀️)
        intArrayOf(0x26f9, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man bouncing ball: light skin tone (⛹🏻‍♂️)
        intArrayOf(0x26f9, 0x1f3fc), // person bouncing ball: medium-light skin tone (⛹🏼)
        intArrayOf(0x26f9, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman bouncing ball: medium-light skin tone (⛹🏼‍♀️)
        intArrayOf(0x26f9, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man bouncing ball: medium-light skin tone (⛹🏼‍♂️)
        intArrayOf(0x26f9, 0x1f3fd), // person bouncing ball: medium skin tone (⛹🏽)
        intArrayOf(0x26f9, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman bouncing ball: medium skin tone (⛹🏽‍♀️)
        intArrayOf(0x26f9, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man bouncing ball: medium skin tone (⛹🏽‍♂️)
        intArrayOf(0x26f9, 0x1f3fe), // person bouncing ball: medium-dark skin tone (⛹🏾)
        intArrayOf(0x26f9, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman bouncing ball: medium-dark skin tone (⛹🏾‍♀️)
        intArrayOf(0x26f9, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man bouncing ball: medium-dark skin tone (⛹🏾‍♂️)
        intArrayOf(0x26f9, 0x1f3ff), // person bouncing ball: dark skin tone (⛹🏿)
        intArrayOf(0x26f9, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman bouncing ball: dark skin tone (⛹🏿‍♀️)
        intArrayOf(0x26f9, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man bouncing ball: dark skin tone (⛹🏿‍♂️)
        intArrayOf(0x270a, 0x1f3fb), // raised fist: light skin tone (✊🏻)
        intArrayOf(0x270a, 0x1f3fc), // raised fist: medium-light skin tone (✊🏼)
        intArrayOf(0x270a, 0x1f3fd), // raised fist: medium skin tone (✊🏽)
        intArrayOf(0x270a, 0x1f3fe), // raised fist: medium-dark skin tone (✊🏾)
        intArrayOf(0x270a, 0x1f3ff), // raised fist: dark skin tone (✊🏿)
        intArrayOf(0x270b, 0x1f3fb), // raised hand: light skin tone (✋🏻)
        intArrayOf(0x270b, 0x1f3fc), // raised hand: medium-light skin tone (✋🏼)
        intArrayOf(0x270b, 0x1f3fd), // raised hand: medium skin tone (✋🏽)
        intArrayOf(0x270b, 0x1f3fe), // raised hand: medium-dark skin tone (✋🏾)
        intArrayOf(0x270b, 0x1f3ff), // raised hand: dark skin tone (✋🏿)
        intArrayOf(0x270c, 0x1f3fb), // victory hand: light skin tone (✌🏻)
        intArrayOf(0x270c, 0x1f3fc), // victory hand: medium-light skin tone (✌🏼)
        intArrayOf(0x270c, 0x1f3fd), // victory hand: medium skin tone (✌🏽)
        intArrayOf(0x270c, 0x1f3fe), // victory hand: medium-dark skin tone (✌🏾)
        intArrayOf(0x270c, 0x1f3ff), // victory hand: dark skin tone (✌🏿)
        intArrayOf(0x270d, 0x1f3fb), // writing hand: light skin tone (✍🏻)
        intArrayOf(0x270d, 0x1f3fc), // writing hand: medium-light skin tone (✍🏼)
        intArrayOf(0x270d, 0x1f3fd), // writing hand: medium skin tone (✍🏽)
        intArrayOf(0x270d, 0x1f3fe), // writing hand: medium-dark skin tone (✍🏾)
        intArrayOf(0x270d, 0x1f3ff), // writing hand: dark skin tone (✍🏿)
        intArrayOf(0x2764, 0xfe0f, 0x200d, 0x1f525), // heart on fire (❤️‍🔥)
        intArrayOf(0x2764, 0xfe0f, 0x200d, 0x1fa79), // mending heart (❤️‍🩹)
        intArrayOf(0x1f385, 0x1f3fb), // Santa Claus: light skin tone (🎅🏻)
        intArrayOf(0x1f385, 0x1f3fc), // Santa Claus: medium-light skin tone (🎅🏼)
        intArrayOf(0x1f385, 0x1f3fd), // Santa Claus: medium skin tone (🎅🏽)
        intArrayOf(0x1f385, 0x1f3fe), // Santa Claus: medium-dark skin tone (🎅🏾)
        intArrayOf(0x1f385, 0x1f3ff), // Santa Claus: dark skin tone (🎅🏿)
        intArrayOf(0x1f3c2, 0x1f3fb), // snowboarder: light skin tone (🏂🏻)
        intArrayOf(0x1f3c2, 0x1f3fc), // snowboarder: medium-light skin tone (🏂🏼)
        intArrayOf(0x1f3c2, 0x1f3fd), // snowboarder: medium skin tone (🏂🏽)
        intArrayOf(0x1f3c2, 0x1f3fe), // snowboarder: medium-dark skin tone (🏂🏾)
        intArrayOf(0x1f3c2, 0x1f3ff), // snowboarder: dark skin tone (🏂🏿)
        intArrayOf(0x1f3c3, 0x200d, 0x2640, 0xfe0f), // woman running (🏃‍♀️)
        intArrayOf(0x1f3c3, 0x200d, 0x2642, 0xfe0f), // man running (🏃‍♂️)
        intArrayOf(0x1f3c3, 0x1f3fb), // person running: light skin tone (🏃🏻)
        intArrayOf(0x1f3c3, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman running: light skin tone (🏃🏻‍♀️)
        intArrayOf(0x1f3c3, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man running: light skin tone (🏃🏻‍♂️)
        intArrayOf(0x1f3c3, 0x1f3fc), // person running: medium-light skin tone (🏃🏼)
        intArrayOf(0x1f3c3, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman running: medium-light skin tone (🏃🏼‍♀️)
        intArrayOf(0x1f3c3, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man running: medium-light skin tone (🏃🏼‍♂️)
        intArrayOf(0x1f3c3, 0x1f3fd), // person running: medium skin tone (🏃🏽)
        intArrayOf(0x1f3c3, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman running: medium skin tone (🏃🏽‍♀️)
        intArrayOf(0x1f3c3, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man running: medium skin tone (🏃🏽‍♂️)
        intArrayOf(0x1f3c3, 0x1f3fe), // person running: medium-dark skin tone (🏃🏾)
        intArrayOf(0x1f3c3, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman running: medium-dark skin tone (🏃🏾‍♀️)
        intArrayOf(0x1f3c3, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man running: medium-dark skin tone (🏃🏾‍♂️)
        intArrayOf(0x1f3c3, 0x1f3ff), // person running: dark skin tone (🏃🏿)
        intArrayOf(0x1f3c3, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman running: dark skin tone (🏃🏿‍♀️)
        intArrayOf(0x1f3c3, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man running: dark skin tone (🏃🏿‍♂️)
        intArrayOf(0x1f3c4, 0x200d, 0x2640, 0xfe0f), // woman surfing (🏄‍♀️)
        intArrayOf(0x1f3c4, 0x200d, 0x2642, 0xfe0f), // man surfing (🏄‍♂️)
        intArrayOf(0x1f3c4, 0x1f3fb), // person surfing: light skin tone (🏄🏻)
        intArrayOf(0x1f3c4, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman surfing: light skin tone (🏄🏻‍♀️)
        intArrayOf(0x1f3c4, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man surfing: light skin tone (🏄🏻‍♂️)
        intArrayOf(0x1f3c4, 0x1f3fc), // person surfing: medium-light skin tone (🏄🏼)
        intArrayOf(0x1f3c4, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman surfing: medium-light skin tone (🏄🏼‍♀️)
        intArrayOf(0x1f3c4, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man surfing: medium-light skin tone (🏄🏼‍♂️)
        intArrayOf(0x1f3c4, 0x1f3fd), // person surfing: medium skin tone (🏄🏽)
        intArrayOf(0x1f3c4, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman surfing: medium skin tone (🏄🏽‍♀️)
        intArrayOf(0x1f3c4, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man surfing: medium skin tone (🏄🏽‍♂️)
        intArrayOf(0x1f3c4, 0x1f3fe), // person surfing: medium-dark skin tone (🏄🏾)
        intArrayOf(0x1f3c4, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman surfing: medium-dark skin tone (🏄🏾‍♀️)
        intArrayOf(0x1f3c4, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man surfing: medium-dark skin tone (🏄🏾‍♂️)
        intArrayOf(0x1f3c4, 0x1f3ff), // person surfing: dark skin tone (🏄🏿)
        intArrayOf(0x1f3c4, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman surfing: dark skin tone (🏄🏿‍♀️)
        intArrayOf(0x1f3c4, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man surfing: dark skin tone (🏄🏿‍♂️)
        intArrayOf(0x1f3c7, 0x1f3fb), // horse racing: light skin tone (🏇🏻)
        intArrayOf(0x1f3c7, 0x1f3fc), // horse racing: medium-light skin tone (🏇🏼)
        intArrayOf(0x1f3c7, 0x1f3fd), // horse racing: medium skin tone (🏇🏽)
        intArrayOf(0x1f3c7, 0x1f3fe), // horse racing: medium-dark skin tone (🏇🏾)
        intArrayOf(0x1f3c7, 0x1f3ff), // horse racing: dark skin tone (🏇🏿)
        intArrayOf(0x1f3ca, 0x200d, 0x2640, 0xfe0f), // woman swimming (🏊‍♀️)
        intArrayOf(0x1f3ca, 0x200d, 0x2642, 0xfe0f), // man swimming (🏊‍♂️)
        intArrayOf(0x1f3ca, 0x1f3fb), // person swimming: light skin tone (🏊🏻)
        intArrayOf(0x1f3ca, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman swimming: light skin tone (🏊🏻‍♀️)
        intArrayOf(0x1f3ca, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man swimming: light skin tone (🏊🏻‍♂️)
        intArrayOf(0x1f3ca, 0x1f3fc), // person swimming: medium-light skin tone (🏊🏼)
        intArrayOf(0x1f3ca, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman swimming: medium-light skin tone (🏊🏼‍♀️)
        intArrayOf(0x1f3ca, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man swimming: medium-light skin tone (🏊🏼‍♂️)
        intArrayOf(0x1f3ca, 0x1f3fd), // person swimming: medium skin tone (🏊🏽)
        intArrayOf(0x1f3ca, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman swimming: medium skin tone (🏊🏽‍♀️)
        intArrayOf(0x1f3ca, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man swimming: medium skin tone (🏊🏽‍♂️)
        intArrayOf(0x1f3ca, 0x1f3fe), // person swimming: medium-dark skin tone (🏊🏾)
        intArrayOf(0x1f3ca, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman swimming: medium-dark skin tone (🏊🏾‍♀️)
        intArrayOf(0x1f3ca, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man swimming: medium-dark skin tone (🏊🏾‍♂️)
        intArrayOf(0x1f3ca, 0x1f3ff), // person swimming: dark skin tone (🏊🏿)
        intArrayOf(0x1f3ca, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman swimming: dark skin tone (🏊🏿‍♀️)
        intArrayOf(0x1f3ca, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man swimming: dark skin tone (🏊🏿‍♂️)
        intArrayOf(0x1f3cb, 0xfe0f, 0x200d, 0x2640, 0xfe0f), // woman lifting weights (🏋️‍♀️)
        intArrayOf(0x1f3cb, 0xfe0f, 0x200d, 0x2642, 0xfe0f), // man lifting weights (🏋️‍♂️)
        intArrayOf(0x1f3cb, 0x1f3fb), // person lifting weights: light skin tone (🏋🏻)
        intArrayOf(0x1f3cb, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman lifting weights: light skin tone (🏋🏻‍♀️)
        intArrayOf(0x1f3cb, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man lifting weights: light skin tone (🏋🏻‍♂️)
        intArrayOf(0x1f3cb, 0x1f3fc), // person lifting weights: medium-light skin tone (🏋🏼)
        intArrayOf(0x1f3cb, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman lifting weights: medium-light skin tone (🏋🏼‍♀️)
        intArrayOf(0x1f3cb, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man lifting weights: medium-light skin tone (🏋🏼‍♂️)
        intArrayOf(0x1f3cb, 0x1f3fd), // person lifting weights: medium skin tone (🏋🏽)
        intArrayOf(0x1f3cb, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman lifting weights: medium skin tone (🏋🏽‍♀️)
        intArrayOf(0x1f3cb, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man lifting weights: medium skin tone (🏋🏽‍♂️)
        intArrayOf(0x1f3cb, 0x1f3fe), // person lifting weights: medium-dark skin tone (🏋🏾)
        intArrayOf(0x1f3cb, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman lifting weights: medium-dark skin tone (🏋🏾‍♀️)
        intArrayOf(0x1f3cb, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man lifting weights: medium-dark skin tone (🏋🏾‍♂️)
        intArrayOf(0x1f3cb, 0x1f3ff), // person lifting weights: dark skin tone (🏋🏿)
        intArrayOf(0x1f3cb, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman lifting weights: dark skin tone (🏋🏿‍♀️)
        intArrayOf(0x1f3cb, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man lifting weights: dark skin tone (🏋🏿‍♂️)
        intArrayOf(0x1f3cc, 0xfe0f, 0x200d, 0x2640, 0xfe0f), // woman golfing (🏌️‍♀️)
        intArrayOf(0x1f3cc, 0xfe0f, 0x200d, 0x2642, 0xfe0f), // man golfing (🏌️‍♂️)
        intArrayOf(0x1f3cc, 0x1f3fb), // person golfing: light skin tone (🏌🏻)
        intArrayOf(0x1f3cc, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman golfing: light skin tone (🏌🏻‍♀️)
        intArrayOf(0x1f3cc, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man golfing: light skin tone (🏌🏻‍♂️)
        intArrayOf(0x1f3cc, 0x1f3fc), // person golfing: medium-light skin tone (🏌🏼)
        intArrayOf(0x1f3cc, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman golfing: medium-light skin tone (🏌🏼‍♀️)
        intArrayOf(0x1f3cc, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man golfing: medium-light skin tone (🏌🏼‍♂️)
        intArrayOf(0x1f3cc, 0x1f3fd), // person golfing: medium skin tone (🏌🏽)
        intArrayOf(0x1f3cc, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman golfing: medium skin tone (🏌🏽‍♀️)
        intArrayOf(0x1f3cc, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man golfing: medium skin tone (🏌🏽‍♂️)
        intArrayOf(0x1f3cc, 0x1f3fe), // person golfing: medium-dark skin tone (🏌🏾)
        intArrayOf(0x1f3cc, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman golfing: medium-dark skin tone (🏌🏾‍♀️)
        intArrayOf(0x1f3cc, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man golfing: medium-dark skin tone (🏌🏾‍♂️)
        intArrayOf(0x1f3cc, 0x1f3ff), // person golfing: dark skin tone (🏌🏿)
        intArrayOf(0x1f3cc, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman golfing: dark skin tone (🏌🏿‍♀️)
        intArrayOf(0x1f3cc, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man golfing: dark skin tone (🏌🏿‍♂️)
        intArrayOf(0x1f3f3, 0xfe0f, 0x200d, 0x26a7, 0xfe0f), // transgender flag (🏳️‍⚧️)
        intArrayOf(0x1f3f3, 0xfe0f, 0x200d, 0x1f308), // rainbow flag (🏳️‍🌈)
        intArrayOf(0x1f3f4, 0x200d, 0x2620, 0xfe0f), // pirate flag (🏴‍☠️)
        intArrayOf(0x1f408, 0x200d, 0x2b1b), // black cat (🐈‍⬛)
        intArrayOf(0x1f415, 0x200d, 0x1f9ba), // service dog (🐕‍🦺)
        intArrayOf(0x1f43b, 0x200d, 0x2744, 0xfe0f), // polar bear (🐻‍❄️)
        intArrayOf(0x1f441, 0xfe0f, 0x200d, 0x1f5e8, 0xfe0f), // eye in speech bubble (👁️‍🗨️)
        intArrayOf(0x1f442, 0x1f3fb), // ear: light skin tone (👂🏻)
        intArrayOf(0x1f442, 0x1f3fc), // ear: medium-light skin tone (👂🏼)
        intArrayOf(0x1f442, 0x1f3fd), // ear: medium skin tone (👂🏽)
        intArrayOf(0x1f442, 0x1f3fe), // ear: medium-dark skin tone (👂🏾)
        intArrayOf(0x1f442, 0x1f3ff), // ear: dark skin tone (👂🏿)
        intArrayOf(0x1f443, 0x1f3fb), // nose: light skin tone (👃🏻)
        intArrayOf(0x1f443, 0x1f3fc), // nose: medium-light skin tone (👃🏼)
        intArrayOf(0x1f443, 0x1f3fd), // nose: medium skin tone (👃🏽)
        intArrayOf(0x1f443, 0x1f3fe), // nose: medium-dark skin tone (👃🏾)
        intArrayOf(0x1f443, 0x1f3ff), // nose: dark skin tone (👃🏿)
        intArrayOf(0x1f446, 0x1f3fb), // backhand index pointing up: light skin tone (👆🏻)
        intArrayOf(0x1f446, 0x1f3fc), // backhand index pointing up: medium-light skin tone (👆🏼)
        intArrayOf(0x1f446, 0x1f3fd), // backhand index pointing up: medium skin tone (👆🏽)
        intArrayOf(0x1f446, 0x1f3fe), // backhand index pointing up: medium-dark skin tone (👆🏾)
        intArrayOf(0x1f446, 0x1f3ff), // backhand index pointing up: dark skin tone (👆🏿)
        intArrayOf(0x1f447, 0x1f3fb), // backhand index pointing down: light skin tone (👇🏻)
        intArrayOf(0x1f447, 0x1f3fc), // backhand index pointing down: medium-light skin tone (👇🏼)
        intArrayOf(0x1f447, 0x1f3fd), // backhand index pointing down: medium skin tone (👇🏽)
        intArrayOf(0x1f447, 0x1f3fe), // backhand index pointing down: medium-dark skin tone (👇🏾)
        intArrayOf(0x1f447, 0x1f3ff), // backhand index pointing down: dark skin tone (👇🏿)
        intArrayOf(0x1f448, 0x1f3fb), // backhand index pointing left: light skin tone (👈🏻)
        intArrayOf(0x1f448, 0x1f3fc), // backhand index pointing left: medium-light skin tone (👈🏼)
        intArrayOf(0x1f448, 0x1f3fd), // backhand index pointing left: medium skin tone (👈🏽)
        intArrayOf(0x1f448, 0x1f3fe), // backhand index pointing left: medium-dark skin tone (👈🏾)
        intArrayOf(0x1f448, 0x1f3ff), // backhand index pointing left: dark skin tone (👈🏿)
        intArrayOf(0x1f449, 0x1f3fb), // backhand index pointing right: light skin tone (👉🏻)
        intArrayOf(0x1f449, 0x1f3fc), // backhand index pointing right: medium-light skin tone (👉🏼)
        intArrayOf(0x1f449, 0x1f3fd), // backhand index pointing right: medium skin tone (👉🏽)
        intArrayOf(0x1f449, 0x1f3fe), // backhand index pointing right: medium-dark skin tone (👉🏾)
        intArrayOf(0x1f449, 0x1f3ff), // backhand index pointing right: dark skin tone (👉🏿)
        intArrayOf(0x1f44a, 0x1f3fb), // oncoming fist: light skin tone (👊🏻)
        intArrayOf(0x1f44a, 0x1f3fc), // oncoming fist: medium-light skin tone (👊🏼)
        intArrayOf(0x1f44a, 0x1f3fd), // oncoming fist: medium skin tone (👊🏽)
        intArrayOf(0x1f44a, 0x1f3fe), // oncoming fist: medium-dark skin tone (👊🏾)
        intArrayOf(0x1f44a, 0x1f3ff), // oncoming fist: dark skin tone (👊🏿)
        intArrayOf(0x1f44b, 0x1f3fb), // waving hand: light skin tone (👋🏻)
        intArrayOf(0x1f44b, 0x1f3fc), // waving hand: medium-light skin tone (👋🏼)
        intArrayOf(0x1f44b, 0x1f3fd), // waving hand: medium skin tone (👋🏽)
        intArrayOf(0x1f44b, 0x1f3fe), // waving hand: medium-dark skin tone (👋🏾)
        intArrayOf(0x1f44b, 0x1f3ff), // waving hand: dark skin tone (👋🏿)
        intArrayOf(0x1f44c, 0x1f3fb), // OK hand: light skin tone (👌🏻)
        intArrayOf(0x1f44c, 0x1f3fc), // OK hand: medium-light skin tone (👌🏼)
        intArrayOf(0x1f44c, 0x1f3fd), // OK hand: medium skin tone (👌🏽)
        intArrayOf(0x1f44c, 0x1f3fe), // OK hand: medium-dark skin tone (👌🏾)
        intArrayOf(0x1f44c, 0x1f3ff), // OK hand: dark skin tone (👌🏿)
        intArrayOf(0x1f44d, 0x1f3fb), // thumbs up: light skin tone (👍🏻)
        intArrayOf(0x1f44d, 0x1f3fc), // thumbs up: medium-light skin tone (👍🏼)
        intArrayOf(0x1f44d, 0x1f3fd), // thumbs up: medium skin tone (👍🏽)
        intArrayOf(0x1f44d, 0x1f3fe), // thumbs up: medium-dark skin tone (👍🏾)
        intArrayOf(0x1f44d, 0x1f3ff), // thumbs up: dark skin tone (👍🏿)
        intArrayOf(0x1f44e, 0x1f3fb), // thumbs down: light skin tone (👎🏻)
        intArrayOf(0x1f44e, 0x1f3fc), // thumbs down: medium-light skin tone (👎🏼)
        intArrayOf(0x1f44e, 0x1f3fd), // thumbs down: medium skin tone (👎🏽)
        intArrayOf(0x1f44e, 0x1f3fe), // thumbs down: medium-dark skin tone (👎🏾)
        intArrayOf(0x1f44e, 0x1f3ff), // thumbs down: dark skin tone (👎🏿)
        intArrayOf(0x1f44f, 0x1f3fb), // clapping hands: light skin tone (👏🏻)
        intArrayOf(0x1f44f, 0x1f3fc), // clapping hands: medium-light skin tone (👏🏼)
        intArrayOf(0x1f44f, 0x1f3fd), // clapping hands: medium skin tone (👏🏽)
        intArrayOf(0x1f44f, 0x1f3fe), // clapping hands: medium-dark skin tone (👏🏾)
        intArrayOf(0x1f44f, 0x1f3ff), // clapping hands: dark skin tone (👏🏿)
        intArrayOf(0x1f450, 0x1f3fb), // open hands: light skin tone (👐🏻)
        intArrayOf(0x1f450, 0x1f3fc), // open hands: medium-light skin tone (👐🏼)
        intArrayOf(0x1f450, 0x1f3fd), // open hands: medium skin tone (👐🏽)
        intArrayOf(0x1f450, 0x1f3fe), // open hands: medium-dark skin tone (👐🏾)
        intArrayOf(0x1f450, 0x1f3ff), // open hands: dark skin tone (👐🏿)
        intArrayOf(0x1f466, 0x1f3fb), // boy: light skin tone (👦🏻)
        intArrayOf(0x1f466, 0x1f3fc), // boy: medium-light skin tone (👦🏼)
        intArrayOf(0x1f466, 0x1f3fd), // boy: medium skin tone (👦🏽)
        intArrayOf(0x1f466, 0x1f3fe), // boy: medium-dark skin tone (👦🏾)
        intArrayOf(0x1f466, 0x1f3ff), // boy: dark skin tone (👦🏿)
        intArrayOf(0x1f467, 0x1f3fb), // girl: light skin tone (👧🏻)
        intArrayOf(0x1f467, 0x1f3fc), // girl: medium-light skin tone (👧🏼)
        intArrayOf(0x1f467, 0x1f3fd), // girl: medium skin tone (👧🏽)
        intArrayOf(0x1f467, 0x1f3fe), // girl: medium-dark skin tone (👧🏾)
        intArrayOf(0x1f467, 0x1f3ff), // girl: dark skin tone (👧🏿)
        intArrayOf(0x1f468, 0x200d, 0x2695, 0xfe0f), // man health worker (👨‍⚕️)
        intArrayOf(0x1f468, 0x200d, 0x2696, 0xfe0f), // man judge (👨‍⚖️)
        intArrayOf(0x1f468, 0x200d, 0x2708, 0xfe0f), // man pilot (👨‍✈️)
        intArrayOf(0x1f468, 0x200d, 0x2764, 0xfe0f, 0x200d, 0x1f468), // couple with heart: man, man (👨‍❤️‍👨)
        intArrayOf(0x1f468, 0x200d, 0x2764, 0xfe0f, 0x200d, 0x1f48b, 0x200d, 0x1f468), // kiss: man, man (👨‍❤️‍💋‍👨)
        intArrayOf(0x1f468, 0x200d, 0x1f33e), // man farmer (👨‍🌾)
        intArrayOf(0x1f468, 0x200d, 0x1f373), // man cook (👨‍🍳)
        intArrayOf(0x1f468, 0x200d, 0x1f37c), // man feeding baby (👨‍🍼)
        intArrayOf(0x1f468, 0x200d, 0x1f393), // man student (👨‍🎓)
        intArrayOf(0x1f468, 0x200d, 0x1f3a4), // man singer (👨‍🎤)
        intArrayOf(0x1f468, 0x200d, 0x1f3a8), // man artist (👨‍🎨)
        intArrayOf(0x1f468, 0x200d, 0x1f3eb), // man teacher (👨‍🏫)
        intArrayOf(0x1f468, 0x200d, 0x1f3ed), // man factory worker (👨‍🏭)
        intArrayOf(0x1f468, 0x200d, 0x1f466), // family: man, boy (👨‍👦)
        intArrayOf(0x1f468, 0x200d, 0x1f466, 0x200d, 0x1f466), // family: man, boy, boy (👨‍👦‍👦)
        intArrayOf(0x1f468, 0x200d, 0x1f467), // family: man, girl (👨‍👧)
        intArrayOf(0x1f468, 0x200d, 0x1f467, 0x200d, 0x1f466), // family: man, girl, boy (👨‍👧‍👦)
        intArrayOf(0x1f468, 0x200d, 0x1f467, 0x200d, 0x1f467), // family: man, girl, girl (👨‍👧‍👧)
        intArrayOf(0x1f468, 0x200d, 0x1f468, 0x200d, 0x1f466), // family: man, man, boy (👨‍👨‍👦)
        intArrayOf(
            0x1f468,
            0x200d,
            0x1f468,
            0x200d,
            0x1f466,
            0x200d,
            0x1f466
        ), // family: man, man, boy, boy (👨‍👨‍👦‍👦)
        intArrayOf(0x1f468, 0x200d, 0x1f468, 0x200d, 0x1f467), // family: man, man, girl (👨‍👨‍👧)
        intArrayOf(
            0x1f468,
            0x200d,
            0x1f468,
            0x200d,
            0x1f467,
            0x200d,
            0x1f466
        ), // family: man, man, girl, boy (👨‍👨‍👧‍👦)
        intArrayOf(
            0x1f468,
            0x200d,
            0x1f468,
            0x200d,
            0x1f467,
            0x200d,
            0x1f467
        ), // family: man, man, girl, girl (👨‍👨‍👧‍👧)
        intArrayOf(0x1f468, 0x200d, 0x1f469, 0x200d, 0x1f466), // family: man, woman, boy (👨‍👩‍👦)
        intArrayOf(
            0x1f468,
            0x200d,
            0x1f469,
            0x200d,
            0x1f466,
            0x200d,
            0x1f466
        ), // family: man, woman, boy, boy (👨‍👩‍👦‍👦)
        intArrayOf(0x1f468, 0x200d, 0x1f469, 0x200d, 0x1f467), // family: man, woman, girl (👨‍👩‍👧)
        intArrayOf(
            0x1f468,
            0x200d,
            0x1f469,
            0x200d,
            0x1f467,
            0x200d,
            0x1f466
        ), // family: man, woman, girl, boy (👨‍👩‍👧‍👦)
        intArrayOf(
            0x1f468,
            0x200d,
            0x1f469,
            0x200d,
            0x1f467,
            0x200d,
            0x1f467
        ), // family: man, woman, girl, girl (👨‍👩‍👧‍👧)
        intArrayOf(0x1f468, 0x200d, 0x1f4bb), // man technologist (👨‍💻)
        intArrayOf(0x1f468, 0x200d, 0x1f4bc), // man office worker (👨‍💼)
        intArrayOf(0x1f468, 0x200d, 0x1f527), // man mechanic (👨‍🔧)
        intArrayOf(0x1f468, 0x200d, 0x1f52c), // man scientist (👨‍🔬)
        intArrayOf(0x1f468, 0x200d, 0x1f680), // man astronaut (👨‍🚀)
        intArrayOf(0x1f468, 0x200d, 0x1f692), // man firefighter (👨‍🚒)
        intArrayOf(0x1f468, 0x200d, 0x1f9af), // man with white cane (👨‍🦯)
        intArrayOf(0x1f468, 0x200d, 0x1f9b0), // man: red hair (👨‍🦰)
        intArrayOf(0x1f468, 0x200d, 0x1f9b1), // man: curly hair (👨‍🦱)
        intArrayOf(0x1f468, 0x200d, 0x1f9b2), // man: bald (👨‍🦲)
        intArrayOf(0x1f468, 0x200d, 0x1f9b3), // man: white hair (👨‍🦳)
        intArrayOf(0x1f468, 0x200d, 0x1f9bc), // man in motorized wheelchair (👨‍🦼)
        intArrayOf(0x1f468, 0x200d, 0x1f9bd), // man in manual wheelchair (👨‍🦽)
        intArrayOf(0x1f468, 0x1f3fb), // man: light skin tone (👨🏻)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x2695, 0xfe0f), // man health worker: light skin tone (👨🏻‍⚕️)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x2696, 0xfe0f), // man judge: light skin tone (👨🏻‍⚖️)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x2708, 0xfe0f), // man pilot: light skin tone (👨🏻‍✈️)
        intArrayOf(
            0x1f468,
            0x1f3fb,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fb
        ), // couple with heart: man, man, light skin tone (👨🏻‍❤️‍👨🏻)
        intArrayOf(
            0x1f468,
            0x1f3fb,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fd
        ), // couple with heart: man, man, light skin tone, medium skin tone (👨🏻‍❤️‍👨🏽)
        intArrayOf(
            0x1f468,
            0x1f3fb,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3ff
        ), // couple with heart: man, man, light skin tone, dark skin tone (👨🏻‍❤️‍👨🏿)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f33e), // man farmer: light skin tone (👨🏻‍🌾)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f373), // man cook: light skin tone (👨🏻‍🍳)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f37c), // man feeding baby: light skin tone (👨🏻‍🍼)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f393), // man student: light skin tone (👨🏻‍🎓)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f3a4), // man singer: light skin tone (👨🏻‍🎤)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f3a8), // man artist: light skin tone (👨🏻‍🎨)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f3eb), // man teacher: light skin tone (👨🏻‍🏫)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f3ed), // man factory worker: light skin tone (👨🏻‍🏭)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f4bb), // man technologist: light skin tone (👨🏻‍💻)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f4bc), // man office worker: light skin tone (👨🏻‍💼)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f527), // man mechanic: light skin tone (👨🏻‍🔧)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f52c), // man scientist: light skin tone (👨🏻‍🔬)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f680), // man astronaut: light skin tone (👨🏻‍🚀)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f692), // man firefighter: light skin tone (👨🏻‍🚒)
        intArrayOf(
            0x1f468,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fc
        ), // men holding hands: light skin tone, medium-light skin tone (👨🏻‍🤝‍👨🏼)
        intArrayOf(
            0x1f468,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fd
        ), // men holding hands: light skin tone, medium skin tone (👨🏻‍🤝‍👨🏽)
        intArrayOf(
            0x1f468,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fe
        ), // men holding hands: light skin tone, medium-dark skin tone (👨🏻‍🤝‍👨🏾)
        intArrayOf(
            0x1f468,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3ff
        ), // men holding hands: light skin tone, dark skin tone (👨🏻‍🤝‍👨🏿)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f9af), // man with white cane: light skin tone (👨🏻‍🦯)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f9b0), // man: light skin tone, red hair (👨🏻‍🦰)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f9b1), // man: light skin tone, curly hair (👨🏻‍🦱)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f9b2), // man: light skin tone, bald (👨🏻‍🦲)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f9b3), // man: light skin tone, white hair (👨🏻‍🦳)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f9bc), // man in motorized wheelchair: light skin tone (👨🏻‍🦼)
        intArrayOf(0x1f468, 0x1f3fb, 0x200d, 0x1f9bd), // man in manual wheelchair: light skin tone (👨🏻‍🦽)
        intArrayOf(0x1f468, 0x1f3fc), // man: medium-light skin tone (👨🏼)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x2695, 0xfe0f), // man health worker: medium-light skin tone (👨🏼‍⚕️)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x2696, 0xfe0f), // man judge: medium-light skin tone (👨🏼‍⚖️)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x2708, 0xfe0f), // man pilot: medium-light skin tone (👨🏼‍✈️)
        intArrayOf(
            0x1f468,
            0x1f3fc,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fc
        ), // couple with heart: man, man, medium-light skin tone (👨🏼‍❤️‍👨🏼)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f33e), // man farmer: medium-light skin tone (👨🏼‍🌾)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f373), // man cook: medium-light skin tone (👨🏼‍🍳)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f37c), // man feeding baby: medium-light skin tone (👨🏼‍🍼)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f393), // man student: medium-light skin tone (👨🏼‍🎓)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f3a4), // man singer: medium-light skin tone (👨🏼‍🎤)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f3a8), // man artist: medium-light skin tone (👨🏼‍🎨)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f3eb), // man teacher: medium-light skin tone (👨🏼‍🏫)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f3ed), // man factory worker: medium-light skin tone (👨🏼‍🏭)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f4bb), // man technologist: medium-light skin tone (👨🏼‍💻)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f4bc), // man office worker: medium-light skin tone (👨🏼‍💼)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f527), // man mechanic: medium-light skin tone (👨🏼‍🔧)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f52c), // man scientist: medium-light skin tone (👨🏼‍🔬)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f680), // man astronaut: medium-light skin tone (👨🏼‍🚀)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f692), // man firefighter: medium-light skin tone (👨🏼‍🚒)
        intArrayOf(
            0x1f468,
            0x1f3fc,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fb
        ), // men holding hands: medium-light skin tone, light skin tone (👨🏼‍🤝‍👨🏻)
        intArrayOf(
            0x1f468,
            0x1f3fc,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fd
        ), // men holding hands: medium-light skin tone, medium skin tone (👨🏼‍🤝‍👨🏽)
        intArrayOf(
            0x1f468,
            0x1f3fc,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3ff
        ), // men holding hands: medium-light skin tone, dark skin tone (👨🏼‍🤝‍👨🏿)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f9af), // man with white cane: medium-light skin tone (👨🏼‍🦯)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f9b0), // man: medium-light skin tone, red hair (👨🏼‍🦰)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f9b1), // man: medium-light skin tone, curly hair (👨🏼‍🦱)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f9b2), // man: medium-light skin tone, bald (👨🏼‍🦲)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f9b3), // man: medium-light skin tone, white hair (👨🏼‍🦳)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f9bc), // man in motorized wheelchair: medium-light skin tone (👨🏼‍🦼)
        intArrayOf(0x1f468, 0x1f3fc, 0x200d, 0x1f9bd), // man in manual wheelchair: medium-light skin tone (👨🏼‍🦽)
        intArrayOf(0x1f468, 0x1f3fd), // man: medium skin tone (👨🏽)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x2695, 0xfe0f), // man health worker: medium skin tone (👨🏽‍⚕️)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x2696, 0xfe0f), // man judge: medium skin tone (👨🏽‍⚖️)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x2708, 0xfe0f), // man pilot: medium skin tone (👨🏽‍✈️)
        intArrayOf(
            0x1f468,
            0x1f3fd,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fb
        ), // couple with heart: man, man, medium skin tone, light skin tone (👨🏽‍❤️‍👨🏻)
        intArrayOf(
            0x1f468,
            0x1f3fd,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fd
        ), // couple with heart: man, man, medium skin tone (👨🏽‍❤️‍👨🏽)
        intArrayOf(
            0x1f468,
            0x1f3fd,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3ff
        ), // couple with heart: man, man, medium skin tone, dark skin tone (👨🏽‍❤️‍👨🏿)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f33e), // man farmer: medium skin tone (👨🏽‍🌾)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f373), // man cook: medium skin tone (👨🏽‍🍳)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f37c), // man feeding baby: medium skin tone (👨🏽‍🍼)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f393), // man student: medium skin tone (👨🏽‍🎓)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f3a4), // man singer: medium skin tone (👨🏽‍🎤)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f3a8), // man artist: medium skin tone (👨🏽‍🎨)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f3eb), // man teacher: medium skin tone (👨🏽‍🏫)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f3ed), // man factory worker: medium skin tone (👨🏽‍🏭)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f4bb), // man technologist: medium skin tone (👨🏽‍💻)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f4bc), // man office worker: medium skin tone (👨🏽‍💼)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f527), // man mechanic: medium skin tone (👨🏽‍🔧)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f52c), // man scientist: medium skin tone (👨🏽‍🔬)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f680), // man astronaut: medium skin tone (👨🏽‍🚀)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f692), // man firefighter: medium skin tone (👨🏽‍🚒)
        intArrayOf(
            0x1f468,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fb
        ), // men holding hands: medium skin tone, light skin tone (👨🏽‍🤝‍👨🏻)
        intArrayOf(
            0x1f468,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fc
        ), // men holding hands: medium skin tone, medium-light skin tone (👨🏽‍🤝‍👨🏼)
        intArrayOf(
            0x1f468,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fe
        ), // men holding hands: medium skin tone, medium-dark skin tone (👨🏽‍🤝‍👨🏾)
        intArrayOf(
            0x1f468,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3ff
        ), // men holding hands: medium skin tone, dark skin tone (👨🏽‍🤝‍👨🏿)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f9af), // man with white cane: medium skin tone (👨🏽‍🦯)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f9b0), // man: medium skin tone, red hair (👨🏽‍🦰)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f9b1), // man: medium skin tone, curly hair (👨🏽‍🦱)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f9b2), // man: medium skin tone, bald (👨🏽‍🦲)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f9b3), // man: medium skin tone, white hair (👨🏽‍🦳)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f9bc), // man in motorized wheelchair: medium skin tone (👨🏽‍🦼)
        intArrayOf(0x1f468, 0x1f3fd, 0x200d, 0x1f9bd), // man in manual wheelchair: medium skin tone (👨🏽‍🦽)
        intArrayOf(0x1f468, 0x1f3fe), // man: medium-dark skin tone (👨🏾)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x2695, 0xfe0f), // man health worker: medium-dark skin tone (👨🏾‍⚕️)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x2696, 0xfe0f), // man judge: medium-dark skin tone (👨🏾‍⚖️)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x2708, 0xfe0f), // man pilot: medium-dark skin tone (👨🏾‍✈️)
        intArrayOf(
            0x1f468,
            0x1f3fe,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fe
        ), // couple with heart: man, man, medium-dark skin tone (👨🏾‍❤️‍👨🏾)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f33e), // man farmer: medium-dark skin tone (👨🏾‍🌾)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f373), // man cook: medium-dark skin tone (👨🏾‍🍳)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f37c), // man feeding baby: medium-dark skin tone (👨🏾‍🍼)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f393), // man student: medium-dark skin tone (👨🏾‍🎓)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f3a4), // man singer: medium-dark skin tone (👨🏾‍🎤)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f3a8), // man artist: medium-dark skin tone (👨🏾‍🎨)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f3eb), // man teacher: medium-dark skin tone (👨🏾‍🏫)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f3ed), // man factory worker: medium-dark skin tone (👨🏾‍🏭)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f4bb), // man technologist: medium-dark skin tone (👨🏾‍💻)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f4bc), // man office worker: medium-dark skin tone (👨🏾‍💼)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f527), // man mechanic: medium-dark skin tone (👨🏾‍🔧)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f52c), // man scientist: medium-dark skin tone (👨🏾‍🔬)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f680), // man astronaut: medium-dark skin tone (👨🏾‍🚀)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f692), // man firefighter: medium-dark skin tone (👨🏾‍🚒)
        intArrayOf(
            0x1f468,
            0x1f3fe,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fb
        ), // men holding hands: medium-dark skin tone, light skin tone (👨🏾‍🤝‍👨🏻)
        intArrayOf(
            0x1f468,
            0x1f3fe,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fd
        ), // men holding hands: medium-dark skin tone, medium skin tone (👨🏾‍🤝‍👨🏽)
        intArrayOf(
            0x1f468,
            0x1f3fe,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3ff
        ), // men holding hands: medium-dark skin tone, dark skin tone (👨🏾‍🤝‍👨🏿)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f9af), // man with white cane: medium-dark skin tone (👨🏾‍🦯)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f9b0), // man: medium-dark skin tone, red hair (👨🏾‍🦰)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f9b1), // man: medium-dark skin tone, curly hair (👨🏾‍🦱)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f9b2), // man: medium-dark skin tone, bald (👨🏾‍🦲)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f9b3), // man: medium-dark skin tone, white hair (👨🏾‍🦳)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f9bc), // man in motorized wheelchair: medium-dark skin tone (👨🏾‍🦼)
        intArrayOf(0x1f468, 0x1f3fe, 0x200d, 0x1f9bd), // man in manual wheelchair: medium-dark skin tone (👨🏾‍🦽)
        intArrayOf(0x1f468, 0x1f3ff), // man: dark skin tone (👨🏿)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x2695, 0xfe0f), // man health worker: dark skin tone (👨🏿‍⚕️)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x2696, 0xfe0f), // man judge: dark skin tone (👨🏿‍⚖️)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x2708, 0xfe0f), // man pilot: dark skin tone (👨🏿‍✈️)
        intArrayOf(
            0x1f468,
            0x1f3ff,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fb
        ), // couple with heart: man, man, dark skin tone, light skin tone (👨🏿‍❤️‍👨🏻)
        intArrayOf(
            0x1f468,
            0x1f3ff,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fd
        ), // couple with heart: man, man, dark skin tone, medium skin tone (👨🏿‍❤️‍👨🏽)
        intArrayOf(
            0x1f468,
            0x1f3ff,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3ff
        ), // couple with heart: man, man, dark skin tone (👨🏿‍❤️‍👨🏿)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f33e), // man farmer: dark skin tone (👨🏿‍🌾)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f373), // man cook: dark skin tone (👨🏿‍🍳)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f37c), // man feeding baby: dark skin tone (👨🏿‍🍼)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f393), // man student: dark skin tone (👨🏿‍🎓)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f3a4), // man singer: dark skin tone (👨🏿‍🎤)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f3a8), // man artist: dark skin tone (👨🏿‍🎨)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f3eb), // man teacher: dark skin tone (👨🏿‍🏫)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f3ed), // man factory worker: dark skin tone (👨🏿‍🏭)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f4bb), // man technologist: dark skin tone (👨🏿‍💻)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f4bc), // man office worker: dark skin tone (👨🏿‍💼)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f527), // man mechanic: dark skin tone (👨🏿‍🔧)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f52c), // man scientist: dark skin tone (👨🏿‍🔬)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f680), // man astronaut: dark skin tone (👨🏿‍🚀)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f692), // man firefighter: dark skin tone (👨🏿‍🚒)
        intArrayOf(
            0x1f468,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fb
        ), // men holding hands: dark skin tone, light skin tone (👨🏿‍🤝‍👨🏻)
        intArrayOf(
            0x1f468,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fc
        ), // men holding hands: dark skin tone, medium-light skin tone (👨🏿‍🤝‍👨🏼)
        intArrayOf(
            0x1f468,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fd
        ), // men holding hands: dark skin tone, medium skin tone (👨🏿‍🤝‍👨🏽)
        intArrayOf(
            0x1f468,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fe
        ), // men holding hands: dark skin tone, medium-dark skin tone (👨🏿‍🤝‍👨🏾)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f9af), // man with white cane: dark skin tone (👨🏿‍🦯)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f9b0), // man: dark skin tone, red hair (👨🏿‍🦰)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f9b1), // man: dark skin tone, curly hair (👨🏿‍🦱)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f9b2), // man: dark skin tone, bald (👨🏿‍🦲)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f9b3), // man: dark skin tone, white hair (👨🏿‍🦳)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f9bc), // man in motorized wheelchair: dark skin tone (👨🏿‍🦼)
        intArrayOf(0x1f468, 0x1f3ff, 0x200d, 0x1f9bd), // man in manual wheelchair: dark skin tone (👨🏿‍🦽)
        intArrayOf(0x1f469, 0x200d, 0x2695, 0xfe0f), // woman health worker (👩‍⚕️)
        intArrayOf(0x1f469, 0x200d, 0x2696, 0xfe0f), // woman judge (👩‍⚖️)
        intArrayOf(0x1f469, 0x200d, 0x2708, 0xfe0f), // woman pilot (👩‍✈️)
        intArrayOf(0x1f469, 0x200d, 0x2764, 0xfe0f, 0x200d, 0x1f468), // couple with heart: woman, man (👩‍❤️‍👨)
        intArrayOf(0x1f469, 0x200d, 0x2764, 0xfe0f, 0x200d, 0x1f469), // couple with heart: woman, woman (👩‍❤️‍👩)
        intArrayOf(0x1f469, 0x200d, 0x2764, 0xfe0f, 0x200d, 0x1f48b, 0x200d, 0x1f468), // kiss: woman, man (👩‍❤️‍💋‍👨)
        intArrayOf(
            0x1f469,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f48b,
            0x200d,
            0x1f469
        ), // kiss: woman, woman (👩‍❤️‍💋‍👩)
        intArrayOf(0x1f469, 0x200d, 0x1f33e), // woman farmer (👩‍🌾)
        intArrayOf(0x1f469, 0x200d, 0x1f373), // woman cook (👩‍🍳)
        intArrayOf(0x1f469, 0x200d, 0x1f37c), // woman feeding baby (👩‍🍼)
        intArrayOf(0x1f469, 0x200d, 0x1f393), // woman student (👩‍🎓)
        intArrayOf(0x1f469, 0x200d, 0x1f3a4), // woman singer (👩‍🎤)
        intArrayOf(0x1f469, 0x200d, 0x1f3a8), // woman artist (👩‍🎨)
        intArrayOf(0x1f469, 0x200d, 0x1f3eb), // woman teacher (👩‍🏫)
        intArrayOf(0x1f469, 0x200d, 0x1f3ed), // woman factory worker (👩‍🏭)
        intArrayOf(0x1f469, 0x200d, 0x1f466), // family: woman, boy (👩‍👦)
        intArrayOf(0x1f469, 0x200d, 0x1f466, 0x200d, 0x1f466), // family: woman, boy, boy (👩‍👦‍👦)
        intArrayOf(0x1f469, 0x200d, 0x1f467), // family: woman, girl (👩‍👧)
        intArrayOf(0x1f469, 0x200d, 0x1f467, 0x200d, 0x1f466), // family: woman, girl, boy (👩‍👧‍👦)
        intArrayOf(0x1f469, 0x200d, 0x1f467, 0x200d, 0x1f467), // family: woman, girl, girl (👩‍👧‍👧)
        intArrayOf(0x1f469, 0x200d, 0x1f469, 0x200d, 0x1f466), // family: woman, woman, boy (👩‍👩‍👦)
        intArrayOf(
            0x1f469,
            0x200d,
            0x1f469,
            0x200d,
            0x1f466,
            0x200d,
            0x1f466
        ), // family: woman, woman, boy, boy (👩‍👩‍👦‍👦)
        intArrayOf(0x1f469, 0x200d, 0x1f469, 0x200d, 0x1f467), // family: woman, woman, girl (👩‍👩‍👧)
        intArrayOf(
            0x1f469,
            0x200d,
            0x1f469,
            0x200d,
            0x1f467,
            0x200d,
            0x1f466
        ), // family: woman, woman, girl, boy (👩‍👩‍👧‍👦)
        intArrayOf(
            0x1f469,
            0x200d,
            0x1f469,
            0x200d,
            0x1f467,
            0x200d,
            0x1f467
        ), // family: woman, woman, girl, girl (👩‍👩‍👧‍👧)
        intArrayOf(0x1f469, 0x200d, 0x1f4bb), // woman technologist (👩‍💻)
        intArrayOf(0x1f469, 0x200d, 0x1f4bc), // woman office worker (👩‍💼)
        intArrayOf(0x1f469, 0x200d, 0x1f527), // woman mechanic (👩‍🔧)
        intArrayOf(0x1f469, 0x200d, 0x1f52c), // woman scientist (👩‍🔬)
        intArrayOf(0x1f469, 0x200d, 0x1f680), // woman astronaut (👩‍🚀)
        intArrayOf(0x1f469, 0x200d, 0x1f692), // woman firefighter (👩‍🚒)
        intArrayOf(0x1f469, 0x200d, 0x1f9af), // woman with white cane (👩‍🦯)
        intArrayOf(0x1f469, 0x200d, 0x1f9b0), // woman: red hair (👩‍🦰)
        intArrayOf(0x1f469, 0x200d, 0x1f9b1), // woman: curly hair (👩‍🦱)
        intArrayOf(0x1f469, 0x200d, 0x1f9b2), // woman: bald (👩‍🦲)
        intArrayOf(0x1f469, 0x200d, 0x1f9b3), // woman: white hair (👩‍🦳)
        intArrayOf(0x1f469, 0x200d, 0x1f9bc), // woman in motorized wheelchair (👩‍🦼)
        intArrayOf(0x1f469, 0x200d, 0x1f9bd), // woman in manual wheelchair (👩‍🦽)
        intArrayOf(0x1f469, 0x1f3fb), // woman: light skin tone (👩🏻)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x2695, 0xfe0f), // woman health worker: light skin tone (👩🏻‍⚕️)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x2696, 0xfe0f), // woman judge: light skin tone (👩🏻‍⚖️)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x2708, 0xfe0f), // woman pilot: light skin tone (👩🏻‍✈️)
        intArrayOf(
            0x1f469,
            0x1f3fb,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fb
        ), // couple with heart: woman, man, light skin tone (👩🏻‍❤️‍👨🏻)
        intArrayOf(
            0x1f469,
            0x1f3fb,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3ff
        ), // couple with heart: woman, man, light skin tone, dark skin tone (👩🏻‍❤️‍👨🏿)
        intArrayOf(
            0x1f469,
            0x1f3fb,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f469,
            0x1f3fb
        ), // couple with heart: woman, woman, light skin tone (👩🏻‍❤️‍👩🏻)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f33e), // woman farmer: light skin tone (👩🏻‍🌾)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f373), // woman cook: light skin tone (👩🏻‍🍳)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f37c), // woman feeding baby: light skin tone (👩🏻‍🍼)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f393), // woman student: light skin tone (👩🏻‍🎓)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f3a4), // woman singer: light skin tone (👩🏻‍🎤)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f3a8), // woman artist: light skin tone (👩🏻‍🎨)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f3eb), // woman teacher: light skin tone (👩🏻‍🏫)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f3ed), // woman factory worker: light skin tone (👩🏻‍🏭)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f4bb), // woman technologist: light skin tone (👩🏻‍💻)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f4bc), // woman office worker: light skin tone (👩🏻‍💼)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f527), // woman mechanic: light skin tone (👩🏻‍🔧)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f52c), // woman scientist: light skin tone (👩🏻‍🔬)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f680), // woman astronaut: light skin tone (👩🏻‍🚀)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f692), // woman firefighter: light skin tone (👩🏻‍🚒)
        intArrayOf(
            0x1f469,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fd
        ), // woman and man holding hands: light skin tone, medium skin tone (👩🏻‍🤝‍👨🏽)
        intArrayOf(
            0x1f469,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3ff
        ), // woman and man holding hands: light skin tone, dark skin tone (👩🏻‍🤝‍👨🏿)
        intArrayOf(
            0x1f469,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fc
        ), // women holding hands: light skin tone, medium-light skin tone (👩🏻‍🤝‍👩🏼)
        intArrayOf(
            0x1f469,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fd
        ), // women holding hands: light skin tone, medium skin tone (👩🏻‍🤝‍👩🏽)
        intArrayOf(
            0x1f469,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fe
        ), // women holding hands: light skin tone, medium-dark skin tone (👩🏻‍🤝‍👩🏾)
        intArrayOf(
            0x1f469,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3ff
        ), // women holding hands: light skin tone, dark skin tone (👩🏻‍🤝‍👩🏿)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f9af), // woman with white cane: light skin tone (👩🏻‍🦯)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f9b0), // woman: light skin tone, red hair (👩🏻‍🦰)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f9b1), // woman: light skin tone, curly hair (👩🏻‍🦱)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f9b2), // woman: light skin tone, bald (👩🏻‍🦲)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f9b3), // woman: light skin tone, white hair (👩🏻‍🦳)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f9bc), // woman in motorized wheelchair: light skin tone (👩🏻‍🦼)
        intArrayOf(0x1f469, 0x1f3fb, 0x200d, 0x1f9bd), // woman in manual wheelchair: light skin tone (👩🏻‍🦽)
        intArrayOf(0x1f469, 0x1f3fc), // woman: medium-light skin tone (👩🏼)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x2695, 0xfe0f), // woman health worker: medium-light skin tone (👩🏼‍⚕️)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x2696, 0xfe0f), // woman judge: medium-light skin tone (👩🏼‍⚖️)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x2708, 0xfe0f), // woman pilot: medium-light skin tone (👩🏼‍✈️)
        intArrayOf(
            0x1f469,
            0x1f3fc,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fc
        ), // couple with heart: woman, man, medium-light skin tone (👩🏼‍❤️‍👨🏼)
        intArrayOf(
            0x1f469,
            0x1f3fc,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f469,
            0x1f3fc
        ), // couple with heart: woman, woman, medium-light skin tone (👩🏼‍❤️‍👩🏼)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f33e), // woman farmer: medium-light skin tone (👩🏼‍🌾)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f373), // woman cook: medium-light skin tone (👩🏼‍🍳)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f37c), // woman feeding baby: medium-light skin tone (👩🏼‍🍼)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f393), // woman student: medium-light skin tone (👩🏼‍🎓)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f3a4), // woman singer: medium-light skin tone (👩🏼‍🎤)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f3a8), // woman artist: medium-light skin tone (👩🏼‍🎨)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f3eb), // woman teacher: medium-light skin tone (👩🏼‍🏫)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f3ed), // woman factory worker: medium-light skin tone (👩🏼‍🏭)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f4bb), // woman technologist: medium-light skin tone (👩🏼‍💻)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f4bc), // woman office worker: medium-light skin tone (👩🏼‍💼)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f527), // woman mechanic: medium-light skin tone (👩🏼‍🔧)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f52c), // woman scientist: medium-light skin tone (👩🏼‍🔬)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f680), // woman astronaut: medium-light skin tone (👩🏼‍🚀)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f692), // woman firefighter: medium-light skin tone (👩🏼‍🚒)
        intArrayOf(
            0x1f469,
            0x1f3fc,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fb
        ), // women holding hands: medium-light skin tone, light skin tone (👩🏼‍🤝‍👩🏻)
        intArrayOf(
            0x1f469,
            0x1f3fc,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fd
        ), // women holding hands: medium-light skin tone, medium skin tone (👩🏼‍🤝‍👩🏽)
        intArrayOf(
            0x1f469,
            0x1f3fc,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3ff
        ), // women holding hands: medium-light skin tone, dark skin tone (👩🏼‍🤝‍👩🏿)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f9af), // woman with white cane: medium-light skin tone (👩🏼‍🦯)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f9b0), // woman: medium-light skin tone, red hair (👩🏼‍🦰)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f9b1), // woman: medium-light skin tone, curly hair (👩🏼‍🦱)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f9b2), // woman: medium-light skin tone, bald (👩🏼‍🦲)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f9b3), // woman: medium-light skin tone, white hair (👩🏼‍🦳)
        intArrayOf(
            0x1f469,
            0x1f3fc,
            0x200d,
            0x1f9bc
        ), // woman in motorized wheelchair: medium-light skin tone (👩🏼‍🦼)
        intArrayOf(0x1f469, 0x1f3fc, 0x200d, 0x1f9bd), // woman in manual wheelchair: medium-light skin tone (👩🏼‍🦽)
        intArrayOf(0x1f469, 0x1f3fd), // woman: medium skin tone (👩🏽)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x2695, 0xfe0f), // woman health worker: medium skin tone (👩🏽‍⚕️)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x2696, 0xfe0f), // woman judge: medium skin tone (👩🏽‍⚖️)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x2708, 0xfe0f), // woman pilot: medium skin tone (👩🏽‍✈️)
        intArrayOf(
            0x1f469,
            0x1f3fd,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fd
        ), // couple with heart: woman, man, medium skin tone (👩🏽‍❤️‍👨🏽)
        intArrayOf(
            0x1f469,
            0x1f3fd,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f469,
            0x1f3fd
        ), // couple with heart: woman, woman, medium skin tone (👩🏽‍❤️‍👩🏽)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f33e), // woman farmer: medium skin tone (👩🏽‍🌾)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f373), // woman cook: medium skin tone (👩🏽‍🍳)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f37c), // woman feeding baby: medium skin tone (👩🏽‍🍼)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f393), // woman student: medium skin tone (👩🏽‍🎓)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f3a4), // woman singer: medium skin tone (👩🏽‍🎤)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f3a8), // woman artist: medium skin tone (👩🏽‍🎨)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f3eb), // woman teacher: medium skin tone (👩🏽‍🏫)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f3ed), // woman factory worker: medium skin tone (👩🏽‍🏭)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f4bb), // woman technologist: medium skin tone (👩🏽‍💻)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f4bc), // woman office worker: medium skin tone (👩🏽‍💼)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f527), // woman mechanic: medium skin tone (👩🏽‍🔧)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f52c), // woman scientist: medium skin tone (👩🏽‍🔬)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f680), // woman astronaut: medium skin tone (👩🏽‍🚀)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f692), // woman firefighter: medium skin tone (👩🏽‍🚒)
        intArrayOf(
            0x1f469,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fb
        ), // woman and man holding hands: medium skin tone, light skin tone (👩🏽‍🤝‍👨🏻)
        intArrayOf(
            0x1f469,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3ff
        ), // woman and man holding hands: medium skin tone, dark skin tone (👩🏽‍🤝‍👨🏿)
        intArrayOf(
            0x1f469,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fb
        ), // women holding hands: medium skin tone, light skin tone (👩🏽‍🤝‍👩🏻)
        intArrayOf(
            0x1f469,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fc
        ), // women holding hands: medium skin tone, medium-light skin tone (👩🏽‍🤝‍👩🏼)
        intArrayOf(
            0x1f469,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fe
        ), // women holding hands: medium skin tone, medium-dark skin tone (👩🏽‍🤝‍👩🏾)
        intArrayOf(
            0x1f469,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3ff
        ), // women holding hands: medium skin tone, dark skin tone (👩🏽‍🤝‍👩🏿)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f9af), // woman with white cane: medium skin tone (👩🏽‍🦯)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f9b0), // woman: medium skin tone, red hair (👩🏽‍🦰)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f9b1), // woman: medium skin tone, curly hair (👩🏽‍🦱)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f9b2), // woman: medium skin tone, bald (👩🏽‍🦲)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f9b3), // woman: medium skin tone, white hair (👩🏽‍🦳)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f9bc), // woman in motorized wheelchair: medium skin tone (👩🏽‍🦼)
        intArrayOf(0x1f469, 0x1f3fd, 0x200d, 0x1f9bd), // woman in manual wheelchair: medium skin tone (👩🏽‍🦽)
        intArrayOf(0x1f469, 0x1f3fe), // woman: medium-dark skin tone (👩🏾)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x2695, 0xfe0f), // woman health worker: medium-dark skin tone (👩🏾‍⚕️)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x2696, 0xfe0f), // woman judge: medium-dark skin tone (👩🏾‍⚖️)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x2708, 0xfe0f), // woman pilot: medium-dark skin tone (👩🏾‍✈️)
        intArrayOf(
            0x1f469,
            0x1f3fe,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fe
        ), // couple with heart: woman, man, medium-dark skin tone (👩🏾‍❤️‍👨🏾)
        intArrayOf(
            0x1f469,
            0x1f3fe,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f469,
            0x1f3fe
        ), // couple with heart: woman, woman, medium-dark skin tone (👩🏾‍❤️‍👩🏾)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f33e), // woman farmer: medium-dark skin tone (👩🏾‍🌾)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f373), // woman cook: medium-dark skin tone (👩🏾‍🍳)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f37c), // woman feeding baby: medium-dark skin tone (👩🏾‍🍼)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f393), // woman student: medium-dark skin tone (👩🏾‍🎓)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f3a4), // woman singer: medium-dark skin tone (👩🏾‍🎤)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f3a8), // woman artist: medium-dark skin tone (👩🏾‍🎨)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f3eb), // woman teacher: medium-dark skin tone (👩🏾‍🏫)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f3ed), // woman factory worker: medium-dark skin tone (👩🏾‍🏭)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f4bb), // woman technologist: medium-dark skin tone (👩🏾‍💻)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f4bc), // woman office worker: medium-dark skin tone (👩🏾‍💼)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f527), // woman mechanic: medium-dark skin tone (👩🏾‍🔧)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f52c), // woman scientist: medium-dark skin tone (👩🏾‍🔬)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f680), // woman astronaut: medium-dark skin tone (👩🏾‍🚀)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f692), // woman firefighter: medium-dark skin tone (👩🏾‍🚒)
        intArrayOf(
            0x1f469,
            0x1f3fe,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fb
        ), // women holding hands: medium-dark skin tone, light skin tone (👩🏾‍🤝‍👩🏻)
        intArrayOf(
            0x1f469,
            0x1f3fe,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fd
        ), // women holding hands: medium-dark skin tone, medium skin tone (👩🏾‍🤝‍👩🏽)
        intArrayOf(
            0x1f469,
            0x1f3fe,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3ff
        ), // women holding hands: medium-dark skin tone, dark skin tone (👩🏾‍🤝‍👩🏿)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f9af), // woman with white cane: medium-dark skin tone (👩🏾‍🦯)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f9b0), // woman: medium-dark skin tone, red hair (👩🏾‍🦰)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f9b1), // woman: medium-dark skin tone, curly hair (👩🏾‍🦱)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f9b2), // woman: medium-dark skin tone, bald (👩🏾‍🦲)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f9b3), // woman: medium-dark skin tone, white hair (👩🏾‍🦳)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f9bc), // woman in motorized wheelchair: medium-dark skin tone (👩🏾‍🦼)
        intArrayOf(0x1f469, 0x1f3fe, 0x200d, 0x1f9bd), // woman in manual wheelchair: medium-dark skin tone (👩🏾‍🦽)
        intArrayOf(0x1f469, 0x1f3ff), // woman: dark skin tone (👩🏿)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x2695, 0xfe0f), // woman health worker: dark skin tone (👩🏿‍⚕️)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x2696, 0xfe0f), // woman judge: dark skin tone (👩🏿‍⚖️)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x2708, 0xfe0f), // woman pilot: dark skin tone (👩🏿‍✈️)
        intArrayOf(
            0x1f469,
            0x1f3ff,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3fb
        ), // couple with heart: woman, man, dark skin tone, light skin tone (👩🏿‍❤️‍👨🏻)
        intArrayOf(
            0x1f469,
            0x1f3ff,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f468,
            0x1f3ff
        ), // couple with heart: woman, man, dark skin tone (👩🏿‍❤️‍👨🏿)
        intArrayOf(
            0x1f469,
            0x1f3ff,
            0x200d,
            0x2764,
            0xfe0f,
            0x200d,
            0x1f469,
            0x1f3ff
        ), // couple with heart: woman, woman, dark skin tone (👩🏿‍❤️‍👩🏿)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f33e), // woman farmer: dark skin tone (👩🏿‍🌾)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f373), // woman cook: dark skin tone (👩🏿‍🍳)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f37c), // woman feeding baby: dark skin tone (👩🏿‍🍼)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f393), // woman student: dark skin tone (👩🏿‍🎓)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f3a4), // woman singer: dark skin tone (👩🏿‍🎤)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f3a8), // woman artist: dark skin tone (👩🏿‍🎨)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f3eb), // woman teacher: dark skin tone (👩🏿‍🏫)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f3ed), // woman factory worker: dark skin tone (👩🏿‍🏭)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f4bb), // woman technologist: dark skin tone (👩🏿‍💻)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f4bc), // woman office worker: dark skin tone (👩🏿‍💼)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f527), // woman mechanic: dark skin tone (👩🏿‍🔧)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f52c), // woman scientist: dark skin tone (👩🏿‍🔬)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f680), // woman astronaut: dark skin tone (👩🏿‍🚀)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f692), // woman firefighter: dark skin tone (👩🏿‍🚒)
        intArrayOf(
            0x1f469,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fb
        ), // woman and man holding hands: dark skin tone, light skin tone (👩🏿‍🤝‍👨🏻)
        intArrayOf(
            0x1f469,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f468,
            0x1f3fd
        ), // woman and man holding hands: dark skin tone, medium skin tone (👩🏿‍🤝‍👨🏽)
        intArrayOf(
            0x1f469,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fb
        ), // women holding hands: dark skin tone, light skin tone (👩🏿‍🤝‍👩🏻)
        intArrayOf(
            0x1f469,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fc
        ), // women holding hands: dark skin tone, medium-light skin tone (👩🏿‍🤝‍👩🏼)
        intArrayOf(
            0x1f469,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fd
        ), // women holding hands: dark skin tone, medium skin tone (👩🏿‍🤝‍👩🏽)
        intArrayOf(
            0x1f469,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f469,
            0x1f3fe
        ), // women holding hands: dark skin tone, medium-dark skin tone (👩🏿‍🤝‍👩🏾)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f9af), // woman with white cane: dark skin tone (👩🏿‍🦯)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f9b0), // woman: dark skin tone, red hair (👩🏿‍🦰)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f9b1), // woman: dark skin tone, curly hair (👩🏿‍🦱)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f9b2), // woman: dark skin tone, bald (👩🏿‍🦲)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f9b3), // woman: dark skin tone, white hair (👩🏿‍🦳)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f9bc), // woman in motorized wheelchair: dark skin tone (👩🏿‍🦼)
        intArrayOf(0x1f469, 0x1f3ff, 0x200d, 0x1f9bd), // woman in manual wheelchair: dark skin tone (👩🏿‍🦽)
        intArrayOf(0x1f46b, 0x1f3fb), // woman and man holding hands: light skin tone (👫🏻)
        intArrayOf(0x1f46b, 0x1f3fc), // woman and man holding hands: medium-light skin tone (👫🏼)
        intArrayOf(0x1f46b, 0x1f3fd), // woman and man holding hands: medium skin tone (👫🏽)
        intArrayOf(0x1f46b, 0x1f3fe), // woman and man holding hands: medium-dark skin tone (👫🏾)
        intArrayOf(0x1f46b, 0x1f3ff), // woman and man holding hands: dark skin tone (👫🏿)
        intArrayOf(0x1f46c, 0x1f3fb), // men holding hands: light skin tone (👬🏻)
        intArrayOf(0x1f46c, 0x1f3fc), // men holding hands: medium-light skin tone (👬🏼)
        intArrayOf(0x1f46c, 0x1f3fd), // men holding hands: medium skin tone (👬🏽)
        intArrayOf(0x1f46c, 0x1f3fe), // men holding hands: medium-dark skin tone (👬🏾)
        intArrayOf(0x1f46c, 0x1f3ff), // men holding hands: dark skin tone (👬🏿)
        intArrayOf(0x1f46d, 0x1f3fb), // women holding hands: light skin tone (👭🏻)
        intArrayOf(0x1f46d, 0x1f3fc), // women holding hands: medium-light skin tone (👭🏼)
        intArrayOf(0x1f46d, 0x1f3fd), // women holding hands: medium skin tone (👭🏽)
        intArrayOf(0x1f46d, 0x1f3fe), // women holding hands: medium-dark skin tone (👭🏾)
        intArrayOf(0x1f46d, 0x1f3ff), // women holding hands: dark skin tone (👭🏿)
        intArrayOf(0x1f46e, 0x200d, 0x2640, 0xfe0f), // woman police officer (👮‍♀️)
        intArrayOf(0x1f46e, 0x200d, 0x2642, 0xfe0f), // man police officer (👮‍♂️)
        intArrayOf(0x1f46e, 0x1f3fb), // police officer: light skin tone (👮🏻)
        intArrayOf(0x1f46e, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman police officer: light skin tone (👮🏻‍♀️)
        intArrayOf(0x1f46e, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man police officer: light skin tone (👮🏻‍♂️)
        intArrayOf(0x1f46e, 0x1f3fc), // police officer: medium-light skin tone (👮🏼)
        intArrayOf(0x1f46e, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman police officer: medium-light skin tone (👮🏼‍♀️)
        intArrayOf(0x1f46e, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man police officer: medium-light skin tone (👮🏼‍♂️)
        intArrayOf(0x1f46e, 0x1f3fd), // police officer: medium skin tone (👮🏽)
        intArrayOf(0x1f46e, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman police officer: medium skin tone (👮🏽‍♀️)
        intArrayOf(0x1f46e, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man police officer: medium skin tone (👮🏽‍♂️)
        intArrayOf(0x1f46e, 0x1f3fe), // police officer: medium-dark skin tone (👮🏾)
        intArrayOf(0x1f46e, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman police officer: medium-dark skin tone (👮🏾‍♀️)
        intArrayOf(0x1f46e, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man police officer: medium-dark skin tone (👮🏾‍♂️)
        intArrayOf(0x1f46e, 0x1f3ff), // police officer: dark skin tone (👮🏿)
        intArrayOf(0x1f46e, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman police officer: dark skin tone (👮🏿‍♀️)
        intArrayOf(0x1f46e, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man police officer: dark skin tone (👮🏿‍♂️)
        intArrayOf(0x1f46f, 0x200d, 0x2640, 0xfe0f), // women with bunny ears (👯‍♀️)
        intArrayOf(0x1f46f, 0x200d, 0x2642, 0xfe0f), // men with bunny ears (👯‍♂️)
        intArrayOf(0x1f470, 0x200d, 0x2640, 0xfe0f), // woman with veil (👰‍♀️)
        intArrayOf(0x1f470, 0x200d, 0x2642, 0xfe0f), // man with veil (👰‍♂️)
        intArrayOf(0x1f470, 0x1f3fb), // person with veil: light skin tone (👰🏻)
        intArrayOf(0x1f470, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman with veil: light skin tone (👰🏻‍♀️)
        intArrayOf(0x1f470, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man with veil: light skin tone (👰🏻‍♂️)
        intArrayOf(0x1f470, 0x1f3fc), // person with veil: medium-light skin tone (👰🏼)
        intArrayOf(0x1f470, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman with veil: medium-light skin tone (👰🏼‍♀️)
        intArrayOf(0x1f470, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man with veil: medium-light skin tone (👰🏼‍♂️)
        intArrayOf(0x1f470, 0x1f3fd), // person with veil: medium skin tone (👰🏽)
        intArrayOf(0x1f470, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman with veil: medium skin tone (👰🏽‍♀️)
        intArrayOf(0x1f470, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man with veil: medium skin tone (👰🏽‍♂️)
        intArrayOf(0x1f470, 0x1f3fe), // person with veil: medium-dark skin tone (👰🏾)
        intArrayOf(0x1f470, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman with veil: medium-dark skin tone (👰🏾‍♀️)
        intArrayOf(0x1f470, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man with veil: medium-dark skin tone (👰🏾‍♂️)
        intArrayOf(0x1f470, 0x1f3ff), // person with veil: dark skin tone (👰🏿)
        intArrayOf(0x1f470, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman with veil: dark skin tone (👰🏿‍♀️)
        intArrayOf(0x1f470, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man with veil: dark skin tone (👰🏿‍♂️)
        intArrayOf(0x1f471, 0x200d, 0x2640, 0xfe0f), // woman: blond hair (👱‍♀️)
        intArrayOf(0x1f471, 0x200d, 0x2642, 0xfe0f), // man: blond hair (👱‍♂️)
        intArrayOf(0x1f471, 0x1f3fb), // person: light skin tone, blond hair (👱🏻)
        intArrayOf(0x1f471, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman: light skin tone, blond hair (👱🏻‍♀️)
        intArrayOf(0x1f471, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man: light skin tone, blond hair (👱🏻‍♂️)
        intArrayOf(0x1f471, 0x1f3fc), // person: medium-light skin tone, blond hair (👱🏼)
        intArrayOf(0x1f471, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman: medium-light skin tone, blond hair (👱🏼‍♀️)
        intArrayOf(0x1f471, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man: medium-light skin tone, blond hair (👱🏼‍♂️)
        intArrayOf(0x1f471, 0x1f3fd), // person: medium skin tone, blond hair (👱🏽)
        intArrayOf(0x1f471, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman: medium skin tone, blond hair (👱🏽‍♀️)
        intArrayOf(0x1f471, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man: medium skin tone, blond hair (👱🏽‍♂️)
        intArrayOf(0x1f471, 0x1f3fe), // person: medium-dark skin tone, blond hair (👱🏾)
        intArrayOf(0x1f471, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman: medium-dark skin tone, blond hair (👱🏾‍♀️)
        intArrayOf(0x1f471, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man: medium-dark skin tone, blond hair (👱🏾‍♂️)
        intArrayOf(0x1f471, 0x1f3ff), // person: dark skin tone, blond hair (👱🏿)
        intArrayOf(0x1f471, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman: dark skin tone, blond hair (👱🏿‍♀️)
        intArrayOf(0x1f471, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man: dark skin tone, blond hair (👱🏿‍♂️)
        intArrayOf(0x1f472, 0x1f3fb), // person with skullcap: light skin tone (👲🏻)
        intArrayOf(0x1f472, 0x1f3fc), // person with skullcap: medium-light skin tone (👲🏼)
        intArrayOf(0x1f472, 0x1f3fd), // person with skullcap: medium skin tone (👲🏽)
        intArrayOf(0x1f472, 0x1f3fe), // person with skullcap: medium-dark skin tone (👲🏾)
        intArrayOf(0x1f472, 0x1f3ff), // person with skullcap: dark skin tone (👲🏿)
        intArrayOf(0x1f473, 0x200d, 0x2640, 0xfe0f), // woman wearing turban (👳‍♀️)
        intArrayOf(0x1f473, 0x200d, 0x2642, 0xfe0f), // man wearing turban (👳‍♂️)
        intArrayOf(0x1f473, 0x1f3fb), // person wearing turban: light skin tone (👳🏻)
        intArrayOf(0x1f473, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman wearing turban: light skin tone (👳🏻‍♀️)
        intArrayOf(0x1f473, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man wearing turban: light skin tone (👳🏻‍♂️)
        intArrayOf(0x1f473, 0x1f3fc), // person wearing turban: medium-light skin tone (👳🏼)
        intArrayOf(0x1f473, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman wearing turban: medium-light skin tone (👳🏼‍♀️)
        intArrayOf(0x1f473, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man wearing turban: medium-light skin tone (👳🏼‍♂️)
        intArrayOf(0x1f473, 0x1f3fd), // person wearing turban: medium skin tone (👳🏽)
        intArrayOf(0x1f473, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman wearing turban: medium skin tone (👳🏽‍♀️)
        intArrayOf(0x1f473, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man wearing turban: medium skin tone (👳🏽‍♂️)
        intArrayOf(0x1f473, 0x1f3fe), // person wearing turban: medium-dark skin tone (👳🏾)
        intArrayOf(0x1f473, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman wearing turban: medium-dark skin tone (👳🏾‍♀️)
        intArrayOf(0x1f473, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man wearing turban: medium-dark skin tone (👳🏾‍♂️)
        intArrayOf(0x1f473, 0x1f3ff), // person wearing turban: dark skin tone (👳🏿)
        intArrayOf(0x1f473, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman wearing turban: dark skin tone (👳🏿‍♀️)
        intArrayOf(0x1f473, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man wearing turban: dark skin tone (👳🏿‍♂️)
        intArrayOf(0x1f474, 0x1f3fb), // old man: light skin tone (👴🏻)
        intArrayOf(0x1f474, 0x1f3fc), // old man: medium-light skin tone (👴🏼)
        intArrayOf(0x1f474, 0x1f3fd), // old man: medium skin tone (👴🏽)
        intArrayOf(0x1f474, 0x1f3fe), // old man: medium-dark skin tone (👴🏾)
        intArrayOf(0x1f474, 0x1f3ff), // old man: dark skin tone (👴🏿)
        intArrayOf(0x1f475, 0x1f3fb), // old woman: light skin tone (👵🏻)
        intArrayOf(0x1f475, 0x1f3fc), // old woman: medium-light skin tone (👵🏼)
        intArrayOf(0x1f475, 0x1f3fd), // old woman: medium skin tone (👵🏽)
        intArrayOf(0x1f475, 0x1f3fe), // old woman: medium-dark skin tone (👵🏾)
        intArrayOf(0x1f475, 0x1f3ff), // old woman: dark skin tone (👵🏿)
        intArrayOf(0x1f476, 0x1f3fb), // baby: light skin tone (👶🏻)
        intArrayOf(0x1f476, 0x1f3fc), // baby: medium-light skin tone (👶🏼)
        intArrayOf(0x1f476, 0x1f3fd), // baby: medium skin tone (👶🏽)
        intArrayOf(0x1f476, 0x1f3fe), // baby: medium-dark skin tone (👶🏾)
        intArrayOf(0x1f476, 0x1f3ff), // baby: dark skin tone (👶🏿)
        intArrayOf(0x1f477, 0x200d, 0x2640, 0xfe0f), // woman construction worker (👷‍♀️)
        intArrayOf(0x1f477, 0x200d, 0x2642, 0xfe0f), // man construction worker (👷‍♂️)
        intArrayOf(0x1f477, 0x1f3fb), // construction worker: light skin tone (👷🏻)
        intArrayOf(0x1f477, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman construction worker: light skin tone (👷🏻‍♀️)
        intArrayOf(0x1f477, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man construction worker: light skin tone (👷🏻‍♂️)
        intArrayOf(0x1f477, 0x1f3fc), // construction worker: medium-light skin tone (👷🏼)
        intArrayOf(
            0x1f477,
            0x1f3fc,
            0x200d,
            0x2640,
            0xfe0f
        ), // woman construction worker: medium-light skin tone (👷🏼‍♀️)
        intArrayOf(
            0x1f477,
            0x1f3fc,
            0x200d,
            0x2642,
            0xfe0f
        ), // man construction worker: medium-light skin tone (👷🏼‍♂️)
        intArrayOf(0x1f477, 0x1f3fd), // construction worker: medium skin tone (👷🏽)
        intArrayOf(0x1f477, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman construction worker: medium skin tone (👷🏽‍♀️)
        intArrayOf(0x1f477, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man construction worker: medium skin tone (👷🏽‍♂️)
        intArrayOf(0x1f477, 0x1f3fe), // construction worker: medium-dark skin tone (👷🏾)
        intArrayOf(
            0x1f477,
            0x1f3fe,
            0x200d,
            0x2640,
            0xfe0f
        ), // woman construction worker: medium-dark skin tone (👷🏾‍♀️)
        intArrayOf(
            0x1f477,
            0x1f3fe,
            0x200d,
            0x2642,
            0xfe0f
        ), // man construction worker: medium-dark skin tone (👷🏾‍♂️)
        intArrayOf(0x1f477, 0x1f3ff), // construction worker: dark skin tone (👷🏿)
        intArrayOf(0x1f477, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman construction worker: dark skin tone (👷🏿‍♀️)
        intArrayOf(0x1f477, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man construction worker: dark skin tone (👷🏿‍♂️)
        intArrayOf(0x1f478, 0x1f3fb), // princess: light skin tone (👸🏻)
        intArrayOf(0x1f478, 0x1f3fc), // princess: medium-light skin tone (👸🏼)
        intArrayOf(0x1f478, 0x1f3fd), // princess: medium skin tone (👸🏽)
        intArrayOf(0x1f478, 0x1f3fe), // princess: medium-dark skin tone (👸🏾)
        intArrayOf(0x1f478, 0x1f3ff), // princess: dark skin tone (👸🏿)
        intArrayOf(0x1f47c, 0x1f3fb), // baby angel: light skin tone (👼🏻)
        intArrayOf(0x1f47c, 0x1f3fc), // baby angel: medium-light skin tone (👼🏼)
        intArrayOf(0x1f47c, 0x1f3fd), // baby angel: medium skin tone (👼🏽)
        intArrayOf(0x1f47c, 0x1f3fe), // baby angel: medium-dark skin tone (👼🏾)
        intArrayOf(0x1f47c, 0x1f3ff), // baby angel: dark skin tone (👼🏿)
        intArrayOf(0x1f481, 0x200d, 0x2640, 0xfe0f), // woman tipping hand (💁‍♀️)
        intArrayOf(0x1f481, 0x200d, 0x2642, 0xfe0f), // man tipping hand (💁‍♂️)
        intArrayOf(0x1f481, 0x1f3fb), // person tipping hand: light skin tone (💁🏻)
        intArrayOf(0x1f481, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman tipping hand: light skin tone (💁🏻‍♀️)
        intArrayOf(0x1f481, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man tipping hand: light skin tone (💁🏻‍♂️)
        intArrayOf(0x1f481, 0x1f3fc), // person tipping hand: medium-light skin tone (💁🏼)
        intArrayOf(0x1f481, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman tipping hand: medium-light skin tone (💁🏼‍♀️)
        intArrayOf(0x1f481, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man tipping hand: medium-light skin tone (💁🏼‍♂️)
        intArrayOf(0x1f481, 0x1f3fd), // person tipping hand: medium skin tone (💁🏽)
        intArrayOf(0x1f481, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman tipping hand: medium skin tone (💁🏽‍♀️)
        intArrayOf(0x1f481, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man tipping hand: medium skin tone (💁🏽‍♂️)
        intArrayOf(0x1f481, 0x1f3fe), // person tipping hand: medium-dark skin tone (💁🏾)
        intArrayOf(0x1f481, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman tipping hand: medium-dark skin tone (💁🏾‍♀️)
        intArrayOf(0x1f481, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man tipping hand: medium-dark skin tone (💁🏾‍♂️)
        intArrayOf(0x1f481, 0x1f3ff), // person tipping hand: dark skin tone (💁🏿)
        intArrayOf(0x1f481, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman tipping hand: dark skin tone (💁🏿‍♀️)
        intArrayOf(0x1f481, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man tipping hand: dark skin tone (💁🏿‍♂️)
        intArrayOf(0x1f482, 0x200d, 0x2640, 0xfe0f), // woman guard (💂‍♀️)
        intArrayOf(0x1f482, 0x200d, 0x2642, 0xfe0f), // man guard (💂‍♂️)
        intArrayOf(0x1f482, 0x1f3fb), // guard: light skin tone (💂🏻)
        intArrayOf(0x1f482, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman guard: light skin tone (💂🏻‍♀️)
        intArrayOf(0x1f482, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man guard: light skin tone (💂🏻‍♂️)
        intArrayOf(0x1f482, 0x1f3fc), // guard: medium-light skin tone (💂🏼)
        intArrayOf(0x1f482, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman guard: medium-light skin tone (💂🏼‍♀️)
        intArrayOf(0x1f482, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man guard: medium-light skin tone (💂🏼‍♂️)
        intArrayOf(0x1f482, 0x1f3fd), // guard: medium skin tone (💂🏽)
        intArrayOf(0x1f482, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman guard: medium skin tone (💂🏽‍♀️)
        intArrayOf(0x1f482, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man guard: medium skin tone (💂🏽‍♂️)
        intArrayOf(0x1f482, 0x1f3fe), // guard: medium-dark skin tone (💂🏾)
        intArrayOf(0x1f482, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman guard: medium-dark skin tone (💂🏾‍♀️)
        intArrayOf(0x1f482, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man guard: medium-dark skin tone (💂🏾‍♂️)
        intArrayOf(0x1f482, 0x1f3ff), // guard: dark skin tone (💂🏿)
        intArrayOf(0x1f482, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman guard: dark skin tone (💂🏿‍♀️)
        intArrayOf(0x1f482, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man guard: dark skin tone (💂🏿‍♂️)
        intArrayOf(0x1f483, 0x1f3fb), // woman dancing: light skin tone (💃🏻)
        intArrayOf(0x1f483, 0x1f3fc), // woman dancing: medium-light skin tone (💃🏼)
        intArrayOf(0x1f483, 0x1f3fd), // woman dancing: medium skin tone (💃🏽)
        intArrayOf(0x1f483, 0x1f3fe), // woman dancing: medium-dark skin tone (💃🏾)
        intArrayOf(0x1f483, 0x1f3ff), // woman dancing: dark skin tone (💃🏿)
        intArrayOf(0x1f485, 0x1f3fb), // nail polish: light skin tone (💅🏻)
        intArrayOf(0x1f485, 0x1f3fc), // nail polish: medium-light skin tone (💅🏼)
        intArrayOf(0x1f485, 0x1f3fd), // nail polish: medium skin tone (💅🏽)
        intArrayOf(0x1f485, 0x1f3fe), // nail polish: medium-dark skin tone (💅🏾)
        intArrayOf(0x1f485, 0x1f3ff), // nail polish: dark skin tone (💅🏿)
        intArrayOf(0x1f486, 0x200d, 0x2640, 0xfe0f), // woman getting massage (💆‍♀️)
        intArrayOf(0x1f486, 0x200d, 0x2642, 0xfe0f), // man getting massage (💆‍♂️)
        intArrayOf(0x1f486, 0x1f3fb), // person getting massage: light skin tone (💆🏻)
        intArrayOf(0x1f486, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman getting massage: light skin tone (💆🏻‍♀️)
        intArrayOf(0x1f486, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man getting massage: light skin tone (💆🏻‍♂️)
        intArrayOf(0x1f486, 0x1f3fc), // person getting massage: medium-light skin tone (💆🏼)
        intArrayOf(0x1f486, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman getting massage: medium-light skin tone (💆🏼‍♀️)
        intArrayOf(0x1f486, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man getting massage: medium-light skin tone (💆🏼‍♂️)
        intArrayOf(0x1f486, 0x1f3fd), // person getting massage: medium skin tone (💆🏽)
        intArrayOf(0x1f486, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman getting massage: medium skin tone (💆🏽‍♀️)
        intArrayOf(0x1f486, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man getting massage: medium skin tone (💆🏽‍♂️)
        intArrayOf(0x1f486, 0x1f3fe), // person getting massage: medium-dark skin tone (💆🏾)
        intArrayOf(0x1f486, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman getting massage: medium-dark skin tone (💆🏾‍♀️)
        intArrayOf(0x1f486, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man getting massage: medium-dark skin tone (💆🏾‍♂️)
        intArrayOf(0x1f486, 0x1f3ff), // person getting massage: dark skin tone (💆🏿)
        intArrayOf(0x1f486, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman getting massage: dark skin tone (💆🏿‍♀️)
        intArrayOf(0x1f486, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man getting massage: dark skin tone (💆🏿‍♂️)
        intArrayOf(0x1f487, 0x200d, 0x2640, 0xfe0f), // woman getting haircut (💇‍♀️)
        intArrayOf(0x1f487, 0x200d, 0x2642, 0xfe0f), // man getting haircut (💇‍♂️)
        intArrayOf(0x1f487, 0x1f3fb), // person getting haircut: light skin tone (💇🏻)
        intArrayOf(0x1f487, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman getting haircut: light skin tone (💇🏻‍♀️)
        intArrayOf(0x1f487, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man getting haircut: light skin tone (💇🏻‍♂️)
        intArrayOf(0x1f487, 0x1f3fc), // person getting haircut: medium-light skin tone (💇🏼)
        intArrayOf(0x1f487, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman getting haircut: medium-light skin tone (💇🏼‍♀️)
        intArrayOf(0x1f487, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man getting haircut: medium-light skin tone (💇🏼‍♂️)
        intArrayOf(0x1f487, 0x1f3fd), // person getting haircut: medium skin tone (💇🏽)
        intArrayOf(0x1f487, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman getting haircut: medium skin tone (💇🏽‍♀️)
        intArrayOf(0x1f487, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man getting haircut: medium skin tone (💇🏽‍♂️)
        intArrayOf(0x1f487, 0x1f3fe), // person getting haircut: medium-dark skin tone (💇🏾)
        intArrayOf(0x1f487, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman getting haircut: medium-dark skin tone (💇🏾‍♀️)
        intArrayOf(0x1f487, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man getting haircut: medium-dark skin tone (💇🏾‍♂️)
        intArrayOf(0x1f487, 0x1f3ff), // person getting haircut: dark skin tone (💇🏿)
        intArrayOf(0x1f487, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman getting haircut: dark skin tone (💇🏿‍♀️)
        intArrayOf(0x1f487, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man getting haircut: dark skin tone (💇🏿‍♂️)
        intArrayOf(0x1f48f, 0x1f3fb), // kiss: light skin tone (💏🏻)
        intArrayOf(0x1f48f, 0x1f3fc), // kiss: medium-light skin tone (💏🏼)
        intArrayOf(0x1f48f, 0x1f3fd), // kiss: medium skin tone (💏🏽)
        intArrayOf(0x1f48f, 0x1f3fe), // kiss: medium-dark skin tone (💏🏾)
        intArrayOf(0x1f48f, 0x1f3ff), // kiss: dark skin tone (💏🏿)
        intArrayOf(0x1f491, 0x1f3fb), // couple with heart: light skin tone (💑🏻)
        intArrayOf(0x1f491, 0x1f3fc), // couple with heart: medium-light skin tone (💑🏼)
        intArrayOf(0x1f491, 0x1f3fd), // couple with heart: medium skin tone (💑🏽)
        intArrayOf(0x1f491, 0x1f3fe), // couple with heart: medium-dark skin tone (💑🏾)
        intArrayOf(0x1f491, 0x1f3ff), // couple with heart: dark skin tone (💑🏿)
        intArrayOf(0x1f4aa, 0x1f3fb), // flexed biceps: light skin tone (💪🏻)
        intArrayOf(0x1f4aa, 0x1f3fc), // flexed biceps: medium-light skin tone (💪🏼)
        intArrayOf(0x1f4aa, 0x1f3fd), // flexed biceps: medium skin tone (💪🏽)
        intArrayOf(0x1f4aa, 0x1f3fe), // flexed biceps: medium-dark skin tone (💪🏾)
        intArrayOf(0x1f4aa, 0x1f3ff), // flexed biceps: dark skin tone (💪🏿)
        intArrayOf(0x1f574, 0x1f3fb), // person in suit levitating: light skin tone (🕴🏻)
        intArrayOf(0x1f574, 0x1f3fc), // person in suit levitating: medium-light skin tone (🕴🏼)
        intArrayOf(0x1f574, 0x1f3fd), // person in suit levitating: medium skin tone (🕴🏽)
        intArrayOf(0x1f574, 0x1f3fe), // person in suit levitating: medium-dark skin tone (🕴🏾)
        intArrayOf(0x1f574, 0x1f3ff), // person in suit levitating: dark skin tone (🕴🏿)
        intArrayOf(0x1f575, 0xfe0f, 0x200d, 0x2640, 0xfe0f), // woman detective (🕵️‍♀️)
        intArrayOf(0x1f575, 0xfe0f, 0x200d, 0x2642, 0xfe0f), // man detective (🕵️‍♂️)
        intArrayOf(0x1f575, 0x1f3fb), // detective: light skin tone (🕵🏻)
        intArrayOf(0x1f575, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman detective: light skin tone (🕵🏻‍♀️)
        intArrayOf(0x1f575, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man detective: light skin tone (🕵🏻‍♂️)
        intArrayOf(0x1f575, 0x1f3fc), // detective: medium-light skin tone (🕵🏼)
        intArrayOf(0x1f575, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman detective: medium-light skin tone (🕵🏼‍♀️)
        intArrayOf(0x1f575, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man detective: medium-light skin tone (🕵🏼‍♂️)
        intArrayOf(0x1f575, 0x1f3fd), // detective: medium skin tone (🕵🏽)
        intArrayOf(0x1f575, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman detective: medium skin tone (🕵🏽‍♀️)
        intArrayOf(0x1f575, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man detective: medium skin tone (🕵🏽‍♂️)
        intArrayOf(0x1f575, 0x1f3fe), // detective: medium-dark skin tone (🕵🏾)
        intArrayOf(0x1f575, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman detective: medium-dark skin tone (🕵🏾‍♀️)
        intArrayOf(0x1f575, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man detective: medium-dark skin tone (🕵🏾‍♂️)
        intArrayOf(0x1f575, 0x1f3ff), // detective: dark skin tone (🕵🏿)
        intArrayOf(0x1f575, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman detective: dark skin tone (🕵🏿‍♀️)
        intArrayOf(0x1f575, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man detective: dark skin tone (🕵🏿‍♂️)
        intArrayOf(0x1f57a, 0x1f3fb), // man dancing: light skin tone (🕺🏻)
        intArrayOf(0x1f57a, 0x1f3fc), // man dancing: medium-light skin tone (🕺🏼)
        intArrayOf(0x1f57a, 0x1f3fd), // man dancing: medium skin tone (🕺🏽)
        intArrayOf(0x1f57a, 0x1f3fe), // man dancing: medium-dark skin tone (🕺🏾)
        intArrayOf(0x1f57a, 0x1f3ff), // man dancing: dark skin tone (🕺🏿)
        intArrayOf(0x1f590, 0x1f3fb), // hand with fingers splayed: light skin tone (🖐🏻)
        intArrayOf(0x1f590, 0x1f3fc), // hand with fingers splayed: medium-light skin tone (🖐🏼)
        intArrayOf(0x1f590, 0x1f3fd), // hand with fingers splayed: medium skin tone (🖐🏽)
        intArrayOf(0x1f590, 0x1f3fe), // hand with fingers splayed: medium-dark skin tone (🖐🏾)
        intArrayOf(0x1f590, 0x1f3ff), // hand with fingers splayed: dark skin tone (🖐🏿)
        intArrayOf(0x1f595, 0x1f3fb), // middle finger: light skin tone (🖕🏻)
        intArrayOf(0x1f595, 0x1f3fc), // middle finger: medium-light skin tone (🖕🏼)
        intArrayOf(0x1f595, 0x1f3fd), // middle finger: medium skin tone (🖕🏽)
        intArrayOf(0x1f595, 0x1f3fe), // middle finger: medium-dark skin tone (🖕🏾)
        intArrayOf(0x1f595, 0x1f3ff), // middle finger: dark skin tone (🖕🏿)
        intArrayOf(0x1f596, 0x1f3fb), // vulcan salute: light skin tone (🖖🏻)
        intArrayOf(0x1f596, 0x1f3fc), // vulcan salute: medium-light skin tone (🖖🏼)
        intArrayOf(0x1f596, 0x1f3fd), // vulcan salute: medium skin tone (🖖🏽)
        intArrayOf(0x1f596, 0x1f3fe), // vulcan salute: medium-dark skin tone (🖖🏾)
        intArrayOf(0x1f596, 0x1f3ff), // vulcan salute: dark skin tone (🖖🏿)
        intArrayOf(0x1f62e, 0x200d, 0x1f4a8), // face exhaling (😮‍💨)
        intArrayOf(0x1f635, 0x200d, 0x1f4ab), // face with spiral eyes (😵‍💫)
        intArrayOf(0x1f636, 0x200d, 0x1f32b, 0xfe0f), // face in clouds (😶‍🌫️)
        intArrayOf(0x1f645, 0x200d, 0x2640, 0xfe0f), // woman gesturing NO (🙅‍♀️)
        intArrayOf(0x1f645, 0x200d, 0x2642, 0xfe0f), // man gesturing NO (🙅‍♂️)
        intArrayOf(0x1f645, 0x1f3fb), // person gesturing NO: light skin tone (🙅🏻)
        intArrayOf(0x1f645, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman gesturing NO: light skin tone (🙅🏻‍♀️)
        intArrayOf(0x1f645, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man gesturing NO: light skin tone (🙅🏻‍♂️)
        intArrayOf(0x1f645, 0x1f3fc), // person gesturing NO: medium-light skin tone (🙅🏼)
        intArrayOf(0x1f645, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman gesturing NO: medium-light skin tone (🙅🏼‍♀️)
        intArrayOf(0x1f645, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man gesturing NO: medium-light skin tone (🙅🏼‍♂️)
        intArrayOf(0x1f645, 0x1f3fd), // person gesturing NO: medium skin tone (🙅🏽)
        intArrayOf(0x1f645, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman gesturing NO: medium skin tone (🙅🏽‍♀️)
        intArrayOf(0x1f645, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man gesturing NO: medium skin tone (🙅🏽‍♂️)
        intArrayOf(0x1f645, 0x1f3fe), // person gesturing NO: medium-dark skin tone (🙅🏾)
        intArrayOf(0x1f645, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman gesturing NO: medium-dark skin tone (🙅🏾‍♀️)
        intArrayOf(0x1f645, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man gesturing NO: medium-dark skin tone (🙅🏾‍♂️)
        intArrayOf(0x1f645, 0x1f3ff), // person gesturing NO: dark skin tone (🙅🏿)
        intArrayOf(0x1f645, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman gesturing NO: dark skin tone (🙅🏿‍♀️)
        intArrayOf(0x1f645, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man gesturing NO: dark skin tone (🙅🏿‍♂️)
        intArrayOf(0x1f646, 0x200d, 0x2640, 0xfe0f), // woman gesturing OK (🙆‍♀️)
        intArrayOf(0x1f646, 0x200d, 0x2642, 0xfe0f), // man gesturing OK (🙆‍♂️)
        intArrayOf(0x1f646, 0x1f3fb), // person gesturing OK: light skin tone (🙆🏻)
        intArrayOf(0x1f646, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman gesturing OK: light skin tone (🙆🏻‍♀️)
        intArrayOf(0x1f646, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man gesturing OK: light skin tone (🙆🏻‍♂️)
        intArrayOf(0x1f646, 0x1f3fc), // person gesturing OK: medium-light skin tone (🙆🏼)
        intArrayOf(0x1f646, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman gesturing OK: medium-light skin tone (🙆🏼‍♀️)
        intArrayOf(0x1f646, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man gesturing OK: medium-light skin tone (🙆🏼‍♂️)
        intArrayOf(0x1f646, 0x1f3fd), // person gesturing OK: medium skin tone (🙆🏽)
        intArrayOf(0x1f646, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman gesturing OK: medium skin tone (🙆🏽‍♀️)
        intArrayOf(0x1f646, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man gesturing OK: medium skin tone (🙆🏽‍♂️)
        intArrayOf(0x1f646, 0x1f3fe), // person gesturing OK: medium-dark skin tone (🙆🏾)
        intArrayOf(0x1f646, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman gesturing OK: medium-dark skin tone (🙆🏾‍♀️)
        intArrayOf(0x1f646, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man gesturing OK: medium-dark skin tone (🙆🏾‍♂️)
        intArrayOf(0x1f646, 0x1f3ff), // person gesturing OK: dark skin tone (🙆🏿)
        intArrayOf(0x1f646, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman gesturing OK: dark skin tone (🙆🏿‍♀️)
        intArrayOf(0x1f646, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man gesturing OK: dark skin tone (🙆🏿‍♂️)
        intArrayOf(0x1f647, 0x200d, 0x2640, 0xfe0f), // woman bowing (🙇‍♀️)
        intArrayOf(0x1f647, 0x200d, 0x2642, 0xfe0f), // man bowing (🙇‍♂️)
        intArrayOf(0x1f647, 0x1f3fb), // person bowing: light skin tone (🙇🏻)
        intArrayOf(0x1f647, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman bowing: light skin tone (🙇🏻‍♀️)
        intArrayOf(0x1f647, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man bowing: light skin tone (🙇🏻‍♂️)
        intArrayOf(0x1f647, 0x1f3fc), // person bowing: medium-light skin tone (🙇🏼)
        intArrayOf(0x1f647, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman bowing: medium-light skin tone (🙇🏼‍♀️)
        intArrayOf(0x1f647, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man bowing: medium-light skin tone (🙇🏼‍♂️)
        intArrayOf(0x1f647, 0x1f3fd), // person bowing: medium skin tone (🙇🏽)
        intArrayOf(0x1f647, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman bowing: medium skin tone (🙇🏽‍♀️)
        intArrayOf(0x1f647, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man bowing: medium skin tone (🙇🏽‍♂️)
        intArrayOf(0x1f647, 0x1f3fe), // person bowing: medium-dark skin tone (🙇🏾)
        intArrayOf(0x1f647, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman bowing: medium-dark skin tone (🙇🏾‍♀️)
        intArrayOf(0x1f647, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man bowing: medium-dark skin tone (🙇🏾‍♂️)
        intArrayOf(0x1f647, 0x1f3ff), // person bowing: dark skin tone (🙇🏿)
        intArrayOf(0x1f647, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman bowing: dark skin tone (🙇🏿‍♀️)
        intArrayOf(0x1f647, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man bowing: dark skin tone (🙇🏿‍♂️)
        intArrayOf(0x1f64b, 0x200d, 0x2640, 0xfe0f), // woman raising hand (🙋‍♀️)
        intArrayOf(0x1f64b, 0x200d, 0x2642, 0xfe0f), // man raising hand (🙋‍♂️)
        intArrayOf(0x1f64b, 0x1f3fb), // person raising hand: light skin tone (🙋🏻)
        intArrayOf(0x1f64b, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman raising hand: light skin tone (🙋🏻‍♀️)
        intArrayOf(0x1f64b, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man raising hand: light skin tone (🙋🏻‍♂️)
        intArrayOf(0x1f64b, 0x1f3fc), // person raising hand: medium-light skin tone (🙋🏼)
        intArrayOf(0x1f64b, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman raising hand: medium-light skin tone (🙋🏼‍♀️)
        intArrayOf(0x1f64b, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man raising hand: medium-light skin tone (🙋🏼‍♂️)
        intArrayOf(0x1f64b, 0x1f3fd), // person raising hand: medium skin tone (🙋🏽)
        intArrayOf(0x1f64b, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman raising hand: medium skin tone (🙋🏽‍♀️)
        intArrayOf(0x1f64b, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man raising hand: medium skin tone (🙋🏽‍♂️)
        intArrayOf(0x1f64b, 0x1f3fe), // person raising hand: medium-dark skin tone (🙋🏾)
        intArrayOf(0x1f64b, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman raising hand: medium-dark skin tone (🙋🏾‍♀️)
        intArrayOf(0x1f64b, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man raising hand: medium-dark skin tone (🙋🏾‍♂️)
        intArrayOf(0x1f64b, 0x1f3ff), // person raising hand: dark skin tone (🙋🏿)
        intArrayOf(0x1f64b, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman raising hand: dark skin tone (🙋🏿‍♀️)
        intArrayOf(0x1f64b, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man raising hand: dark skin tone (🙋🏿‍♂️)
        intArrayOf(0x1f64c, 0x1f3fb), // raising hands: light skin tone (🙌🏻)
        intArrayOf(0x1f64c, 0x1f3fc), // raising hands: medium-light skin tone (🙌🏼)
        intArrayOf(0x1f64c, 0x1f3fd), // raising hands: medium skin tone (🙌🏽)
        intArrayOf(0x1f64c, 0x1f3fe), // raising hands: medium-dark skin tone (🙌🏾)
        intArrayOf(0x1f64c, 0x1f3ff), // raising hands: dark skin tone (🙌🏿)
        intArrayOf(0x1f64d, 0x200d, 0x2640, 0xfe0f), // woman frowning (🙍‍♀️)
        intArrayOf(0x1f64d, 0x200d, 0x2642, 0xfe0f), // man frowning (🙍‍♂️)
        intArrayOf(0x1f64d, 0x1f3fb), // person frowning: light skin tone (🙍🏻)
        intArrayOf(0x1f64d, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman frowning: light skin tone (🙍🏻‍♀️)
        intArrayOf(0x1f64d, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man frowning: light skin tone (🙍🏻‍♂️)
        intArrayOf(0x1f64d, 0x1f3fc), // person frowning: medium-light skin tone (🙍🏼)
        intArrayOf(0x1f64d, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman frowning: medium-light skin tone (🙍🏼‍♀️)
        intArrayOf(0x1f64d, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man frowning: medium-light skin tone (🙍🏼‍♂️)
        intArrayOf(0x1f64d, 0x1f3fd), // person frowning: medium skin tone (🙍🏽)
        intArrayOf(0x1f64d, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman frowning: medium skin tone (🙍🏽‍♀️)
        intArrayOf(0x1f64d, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man frowning: medium skin tone (🙍🏽‍♂️)
        intArrayOf(0x1f64d, 0x1f3fe), // person frowning: medium-dark skin tone (🙍🏾)
        intArrayOf(0x1f64d, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman frowning: medium-dark skin tone (🙍🏾‍♀️)
        intArrayOf(0x1f64d, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man frowning: medium-dark skin tone (🙍🏾‍♂️)
        intArrayOf(0x1f64d, 0x1f3ff), // person frowning: dark skin tone (🙍🏿)
        intArrayOf(0x1f64d, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman frowning: dark skin tone (🙍🏿‍♀️)
        intArrayOf(0x1f64d, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man frowning: dark skin tone (🙍🏿‍♂️)
        intArrayOf(0x1f64e, 0x200d, 0x2640, 0xfe0f), // woman pouting (🙎‍♀️)
        intArrayOf(0x1f64e, 0x200d, 0x2642, 0xfe0f), // man pouting (🙎‍♂️)
        intArrayOf(0x1f64e, 0x1f3fb), // person pouting: light skin tone (🙎🏻)
        intArrayOf(0x1f64e, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman pouting: light skin tone (🙎🏻‍♀️)
        intArrayOf(0x1f64e, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man pouting: light skin tone (🙎🏻‍♂️)
        intArrayOf(0x1f64e, 0x1f3fc), // person pouting: medium-light skin tone (🙎🏼)
        intArrayOf(0x1f64e, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman pouting: medium-light skin tone (🙎🏼‍♀️)
        intArrayOf(0x1f64e, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man pouting: medium-light skin tone (🙎🏼‍♂️)
        intArrayOf(0x1f64e, 0x1f3fd), // person pouting: medium skin tone (🙎🏽)
        intArrayOf(0x1f64e, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman pouting: medium skin tone (🙎🏽‍♀️)
        intArrayOf(0x1f64e, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man pouting: medium skin tone (🙎🏽‍♂️)
        intArrayOf(0x1f64e, 0x1f3fe), // person pouting: medium-dark skin tone (🙎🏾)
        intArrayOf(0x1f64e, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman pouting: medium-dark skin tone (🙎🏾‍♀️)
        intArrayOf(0x1f64e, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man pouting: medium-dark skin tone (🙎🏾‍♂️)
        intArrayOf(0x1f64e, 0x1f3ff), // person pouting: dark skin tone (🙎🏿)
        intArrayOf(0x1f64e, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman pouting: dark skin tone (🙎🏿‍♀️)
        intArrayOf(0x1f64e, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man pouting: dark skin tone (🙎🏿‍♂️)
        intArrayOf(0x1f64f, 0x1f3fb), // folded hands: light skin tone (🙏🏻)
        intArrayOf(0x1f64f, 0x1f3fc), // folded hands: medium-light skin tone (🙏🏼)
        intArrayOf(0x1f64f, 0x1f3fd), // folded hands: medium skin tone (🙏🏽)
        intArrayOf(0x1f64f, 0x1f3fe), // folded hands: medium-dark skin tone (🙏🏾)
        intArrayOf(0x1f64f, 0x1f3ff), // folded hands: dark skin tone (🙏🏿)
        intArrayOf(0x1f6a3, 0x200d, 0x2640, 0xfe0f), // woman rowing boat (🚣‍♀️)
        intArrayOf(0x1f6a3, 0x200d, 0x2642, 0xfe0f), // man rowing boat (🚣‍♂️)
        intArrayOf(0x1f6a3, 0x1f3fb), // person rowing boat: light skin tone (🚣🏻)
        intArrayOf(0x1f6a3, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman rowing boat: light skin tone (🚣🏻‍♀️)
        intArrayOf(0x1f6a3, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man rowing boat: light skin tone (🚣🏻‍♂️)
        intArrayOf(0x1f6a3, 0x1f3fc), // person rowing boat: medium-light skin tone (🚣🏼)
        intArrayOf(0x1f6a3, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman rowing boat: medium-light skin tone (🚣🏼‍♀️)
        intArrayOf(0x1f6a3, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man rowing boat: medium-light skin tone (🚣🏼‍♂️)
        intArrayOf(0x1f6a3, 0x1f3fd), // person rowing boat: medium skin tone (🚣🏽)
        intArrayOf(0x1f6a3, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman rowing boat: medium skin tone (🚣🏽‍♀️)
        intArrayOf(0x1f6a3, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man rowing boat: medium skin tone (🚣🏽‍♂️)
        intArrayOf(0x1f6a3, 0x1f3fe), // person rowing boat: medium-dark skin tone (🚣🏾)
        intArrayOf(0x1f6a3, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman rowing boat: medium-dark skin tone (🚣🏾‍♀️)
        intArrayOf(0x1f6a3, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man rowing boat: medium-dark skin tone (🚣🏾‍♂️)
        intArrayOf(0x1f6a3, 0x1f3ff), // person rowing boat: dark skin tone (🚣🏿)
        intArrayOf(0x1f6a3, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman rowing boat: dark skin tone (🚣🏿‍♀️)
        intArrayOf(0x1f6a3, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man rowing boat: dark skin tone (🚣🏿‍♂️)
        intArrayOf(0x1f6b4, 0x200d, 0x2640, 0xfe0f), // woman biking (🚴‍♀️)
        intArrayOf(0x1f6b4, 0x200d, 0x2642, 0xfe0f), // man biking (🚴‍♂️)
        intArrayOf(0x1f6b4, 0x1f3fb), // person biking: light skin tone (🚴🏻)
        intArrayOf(0x1f6b4, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman biking: light skin tone (🚴🏻‍♀️)
        intArrayOf(0x1f6b4, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man biking: light skin tone (🚴🏻‍♂️)
        intArrayOf(0x1f6b4, 0x1f3fc), // person biking: medium-light skin tone (🚴🏼)
        intArrayOf(0x1f6b4, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman biking: medium-light skin tone (🚴🏼‍♀️)
        intArrayOf(0x1f6b4, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man biking: medium-light skin tone (🚴🏼‍♂️)
        intArrayOf(0x1f6b4, 0x1f3fd), // person biking: medium skin tone (🚴🏽)
        intArrayOf(0x1f6b4, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman biking: medium skin tone (🚴🏽‍♀️)
        intArrayOf(0x1f6b4, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man biking: medium skin tone (🚴🏽‍♂️)
        intArrayOf(0x1f6b4, 0x1f3fe), // person biking: medium-dark skin tone (🚴🏾)
        intArrayOf(0x1f6b4, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman biking: medium-dark skin tone (🚴🏾‍♀️)
        intArrayOf(0x1f6b4, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man biking: medium-dark skin tone (🚴🏾‍♂️)
        intArrayOf(0x1f6b4, 0x1f3ff), // person biking: dark skin tone (🚴🏿)
        intArrayOf(0x1f6b4, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman biking: dark skin tone (🚴🏿‍♀️)
        intArrayOf(0x1f6b4, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man biking: dark skin tone (🚴🏿‍♂️)
        intArrayOf(0x1f6b5, 0x200d, 0x2640, 0xfe0f), // woman mountain biking (🚵‍♀️)
        intArrayOf(0x1f6b5, 0x200d, 0x2642, 0xfe0f), // man mountain biking (🚵‍♂️)
        intArrayOf(0x1f6b5, 0x1f3fb), // person mountain biking: light skin tone (🚵🏻)
        intArrayOf(0x1f6b5, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman mountain biking: light skin tone (🚵🏻‍♀️)
        intArrayOf(0x1f6b5, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man mountain biking: light skin tone (🚵🏻‍♂️)
        intArrayOf(0x1f6b5, 0x1f3fc), // person mountain biking: medium-light skin tone (🚵🏼)
        intArrayOf(0x1f6b5, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman mountain biking: medium-light skin tone (🚵🏼‍♀️)
        intArrayOf(0x1f6b5, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man mountain biking: medium-light skin tone (🚵🏼‍♂️)
        intArrayOf(0x1f6b5, 0x1f3fd), // person mountain biking: medium skin tone (🚵🏽)
        intArrayOf(0x1f6b5, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman mountain biking: medium skin tone (🚵🏽‍♀️)
        intArrayOf(0x1f6b5, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man mountain biking: medium skin tone (🚵🏽‍♂️)
        intArrayOf(0x1f6b5, 0x1f3fe), // person mountain biking: medium-dark skin tone (🚵🏾)
        intArrayOf(0x1f6b5, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman mountain biking: medium-dark skin tone (🚵🏾‍♀️)
        intArrayOf(0x1f6b5, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man mountain biking: medium-dark skin tone (🚵🏾‍♂️)
        intArrayOf(0x1f6b5, 0x1f3ff), // person mountain biking: dark skin tone (🚵🏿)
        intArrayOf(0x1f6b5, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman mountain biking: dark skin tone (🚵🏿‍♀️)
        intArrayOf(0x1f6b5, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man mountain biking: dark skin tone (🚵🏿‍♂️)
        intArrayOf(0x1f6b6, 0x200d, 0x2640, 0xfe0f), // woman walking (🚶‍♀️)
        intArrayOf(0x1f6b6, 0x200d, 0x2642, 0xfe0f), // man walking (🚶‍♂️)
        intArrayOf(0x1f6b6, 0x1f3fb), // person walking: light skin tone (🚶🏻)
        intArrayOf(0x1f6b6, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman walking: light skin tone (🚶🏻‍♀️)
        intArrayOf(0x1f6b6, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man walking: light skin tone (🚶🏻‍♂️)
        intArrayOf(0x1f6b6, 0x1f3fc), // person walking: medium-light skin tone (🚶🏼)
        intArrayOf(0x1f6b6, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman walking: medium-light skin tone (🚶🏼‍♀️)
        intArrayOf(0x1f6b6, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man walking: medium-light skin tone (🚶🏼‍♂️)
        intArrayOf(0x1f6b6, 0x1f3fd), // person walking: medium skin tone (🚶🏽)
        intArrayOf(0x1f6b6, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman walking: medium skin tone (🚶🏽‍♀️)
        intArrayOf(0x1f6b6, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man walking: medium skin tone (🚶🏽‍♂️)
        intArrayOf(0x1f6b6, 0x1f3fe), // person walking: medium-dark skin tone (🚶🏾)
        intArrayOf(0x1f6b6, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman walking: medium-dark skin tone (🚶🏾‍♀️)
        intArrayOf(0x1f6b6, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man walking: medium-dark skin tone (🚶🏾‍♂️)
        intArrayOf(0x1f6b6, 0x1f3ff), // person walking: dark skin tone (🚶🏿)
        intArrayOf(0x1f6b6, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman walking: dark skin tone (🚶🏿‍♀️)
        intArrayOf(0x1f6b6, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man walking: dark skin tone (🚶🏿‍♂️)
        intArrayOf(0x1f6c0, 0x1f3fb), // person taking bath: light skin tone (🛀🏻)
        intArrayOf(0x1f6c0, 0x1f3fc), // person taking bath: medium-light skin tone (🛀🏼)
        intArrayOf(0x1f6c0, 0x1f3fd), // person taking bath: medium skin tone (🛀🏽)
        intArrayOf(0x1f6c0, 0x1f3fe), // person taking bath: medium-dark skin tone (🛀🏾)
        intArrayOf(0x1f6c0, 0x1f3ff), // person taking bath: dark skin tone (🛀🏿)
        intArrayOf(0x1f6cc, 0x1f3fb), // person in bed: light skin tone (🛌🏻)
        intArrayOf(0x1f6cc, 0x1f3fc), // person in bed: medium-light skin tone (🛌🏼)
        intArrayOf(0x1f6cc, 0x1f3fd), // person in bed: medium skin tone (🛌🏽)
        intArrayOf(0x1f6cc, 0x1f3fe), // person in bed: medium-dark skin tone (🛌🏾)
        intArrayOf(0x1f6cc, 0x1f3ff), // person in bed: dark skin tone (🛌🏿)
        intArrayOf(0x1f90c, 0x1f3fb), // pinched fingers: light skin tone (🤌🏻)
        intArrayOf(0x1f90c, 0x1f3fc), // pinched fingers: medium-light skin tone (🤌🏼)
        intArrayOf(0x1f90c, 0x1f3fd), // pinched fingers: medium skin tone (🤌🏽)
        intArrayOf(0x1f90c, 0x1f3fe), // pinched fingers: medium-dark skin tone (🤌🏾)
        intArrayOf(0x1f90c, 0x1f3ff), // pinched fingers: dark skin tone (🤌🏿)
        intArrayOf(0x1f90f, 0x1f3fb), // pinching hand: light skin tone (🤏🏻)
        intArrayOf(0x1f90f, 0x1f3fc), // pinching hand: medium-light skin tone (🤏🏼)
        intArrayOf(0x1f90f, 0x1f3fd), // pinching hand: medium skin tone (🤏🏽)
        intArrayOf(0x1f90f, 0x1f3fe), // pinching hand: medium-dark skin tone (🤏🏾)
        intArrayOf(0x1f90f, 0x1f3ff), // pinching hand: dark skin tone (🤏🏿)
        intArrayOf(0x1f918, 0x1f3fb), // sign of the horns: light skin tone (🤘🏻)
        intArrayOf(0x1f918, 0x1f3fc), // sign of the horns: medium-light skin tone (🤘🏼)
        intArrayOf(0x1f918, 0x1f3fd), // sign of the horns: medium skin tone (🤘🏽)
        intArrayOf(0x1f918, 0x1f3fe), // sign of the horns: medium-dark skin tone (🤘🏾)
        intArrayOf(0x1f918, 0x1f3ff), // sign of the horns: dark skin tone (🤘🏿)
        intArrayOf(0x1f919, 0x1f3fb), // call me hand: light skin tone (🤙🏻)
        intArrayOf(0x1f919, 0x1f3fc), // call me hand: medium-light skin tone (🤙🏼)
        intArrayOf(0x1f919, 0x1f3fd), // call me hand: medium skin tone (🤙🏽)
        intArrayOf(0x1f919, 0x1f3fe), // call me hand: medium-dark skin tone (🤙🏾)
        intArrayOf(0x1f919, 0x1f3ff), // call me hand: dark skin tone (🤙🏿)
        intArrayOf(0x1f91a, 0x1f3fb), // raised back of hand: light skin tone (🤚🏻)
        intArrayOf(0x1f91a, 0x1f3fc), // raised back of hand: medium-light skin tone (🤚🏼)
        intArrayOf(0x1f91a, 0x1f3fd), // raised back of hand: medium skin tone (🤚🏽)
        intArrayOf(0x1f91a, 0x1f3fe), // raised back of hand: medium-dark skin tone (🤚🏾)
        intArrayOf(0x1f91a, 0x1f3ff), // raised back of hand: dark skin tone (🤚🏿)
        intArrayOf(0x1f91b, 0x1f3fb), // left-facing fist: light skin tone (🤛🏻)
        intArrayOf(0x1f91b, 0x1f3fc), // left-facing fist: medium-light skin tone (🤛🏼)
        intArrayOf(0x1f91b, 0x1f3fd), // left-facing fist: medium skin tone (🤛🏽)
        intArrayOf(0x1f91b, 0x1f3fe), // left-facing fist: medium-dark skin tone (🤛🏾)
        intArrayOf(0x1f91b, 0x1f3ff), // left-facing fist: dark skin tone (🤛🏿)
        intArrayOf(0x1f91c, 0x1f3fb), // right-facing fist: light skin tone (🤜🏻)
        intArrayOf(0x1f91c, 0x1f3fc), // right-facing fist: medium-light skin tone (🤜🏼)
        intArrayOf(0x1f91c, 0x1f3fd), // right-facing fist: medium skin tone (🤜🏽)
        intArrayOf(0x1f91c, 0x1f3fe), // right-facing fist: medium-dark skin tone (🤜🏾)
        intArrayOf(0x1f91c, 0x1f3ff), // right-facing fist: dark skin tone (🤜🏿)
        intArrayOf(0x1f91d, 0x1f3fb), // handshake: light skin tone (🤝🏻)
        intArrayOf(0x1f91d, 0x1f3fc), // handshake: medium-light skin tone (🤝🏼)
        intArrayOf(0x1f91d, 0x1f3fd), // handshake: medium skin tone (🤝🏽)
        intArrayOf(0x1f91d, 0x1f3fe), // handshake: medium-dark skin tone (🤝🏾)
        intArrayOf(0x1f91d, 0x1f3ff), // handshake: dark skin tone (🤝🏿)
        intArrayOf(0x1f91e, 0x1f3fb), // crossed fingers: light skin tone (🤞🏻)
        intArrayOf(0x1f91e, 0x1f3fc), // crossed fingers: medium-light skin tone (🤞🏼)
        intArrayOf(0x1f91e, 0x1f3fd), // crossed fingers: medium skin tone (🤞🏽)
        intArrayOf(0x1f91e, 0x1f3fe), // crossed fingers: medium-dark skin tone (🤞🏾)
        intArrayOf(0x1f91e, 0x1f3ff), // crossed fingers: dark skin tone (🤞🏿)
        intArrayOf(0x1f91f, 0x1f3fb), // love-you gesture: light skin tone (🤟🏻)
        intArrayOf(0x1f91f, 0x1f3fc), // love-you gesture: medium-light skin tone (🤟🏼)
        intArrayOf(0x1f91f, 0x1f3fd), // love-you gesture: medium skin tone (🤟🏽)
        intArrayOf(0x1f91f, 0x1f3fe), // love-you gesture: medium-dark skin tone (🤟🏾)
        intArrayOf(0x1f91f, 0x1f3ff), // love-you gesture: dark skin tone (🤟🏿)
        intArrayOf(0x1f926, 0x200d, 0x2640, 0xfe0f), // woman facepalming (🤦‍♀️)
        intArrayOf(0x1f926, 0x200d, 0x2642, 0xfe0f), // man facepalming (🤦‍♂️)
        intArrayOf(0x1f926, 0x1f3fb), // person facepalming: light skin tone (🤦🏻)
        intArrayOf(0x1f926, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman facepalming: light skin tone (🤦🏻‍♀️)
        intArrayOf(0x1f926, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man facepalming: light skin tone (🤦🏻‍♂️)
        intArrayOf(0x1f926, 0x1f3fc), // person facepalming: medium-light skin tone (🤦🏼)
        intArrayOf(0x1f926, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman facepalming: medium-light skin tone (🤦🏼‍♀️)
        intArrayOf(0x1f926, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man facepalming: medium-light skin tone (🤦🏼‍♂️)
        intArrayOf(0x1f926, 0x1f3fd), // person facepalming: medium skin tone (🤦🏽)
        intArrayOf(0x1f926, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman facepalming: medium skin tone (🤦🏽‍♀️)
        intArrayOf(0x1f926, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man facepalming: medium skin tone (🤦🏽‍♂️)
        intArrayOf(0x1f926, 0x1f3fe), // person facepalming: medium-dark skin tone (🤦🏾)
        intArrayOf(0x1f926, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman facepalming: medium-dark skin tone (🤦🏾‍♀️)
        intArrayOf(0x1f926, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man facepalming: medium-dark skin tone (🤦🏾‍♂️)
        intArrayOf(0x1f926, 0x1f3ff), // person facepalming: dark skin tone (🤦🏿)
        intArrayOf(0x1f926, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman facepalming: dark skin tone (🤦🏿‍♀️)
        intArrayOf(0x1f926, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man facepalming: dark skin tone (🤦🏿‍♂️)
        intArrayOf(0x1f930, 0x1f3fb), // pregnant woman: light skin tone (🤰🏻)
        intArrayOf(0x1f930, 0x1f3fc), // pregnant woman: medium-light skin tone (🤰🏼)
        intArrayOf(0x1f930, 0x1f3fd), // pregnant woman: medium skin tone (🤰🏽)
        intArrayOf(0x1f930, 0x1f3fe), // pregnant woman: medium-dark skin tone (🤰🏾)
        intArrayOf(0x1f930, 0x1f3ff), // pregnant woman: dark skin tone (🤰🏿)
        intArrayOf(0x1f931, 0x1f3fb), // breast-feeding: light skin tone (🤱🏻)
        intArrayOf(0x1f931, 0x1f3fc), // breast-feeding: medium-light skin tone (🤱🏼)
        intArrayOf(0x1f931, 0x1f3fd), // breast-feeding: medium skin tone (🤱🏽)
        intArrayOf(0x1f931, 0x1f3fe), // breast-feeding: medium-dark skin tone (🤱🏾)
        intArrayOf(0x1f931, 0x1f3ff), // breast-feeding: dark skin tone (🤱🏿)
        intArrayOf(0x1f932, 0x1f3fb), // palms up together: light skin tone (🤲🏻)
        intArrayOf(0x1f932, 0x1f3fc), // palms up together: medium-light skin tone (🤲🏼)
        intArrayOf(0x1f932, 0x1f3fd), // palms up together: medium skin tone (🤲🏽)
        intArrayOf(0x1f932, 0x1f3fe), // palms up together: medium-dark skin tone (🤲🏾)
        intArrayOf(0x1f932, 0x1f3ff), // palms up together: dark skin tone (🤲🏿)
        intArrayOf(0x1f933, 0x1f3fb), // selfie: light skin tone (🤳🏻)
        intArrayOf(0x1f933, 0x1f3fc), // selfie: medium-light skin tone (🤳🏼)
        intArrayOf(0x1f933, 0x1f3fd), // selfie: medium skin tone (🤳🏽)
        intArrayOf(0x1f933, 0x1f3fe), // selfie: medium-dark skin tone (🤳🏾)
        intArrayOf(0x1f933, 0x1f3ff), // selfie: dark skin tone (🤳🏿)
        intArrayOf(0x1f934, 0x1f3fb), // prince: light skin tone (🤴🏻)
        intArrayOf(0x1f934, 0x1f3fc), // prince: medium-light skin tone (🤴🏼)
        intArrayOf(0x1f934, 0x1f3fd), // prince: medium skin tone (🤴🏽)
        intArrayOf(0x1f934, 0x1f3fe), // prince: medium-dark skin tone (🤴🏾)
        intArrayOf(0x1f934, 0x1f3ff), // prince: dark skin tone (🤴🏿)
        intArrayOf(0x1f935, 0x200d, 0x2640, 0xfe0f), // woman in tuxedo (🤵‍♀️)
        intArrayOf(0x1f935, 0x200d, 0x2642, 0xfe0f), // man in tuxedo (🤵‍♂️)
        intArrayOf(0x1f935, 0x1f3fb), // person in tuxedo: light skin tone (🤵🏻)
        intArrayOf(0x1f935, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman in tuxedo: light skin tone (🤵🏻‍♀️)
        intArrayOf(0x1f935, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man in tuxedo: light skin tone (🤵🏻‍♂️)
        intArrayOf(0x1f935, 0x1f3fc), // person in tuxedo: medium-light skin tone (🤵🏼)
        intArrayOf(0x1f935, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman in tuxedo: medium-light skin tone (🤵🏼‍♀️)
        intArrayOf(0x1f935, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man in tuxedo: medium-light skin tone (🤵🏼‍♂️)
        intArrayOf(0x1f935, 0x1f3fd), // person in tuxedo: medium skin tone (🤵🏽)
        intArrayOf(0x1f935, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman in tuxedo: medium skin tone (🤵🏽‍♀️)
        intArrayOf(0x1f935, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man in tuxedo: medium skin tone (🤵🏽‍♂️)
        intArrayOf(0x1f935, 0x1f3fe), // person in tuxedo: medium-dark skin tone (🤵🏾)
        intArrayOf(0x1f935, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman in tuxedo: medium-dark skin tone (🤵🏾‍♀️)
        intArrayOf(0x1f935, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man in tuxedo: medium-dark skin tone (🤵🏾‍♂️)
        intArrayOf(0x1f935, 0x1f3ff), // person in tuxedo: dark skin tone (🤵🏿)
        intArrayOf(0x1f935, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman in tuxedo: dark skin tone (🤵🏿‍♀️)
        intArrayOf(0x1f935, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man in tuxedo: dark skin tone (🤵🏿‍♂️)
        intArrayOf(0x1f936, 0x1f3fb), // Mrs. Claus: light skin tone (🤶🏻)
        intArrayOf(0x1f936, 0x1f3fc), // Mrs. Claus: medium-light skin tone (🤶🏼)
        intArrayOf(0x1f936, 0x1f3fd), // Mrs. Claus: medium skin tone (🤶🏽)
        intArrayOf(0x1f936, 0x1f3fe), // Mrs. Claus: medium-dark skin tone (🤶🏾)
        intArrayOf(0x1f936, 0x1f3ff), // Mrs. Claus: dark skin tone (🤶🏿)
        intArrayOf(0x1f937, 0x200d, 0x2640, 0xfe0f), // woman shrugging (🤷‍♀️)
        intArrayOf(0x1f937, 0x200d, 0x2642, 0xfe0f), // man shrugging (🤷‍♂️)
        intArrayOf(0x1f937, 0x1f3fb), // person shrugging: light skin tone (🤷🏻)
        intArrayOf(0x1f937, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman shrugging: light skin tone (🤷🏻‍♀️)
        intArrayOf(0x1f937, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man shrugging: light skin tone (🤷🏻‍♂️)
        intArrayOf(0x1f937, 0x1f3fc), // person shrugging: medium-light skin tone (🤷🏼)
        intArrayOf(0x1f937, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman shrugging: medium-light skin tone (🤷🏼‍♀️)
        intArrayOf(0x1f937, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man shrugging: medium-light skin tone (🤷🏼‍♂️)
        intArrayOf(0x1f937, 0x1f3fd), // person shrugging: medium skin tone (🤷🏽)
        intArrayOf(0x1f937, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman shrugging: medium skin tone (🤷🏽‍♀️)
        intArrayOf(0x1f937, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man shrugging: medium skin tone (🤷🏽‍♂️)
        intArrayOf(0x1f937, 0x1f3fe), // person shrugging: medium-dark skin tone (🤷🏾)
        intArrayOf(0x1f937, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman shrugging: medium-dark skin tone (🤷🏾‍♀️)
        intArrayOf(0x1f937, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man shrugging: medium-dark skin tone (🤷🏾‍♂️)
        intArrayOf(0x1f937, 0x1f3ff), // person shrugging: dark skin tone (🤷🏿)
        intArrayOf(0x1f937, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman shrugging: dark skin tone (🤷🏿‍♀️)
        intArrayOf(0x1f937, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man shrugging: dark skin tone (🤷🏿‍♂️)
        intArrayOf(0x1f938, 0x200d, 0x2640, 0xfe0f), // woman cartwheeling (🤸‍♀️)
        intArrayOf(0x1f938, 0x200d, 0x2642, 0xfe0f), // man cartwheeling (🤸‍♂️)
        intArrayOf(0x1f938, 0x1f3fb), // person cartwheeling: light skin tone (🤸🏻)
        intArrayOf(0x1f938, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman cartwheeling: light skin tone (🤸🏻‍♀️)
        intArrayOf(0x1f938, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man cartwheeling: light skin tone (🤸🏻‍♂️)
        intArrayOf(0x1f938, 0x1f3fc), // person cartwheeling: medium-light skin tone (🤸🏼)
        intArrayOf(0x1f938, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman cartwheeling: medium-light skin tone (🤸🏼‍♀️)
        intArrayOf(0x1f938, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man cartwheeling: medium-light skin tone (🤸🏼‍♂️)
        intArrayOf(0x1f938, 0x1f3fd), // person cartwheeling: medium skin tone (🤸🏽)
        intArrayOf(0x1f938, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman cartwheeling: medium skin tone (🤸🏽‍♀️)
        intArrayOf(0x1f938, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man cartwheeling: medium skin tone (🤸🏽‍♂️)
        intArrayOf(0x1f938, 0x1f3fe), // person cartwheeling: medium-dark skin tone (🤸🏾)
        intArrayOf(0x1f938, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman cartwheeling: medium-dark skin tone (🤸🏾‍♀️)
        intArrayOf(0x1f938, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man cartwheeling: medium-dark skin tone (🤸🏾‍♂️)
        intArrayOf(0x1f938, 0x1f3ff), // person cartwheeling: dark skin tone (🤸🏿)
        intArrayOf(0x1f938, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman cartwheeling: dark skin tone (🤸🏿‍♀️)
        intArrayOf(0x1f938, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man cartwheeling: dark skin tone (🤸🏿‍♂️)
        intArrayOf(0x1f939, 0x200d, 0x2640, 0xfe0f), // woman juggling (🤹‍♀️)
        intArrayOf(0x1f939, 0x200d, 0x2642, 0xfe0f), // man juggling (🤹‍♂️)
        intArrayOf(0x1f939, 0x1f3fb), // person juggling: light skin tone (🤹🏻)
        intArrayOf(0x1f939, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman juggling: light skin tone (🤹🏻‍♀️)
        intArrayOf(0x1f939, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man juggling: light skin tone (🤹🏻‍♂️)
        intArrayOf(0x1f939, 0x1f3fc), // person juggling: medium-light skin tone (🤹🏼)
        intArrayOf(0x1f939, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman juggling: medium-light skin tone (🤹🏼‍♀️)
        intArrayOf(0x1f939, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man juggling: medium-light skin tone (🤹🏼‍♂️)
        intArrayOf(0x1f939, 0x1f3fd), // person juggling: medium skin tone (🤹🏽)
        intArrayOf(0x1f939, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman juggling: medium skin tone (🤹🏽‍♀️)
        intArrayOf(0x1f939, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man juggling: medium skin tone (🤹🏽‍♂️)
        intArrayOf(0x1f939, 0x1f3fe), // person juggling: medium-dark skin tone (🤹🏾)
        intArrayOf(0x1f939, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman juggling: medium-dark skin tone (🤹🏾‍♀️)
        intArrayOf(0x1f939, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man juggling: medium-dark skin tone (🤹🏾‍♂️)
        intArrayOf(0x1f939, 0x1f3ff), // person juggling: dark skin tone (🤹🏿)
        intArrayOf(0x1f939, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman juggling: dark skin tone (🤹🏿‍♀️)
        intArrayOf(0x1f939, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man juggling: dark skin tone (🤹🏿‍♂️)
        intArrayOf(0x1f93c, 0x200d, 0x2640, 0xfe0f), // women wrestling (🤼‍♀️)
        intArrayOf(0x1f93c, 0x200d, 0x2642, 0xfe0f), // men wrestling (🤼‍♂️)
        intArrayOf(0x1f93d, 0x200d, 0x2640, 0xfe0f), // woman playing water polo (🤽‍♀️)
        intArrayOf(0x1f93d, 0x200d, 0x2642, 0xfe0f), // man playing water polo (🤽‍♂️)
        intArrayOf(0x1f93d, 0x1f3fb), // person playing water polo: light skin tone (🤽🏻)
        intArrayOf(0x1f93d, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman playing water polo: light skin tone (🤽🏻‍♀️)
        intArrayOf(0x1f93d, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man playing water polo: light skin tone (🤽🏻‍♂️)
        intArrayOf(0x1f93d, 0x1f3fc), // person playing water polo: medium-light skin tone (🤽🏼)
        intArrayOf(
            0x1f93d,
            0x1f3fc,
            0x200d,
            0x2640,
            0xfe0f
        ), // woman playing water polo: medium-light skin tone (🤽🏼‍♀️)
        intArrayOf(
            0x1f93d,
            0x1f3fc,
            0x200d,
            0x2642,
            0xfe0f
        ), // man playing water polo: medium-light skin tone (🤽🏼‍♂️)
        intArrayOf(0x1f93d, 0x1f3fd), // person playing water polo: medium skin tone (🤽🏽)
        intArrayOf(0x1f93d, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman playing water polo: medium skin tone (🤽🏽‍♀️)
        intArrayOf(0x1f93d, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man playing water polo: medium skin tone (🤽🏽‍♂️)
        intArrayOf(0x1f93d, 0x1f3fe), // person playing water polo: medium-dark skin tone (🤽🏾)
        intArrayOf(
            0x1f93d,
            0x1f3fe,
            0x200d,
            0x2640,
            0xfe0f
        ), // woman playing water polo: medium-dark skin tone (🤽🏾‍♀️)
        intArrayOf(0x1f93d, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man playing water polo: medium-dark skin tone (🤽🏾‍♂️)
        intArrayOf(0x1f93d, 0x1f3ff), // person playing water polo: dark skin tone (🤽🏿)
        intArrayOf(0x1f93d, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman playing water polo: dark skin tone (🤽🏿‍♀️)
        intArrayOf(0x1f93d, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man playing water polo: dark skin tone (🤽🏿‍♂️)
        intArrayOf(0x1f93e, 0x200d, 0x2640, 0xfe0f), // woman playing handball (🤾‍♀️)
        intArrayOf(0x1f93e, 0x200d, 0x2642, 0xfe0f), // man playing handball (🤾‍♂️)
        intArrayOf(0x1f93e, 0x1f3fb), // person playing handball: light skin tone (🤾🏻)
        intArrayOf(0x1f93e, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman playing handball: light skin tone (🤾🏻‍♀️)
        intArrayOf(0x1f93e, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man playing handball: light skin tone (🤾🏻‍♂️)
        intArrayOf(0x1f93e, 0x1f3fc), // person playing handball: medium-light skin tone (🤾🏼)
        intArrayOf(
            0x1f93e,
            0x1f3fc,
            0x200d,
            0x2640,
            0xfe0f
        ), // woman playing handball: medium-light skin tone (🤾🏼‍♀️)
        intArrayOf(0x1f93e, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man playing handball: medium-light skin tone (🤾🏼‍♂️)
        intArrayOf(0x1f93e, 0x1f3fd), // person playing handball: medium skin tone (🤾🏽)
        intArrayOf(0x1f93e, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman playing handball: medium skin tone (🤾🏽‍♀️)
        intArrayOf(0x1f93e, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man playing handball: medium skin tone (🤾🏽‍♂️)
        intArrayOf(0x1f93e, 0x1f3fe), // person playing handball: medium-dark skin tone (🤾🏾)
        intArrayOf(0x1f93e, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman playing handball: medium-dark skin tone (🤾🏾‍♀️)
        intArrayOf(0x1f93e, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man playing handball: medium-dark skin tone (🤾🏾‍♂️)
        intArrayOf(0x1f93e, 0x1f3ff), // person playing handball: dark skin tone (🤾🏿)
        intArrayOf(0x1f93e, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman playing handball: dark skin tone (🤾🏿‍♀️)
        intArrayOf(0x1f93e, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man playing handball: dark skin tone (🤾🏿‍♂️)
        intArrayOf(0x1f977, 0x1f3fb), // ninja: light skin tone (🥷🏻)
        intArrayOf(0x1f977, 0x1f3fc), // ninja: medium-light skin tone (🥷🏼)
        intArrayOf(0x1f977, 0x1f3fd), // ninja: medium skin tone (🥷🏽)
        intArrayOf(0x1f977, 0x1f3fe), // ninja: medium-dark skin tone (🥷🏾)
        intArrayOf(0x1f977, 0x1f3ff), // ninja: dark skin tone (🥷🏿)
        intArrayOf(0x1f9b5, 0x1f3fb), // leg: light skin tone (🦵🏻)
        intArrayOf(0x1f9b5, 0x1f3fc), // leg: medium-light skin tone (🦵🏼)
        intArrayOf(0x1f9b5, 0x1f3fd), // leg: medium skin tone (🦵🏽)
        intArrayOf(0x1f9b5, 0x1f3fe), // leg: medium-dark skin tone (🦵🏾)
        intArrayOf(0x1f9b5, 0x1f3ff), // leg: dark skin tone (🦵🏿)
        intArrayOf(0x1f9b6, 0x1f3fb), // foot: light skin tone (🦶🏻)
        intArrayOf(0x1f9b6, 0x1f3fc), // foot: medium-light skin tone (🦶🏼)
        intArrayOf(0x1f9b6, 0x1f3fd), // foot: medium skin tone (🦶🏽)
        intArrayOf(0x1f9b6, 0x1f3fe), // foot: medium-dark skin tone (🦶🏾)
        intArrayOf(0x1f9b6, 0x1f3ff), // foot: dark skin tone (🦶🏿)
        intArrayOf(0x1f9b8, 0x200d, 0x2640, 0xfe0f), // woman superhero (🦸‍♀️)
        intArrayOf(0x1f9b8, 0x200d, 0x2642, 0xfe0f), // man superhero (🦸‍♂️)
        intArrayOf(0x1f9b8, 0x1f3fb), // superhero: light skin tone (🦸🏻)
        intArrayOf(0x1f9b8, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman superhero: light skin tone (🦸🏻‍♀️)
        intArrayOf(0x1f9b8, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man superhero: light skin tone (🦸🏻‍♂️)
        intArrayOf(0x1f9b8, 0x1f3fc), // superhero: medium-light skin tone (🦸🏼)
        intArrayOf(0x1f9b8, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman superhero: medium-light skin tone (🦸🏼‍♀️)
        intArrayOf(0x1f9b8, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man superhero: medium-light skin tone (🦸🏼‍♂️)
        intArrayOf(0x1f9b8, 0x1f3fd), // superhero: medium skin tone (🦸🏽)
        intArrayOf(0x1f9b8, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman superhero: medium skin tone (🦸🏽‍♀️)
        intArrayOf(0x1f9b8, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man superhero: medium skin tone (🦸🏽‍♂️)
        intArrayOf(0x1f9b8, 0x1f3fe), // superhero: medium-dark skin tone (🦸🏾)
        intArrayOf(0x1f9b8, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman superhero: medium-dark skin tone (🦸🏾‍♀️)
        intArrayOf(0x1f9b8, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man superhero: medium-dark skin tone (🦸🏾‍♂️)
        intArrayOf(0x1f9b8, 0x1f3ff), // superhero: dark skin tone (🦸🏿)
        intArrayOf(0x1f9b8, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman superhero: dark skin tone (🦸🏿‍♀️)
        intArrayOf(0x1f9b8, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man superhero: dark skin tone (🦸🏿‍♂️)
        intArrayOf(0x1f9b9, 0x200d, 0x2640, 0xfe0f), // woman supervillain (🦹‍♀️)
        intArrayOf(0x1f9b9, 0x200d, 0x2642, 0xfe0f), // man supervillain (🦹‍♂️)
        intArrayOf(0x1f9b9, 0x1f3fb), // supervillain: light skin tone (🦹🏻)
        intArrayOf(0x1f9b9, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman supervillain: light skin tone (🦹🏻‍♀️)
        intArrayOf(0x1f9b9, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man supervillain: light skin tone (🦹🏻‍♂️)
        intArrayOf(0x1f9b9, 0x1f3fc), // supervillain: medium-light skin tone (🦹🏼)
        intArrayOf(0x1f9b9, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman supervillain: medium-light skin tone (🦹🏼‍♀️)
        intArrayOf(0x1f9b9, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man supervillain: medium-light skin tone (🦹🏼‍♂️)
        intArrayOf(0x1f9b9, 0x1f3fd), // supervillain: medium skin tone (🦹🏽)
        intArrayOf(0x1f9b9, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman supervillain: medium skin tone (🦹🏽‍♀️)
        intArrayOf(0x1f9b9, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man supervillain: medium skin tone (🦹🏽‍♂️)
        intArrayOf(0x1f9b9, 0x1f3fe), // supervillain: medium-dark skin tone (🦹🏾)
        intArrayOf(0x1f9b9, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman supervillain: medium-dark skin tone (🦹🏾‍♀️)
        intArrayOf(0x1f9b9, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man supervillain: medium-dark skin tone (🦹🏾‍♂️)
        intArrayOf(0x1f9b9, 0x1f3ff), // supervillain: dark skin tone (🦹🏿)
        intArrayOf(0x1f9b9, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman supervillain: dark skin tone (🦹🏿‍♀️)
        intArrayOf(0x1f9b9, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man supervillain: dark skin tone (🦹🏿‍♂️)
        intArrayOf(0x1f9bb, 0x1f3fb), // ear with hearing aid: light skin tone (🦻🏻)
        intArrayOf(0x1f9bb, 0x1f3fc), // ear with hearing aid: medium-light skin tone (🦻🏼)
        intArrayOf(0x1f9bb, 0x1f3fd), // ear with hearing aid: medium skin tone (🦻🏽)
        intArrayOf(0x1f9bb, 0x1f3fe), // ear with hearing aid: medium-dark skin tone (🦻🏾)
        intArrayOf(0x1f9bb, 0x1f3ff), // ear with hearing aid: dark skin tone (🦻🏿)
        intArrayOf(0x1f9cd, 0x200d, 0x2640, 0xfe0f), // woman standing (🧍‍♀️)
        intArrayOf(0x1f9cd, 0x200d, 0x2642, 0xfe0f), // man standing (🧍‍♂️)
        intArrayOf(0x1f9cd, 0x1f3fb), // person standing: light skin tone (🧍🏻)
        intArrayOf(0x1f9cd, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman standing: light skin tone (🧍🏻‍♀️)
        intArrayOf(0x1f9cd, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man standing: light skin tone (🧍🏻‍♂️)
        intArrayOf(0x1f9cd, 0x1f3fc), // person standing: medium-light skin tone (🧍🏼)
        intArrayOf(0x1f9cd, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman standing: medium-light skin tone (🧍🏼‍♀️)
        intArrayOf(0x1f9cd, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man standing: medium-light skin tone (🧍🏼‍♂️)
        intArrayOf(0x1f9cd, 0x1f3fd), // person standing: medium skin tone (🧍🏽)
        intArrayOf(0x1f9cd, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman standing: medium skin tone (🧍🏽‍♀️)
        intArrayOf(0x1f9cd, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man standing: medium skin tone (🧍🏽‍♂️)
        intArrayOf(0x1f9cd, 0x1f3fe), // person standing: medium-dark skin tone (🧍🏾)
        intArrayOf(0x1f9cd, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman standing: medium-dark skin tone (🧍🏾‍♀️)
        intArrayOf(0x1f9cd, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man standing: medium-dark skin tone (🧍🏾‍♂️)
        intArrayOf(0x1f9cd, 0x1f3ff), // person standing: dark skin tone (🧍🏿)
        intArrayOf(0x1f9cd, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman standing: dark skin tone (🧍🏿‍♀️)
        intArrayOf(0x1f9cd, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man standing: dark skin tone (🧍🏿‍♂️)
        intArrayOf(0x1f9ce, 0x200d, 0x2640, 0xfe0f), // woman kneeling (🧎‍♀️)
        intArrayOf(0x1f9ce, 0x200d, 0x2642, 0xfe0f), // man kneeling (🧎‍♂️)
        intArrayOf(0x1f9ce, 0x1f3fb), // person kneeling: light skin tone (🧎🏻)
        intArrayOf(0x1f9ce, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman kneeling: light skin tone (🧎🏻‍♀️)
        intArrayOf(0x1f9ce, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man kneeling: light skin tone (🧎🏻‍♂️)
        intArrayOf(0x1f9ce, 0x1f3fc), // person kneeling: medium-light skin tone (🧎🏼)
        intArrayOf(0x1f9ce, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman kneeling: medium-light skin tone (🧎🏼‍♀️)
        intArrayOf(0x1f9ce, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man kneeling: medium-light skin tone (🧎🏼‍♂️)
        intArrayOf(0x1f9ce, 0x1f3fd), // person kneeling: medium skin tone (🧎🏽)
        intArrayOf(0x1f9ce, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman kneeling: medium skin tone (🧎🏽‍♀️)
        intArrayOf(0x1f9ce, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man kneeling: medium skin tone (🧎🏽‍♂️)
        intArrayOf(0x1f9ce, 0x1f3fe), // person kneeling: medium-dark skin tone (🧎🏾)
        intArrayOf(0x1f9ce, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman kneeling: medium-dark skin tone (🧎🏾‍♀️)
        intArrayOf(0x1f9ce, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man kneeling: medium-dark skin tone (🧎🏾‍♂️)
        intArrayOf(0x1f9ce, 0x1f3ff), // person kneeling: dark skin tone (🧎🏿)
        intArrayOf(0x1f9ce, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman kneeling: dark skin tone (🧎🏿‍♀️)
        intArrayOf(0x1f9ce, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man kneeling: dark skin tone (🧎🏿‍♂️)
        intArrayOf(0x1f9cf, 0x200d, 0x2640, 0xfe0f), // deaf woman (🧏‍♀️)
        intArrayOf(0x1f9cf, 0x200d, 0x2642, 0xfe0f), // deaf man (🧏‍♂️)
        intArrayOf(0x1f9cf, 0x1f3fb), // deaf person: light skin tone (🧏🏻)
        intArrayOf(0x1f9cf, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // deaf woman: light skin tone (🧏🏻‍♀️)
        intArrayOf(0x1f9cf, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // deaf man: light skin tone (🧏🏻‍♂️)
        intArrayOf(0x1f9cf, 0x1f3fc), // deaf person: medium-light skin tone (🧏🏼)
        intArrayOf(0x1f9cf, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // deaf woman: medium-light skin tone (🧏🏼‍♀️)
        intArrayOf(0x1f9cf, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // deaf man: medium-light skin tone (🧏🏼‍♂️)
        intArrayOf(0x1f9cf, 0x1f3fd), // deaf person: medium skin tone (🧏🏽)
        intArrayOf(0x1f9cf, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // deaf woman: medium skin tone (🧏🏽‍♀️)
        intArrayOf(0x1f9cf, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // deaf man: medium skin tone (🧏🏽‍♂️)
        intArrayOf(0x1f9cf, 0x1f3fe), // deaf person: medium-dark skin tone (🧏🏾)
        intArrayOf(0x1f9cf, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // deaf woman: medium-dark skin tone (🧏🏾‍♀️)
        intArrayOf(0x1f9cf, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // deaf man: medium-dark skin tone (🧏🏾‍♂️)
        intArrayOf(0x1f9cf, 0x1f3ff), // deaf person: dark skin tone (🧏🏿)
        intArrayOf(0x1f9cf, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // deaf woman: dark skin tone (🧏🏿‍♀️)
        intArrayOf(0x1f9cf, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // deaf man: dark skin tone (🧏🏿‍♂️)
        intArrayOf(0x1f9d1, 0x200d, 0x2695, 0xfe0f), // health worker (🧑‍⚕️)
        intArrayOf(0x1f9d1, 0x200d, 0x2696, 0xfe0f), // judge (🧑‍⚖️)
        intArrayOf(0x1f9d1, 0x200d, 0x2708, 0xfe0f), // pilot (🧑‍✈️)
        intArrayOf(0x1f9d1, 0x200d, 0x1f33e), // farmer (🧑‍🌾)
        intArrayOf(0x1f9d1, 0x200d, 0x1f373), // cook (🧑‍🍳)
        intArrayOf(0x1f9d1, 0x200d, 0x1f37c), // person feeding baby (🧑‍🍼)
        intArrayOf(0x1f9d1, 0x200d, 0x1f384), // mx claus (🧑‍🎄)
        intArrayOf(0x1f9d1, 0x200d, 0x1f393), // student (🧑‍🎓)
        intArrayOf(0x1f9d1, 0x200d, 0x1f3a4), // singer (🧑‍🎤)
        intArrayOf(0x1f9d1, 0x200d, 0x1f3a8), // artist (🧑‍🎨)
        intArrayOf(0x1f9d1, 0x200d, 0x1f3eb), // teacher (🧑‍🏫)
        intArrayOf(0x1f9d1, 0x200d, 0x1f3ed), // factory worker (🧑‍🏭)
        intArrayOf(0x1f9d1, 0x200d, 0x1f4bb), // technologist (🧑‍💻)
        intArrayOf(0x1f9d1, 0x200d, 0x1f4bc), // office worker (🧑‍💼)
        intArrayOf(0x1f9d1, 0x200d, 0x1f527), // mechanic (🧑‍🔧)
        intArrayOf(0x1f9d1, 0x200d, 0x1f52c), // scientist (🧑‍🔬)
        intArrayOf(0x1f9d1, 0x200d, 0x1f680), // astronaut (🧑‍🚀)
        intArrayOf(0x1f9d1, 0x200d, 0x1f692), // firefighter (🧑‍🚒)
        intArrayOf(0x1f9d1, 0x200d, 0x1f91d, 0x200d, 0x1f9d1), // people holding hands (🧑‍🤝‍🧑)
        intArrayOf(0x1f9d1, 0x200d, 0x1f9af), // person with white cane (🧑‍🦯)
        intArrayOf(0x1f9d1, 0x200d, 0x1f9b0), // person: red hair (🧑‍🦰)
        intArrayOf(0x1f9d1, 0x200d, 0x1f9b1), // person: curly hair (🧑‍🦱)
        intArrayOf(0x1f9d1, 0x200d, 0x1f9b2), // person: bald (🧑‍🦲)
        intArrayOf(0x1f9d1, 0x200d, 0x1f9b3), // person: white hair (🧑‍🦳)
        intArrayOf(0x1f9d1, 0x200d, 0x1f9bc), // person in motorized wheelchair (🧑‍🦼)
        intArrayOf(0x1f9d1, 0x200d, 0x1f9bd), // person in manual wheelchair (🧑‍🦽)
        intArrayOf(0x1f9d1, 0x1f3fb), // person: light skin tone (🧑🏻)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x2695, 0xfe0f), // health worker: light skin tone (🧑🏻‍⚕️)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x2696, 0xfe0f), // judge: light skin tone (🧑🏻‍⚖️)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x2708, 0xfe0f), // pilot: light skin tone (🧑🏻‍✈️)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f33e), // farmer: light skin tone (🧑🏻‍🌾)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f373), // cook: light skin tone (🧑🏻‍🍳)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f37c), // person feeding baby: light skin tone (🧑🏻‍🍼)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f384), // mx claus: light skin tone (🧑🏻‍🎄)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f393), // student: light skin tone (🧑🏻‍🎓)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f3a4), // singer: light skin tone (🧑🏻‍🎤)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f3a8), // artist: light skin tone (🧑🏻‍🎨)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f3eb), // teacher: light skin tone (🧑🏻‍🏫)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f3ed), // factory worker: light skin tone (🧑🏻‍🏭)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f4bb), // technologist: light skin tone (🧑🏻‍💻)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f4bc), // office worker: light skin tone (🧑🏻‍💼)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f527), // mechanic: light skin tone (🧑🏻‍🔧)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f52c), // scientist: light skin tone (🧑🏻‍🔬)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f680), // astronaut: light skin tone (🧑🏻‍🚀)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f692), // firefighter: light skin tone (🧑🏻‍🚒)
        intArrayOf(
            0x1f9d1,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fb
        ), // people holding hands: light skin tone (🧑🏻‍🤝‍🧑🏻)
        intArrayOf(
            0x1f9d1,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fc
        ), // people holding hands: light skin tone, medium-light skin tone (🧑🏻‍🤝‍🧑🏼)
        intArrayOf(
            0x1f9d1,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fd
        ), // people holding hands: light skin tone, medium skin tone (🧑🏻‍🤝‍🧑🏽)
        intArrayOf(
            0x1f9d1,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fe
        ), // people holding hands: light skin tone, medium-dark skin tone (🧑🏻‍🤝‍🧑🏾)
        intArrayOf(
            0x1f9d1,
            0x1f3fb,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3ff
        ), // people holding hands: light skin tone, dark skin tone (🧑🏻‍🤝‍🧑🏿)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f9af), // person with white cane: light skin tone (🧑🏻‍🦯)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f9b0), // person: light skin tone, red hair (🧑🏻‍🦰)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f9b1), // person: light skin tone, curly hair (🧑🏻‍🦱)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f9b2), // person: light skin tone, bald (🧑🏻‍🦲)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f9b3), // person: light skin tone, white hair (🧑🏻‍🦳)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f9bc), // person in motorized wheelchair: light skin tone (🧑🏻‍🦼)
        intArrayOf(0x1f9d1, 0x1f3fb, 0x200d, 0x1f9bd), // person in manual wheelchair: light skin tone (🧑🏻‍🦽)
        intArrayOf(0x1f9d1, 0x1f3fc), // person: medium-light skin tone (🧑🏼)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x2695, 0xfe0f), // health worker: medium-light skin tone (🧑🏼‍⚕️)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x2696, 0xfe0f), // judge: medium-light skin tone (🧑🏼‍⚖️)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x2708, 0xfe0f), // pilot: medium-light skin tone (🧑🏼‍✈️)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f33e), // farmer: medium-light skin tone (🧑🏼‍🌾)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f373), // cook: medium-light skin tone (🧑🏼‍🍳)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f37c), // person feeding baby: medium-light skin tone (🧑🏼‍🍼)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f384), // mx claus: medium-light skin tone (🧑🏼‍🎄)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f393), // student: medium-light skin tone (🧑🏼‍🎓)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f3a4), // singer: medium-light skin tone (🧑🏼‍🎤)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f3a8), // artist: medium-light skin tone (🧑🏼‍🎨)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f3eb), // teacher: medium-light skin tone (🧑🏼‍🏫)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f3ed), // factory worker: medium-light skin tone (🧑🏼‍🏭)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f4bb), // technologist: medium-light skin tone (🧑🏼‍💻)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f4bc), // office worker: medium-light skin tone (🧑🏼‍💼)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f527), // mechanic: medium-light skin tone (🧑🏼‍🔧)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f52c), // scientist: medium-light skin tone (🧑🏼‍🔬)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f680), // astronaut: medium-light skin tone (🧑🏼‍🚀)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f692), // firefighter: medium-light skin tone (🧑🏼‍🚒)
        intArrayOf(
            0x1f9d1,
            0x1f3fc,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fb
        ), // people holding hands: medium-light skin tone, light skin tone (🧑🏼‍🤝‍🧑🏻)
        intArrayOf(
            0x1f9d1,
            0x1f3fc,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fc
        ), // people holding hands: medium-light skin tone (🧑🏼‍🤝‍🧑🏼)
        intArrayOf(
            0x1f9d1,
            0x1f3fc,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fd
        ), // people holding hands: medium-light skin tone, medium skin tone (🧑🏼‍🤝‍🧑🏽)
        intArrayOf(
            0x1f9d1,
            0x1f3fc,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3ff
        ), // people holding hands: medium-light skin tone, dark skin tone (🧑🏼‍🤝‍🧑🏿)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f9af), // person with white cane: medium-light skin tone (🧑🏼‍🦯)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f9b0), // person: medium-light skin tone, red hair (🧑🏼‍🦰)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f9b1), // person: medium-light skin tone, curly hair (🧑🏼‍🦱)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f9b2), // person: medium-light skin tone, bald (🧑🏼‍🦲)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f9b3), // person: medium-light skin tone, white hair (🧑🏼‍🦳)
        intArrayOf(
            0x1f9d1,
            0x1f3fc,
            0x200d,
            0x1f9bc
        ), // person in motorized wheelchair: medium-light skin tone (🧑🏼‍🦼)
        intArrayOf(0x1f9d1, 0x1f3fc, 0x200d, 0x1f9bd), // person in manual wheelchair: medium-light skin tone (🧑🏼‍🦽)
        intArrayOf(0x1f9d1, 0x1f3fd), // person: medium skin tone (🧑🏽)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x2695, 0xfe0f), // health worker: medium skin tone (🧑🏽‍⚕️)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x2696, 0xfe0f), // judge: medium skin tone (🧑🏽‍⚖️)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x2708, 0xfe0f), // pilot: medium skin tone (🧑🏽‍✈️)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f33e), // farmer: medium skin tone (🧑🏽‍🌾)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f373), // cook: medium skin tone (🧑🏽‍🍳)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f37c), // person feeding baby: medium skin tone (🧑🏽‍🍼)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f384), // mx claus: medium skin tone (🧑🏽‍🎄)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f393), // student: medium skin tone (🧑🏽‍🎓)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f3a4), // singer: medium skin tone (🧑🏽‍🎤)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f3a8), // artist: medium skin tone (🧑🏽‍🎨)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f3eb), // teacher: medium skin tone (🧑🏽‍🏫)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f3ed), // factory worker: medium skin tone (🧑🏽‍🏭)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f4bb), // technologist: medium skin tone (🧑🏽‍💻)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f4bc), // office worker: medium skin tone (🧑🏽‍💼)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f527), // mechanic: medium skin tone (🧑🏽‍🔧)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f52c), // scientist: medium skin tone (🧑🏽‍🔬)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f680), // astronaut: medium skin tone (🧑🏽‍🚀)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f692), // firefighter: medium skin tone (🧑🏽‍🚒)
        intArrayOf(
            0x1f9d1,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fb
        ), // people holding hands: medium skin tone, light skin tone (🧑🏽‍🤝‍🧑🏻)
        intArrayOf(
            0x1f9d1,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fc
        ), // people holding hands: medium skin tone, medium-light skin tone (🧑🏽‍🤝‍🧑🏼)
        intArrayOf(
            0x1f9d1,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fd
        ), // people holding hands: medium skin tone (🧑🏽‍🤝‍🧑🏽)
        intArrayOf(
            0x1f9d1,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fe
        ), // people holding hands: medium skin tone, medium-dark skin tone (🧑🏽‍🤝‍🧑🏾)
        intArrayOf(
            0x1f9d1,
            0x1f3fd,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3ff
        ), // people holding hands: medium skin tone, dark skin tone (🧑🏽‍🤝‍🧑🏿)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f9af), // person with white cane: medium skin tone (🧑🏽‍🦯)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f9b0), // person: medium skin tone, red hair (🧑🏽‍🦰)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f9b1), // person: medium skin tone, curly hair (🧑🏽‍🦱)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f9b2), // person: medium skin tone, bald (🧑🏽‍🦲)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f9b3), // person: medium skin tone, white hair (🧑🏽‍🦳)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f9bc), // person in motorized wheelchair: medium skin tone (🧑🏽‍🦼)
        intArrayOf(0x1f9d1, 0x1f3fd, 0x200d, 0x1f9bd), // person in manual wheelchair: medium skin tone (🧑🏽‍🦽)
        intArrayOf(0x1f9d1, 0x1f3fe), // person: medium-dark skin tone (🧑🏾)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x2695, 0xfe0f), // health worker: medium-dark skin tone (🧑🏾‍⚕️)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x2696, 0xfe0f), // judge: medium-dark skin tone (🧑🏾‍⚖️)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x2708, 0xfe0f), // pilot: medium-dark skin tone (🧑🏾‍✈️)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f33e), // farmer: medium-dark skin tone (🧑🏾‍🌾)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f373), // cook: medium-dark skin tone (🧑🏾‍🍳)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f37c), // person feeding baby: medium-dark skin tone (🧑🏾‍🍼)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f384), // mx claus: medium-dark skin tone (🧑🏾‍🎄)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f393), // student: medium-dark skin tone (🧑🏾‍🎓)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f3a4), // singer: medium-dark skin tone (🧑🏾‍🎤)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f3a8), // artist: medium-dark skin tone (🧑🏾‍🎨)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f3eb), // teacher: medium-dark skin tone (🧑🏾‍🏫)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f3ed), // factory worker: medium-dark skin tone (🧑🏾‍🏭)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f4bb), // technologist: medium-dark skin tone (🧑🏾‍💻)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f4bc), // office worker: medium-dark skin tone (🧑🏾‍💼)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f527), // mechanic: medium-dark skin tone (🧑🏾‍🔧)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f52c), // scientist: medium-dark skin tone (🧑🏾‍🔬)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f680), // astronaut: medium-dark skin tone (🧑🏾‍🚀)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f692), // firefighter: medium-dark skin tone (🧑🏾‍🚒)
        intArrayOf(
            0x1f9d1,
            0x1f3fe,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fb
        ), // people holding hands: medium-dark skin tone, light skin tone (🧑🏾‍🤝‍🧑🏻)
        intArrayOf(
            0x1f9d1,
            0x1f3fe,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fd
        ), // people holding hands: medium-dark skin tone, medium skin tone (🧑🏾‍🤝‍🧑🏽)
        intArrayOf(
            0x1f9d1,
            0x1f3fe,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fe
        ), // people holding hands: medium-dark skin tone (🧑🏾‍🤝‍🧑🏾)
        intArrayOf(
            0x1f9d1,
            0x1f3fe,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3ff
        ), // people holding hands: medium-dark skin tone, dark skin tone (🧑🏾‍🤝‍🧑🏿)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f9af), // person with white cane: medium-dark skin tone (🧑🏾‍🦯)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f9b0), // person: medium-dark skin tone, red hair (🧑🏾‍🦰)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f9b1), // person: medium-dark skin tone, curly hair (🧑🏾‍🦱)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f9b2), // person: medium-dark skin tone, bald (🧑🏾‍🦲)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f9b3), // person: medium-dark skin tone, white hair (🧑🏾‍🦳)
        intArrayOf(
            0x1f9d1,
            0x1f3fe,
            0x200d,
            0x1f9bc
        ), // person in motorized wheelchair: medium-dark skin tone (🧑🏾‍🦼)
        intArrayOf(0x1f9d1, 0x1f3fe, 0x200d, 0x1f9bd), // person in manual wheelchair: medium-dark skin tone (🧑🏾‍🦽)
        intArrayOf(0x1f9d1, 0x1f3ff), // person: dark skin tone (🧑🏿)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x2695, 0xfe0f), // health worker: dark skin tone (🧑🏿‍⚕️)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x2696, 0xfe0f), // judge: dark skin tone (🧑🏿‍⚖️)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x2708, 0xfe0f), // pilot: dark skin tone (🧑🏿‍✈️)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f33e), // farmer: dark skin tone (🧑🏿‍🌾)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f373), // cook: dark skin tone (🧑🏿‍🍳)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f37c), // person feeding baby: dark skin tone (🧑🏿‍🍼)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f384), // mx claus: dark skin tone (🧑🏿‍🎄)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f393), // student: dark skin tone (🧑🏿‍🎓)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f3a4), // singer: dark skin tone (🧑🏿‍🎤)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f3a8), // artist: dark skin tone (🧑🏿‍🎨)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f3eb), // teacher: dark skin tone (🧑🏿‍🏫)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f3ed), // factory worker: dark skin tone (🧑🏿‍🏭)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f4bb), // technologist: dark skin tone (🧑🏿‍💻)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f4bc), // office worker: dark skin tone (🧑🏿‍💼)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f527), // mechanic: dark skin tone (🧑🏿‍🔧)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f52c), // scientist: dark skin tone (🧑🏿‍🔬)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f680), // astronaut: dark skin tone (🧑🏿‍🚀)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f692), // firefighter: dark skin tone (🧑🏿‍🚒)
        intArrayOf(
            0x1f9d1,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fb
        ), // people holding hands: dark skin tone, light skin tone (🧑🏿‍🤝‍🧑🏻)
        intArrayOf(
            0x1f9d1,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fc
        ), // people holding hands: dark skin tone, medium-light skin tone (🧑🏿‍🤝‍🧑🏼)
        intArrayOf(
            0x1f9d1,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fd
        ), // people holding hands: dark skin tone, medium skin tone (🧑🏿‍🤝‍🧑🏽)
        intArrayOf(
            0x1f9d1,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3fe
        ), // people holding hands: dark skin tone, medium-dark skin tone (🧑🏿‍🤝‍🧑🏾)
        intArrayOf(
            0x1f9d1,
            0x1f3ff,
            0x200d,
            0x1f91d,
            0x200d,
            0x1f9d1,
            0x1f3ff
        ), // people holding hands: dark skin tone (🧑🏿‍🤝‍🧑🏿)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f9af), // person with white cane: dark skin tone (🧑🏿‍🦯)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f9b0), // person: dark skin tone, red hair (🧑🏿‍🦰)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f9b1), // person: dark skin tone, curly hair (🧑🏿‍🦱)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f9b2), // person: dark skin tone, bald (🧑🏿‍🦲)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f9b3), // person: dark skin tone, white hair (🧑🏿‍🦳)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f9bc), // person in motorized wheelchair: dark skin tone (🧑🏿‍🦼)
        intArrayOf(0x1f9d1, 0x1f3ff, 0x200d, 0x1f9bd), // person in manual wheelchair: dark skin tone (🧑🏿‍🦽)
        intArrayOf(0x1f9d2, 0x1f3fb), // child: light skin tone (🧒🏻)
        intArrayOf(0x1f9d2, 0x1f3fc), // child: medium-light skin tone (🧒🏼)
        intArrayOf(0x1f9d2, 0x1f3fd), // child: medium skin tone (🧒🏽)
        intArrayOf(0x1f9d2, 0x1f3fe), // child: medium-dark skin tone (🧒🏾)
        intArrayOf(0x1f9d2, 0x1f3ff), // child: dark skin tone (🧒🏿)
        intArrayOf(0x1f9d3, 0x1f3fb), // older person: light skin tone (🧓🏻)
        intArrayOf(0x1f9d3, 0x1f3fc), // older person: medium-light skin tone (🧓🏼)
        intArrayOf(0x1f9d3, 0x1f3fd), // older person: medium skin tone (🧓🏽)
        intArrayOf(0x1f9d3, 0x1f3fe), // older person: medium-dark skin tone (🧓🏾)
        intArrayOf(0x1f9d3, 0x1f3ff), // older person: dark skin tone (🧓🏿)
        intArrayOf(0x1f9d4, 0x200d, 0x2640, 0xfe0f), // woman: beard (🧔‍♀️)
        intArrayOf(0x1f9d4, 0x200d, 0x2642, 0xfe0f), // man: beard (🧔‍♂️)
        intArrayOf(0x1f9d4, 0x1f3fb), // person: light skin tone, beard (🧔🏻)
        intArrayOf(0x1f9d4, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman: light skin tone, beard (🧔🏻‍♀️)
        intArrayOf(0x1f9d4, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man: light skin tone, beard (🧔🏻‍♂️)
        intArrayOf(0x1f9d4, 0x1f3fc), // person: medium-light skin tone, beard (🧔🏼)
        intArrayOf(0x1f9d4, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman: medium-light skin tone, beard (🧔🏼‍♀️)
        intArrayOf(0x1f9d4, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man: medium-light skin tone, beard (🧔🏼‍♂️)
        intArrayOf(0x1f9d4, 0x1f3fd), // person: medium skin tone, beard (🧔🏽)
        intArrayOf(0x1f9d4, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman: medium skin tone, beard (🧔🏽‍♀️)
        intArrayOf(0x1f9d4, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man: medium skin tone, beard (🧔🏽‍♂️)
        intArrayOf(0x1f9d4, 0x1f3fe), // person: medium-dark skin tone, beard (🧔🏾)
        intArrayOf(0x1f9d4, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman: medium-dark skin tone, beard (🧔🏾‍♀️)
        intArrayOf(0x1f9d4, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man: medium-dark skin tone, beard (🧔🏾‍♂️)
        intArrayOf(0x1f9d4, 0x1f3ff), // person: dark skin tone, beard (🧔🏿)
        intArrayOf(0x1f9d4, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman: dark skin tone, beard (🧔🏿‍♀️)
        intArrayOf(0x1f9d4, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man: dark skin tone, beard (🧔🏿‍♂️)
        intArrayOf(0x1f9d5, 0x1f3fb), // woman with headscarf: light skin tone (🧕🏻)
        intArrayOf(0x1f9d5, 0x1f3fc), // woman with headscarf: medium-light skin tone (🧕🏼)
        intArrayOf(0x1f9d5, 0x1f3fd), // woman with headscarf: medium skin tone (🧕🏽)
        intArrayOf(0x1f9d5, 0x1f3fe), // woman with headscarf: medium-dark skin tone (🧕🏾)
        intArrayOf(0x1f9d5, 0x1f3ff), // woman with headscarf: dark skin tone (🧕🏿)
        intArrayOf(0x1f9d6, 0x200d, 0x2640, 0xfe0f), // woman in steamy room (🧖‍♀️)
        intArrayOf(0x1f9d6, 0x200d, 0x2642, 0xfe0f), // man in steamy room (🧖‍♂️)
        intArrayOf(0x1f9d6, 0x1f3fb), // person in steamy room: light skin tone (🧖🏻)
        intArrayOf(0x1f9d6, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman in steamy room: light skin tone (🧖🏻‍♀️)
        intArrayOf(0x1f9d6, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man in steamy room: light skin tone (🧖🏻‍♂️)
        intArrayOf(0x1f9d6, 0x1f3fc), // person in steamy room: medium-light skin tone (🧖🏼)
        intArrayOf(0x1f9d6, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman in steamy room: medium-light skin tone (🧖🏼‍♀️)
        intArrayOf(0x1f9d6, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man in steamy room: medium-light skin tone (🧖🏼‍♂️)
        intArrayOf(0x1f9d6, 0x1f3fd), // person in steamy room: medium skin tone (🧖🏽)
        intArrayOf(0x1f9d6, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman in steamy room: medium skin tone (🧖🏽‍♀️)
        intArrayOf(0x1f9d6, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man in steamy room: medium skin tone (🧖🏽‍♂️)
        intArrayOf(0x1f9d6, 0x1f3fe), // person in steamy room: medium-dark skin tone (🧖🏾)
        intArrayOf(0x1f9d6, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman in steamy room: medium-dark skin tone (🧖🏾‍♀️)
        intArrayOf(0x1f9d6, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man in steamy room: medium-dark skin tone (🧖🏾‍♂️)
        intArrayOf(0x1f9d6, 0x1f3ff), // person in steamy room: dark skin tone (🧖🏿)
        intArrayOf(0x1f9d6, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman in steamy room: dark skin tone (🧖🏿‍♀️)
        intArrayOf(0x1f9d6, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man in steamy room: dark skin tone (🧖🏿‍♂️)
        intArrayOf(0x1f9d7, 0x200d, 0x2640, 0xfe0f), // woman climbing (🧗‍♀️)
        intArrayOf(0x1f9d7, 0x200d, 0x2642, 0xfe0f), // man climbing (🧗‍♂️)
        intArrayOf(0x1f9d7, 0x1f3fb), // person climbing: light skin tone (🧗🏻)
        intArrayOf(0x1f9d7, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman climbing: light skin tone (🧗🏻‍♀️)
        intArrayOf(0x1f9d7, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man climbing: light skin tone (🧗🏻‍♂️)
        intArrayOf(0x1f9d7, 0x1f3fc), // person climbing: medium-light skin tone (🧗🏼)
        intArrayOf(0x1f9d7, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman climbing: medium-light skin tone (🧗🏼‍♀️)
        intArrayOf(0x1f9d7, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man climbing: medium-light skin tone (🧗🏼‍♂️)
        intArrayOf(0x1f9d7, 0x1f3fd), // person climbing: medium skin tone (🧗🏽)
        intArrayOf(0x1f9d7, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman climbing: medium skin tone (🧗🏽‍♀️)
        intArrayOf(0x1f9d7, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man climbing: medium skin tone (🧗🏽‍♂️)
        intArrayOf(0x1f9d7, 0x1f3fe), // person climbing: medium-dark skin tone (🧗🏾)
        intArrayOf(0x1f9d7, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman climbing: medium-dark skin tone (🧗🏾‍♀️)
        intArrayOf(0x1f9d7, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man climbing: medium-dark skin tone (🧗🏾‍♂️)
        intArrayOf(0x1f9d7, 0x1f3ff), // person climbing: dark skin tone (🧗🏿)
        intArrayOf(0x1f9d7, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman climbing: dark skin tone (🧗🏿‍♀️)
        intArrayOf(0x1f9d7, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man climbing: dark skin tone (🧗🏿‍♂️)
        intArrayOf(0x1f9d8, 0x200d, 0x2640, 0xfe0f), // woman in lotus position (🧘‍♀️)
        intArrayOf(0x1f9d8, 0x200d, 0x2642, 0xfe0f), // man in lotus position (🧘‍♂️)
        intArrayOf(0x1f9d8, 0x1f3fb), // person in lotus position: light skin tone (🧘🏻)
        intArrayOf(0x1f9d8, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman in lotus position: light skin tone (🧘🏻‍♀️)
        intArrayOf(0x1f9d8, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man in lotus position: light skin tone (🧘🏻‍♂️)
        intArrayOf(0x1f9d8, 0x1f3fc), // person in lotus position: medium-light skin tone (🧘🏼)
        intArrayOf(
            0x1f9d8,
            0x1f3fc,
            0x200d,
            0x2640,
            0xfe0f
        ), // woman in lotus position: medium-light skin tone (🧘🏼‍♀️)
        intArrayOf(0x1f9d8, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man in lotus position: medium-light skin tone (🧘🏼‍♂️)
        intArrayOf(0x1f9d8, 0x1f3fd), // person in lotus position: medium skin tone (🧘🏽)
        intArrayOf(0x1f9d8, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman in lotus position: medium skin tone (🧘🏽‍♀️)
        intArrayOf(0x1f9d8, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man in lotus position: medium skin tone (🧘🏽‍♂️)
        intArrayOf(0x1f9d8, 0x1f3fe), // person in lotus position: medium-dark skin tone (🧘🏾)
        intArrayOf(
            0x1f9d8,
            0x1f3fe,
            0x200d,
            0x2640,
            0xfe0f
        ), // woman in lotus position: medium-dark skin tone (🧘🏾‍♀️)
        intArrayOf(0x1f9d8, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man in lotus position: medium-dark skin tone (🧘🏾‍♂️)
        intArrayOf(0x1f9d8, 0x1f3ff), // person in lotus position: dark skin tone (🧘🏿)
        intArrayOf(0x1f9d8, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman in lotus position: dark skin tone (🧘🏿‍♀️)
        intArrayOf(0x1f9d8, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man in lotus position: dark skin tone (🧘🏿‍♂️)
        intArrayOf(0x1f9d9, 0x200d, 0x2640, 0xfe0f), // woman mage (🧙‍♀️)
        intArrayOf(0x1f9d9, 0x200d, 0x2642, 0xfe0f), // man mage (🧙‍♂️)
        intArrayOf(0x1f9d9, 0x1f3fb), // mage: light skin tone (🧙🏻)
        intArrayOf(0x1f9d9, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman mage: light skin tone (🧙🏻‍♀️)
        intArrayOf(0x1f9d9, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man mage: light skin tone (🧙🏻‍♂️)
        intArrayOf(0x1f9d9, 0x1f3fc), // mage: medium-light skin tone (🧙🏼)
        intArrayOf(0x1f9d9, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman mage: medium-light skin tone (🧙🏼‍♀️)
        intArrayOf(0x1f9d9, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man mage: medium-light skin tone (🧙🏼‍♂️)
        intArrayOf(0x1f9d9, 0x1f3fd), // mage: medium skin tone (🧙🏽)
        intArrayOf(0x1f9d9, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman mage: medium skin tone (🧙🏽‍♀️)
        intArrayOf(0x1f9d9, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man mage: medium skin tone (🧙🏽‍♂️)
        intArrayOf(0x1f9d9, 0x1f3fe), // mage: medium-dark skin tone (🧙🏾)
        intArrayOf(0x1f9d9, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman mage: medium-dark skin tone (🧙🏾‍♀️)
        intArrayOf(0x1f9d9, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man mage: medium-dark skin tone (🧙🏾‍♂️)
        intArrayOf(0x1f9d9, 0x1f3ff), // mage: dark skin tone (🧙🏿)
        intArrayOf(0x1f9d9, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman mage: dark skin tone (🧙🏿‍♀️)
        intArrayOf(0x1f9d9, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man mage: dark skin tone (🧙🏿‍♂️)
        intArrayOf(0x1f9da, 0x200d, 0x2640, 0xfe0f), // woman fairy (🧚‍♀️)
        intArrayOf(0x1f9da, 0x200d, 0x2642, 0xfe0f), // man fairy (🧚‍♂️)
        intArrayOf(0x1f9da, 0x1f3fb), // fairy: light skin tone (🧚🏻)
        intArrayOf(0x1f9da, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman fairy: light skin tone (🧚🏻‍♀️)
        intArrayOf(0x1f9da, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man fairy: light skin tone (🧚🏻‍♂️)
        intArrayOf(0x1f9da, 0x1f3fc), // fairy: medium-light skin tone (🧚🏼)
        intArrayOf(0x1f9da, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman fairy: medium-light skin tone (🧚🏼‍♀️)
        intArrayOf(0x1f9da, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man fairy: medium-light skin tone (🧚🏼‍♂️)
        intArrayOf(0x1f9da, 0x1f3fd), // fairy: medium skin tone (🧚🏽)
        intArrayOf(0x1f9da, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman fairy: medium skin tone (🧚🏽‍♀️)
        intArrayOf(0x1f9da, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man fairy: medium skin tone (🧚🏽‍♂️)
        intArrayOf(0x1f9da, 0x1f3fe), // fairy: medium-dark skin tone (🧚🏾)
        intArrayOf(0x1f9da, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman fairy: medium-dark skin tone (🧚🏾‍♀️)
        intArrayOf(0x1f9da, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man fairy: medium-dark skin tone (🧚🏾‍♂️)
        intArrayOf(0x1f9da, 0x1f3ff), // fairy: dark skin tone (🧚🏿)
        intArrayOf(0x1f9da, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman fairy: dark skin tone (🧚🏿‍♀️)
        intArrayOf(0x1f9da, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man fairy: dark skin tone (🧚🏿‍♂️)
        intArrayOf(0x1f9db, 0x200d, 0x2640, 0xfe0f), // woman vampire (🧛‍♀️)
        intArrayOf(0x1f9db, 0x200d, 0x2642, 0xfe0f), // man vampire (🧛‍♂️)
        intArrayOf(0x1f9db, 0x1f3fb), // vampire: light skin tone (🧛🏻)
        intArrayOf(0x1f9db, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman vampire: light skin tone (🧛🏻‍♀️)
        intArrayOf(0x1f9db, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man vampire: light skin tone (🧛🏻‍♂️)
        intArrayOf(0x1f9db, 0x1f3fc), // vampire: medium-light skin tone (🧛🏼)
        intArrayOf(0x1f9db, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman vampire: medium-light skin tone (🧛🏼‍♀️)
        intArrayOf(0x1f9db, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man vampire: medium-light skin tone (🧛🏼‍♂️)
        intArrayOf(0x1f9db, 0x1f3fd), // vampire: medium skin tone (🧛🏽)
        intArrayOf(0x1f9db, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman vampire: medium skin tone (🧛🏽‍♀️)
        intArrayOf(0x1f9db, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man vampire: medium skin tone (🧛🏽‍♂️)
        intArrayOf(0x1f9db, 0x1f3fe), // vampire: medium-dark skin tone (🧛🏾)
        intArrayOf(0x1f9db, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman vampire: medium-dark skin tone (🧛🏾‍♀️)
        intArrayOf(0x1f9db, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man vampire: medium-dark skin tone (🧛🏾‍♂️)
        intArrayOf(0x1f9db, 0x1f3ff), // vampire: dark skin tone (🧛🏿)
        intArrayOf(0x1f9db, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman vampire: dark skin tone (🧛🏿‍♀️)
        intArrayOf(0x1f9db, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man vampire: dark skin tone (🧛🏿‍♂️)
        intArrayOf(0x1f9dc, 0x200d, 0x2640, 0xfe0f), // mermaid (🧜‍♀️)
        intArrayOf(0x1f9dc, 0x200d, 0x2642, 0xfe0f), // merman (🧜‍♂️)
        intArrayOf(0x1f9dc, 0x1f3fb), // merperson: light skin tone (🧜🏻)
        intArrayOf(0x1f9dc, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // mermaid: light skin tone (🧜🏻‍♀️)
        intArrayOf(0x1f9dc, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // merman: light skin tone (🧜🏻‍♂️)
        intArrayOf(0x1f9dc, 0x1f3fc), // merperson: medium-light skin tone (🧜🏼)
        intArrayOf(0x1f9dc, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // mermaid: medium-light skin tone (🧜🏼‍♀️)
        intArrayOf(0x1f9dc, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // merman: medium-light skin tone (🧜🏼‍♂️)
        intArrayOf(0x1f9dc, 0x1f3fd), // merperson: medium skin tone (🧜🏽)
        intArrayOf(0x1f9dc, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // mermaid: medium skin tone (🧜🏽‍♀️)
        intArrayOf(0x1f9dc, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // merman: medium skin tone (🧜🏽‍♂️)
        intArrayOf(0x1f9dc, 0x1f3fe), // merperson: medium-dark skin tone (🧜🏾)
        intArrayOf(0x1f9dc, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // mermaid: medium-dark skin tone (🧜🏾‍♀️)
        intArrayOf(0x1f9dc, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // merman: medium-dark skin tone (🧜🏾‍♂️)
        intArrayOf(0x1f9dc, 0x1f3ff), // merperson: dark skin tone (🧜🏿)
        intArrayOf(0x1f9dc, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // mermaid: dark skin tone (🧜🏿‍♀️)
        intArrayOf(0x1f9dc, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // merman: dark skin tone (🧜🏿‍♂️)
        intArrayOf(0x1f9dd, 0x200d, 0x2640, 0xfe0f), // woman elf (🧝‍♀️)
        intArrayOf(0x1f9dd, 0x200d, 0x2642, 0xfe0f), // man elf (🧝‍♂️)
        intArrayOf(0x1f9dd, 0x1f3fb), // elf: light skin tone (🧝🏻)
        intArrayOf(0x1f9dd, 0x1f3fb, 0x200d, 0x2640, 0xfe0f), // woman elf: light skin tone (🧝🏻‍♀️)
        intArrayOf(0x1f9dd, 0x1f3fb, 0x200d, 0x2642, 0xfe0f), // man elf: light skin tone (🧝🏻‍♂️)
        intArrayOf(0x1f9dd, 0x1f3fc), // elf: medium-light skin tone (🧝🏼)
        intArrayOf(0x1f9dd, 0x1f3fc, 0x200d, 0x2640, 0xfe0f), // woman elf: medium-light skin tone (🧝🏼‍♀️)
        intArrayOf(0x1f9dd, 0x1f3fc, 0x200d, 0x2642, 0xfe0f), // man elf: medium-light skin tone (🧝🏼‍♂️)
        intArrayOf(0x1f9dd, 0x1f3fd), // elf: medium skin tone (🧝🏽)
        intArrayOf(0x1f9dd, 0x1f3fd, 0x200d, 0x2640, 0xfe0f), // woman elf: medium skin tone (🧝🏽‍♀️)
        intArrayOf(0x1f9dd, 0x1f3fd, 0x200d, 0x2642, 0xfe0f), // man elf: medium skin tone (🧝🏽‍♂️)
        intArrayOf(0x1f9dd, 0x1f3fe), // elf: medium-dark skin tone (🧝🏾)
        intArrayOf(0x1f9dd, 0x1f3fe, 0x200d, 0x2640, 0xfe0f), // woman elf: medium-dark skin tone (🧝🏾‍♀️)
        intArrayOf(0x1f9dd, 0x1f3fe, 0x200d, 0x2642, 0xfe0f), // man elf: medium-dark skin tone (🧝🏾‍♂️)
        intArrayOf(0x1f9dd, 0x1f3ff), // elf: dark skin tone (🧝🏿)
        intArrayOf(0x1f9dd, 0x1f3ff, 0x200d, 0x2640, 0xfe0f), // woman elf: dark skin tone (🧝🏿‍♀️)
        intArrayOf(0x1f9dd, 0x1f3ff, 0x200d, 0x2642, 0xfe0f), // man elf: dark skin tone (🧝🏿‍♂️)
        intArrayOf(0x1f9de, 0x200d, 0x2640, 0xfe0f), // woman genie (🧞‍♀️)
        intArrayOf(0x1f9de, 0x200d, 0x2642, 0xfe0f), // man genie (🧞‍♂️)
        intArrayOf(0x1f9df, 0x200d, 0x2640, 0xfe0f), // woman zombie (🧟‍♀️)
        intArrayOf(0x1f9df, 0x200d, 0x2642, 0xfe0f), // man zombie (🧟‍♂️)
        intArrayOf(0x1fac3, 0x1f3fb), // pregnant man: light skin tone (🫃🏻)
        intArrayOf(0x1fac3, 0x1f3fc), // pregnant man: medium-light skin tone (🫃🏼)
        intArrayOf(0x1fac3, 0x1f3fd), // pregnant man: medium skin tone (🫃🏽)
        intArrayOf(0x1fac3, 0x1f3fe), // pregnant man: medium-dark skin tone (🫃🏾)
        intArrayOf(0x1fac3, 0x1f3ff), // pregnant man: dark skin tone (🫃🏿)
        intArrayOf(0x1fac4, 0x1f3fb), // pregnant person: light skin tone (🫄🏻)
        intArrayOf(0x1fac4, 0x1f3fc), // pregnant person: medium-light skin tone (🫄🏼)
        intArrayOf(0x1fac4, 0x1f3fd), // pregnant person: medium skin tone (🫄🏽)
        intArrayOf(0x1fac4, 0x1f3fe), // pregnant person: medium-dark skin tone (🫄🏾)
        intArrayOf(0x1fac4, 0x1f3ff), // pregnant person: dark skin tone (🫄🏿)
        intArrayOf(0x1fac5, 0x1f3fb), // person with crown: light skin tone (🫅🏻)
        intArrayOf(0x1fac5, 0x1f3fc), // person with crown: medium-light skin tone (🫅🏼)
        intArrayOf(0x1fac5, 0x1f3fd), // person with crown: medium skin tone (🫅🏽)
        intArrayOf(0x1fac5, 0x1f3fe), // person with crown: medium-dark skin tone (🫅🏾)
        intArrayOf(0x1fac5, 0x1f3ff), // person with crown: dark skin tone (🫅🏿)
        intArrayOf(0x1faf0, 0x1f3fb), // hand with index finger and thumb crossed: light skin tone (🫰🏻)
        intArrayOf(0x1faf0, 0x1f3fd), // hand with index finger and thumb crossed: medium skin tone (🫰🏽)
        intArrayOf(0x1faf0, 0x1f3ff), // hand with index finger and thumb crossed: dark skin tone (🫰🏿)
        intArrayOf(0x1faf1, 0x1f3fb), // rightwards hand: light skin tone (🫱🏻)
        intArrayOf(
            0x1faf1,
            0x1f3fb,
            0x200d,
            0x1faf2,
            0x1f3fc
        ), // handshake: light skin tone, medium-light skin tone (🫱🏻‍🫲🏼)
        intArrayOf(
            0x1faf1,
            0x1f3fb,
            0x200d,
            0x1faf2,
            0x1f3fd
        ), // handshake: light skin tone, medium skin tone (🫱🏻‍🫲🏽)
        intArrayOf(
            0x1faf1,
            0x1f3fb,
            0x200d,
            0x1faf2,
            0x1f3fe
        ), // handshake: light skin tone, medium-dark skin tone (🫱🏻‍🫲🏾)
        intArrayOf(
            0x1faf1,
            0x1f3fb,
            0x200d,
            0x1faf2,
            0x1f3ff
        ), // handshake: light skin tone, dark skin tone (🫱🏻‍🫲🏿)
        intArrayOf(0x1faf1, 0x1f3fc), // rightwards hand: medium-light skin tone (🫱🏼)
        intArrayOf(
            0x1faf1,
            0x1f3fc,
            0x200d,
            0x1faf2,
            0x1f3fb
        ), // handshake: medium-light skin tone, light skin tone (🫱🏼‍🫲🏻)
        intArrayOf(
            0x1faf1,
            0x1f3fc,
            0x200d,
            0x1faf2,
            0x1f3fd
        ), // handshake: medium-light skin tone, medium skin tone (🫱🏼‍🫲🏽)
        intArrayOf(
            0x1faf1,
            0x1f3fc,
            0x200d,
            0x1faf2,
            0x1f3fe
        ), // handshake: medium-light skin tone, medium-dark skin tone (🫱🏼‍🫲🏾)
        intArrayOf(
            0x1faf1,
            0x1f3fc,
            0x200d,
            0x1faf2,
            0x1f3ff
        ), // handshake: medium-light skin tone, dark skin tone (🫱🏼‍🫲🏿)
        intArrayOf(0x1faf1, 0x1f3fd), // rightwards hand: medium skin tone (🫱🏽)
        intArrayOf(
            0x1faf1,
            0x1f3fd,
            0x200d,
            0x1faf2,
            0x1f3fb
        ), // handshake: medium skin tone, light skin tone (🫱🏽‍🫲🏻)
        intArrayOf(
            0x1faf1,
            0x1f3fd,
            0x200d,
            0x1faf2,
            0x1f3fc
        ), // handshake: medium skin tone, medium-light skin tone (🫱🏽‍🫲🏼)
        intArrayOf(
            0x1faf1,
            0x1f3fd,
            0x200d,
            0x1faf2,
            0x1f3fe
        ), // handshake: medium skin tone, medium-dark skin tone (🫱🏽‍🫲🏾)
        intArrayOf(
            0x1faf1,
            0x1f3fd,
            0x200d,
            0x1faf2,
            0x1f3ff
        ), // handshake: medium skin tone, dark skin tone (🫱🏽‍🫲🏿)
        intArrayOf(0x1faf1, 0x1f3fe), // rightwards hand: medium-dark skin tone (🫱🏾)
        intArrayOf(
            0x1faf1,
            0x1f3fe,
            0x200d,
            0x1faf2,
            0x1f3fb
        ), // handshake: medium-dark skin tone, light skin tone (🫱🏾‍🫲🏻)
        intArrayOf(
            0x1faf1,
            0x1f3fe,
            0x200d,
            0x1faf2,
            0x1f3fc
        ), // handshake: medium-dark skin tone, medium-light skin tone (🫱🏾‍🫲🏼)
        intArrayOf(
            0x1faf1,
            0x1f3fe,
            0x200d,
            0x1faf2,
            0x1f3fd
        ), // handshake: medium-dark skin tone, medium skin tone (🫱🏾‍🫲🏽)
        intArrayOf(
            0x1faf1,
            0x1f3fe,
            0x200d,
            0x1faf2,
            0x1f3ff
        ), // handshake: medium-dark skin tone, dark skin tone (🫱🏾‍🫲🏿)
        intArrayOf(0x1faf1, 0x1f3ff), // rightwards hand: dark skin tone (🫱🏿)
        intArrayOf(
            0x1faf1,
            0x1f3ff,
            0x200d,
            0x1faf2,
            0x1f3fb
        ), // handshake: dark skin tone, light skin tone (🫱🏿‍🫲🏻)
        intArrayOf(
            0x1faf1,
            0x1f3ff,
            0x200d,
            0x1faf2,
            0x1f3fc
        ), // handshake: dark skin tone, medium-light skin tone (🫱🏿‍🫲🏼)
        intArrayOf(
            0x1faf1,
            0x1f3ff,
            0x200d,
            0x1faf2,
            0x1f3fd
        ), // handshake: dark skin tone, medium skin tone (🫱🏿‍🫲🏽)
        intArrayOf(
            0x1faf1,
            0x1f3ff,
            0x200d,
            0x1faf2,
            0x1f3fe
        ), // handshake: dark skin tone, medium-dark skin tone (🫱🏿‍🫲🏾)
        intArrayOf(0x1faf2, 0x1f3fb), // leftwards hand: light skin tone (🫲🏻)
        intArrayOf(0x1faf2, 0x1f3fc), // leftwards hand: medium-light skin tone (🫲🏼)
        intArrayOf(0x1faf2, 0x1f3fd), // leftwards hand: medium skin tone (🫲🏽)
        intArrayOf(0x1faf2, 0x1f3fe), // leftwards hand: medium-dark skin tone (🫲🏾)
        intArrayOf(0x1faf2, 0x1f3ff), // leftwards hand: dark skin tone (🫲🏿)
        intArrayOf(0x1faf3, 0x1f3fb), // palm down hand: light skin tone (🫳🏻)
        intArrayOf(0x1faf3, 0x1f3fc), // palm down hand: medium-light skin tone (🫳🏼)
        intArrayOf(0x1faf3, 0x1f3fd), // palm down hand: medium skin tone (🫳🏽)
        intArrayOf(0x1faf3, 0x1f3fe), // palm down hand: medium-dark skin tone (🫳🏾)
        intArrayOf(0x1faf3, 0x1f3ff), // palm down hand: dark skin tone (🫳🏿)
        intArrayOf(0x1faf4, 0x1f3fb), // palm up hand: light skin tone (🫴🏻)
        intArrayOf(0x1faf4, 0x1f3fc), // palm up hand: medium-light skin tone (🫴🏼)
        intArrayOf(0x1faf4, 0x1f3fd), // palm up hand: medium skin tone (🫴🏽)
        intArrayOf(0x1faf4, 0x1f3fe), // palm up hand: medium-dark skin tone (🫴🏾)
        intArrayOf(0x1faf4, 0x1f3ff), // palm up hand: dark skin tone (🫴🏿)
        intArrayOf(0x1faf5, 0x1f3fb), // index pointing at the viewer: light skin tone (🫵🏻)
        intArrayOf(0x1faf5, 0x1f3fc), // index pointing at the viewer: medium-light skin tone (🫵🏼)
        intArrayOf(0x1faf5, 0x1f3fd), // index pointing at the viewer: medium skin tone (🫵🏽)
        intArrayOf(0x1faf5, 0x1f3fe), // index pointing at the viewer: medium-dark skin tone (🫵🏾)
        intArrayOf(0x1faf5, 0x1f3ff), // index pointing at the viewer: dark skin tone (🫵🏿)
        intArrayOf(0x1faf6, 0x1f3fb), // heart hands: light skin tone (🫶🏻)
        intArrayOf(0x1faf6, 0x1f3fc), // heart hands: medium-light skin tone (🫶🏼)
        intArrayOf(0x1faf6, 0x1f3fd), // heart hands: medium skin tone (🫶🏽)
        intArrayOf(0x1faf6, 0x1f3fe), // heart hands: medium-dark skin tone (🫶🏾)
        intArrayOf(0x1faf6, 0x1f3ff), // heart hands: dark skin tone (🫶🏿)
    )

    val root = IntTrie()
    for (seq in sequences) {
        var node = root
        for (i in 0 until seq.lastIndex) {
            node = node.children.getOrPut(seq[i]) { IntTrie() }
        }
        node.values += seq.last()
    }
    return root
}
