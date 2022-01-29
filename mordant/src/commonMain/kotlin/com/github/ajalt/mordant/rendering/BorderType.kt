package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.internal.DEFAULT_STYLE


class BorderTypeSection(
    private val corners: String,
) {
    init {
        require(corners.length == 15) { "string of corners must have length==15" }
    }

    // Indexing into this array is ~2x faster than if we indexed into `corners` directly.
    private val array = arrayOf(" ", w, s, sw, e, ew, es, esw, n, nw, ns, nsw, ne, new, nes, nesw)

    val es: String get() = corners[0].toString()
    val esw: String get() = corners[1].toString()
    val sw: String get() = corners[2].toString()
    val nes: String get() = corners[3].toString()
    val nesw: String get() = corners[4].toString()
    val nsw: String get() = corners[5].toString()
    val ne: String get() = corners[6].toString()
    val new: String get() = corners[7].toString()
    val nw: String get() = corners[8].toString()
    val ew: String get() = corners[9].toString()
    val ns: String get() = corners[10].toString()
    val s: String get() = corners[11].toString()
    val n: String get() = corners[12].toString()
    val w: String get() = corners[13].toString()
    val e: String get() = corners[14].toString()

    fun getCorner(n: Boolean, e: Boolean, s: Boolean, w: Boolean, textStyle: TextStyle = DEFAULT_STYLE): Span {
        val i = (if (n) 8 else 0) or (if (e) 4 else 0) or (if (s) 2 else 0) or (if (w) 1 else 0)
        return Span.word(array[i], textStyle)
    }
}

class BorderType(
    val head: BorderTypeSection,
    val headBottom: BorderTypeSection,
    val body: BorderTypeSection,
    val bodyBottom: BorderTypeSection,
    val foot: BorderTypeSection,
) {
    companion object {
        fun build(
            head: String,
            headBottom: String = head,
            body: String = head,
            bodyBottom: String = body,
            foot: String = body,
        ): BorderType = BorderType(
            head = BorderTypeSection(head),
            headBottom = BorderTypeSection(headBottom),
            body = BorderTypeSection(body),
            bodyBottom = BorderTypeSection(bodyBottom),
            foot = BorderTypeSection(foot),
        )

        /**
         * ### Example
         *
         * ```
         *┌──┬──┐
         *│  │  │
         *├──┼──┤
         *│  │  │
         *├──┼──┤
         *│  │  │
         *├──┼──┤
         *│  │  │
         *└──┴──┘
         * ```
         */
        val SQUARE = build(
            "┌┬┐├┼┤└┴┘─│╷╵╴╶"
        )

        /**
         * ### Example
         *
         * ```
         *┌──┬──┐
         *│  │  │
         *╞══╪══╡
         *│  │  │
         *├──┼──┤
         *│  │  │
         *╞══╪══╡
         *│  │  │
         *└──┴──┘
         * ```
         */
        val SQUARE_DOUBLE_SECTION_SEPARATOR = build(
            "┌┬┐├┼┤└┴┘─│╷╵╴╶",
            "╒╤╕╞╪╡╘╧╛═│╷╵  ",
            "┌┬┐├┼┤└┴┘─│╷╵╴╶",
            "╒╤╕╞╪╡╘╧╛═│╷╵  ",
        )

        /**
         * ### Example
         *
         * ```
         *╭──┬──╮
         *│  │  │
         *├──┼──┤
         *│  │  │
         *├──┼──┤
         *│  │  │
         *├──┼──┤
         *│  │  │
         *╰──┴──╯
         * ```
         */
        val ROUNDED = build(
            "╭┬╮├┼┤╰┴╯─│╷╵╴╶",
        )

        /**
         * ### Example
         *
         * ```
         *┏━━┳━━┓
         *┃  ┃  ┃
         *┣━━╋━━┫
         *┃  ┃  ┃
         *┣━━╋━━┫
         *┃  ┃  ┃
         *┣━━╋━━┫
         *┃  ┃  ┃
         *┗━━┻━━┛
         * ```
         */
        val HEAVY = build(
            "┏┳┓┣╋┫┗┻┛━┃╻╹╸╺",
        )

        /**
         * ### Example
         *
         * ```
         *╔══╦══╗
         *║  ║  ║
         *╠══╬══╣
         *║  ║  ║
         *╠══╬══╣
         *║  ║  ║
         *╠══╬══╣
         *║  ║  ║
         *╚══╩══╝
         * ```
         */
        val DOUBLE = build(
            "╔╦╗╠╬╣╚╩╝═║    ",
        )

        /**
         * ### Example
         *
         * ```
         *┏━━┳━━┓
         *┃  ┃  ┃
         *┡━━╇━━┩
         *│  │  │
         *├──┼──┤
         *│  │  │
         *┢━━╈━━┪
         *┃  ┃  ┃
         *┗━━┻━━┛
         * ```
         */
        val HEAVY_HEAD_FOOT = build(
            "┏┳┓┣╋┫┗┻┛━┃╻╹╸╺",
            "┍┯┑┡╇┩┗┻┛━╿╷╹╸╺",
            "┌┬┐├┼┤└┴┘─│╷╵╴╶",
            "┏┳┓┢╈┪┕┷┙━╽╻╵╸╺",
            "┏┳┓┣╋┫┗┻┛━┃╻╹╸╺",
        )

        /**
         * ### Example
         *
         * ```
         *+--+--+
         *|  |  |
         *+--+--+
         *|  |  |
         *+--+--+
         *|  |  |
         *+--+--+
         *|  |  |
         *+--+--+
         * ```
         */
        val ASCII = build(
            "+++++++++-|    ",
        )

        /**
         * ### Example
         *
         * ```
         *+--+--+
         *|  |  |
         *+==+==+
         *|  |  |
         *+--+--+
         *|  |  |
         *+==+==+
         *|  |  |
         *+--+--+
         * ```
         */
        val ASCII_DOUBLE_SECTION_SEPARATOR = build(
            "+++++++++-|    ",
            "+++++++++=|    ",
            "+++++++++-|    ",
            "+++++++++=|    ",
        )

        /** Whitespace as borders */
        val BLANK = build(
            "               ",
        )
    }
}
