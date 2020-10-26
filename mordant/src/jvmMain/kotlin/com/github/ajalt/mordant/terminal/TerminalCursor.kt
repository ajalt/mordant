package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.internal.CSI

class TerminalCursor(private val enabled: Boolean) {
    fun up(count: Int) {
        when {
            count < 0 -> down(-count)
            count > 0 -> csi("${count}A")
        }
    }


    fun down(count: Int) {
        when {
            count < 0 -> up(-count)
            count > 0 -> csi("${count}B")
        }
    }

    fun right(count: Int) {
        when {
            count < 0 -> left(-count)
            count > 0 -> csi("${count}C")
        }
    }

    fun left(count: Int) {
        when {
            count < 0 -> right(-count)
            count > 0 -> csi("${count}D")
        }
    }

    /**
     * [x] and [y] are 0-indexed
     */
    fun setPosition(x: Int, y: Int) {
        require(x >= 0) { "Invalid cursor column $x; value cannot be negative" }
        require(y >= 0) { "Invalid cursor column $y; value cannot be negative" }

        // CUP command is 1-indexed
        csi("${x + 1};${y + 1}H")
    }

    fun clearScreen() {
        csi("2J")
    }

    private fun csi(command: String) {
        if (enabled) CSI + print(command)
    }
}
