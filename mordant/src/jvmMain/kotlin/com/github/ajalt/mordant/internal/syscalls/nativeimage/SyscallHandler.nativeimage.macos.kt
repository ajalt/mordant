package com.github.ajalt.mordant.internal.syscalls.nativeimage

import com.github.ajalt.mordant.internal.Size
import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerJvmPosix
import org.graalvm.nativeimage.Platform
import org.graalvm.nativeimage.Platforms
import org.graalvm.nativeimage.StackValue
import org.graalvm.nativeimage.c.CContext
import org.graalvm.nativeimage.c.constant.CConstant
import org.graalvm.nativeimage.c.function.CFunction
import org.graalvm.nativeimage.c.struct.CField
import org.graalvm.nativeimage.c.struct.CFieldAddress
import org.graalvm.nativeimage.c.struct.CStruct
import org.graalvm.nativeimage.c.type.CCharPointer
import org.graalvm.word.PointerBase

@CContext(MacosLibC.Directives::class)
@Platforms(Platform.MACOS::class)
@Suppress("ClassName", "PropertyName", "SpellCheckingInspection", "FunctionName")
private object MacosLibC {

    class Directives : CContext.Directives {
        override fun getHeaderFiles() = listOf("<unistd.h>", "<sys/ioctl.h>", "<termios.h>")
    }

    @CConstant("TIOCGWINSZ")
    external fun TIOCGWINSZ(): Int

    @CConstant("TCSADRAIN")
    external fun TCSADRAIN(): Int

    @CConstant("NCCS")
    external fun NCCS(): Int

    @CStruct("winsize", addStructKeyword = true)
    interface winsize : PointerBase {

        @get:CField("ws_row")
        val ws_row: Short

        @get:CField("ws_col")
        val ws_col: Short
    }

    @CStruct("termios", addStructKeyword = true)
    interface termios : PointerBase {
        @get:CField("c_iflag")
        @set:CField("c_iflag")
        var c_iflag: Long

        @get:CField("c_oflag")
        @set:CField("c_oflag")
        var c_oflag: Long

        @get:CField("c_cflag")
        @set:CField("c_cflag")
        var c_cflag: Long

        @get:CField("c_lflag")
        @set:CField("c_lflag")
        var c_lflag: Long

        @get:CFieldAddress("c_cc")
        val c_cc: CCharPointer

        @get:CField("c_ispeed")
        @set:CField("c_ispeed")
        var c_ispeed: Long

        @get:CField("c_ospeed")
        @set:CField("c_ospeed")
        var c_ospeed: Long
    }

    @CFunction("isatty")
    external fun isatty(fd: Int): Boolean

    @CFunction("ioctl")
    external fun ioctl(fd: Int, cmd: Int, winSize: winsize?): Int

    @CFunction("tcgetattr")
    external fun tcgetattr(fd: Int, termios: termios?): Int

    @CFunction("tcsetattr")
    external fun tcsetattr(fd: Int, cmd: Int, termios: termios?): Int
}

@Platforms(Platform.MACOS::class)
internal class SyscallHandlerNativeImageMacos : SyscallHandlerJvmPosix() {
    override val termiosConstants: TermiosConstants get() = MacosTermiosConstants
    override fun isatty(fd: Int): Boolean = MacosLibC.isatty(fd)

    override fun getTerminalSize(): Size? {
        val size = StackValue.get(MacosLibC.winsize::class.java)
        return if (MacosLibC.ioctl(0, MacosLibC.TIOCGWINSZ(), size) < 0) {
            null
        } else {
            Size(width = size.ws_col.toInt(), height = size.ws_row.toInt())
        }
    }

    override fun getStdinTermios(): Termios {
        val termios = StackValue.get(MacosLibC.termios::class.java)
        if (MacosLibC.tcgetattr(STDIN_FILENO, termios) != 0) {
            throw RuntimeException("Error reading terminal attributes")
        }
        return Termios(
            iflag = termios.c_iflag.toUInt(),
            oflag = termios.c_oflag.toUInt(),
            cflag = termios.c_cflag.toUInt(),
            lflag = termios.c_lflag.toUInt(),
            cc = ByteArray(MacosLibC.NCCS()) { termios.c_cc.read(it) },
        )
    }

    override fun setStdinTermios(termios: Termios) {
        val nativeTermios = StackValue.get(MacosLibC.termios::class.java)
        if (MacosLibC.tcgetattr(STDIN_FILENO, nativeTermios) != 0) {
            throw RuntimeException("Error reading terminal attributes")
        }
        nativeTermios.c_iflag = termios.iflag.toLong()
        nativeTermios.c_oflag = termios.oflag.toLong()
        nativeTermios.c_cflag = termios.cflag.toLong()
        nativeTermios.c_lflag = termios.lflag.toLong()
        termios.cc.forEachIndexed { i, b -> nativeTermios.c_cc.write(i, b) }
        if (MacosLibC.tcsetattr(STDIN_FILENO, MacosLibC.TCSADRAIN(), nativeTermios) != 0) {
            throw RuntimeException("Error setting terminal attributes")
        }
    }
}
