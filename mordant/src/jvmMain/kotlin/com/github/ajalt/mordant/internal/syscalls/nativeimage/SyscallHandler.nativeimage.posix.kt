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
import org.graalvm.nativeimage.c.struct.CStruct
import org.graalvm.word.PointerBase

@CContext(PosixLibC.Directives::class)
@Platforms(Platform.LINUX::class, Platform.MACOS::class)
@Suppress("ClassName", "PropertyName", "SpellCheckingInspection", "FunctionName")
private object PosixLibC {

    class Directives : CContext.Directives {
        override fun getHeaderFiles() = listOf("<unistd.h>", "<sys/ioctl.h>", "<termios.h>")
    }

    @CConstant("TIOCGWINSZ")
    external fun TIOCGWINSZ(): Int

//    @CConstant("TCSADRAIN")
//    external fun TCSADRAIN(): Int
//
//    @CConstant("NCCS")
//    external fun NCCS(): Int

    @CStruct("winsize", addStructKeyword = true)
    interface winsize : PointerBase {

        @get:CField("ws_row")
        val ws_row: Short

        @get:CField("ws_col")
        val ws_col: Short
    }

//    @CStruct("termios", addStructKeyword = true)
//    interface termios : PointerBase {
//        @get:CField("c_iflag")
//        @set:CField("c_iflag")
//        var c_iflag: Int
//
//        @get:CField("c_oflag")
//        @set:CField("c_oflag")
//        var c_oflag: Int
//
//        @get:CField("c_cflag")
//        @set:CField("c_cflag")
//        var c_cflag: Int
//
//        @get:CField("c_lflag")
//        @set:CField("c_lflag")
//        var c_lflag: Int
//
//        @get:CField("c_line")
//        @set:CField("c_line")
//        var c_line: Byte
//
//        @get:CFieldAddress("c_cc")
//        val c_cc: CCharPointer
//
//        @get:CField("c_ispeed")
//        @set:CField("c_ispeed")
//        var c_ispeed: Int
//
//        @get:CField("c_ospeed")
//        @set:CField("c_ospeed")
//        var c_ospeed: Int
//    }

    @CFunction("isatty")
    external fun isatty(fd: Int): Boolean

    @CFunction("ioctl")
    external fun ioctl(fd: Int, cmd: Int, winSize: winsize?): Int

//    @CFunction("tcgetattr")
//    external fun tcgetattr(fd: Int, termios: termios): Int
//
//    @CFunction("tcsetattr")
//    external fun tcsetattr(fd: Int, cmd: Int, termios: termios)
}

@Platforms(Platform.LINUX::class, Platform.MACOS::class)
internal class SyscallHandlerNativeImagePosix : SyscallHandlerJvmPosix() {
    override fun isatty(fd: Int): Boolean = PosixLibC.isatty(fd)

    override fun getTerminalSize(): Size? {
        val size = StackValue.get(PosixLibC.winsize::class.java)
        return if (PosixLibC.ioctl(0, PosixLibC.TIOCGWINSZ(), size) < 0) {
            null
        } else {
            Size(width = size.ws_col.toInt(), height = size.ws_row.toInt())
        }
    }

    override fun getStdinTermios(): Termios {
        throw NotImplementedError(
            "Raw mode is not currently supported for native-image. If you are familiar with " +
                    "GraalVM native-image and would like to contribute, see the commented out " +
                    "code in the file SyscallHandler.nativeimage.posix"
        )
        /*
        This fails with the following error:

        Error: Expected Object but got Word for call argument in
        com.github.ajalt.mordant.internal.syscalls.nativeimage.SyscallHandlerNativeImagePosix.getStdinTermios(SyscallHandler.nativeimage.posix.kt:117).
        One possible cause for this error is when word values are passed into lambdas as parameters
        or from variables in an enclosing scope, which is not supported, but can be solved by
        instead using explicit classes (including anonymous classes).

        I've tried all the ways I can think of to declare tcgetattr and tcsetattr, but the error
        persists.
        */
//        val termios = StackValue.get(PosixLibC.termios::class.java)
//        if(PosixLibC.tcgetattr(STDIN_FILENO, termios) != 0) return null
//        return Termios(
//            iflag = termios.c_iflag.toUInt(),
//            oflag = termios.c_oflag.toUInt(),
//            cflag = termios.c_cflag.toUInt(),
//            lflag = termios.c_lflag.toUInt(),
//            cline = termios.c_line,
//            cc = ByteArray(PosixLibC.NCCS()) { termios.c_cc.read(it) },
//            ispeed = termios.c_ispeed.toUInt(),
//            ospeed = termios.c_ospeed.toUInt(),
//        )
    }

    override fun setStdinTermios(termios: Termios) {
//        val nativeTermios = StackValue.get(PosixLibC.termios::class.java)
//        nativeTermios.c_iflag = termios.iflag.toInt()
//        nativeTermios.c_oflag = termios.oflag.toInt()
//        nativeTermios.c_cflag = termios.cflag.toInt()
//        nativeTermios.c_lflag = termios.lflag.toInt()
//        nativeTermios.c_line = termios.cline
//        termios.cc.forEachIndexed { i, b -> nativeTermios.c_cc.write(i, b) }
//        nativeTermios.c_ispeed = termios.ispeed.toInt()
//        nativeTermios.c_ospeed = termios.ospeed.toInt()
//        PosixLibC.tcsetattr(STDIN_FILENO, PosixLibC.TCSADRAIN(), nativeTermios)
    }
}
