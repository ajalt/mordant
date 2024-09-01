package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.rendering.Size
import kotlinx.cinterop.*
import platform.posix.*

// XXX: The source code for this file is identical between linux and the various apple targets, but
// they have different bit widths for some fields, so the compileMetadata task fails if we don't use
// separate files. Hopefully some day there will be solution that doesn't require copy-pasting.

internal class TerminalInterfaceNativeCopyPasted : TerminalInterfaceNativePosix() {
    override val termiosConstants: TermiosConstants = TermiosConstants(
        VTIME = VTIME,
        VMIN = VMIN,
        INPCK = INPCK.convert(),
        ISTRIP = ISTRIP.convert(),
        INLCR = INLCR.convert(),
        IGNCR = IGNCR.convert(),
        ICRNL = ICRNL.convert(),
        IXON = IXON.convert(),
        OPOST = OPOST.convert(),
        CS8 = CS8.convert(),
        ISIG = ISIG.convert(),
        ICANON = ICANON.convert(),
        ECHO = ECHO.convert(),
        IEXTEN = IEXTEN.convert(),
    )

    override fun readIntoBuffer(c: ByteVar): Long {
        return read(platform.posix.STDIN_FILENO, c.ptr, 1u).convert()
    }

    override fun getTerminalSize(): Size? = memScoped {
        val size = alloc<winsize>()
        if (ioctl(STDIN_FILENO, TIOCGWINSZ.convert(), size) < 0) {
            null
        } else {
            Size(width = size.ws_col.toInt(), height = size.ws_row.toInt())
        }
    }

    override fun getStdinTermios(): Termios = memScoped {
        val termios = alloc<termios>()
        if (tcgetattr(platform.posix.STDIN_FILENO, termios.ptr) != 0) {
            throw RuntimeException("Error reading terminal attributes")
        }
        return Termios(
            iflag = termios.c_iflag.convert(),
            oflag = termios.c_oflag.convert(),
            cflag = termios.c_cflag.convert(),
            lflag = termios.c_lflag.convert(),
            cc = ByteArray(NCCS) { termios.c_cc[it].convert() },
        )
    }

    override fun setStdinTermios(termios: Termios) = memScoped {
        val nativeTermios = alloc<termios>()
        // different platforms have different fields in termios, so we need to read the current
        // struct before we set the fields we care about.
        if (tcgetattr(platform.posix.STDIN_FILENO, nativeTermios.ptr) != 0) {
            throw RuntimeException("Error reading terminal attributes")
        }
        nativeTermios.c_iflag = termios.iflag.convert()
        nativeTermios.c_oflag = termios.oflag.convert()
        nativeTermios.c_cflag = termios.cflag.convert()
        nativeTermios.c_lflag = termios.lflag.convert()
        repeat(NCCS) { nativeTermios.c_cc[it] = termios.cc[it].convert<UByte>() }
        if (tcsetattr(platform.posix.STDIN_FILENO, TCSADRAIN, nativeTermios.ptr) != 0) {
            throw RuntimeException("Error setting terminal attributes")
        }
    }
}
