package com.github.ajalt.mordant.internal.nativeimage

import com.github.ajalt.mordant.internal.MppImpls
import com.github.ajalt.mordant.internal.Size
import org.graalvm.nativeimage.Platform
import org.graalvm.nativeimage.Platforms
import org.graalvm.nativeimage.StackValue
import org.graalvm.nativeimage.c.CContext
import org.graalvm.nativeimage.c.constant.CConstant
import org.graalvm.nativeimage.c.function.CFunction
import org.graalvm.nativeimage.c.struct.CField
import org.graalvm.nativeimage.c.struct.CStruct
import org.graalvm.word.PointerBase

@CContext(PosixLibC.Directives::class)
@Platforms(Platform.LINUX::class, Platform.MACOS::class)
@Suppress("ClassName", "PropertyName", "SpellCheckingInspection", "FunctionName")
private object PosixLibC {

    class Directives : CContext.Directives {
        override fun getHeaderFiles() = listOf("<unistd.h>", "<sys/ioctl.h>")
    }

    @CConstant("STDIN_FILENO")
    external fun STDIN_FILENO(): Int

    @CConstant("STDOUT_FILENO")
    external fun STDOUT_FILENO(): Int

    @CConstant("STDERR_FILENO")
    external fun STDERR_FILENO(): Int

    @CConstant("TIOCGWINSZ")
    external fun TIOCGWINSZ(): Int

    @CStruct("winsize", addStructKeyword = true)
    interface winsize : PointerBase {

        @get:CField("ws_row")
        val ws_row: Short

        @get:CField("ws_col")
        val ws_col: Short
    }

    @CFunction("isatty")
    external fun isatty(fd: Int): Boolean

    @CFunction("ioctl")
    external fun ioctl(fd: Int, cmd: Int, winSize: winsize?): Int

}

@Platforms(Platform.LINUX::class, Platform.MACOS::class)
internal class NativeImagePosixMppImpls : MppImpls {

    override fun stdoutInteractive() = PosixLibC.isatty(PosixLibC.STDOUT_FILENO())
    override fun stdinInteractive() = PosixLibC.isatty(PosixLibC.STDIN_FILENO())
    override fun stderrInteractive() = PosixLibC.isatty(PosixLibC.STDERR_FILENO())

    override fun getTerminalSize(): Size? {
        val size = StackValue.get(PosixLibC.winsize::class.java)
        return if (PosixLibC.ioctl(0, PosixLibC.TIOCGWINSZ(), size) < 0) {
            null
        } else {
            Size(width = size.ws_col.toInt(), height = size.ws_row.toInt())
        }
    }
}
