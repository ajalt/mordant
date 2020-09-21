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
        val n: String,
        val w: String,
        val e: String,
        val s: String,
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
                n = string[10].toString(),
                w = string[16].toString(),
                e = string[17].toString(),
                s = string[24].toString(),
        )
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
            val s = string.trimIndent()
            val head = BorderStyleSection.build(s)
            val headBottom = head.copy(
                    nes = s[26].toString(),
                    nesw = s[76].toString(),
                    nsw = s[28].toString(),
                    ns = s[29].toString()
            )
            val body = BorderStyleSection.build(s.drop(31))
            val bodyBottom = body.copy(
                    nes = s[57].toString(),
                    nesw = s[58].toString(),
                    nsw = s[59].toString(),
                    ns = s[60].toString()
            )
            val foot = BorderStyleSection.build(s.drop(62))
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
                ┡╇┩╿
                ┏━┳┓
                ┣━╋┫ ╻
                ┃ ┃┃╺╸
                ┗━┻┛ ╹
                ┢╈┪╽
                ┏━┳┓
                ┣━╋┫ ╻
                ┃ ┃┃╺╸
                ┗━┻┛ ╹
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
                +-++  
                | ||  
                +-++  
                +++|
                +-++
                +-++  
                | ||  
                +-++  
                +++|
                +-++
                +-++  
                | ||  
                +-++  
                """
        )
    }
}
