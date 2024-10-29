package com.github.ajalt.mordant.terminal.terminalinterface.ffm

import com.github.ajalt.mordant.rendering.Size
import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfaceJvmPosix
import java.lang.foreign.Arena
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment

@Suppress("ClassName", "PropertyName", "SpellCheckingInspection")
private class LinuxCLibrary {
    class winsize(override val segment: MemorySegment) : StructAccessor {
        object Layout : StructLayout() {
            val ws_row by shortField()
            val ws_col by shortField()
            val ws_xpixel by shortField()
            val ws_ypixel by shortField()
        }

        constructor(arena: Arena) : this(arena.allocate(Layout.layout))

        val ws_row by Layout.ws_row
        val ws_col by Layout.ws_col
        val ws_xpixel by Layout.ws_xpixel
        val ws_ypixel by Layout.ws_ypixel
    }

    class termios(override val segment: MemorySegment) : StructAccessor {
        object Layout : StructLayout() {
            val c_iflag by intField()
            val c_oflag by intField()
            val c_cflag by intField()
            val c_lflag by intField()
            val c_line by byteField()
            val c_cc by arrayField(32)
            @Suppress("unused")
            val padding by paddingField(3)
            val c_ispeed by intField()
            val c_ospeed by intField()
        }

        constructor(arena: Arena) : this(arena.allocate(Layout.layout))

        var c_iflag by Layout.c_iflag
        var c_oflag by Layout.c_oflag
        var c_cflag by Layout.c_cflag
        var c_lflag by Layout.c_lflag
        var c_line by Layout.c_line
        val c_cc by Layout.c_cc
        var c_ispeed by Layout.c_ispeed
        var c_ospeed by Layout.c_ospeed
    }

    object MethodHandles : MethodHandlesHolder() {
        val isatty by handle(Layouts.INT, Layouts.INT)
        val tcgetattr by handle(Layouts.INT, Layouts.INT, Layouts.POINTER)
        val tcsetattr by handle(Layouts.INT, Layouts.INT, Layouts.INT, Layouts.POINTER)
        val ioctl by handle(
            Layouts.INT, Layouts.INT, Layouts.LONG, Layouts.POINTER, linkerOptions = arrayOf(
                Linker.Option.firstVariadicArg(2)
            )
        )
    }

    fun isatty(fd: Int): Boolean {
        return MethodHandles.isatty.invokeExact(fd) as Int == 1
    }

    fun ioctl(fd: Int, cmd: Int, data: MemorySegment): Boolean {
        return MethodHandles.ioctl.invoke(fd, cmd.toLong(), data) as Int == 0
    }

    fun tcgetattr(fd: Int, termios: termios): Boolean {
        return MethodHandles.tcgetattr.invokeExact(fd, termios.segment) as Int == 0
    }

    fun tcsetattr(fd: Int, cmd: Int, termios: termios): Boolean {
        return MethodHandles.tcsetattr.invokeExact(fd, cmd, termios.segment) as Int == 0
    }
}

internal class TerminalInterfaceFfmLinux : TerminalInterfaceJvmPosix() {
    override val termiosConstants: TermiosConstants get() = LinuxTermiosConstants

    private companion object {
        const val TIOCGWINSZ = 0x00005413
        const val TCSADRAIN: Int = 0x1
    }

    private val libC = LinuxCLibrary()
    override fun isatty(fd: Int): Boolean = libC.isatty(fd)

    override fun getTerminalSize(): Size? = Arena.ofConfined().use { arena ->
        val size = LinuxCLibrary.winsize(arena)
        if (!libC.ioctl(STDIN_FILENO, TIOCGWINSZ, size.segment)) {
            null
        } else {
            Size(width = size.ws_col.toInt(), height = size.ws_row.toInt())
        }
    }

    override fun getStdinTermios(): Termios = Arena.ofConfined().use { arena ->
        val termios = LinuxCLibrary.termios(arena)
        if (!libC.tcgetattr(STDIN_FILENO, termios)) {
            throw RuntimeException("failed to read terminal settings")
        }
        return Termios(
            iflag = termios.c_iflag.toUInt(),
            oflag = termios.c_oflag.toUInt(),
            cflag = termios.c_cflag.toUInt(),
            lflag = termios.c_lflag.toUInt(),
            cc = termios.c_cc.toArray(Layouts.BYTE),
        )
    }

    override fun setStdinTermios(termios: Termios): Unit = Arena.ofConfined().use { arena ->
        val nativeTermios = LinuxCLibrary.termios(arena)
        if (!libC.tcgetattr(STDIN_FILENO, nativeTermios)) {
            throw RuntimeException("failed to update terminal settings")
        }
        nativeTermios.c_iflag = termios.iflag.toInt()
        nativeTermios.c_oflag = termios.oflag.toInt()
        nativeTermios.c_cflag = termios.cflag.toInt()
        nativeTermios.c_lflag = termios.lflag.toInt()
        nativeTermios.c_cc.copyFrom(MemorySegment.ofArray(termios.cc))
        libC.tcsetattr(STDIN_FILENO, TCSADRAIN, nativeTermios)
    }
}
