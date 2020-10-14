package com.github.ajalt.mordant.rendering


data class BorderStyleSection(
        val es: String,
        val esw: String,
        val sw: String,
        val nes: String,
        val nesw: String,
        val nsw: String,
        val ns: String,
        val ne: String,
        val ew: String,
        val new: String,
        val nw: String,
        val s: String,
        val e: String,
        val w: String,
        val n: String,
) {
    private val array = arrayOf(" ", w, s, sw, e, ew, es, esw, n, nw, ns, nsw, ne, new, nes, nesw)

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
        private fun buildSection(string: String) = BorderStyleSection(
                es = string[0].toString(),
                esw = string[2].toString(),
                sw = string[3].toString(),
                nes = string[5].toString(),
                nesw = string[7].toString(),
                nsw = string[8].toString(),
                ns = string[12].toString(),
                ne = string[19].toString(),
                ew = string[20].toString(),
                new = string[21].toString(),
                nw = string[22].toString(),
                s = string[10].toString(),
                e = string[16].toString(),
                w = string[17].toString(),
                n = string[24].toString(),
        )

        fun build(string: String): BorderStyle {
            val s = string.trimIndent().replace("·", " ")
            val head = buildSection(s)
            val body = buildSection(s.drop(32))
            val foot = buildSection(s.drop(64))
            val headBottom = head.copy(
                    nes = s[26].toString(),
                    ew = s[27].toString(),
                    nesw = s[28].toString(),
                    nsw = s[29].toString(),
                    ns = s[30].toString()
            )
            val bodyBottom = foot.copy(
                    nes = s[58].toString(),
                    ew = s[59].toString(),
                    nesw = s[60].toString(),
                    nsw = s[61].toString(),
                    ns = s[62].toString()
            )
            return BorderStyle(head, headBottom, body, bodyBottom, foot)
        }

        val SQUARE = build(
                """
                ┌─┬┐
                ├─┼┤ ╷
                │ ││╶╴
                └─┴┘ ╵
                ├─┼┤│
                ┌─┬┐
                ├─┼┤ ╷
                │ ││╶╴
                └─┴┘ ╵
                ├─┼┤│
                ┌─┬┐
                ├─┼┤ ╷
                │ ││╶╴
                └─┴┘ ╵
                """
        )

        val SQUARE_DOUBLE_HEAD_SEPARATOR = build(
                """
                ┌─┬┐
                ├─┼┤ ╷
                │ ││╶╴
                └─┴┘ ╵
                ╞═╪╡│
                ┌─┬┐
                ├─┼┤ ╷
                │ ││╶╴
                └─┴┘ ╵
                ├─┼┤│
                ┌─┬┐
                ├─┼┤ ╷
                │ ││╶╴
                └─┴┘ ╵
                """
        )

        val ROUNDED = build(
                """
                ╭─┬╮
                ├─┼┤ ╷
                │ ││╶╴
                ╰─┴╯ ╵
                ├─┼┤│
                ╭─┬╮
                ├─┼┤ ╷
                │ ││╶╴
                ╰─┴╯ ╵
                ├─┼┤│
                ╭─┬╮
                ├─┼┤ ╷
                │ ││╶╴
                ╰─┴╯ ╵
                """
        )

        val HEAVY = build(
                """
                ┏━┳┓
                ┣━╋┫ ╻
                ┃ ┃┃╺╸
                ┗━┻┛ ╹
                ┣━╋┫┃
                ┏━┳┓
                ┣━╋┫ ╻
                ┃ ┃┃╺╸
                ┗━┻┛ ╹
                ┣━╋┫┃
                ┏━┳┓
                ┣━╋┫ ╻
                ┃ ┃┃╺╸
                ┗━┻┛ ╹
                """
        )

        val DOUBLE = build(
                """
                ╔═╦╗
                ╠═╬╣ ·
                ║ ║║··
                ╚═╩╝ ·
                ╠═╬╣║
                ╔═╦╗
                ╠═╬╣ ·
                ║ ║║··
                ╚═╩╝ ·
                ╠═╬╣║
                ╔═╦╗
                ╠═╬╣ ·
                ║ ║║··
                ╚═╩╝ ·
                """
        )

        val HEAVY_HEAD_FOOT = build(
                """
                ┏━┳┓
                ┣━╋┫ ╻
                ┃ ┃┃╺╸
                ┗━┻┛ ╹
                ┡━╇┩╿
                ┌─┬┐
                ├─┼┤ ╷
                │ ││╶╴
                └─┴┘ ╵
                ┢━╈┪╽
                ┏━┳┓
                ┣━╋┫ ╻
                ┃ ┃┃╺╸
                ┗━┻┛ ╹
                """
        )

        val ASCII = build(
                """
                +-++
                +-++ ·
                | ||··
                +-++ ·
                +-++|
                +-++
                +-++ ·
                | ||··
                +-++ ·
                +-++|
                +-++
                +-++ ·
                | ||··
                +-++ ·
                """
        )
    }
}
