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
    companion object {
        fun build(string: String) = BorderStyleSection(
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
    }

    fun getCorner(n: Boolean, e: Boolean, s: Boolean, w: Boolean, textStyle: TextStyle = DEFAULT_STYLE): Span {
        val char = when {
            !n && e && s && !w -> this.es
            !n && e && s && w -> this.esw
            !n && !e && s && w -> this.sw
            n && e && s && !w -> this.nes
            n && e && s && w -> this.nesw
            n && !e && s && w -> this.nsw
            n && e && !s && !w -> this.ne
            n && e && !s && w -> this.new
            n && !e && !s && w -> this.nw
            !n && e && !s && w -> this.ew
            n && !e && s && !w -> this.ns
            n && !e && !s && !w -> this.n
            !n && e && !s && !w -> this.e
            !n && !e && s && !w -> this.s
            !n && !e && !s && w -> this.w
            !n && !e && !s && !w -> return SINGLE_SPACE
            else -> error("impossible corner: n=$n $e=e s=$s w=$w")
        }
        return Span.word(char, textStyle)
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
        private fun build(string: String): BorderStyle {
            val s = string.trimIndent().replace("·", " ")
            val head = BorderStyleSection.build(s)
            val body = BorderStyleSection.build(s.drop(31))
            val foot = BorderStyleSection.build(s.drop(62))
            val headBottom = head.copy(
                    nes = s[26].toString(),
                    nesw = s[27].toString(),
                    nsw = s[28].toString(),
                    ns = s[29].toString()
            )
            val bodyBottom = foot.copy(
                    nes = s[57].toString(),
                    nesw = s[58].toString(),
                    nsw = s[59].toString(),
                    ns = s[60].toString()
            )
            return BorderStyle(head, headBottom, body, bodyBottom, foot)
        }

        val SQUARE = build(
                """
                ┌─┬┐
                ├─┼┤ ╷
                │ ││╶╴
                └─┴┘ ╵
                ├┼┤│
                ┌─┬┐
                ├─┼┤ ╷
                │ ││╶╴
                └─┴┘ ╵
                ├┼┤│
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
                ├┼┤│
                ╭─┬╮
                ├─┼┤ ╷
                │ ││╶╴
                ╰─┴╯ ╵
                ├┼┤│
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
                ┣╋┫┃
                ┏━┳┓
                ┣━╋┫ ╻
                ┃ ┃┃╺╸
                ┗━┻┛ ╹
                ┣╋┫┃
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
                ╠╬╣║
                ╔═╦╗
                ╠═╬╣ ·
                ║ ║║··
                ╚═╩╝ ·
                ╠╬╣║
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
                ┡╇┩╿
                ┌─┬┐
                ├─┼┤ ╷
                │ ││╶╴
                └─┴┘ ╵
                ┢╈┪╽
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
                +++|
                +-++
                +-++ ·
                | ||··
                +-++ ·
                +++|
                +-++
                +-++ ·
                | ||··
                +-++ ·
                """
        )
    }
}
