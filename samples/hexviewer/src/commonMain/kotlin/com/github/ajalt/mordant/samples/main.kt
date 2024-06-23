package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.rendering.BorderType
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM


fun main(args: Array<String>) {
    val terminal = Terminal()
    if (args.size != 1) {
        terminal.danger("Usage: hexviewer <filename>")
        return
    }
    val path = args[0].toPath()
    if (!FileSystem.SYSTEM.exists(path)) {
        terminal.danger("File not found: $path")
        return
    }

    // The characters to show for each byte value
    val display = "·␁␂␃␄␅␆␇␈␉␊␋␌␍␎␏␐␑␒␓␔␕␖␗␘␙␚␛␜␝␞␟" +
            " !\"#$%&'()*+,-./0123456789:;<=>?@" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~␡"

    // The text style for each byte value
    val styles = List(256) {
        when (it) {
            0 -> TextStyle(dim = true)
            in 1..31, 127 -> terminal.theme.danger
            in 32..126 -> TextStyle()
            else -> terminal.theme.warning
        }
    }

    // 4 chars per octet, -20 for borders + address
    val w = (terminal.info.width - 20) / 4
    // round down to nearest multiple of 8
    val octetsPerRow = (w - w % 8).coerceAtLeast(1)

    val bytes = FileSystem.SYSTEM.read(path) { readByteArray() }

    val table = table {
        cellBorders = Borders.LEFT_RIGHT
        tableBorders = Borders.ALL
        borderType = BorderType.ROUNDED
        column(0) { style = terminal.theme.info }

        body {
            for (addr in bytes.indices step octetsPerRow) {
                val hex = StringBuilder()
                val ascii = StringBuilder()
                for (i in addr..<(addr + octetsPerRow).coerceAtMost(bytes.size)) {
                    val byte = bytes[i].toInt() and 0xff
                    if (i > addr) {
                        if (i % 8 == 0) hex.append("┆") else hex.append(" ")
                    }
                    val s = styles[byte]
                    hex.append(s(byte.toString(16).padStart(2, '0')))
                    ascii.append(s(display.getOrElse(byte) { '·' }.toString()))
                }
                row(
                    "0x" + addr.toString(16).padStart(8, '0'),
                    hex.toString(),
                    ascii.toString()
                )
            }
        }

    }
    terminal.println(table)
}
