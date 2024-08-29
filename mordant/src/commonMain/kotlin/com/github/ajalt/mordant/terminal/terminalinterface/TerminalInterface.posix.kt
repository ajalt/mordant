package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.terminal.StandardTerminalInterface
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

/**
 * A terminal interface for POSIX systems.
 *
 * This class provides a base interface for POSIX systems like Linux and macOS that use termios for
 * terminal configuration.
 */
@Suppress("unused", "SpellCheckingInspection")
abstract class TerminalInterfacePosix : StandardTerminalInterface() {
    protected companion object {
        const val STDIN_FILENO = 0
        const val STDOUT_FILENO = 1
        const val STDERR_FILENO = 2

        val MacosTermiosConstants = TermiosConstants(
            VTIME = 17,
            VMIN = 16,
            INPCK = 0x00000010u,
            ISTRIP = 0x00000020u,
            INLCR = 0x00000040u,
            IGNCR = 0x00000080u,
            ICRNL = 0x00000100u,
            IXON = 0x00000200u,
            OPOST = 0x00000001u,
            CS8 = 0x00000300u,
            ISIG = 0x00000080u,
            ICANON = 0x00000100u,
            ECHO = 0x00000008u,
            IEXTEN = 0x00000400u,
        )

        val LinuxTermiosConstants = TermiosConstants(
            VTIME = 5,
            VMIN = 6,
            INPCK = 0x0000010u,
            ISTRIP = 0x0000020u,
            INLCR = 0x0000040u,
            IGNCR = 0x0000080u,
            ICRNL = 0x0000100u,
            IXON = 0x0000400u,
            OPOST = 0x0000001u,
            CS8 = 0x0000030u,
            ISIG = 0x0000001u,
            ICANON = 0x0000002u,
            ECHO = 0x0000008u,
            IEXTEN = 0x0008000u,
        )
    }

    @Suppress("ArrayInDataClass")
    data class Termios(
        val iflag: UInt,
        val oflag: UInt,
        val cflag: UInt,
        val lflag: UInt,
        val cc: ByteArray,
    )

    /**
     * Constants for termios flags and control characters.
     *
     * The values for these are platform-specific.
     * https://www.man7.org/linux/man-pages/man3/termios.3.html
     */
    @Suppress("PropertyName")
    data class TermiosConstants(
        val VTIME: Int,
        val VMIN: Int,
        val INPCK: UInt,
        val ISTRIP: UInt,
        val INLCR: UInt,
        val IGNCR: UInt,
        val ICRNL: UInt,
        val IXON: UInt,
        val OPOST: UInt,
        val CS8: UInt,
        val ISIG: UInt,
        val ICANON: UInt,
        val ECHO: UInt,
        val IEXTEN: UInt,
    )


    abstract fun getStdinTermios(): Termios
    abstract fun setStdinTermios(termios: Termios)
    abstract val termiosConstants: TermiosConstants
    protected abstract fun isatty(fd: Int): Boolean
    protected abstract fun readRawByte(t0: ComparableTimeMark, timeout: Duration): Int

    override fun stdoutInteractive(): Boolean = isatty(STDOUT_FILENO)
    override fun stdinInteractive(): Boolean = isatty(STDIN_FILENO)

    // https://viewsourcecode.org/snaptoken/kilo/02.enteringRawMode.html
    // https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h2-Mouse-Tracking
    override fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable {
        val orig = getStdinTermios()
        val c = termiosConstants
        val new = Termios(
            iflag = orig.iflag and (c.ICRNL or c.IGNCR or c.INPCK or c.ISTRIP or c.IXON).inv(),
            // we leave OPOST on so we don't change \r\n handling
            oflag = orig.oflag,
            cflag = orig.cflag or c.CS8,
            lflag = orig.lflag and (c.ECHO or c.ICANON or c.IEXTEN or c.ISIG).inv(),
            cc = orig.cc.copyOf().also {
                it[c.VMIN] = 0 // min wait time on read
                it[c.VTIME] = 1 // max wait time on read, in 10ths of a second
            },
        )
        setStdinTermios(new)
        when (mouseTracking) {
            MouseTracking.Off -> {}
            MouseTracking.Normal -> print("${CSI}?1005h${CSI}?1000h")
            MouseTracking.Button -> print("${CSI}?1005h${CSI}?1002h")
            MouseTracking.Any -> print("${CSI}?1005h${CSI}?1003h")
        }
        return AutoCloseable {
            if (mouseTracking != MouseTracking.Off) print("${CSI}?1000l")
            setStdinTermios(orig)
        }
    }

    override fun readInputEvent(timeout: Duration, mouseTracking: MouseTracking): InputEvent? {
        return PosixEventParser { t0, t -> readRawByte(t0, t) }.readInputEvent(timeout)
    }
}

