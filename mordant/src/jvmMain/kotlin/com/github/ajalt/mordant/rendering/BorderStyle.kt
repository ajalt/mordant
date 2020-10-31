package com.github.ajalt.mordant.rendering


class BorderStyleSection(
        private val corners: String
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
        fun bit(it: Boolean, shift: Int) = (if (it) 1 else 0) shl shift
        val i = bit(n, 3) or bit(e, 2) or bit(s, 1) or bit(w, 0)
        return Span.word(array[i], textStyle)
    }
}

class BorderStyle(
        val head: BorderStyleSection,
        val headBottom: BorderStyleSection,
        val body: BorderStyleSection,
        val bodyBottom: BorderStyleSection,
        val foot: BorderStyleSection
) {
    companion object {
        fun build(
                head: String,
                headBottom: String = head,
                body: String = head,
                bodyBottom: String = body,
                foot: String = body
        ): BorderStyle = BorderStyle(
                head = BorderStyleSection(head),
                headBottom = BorderStyleSection(headBottom),
                body = BorderStyleSection(body),
                bodyBottom = BorderStyleSection(bodyBottom),
                foot = BorderStyleSection(foot),
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

        val BLANK = build(
                "               ",
        )
    }
}
