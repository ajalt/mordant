package com.github.ajalt.mordant.terminal.terminalinterface.nativeimage

import com.github.ajalt.mordant.rendering.Size
import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfaceWindows
import org.graalvm.nativeimage.Platform
import org.graalvm.nativeimage.Platforms
import org.graalvm.nativeimage.StackValue
import org.graalvm.nativeimage.c.CContext
import org.graalvm.nativeimage.c.constant.CConstant
import org.graalvm.nativeimage.c.function.CFunction
import org.graalvm.nativeimage.c.struct.CField
import org.graalvm.nativeimage.c.struct.CStruct
import org.graalvm.nativeimage.c.type.CIntPointer
import org.graalvm.word.Pointer
import org.graalvm.word.PointerBase

@Platforms(Platform.WINDOWS::class)
@CContext(WinKernel32Lib.Directives::class)
@Suppress("FunctionName", "PropertyName", "ClassName")
private object WinKernel32Lib {

    class Directives : CContext.Directives {
        override fun getHeaderFiles() = listOf("<Windows.h>")
    }

    @CConstant("STD_INPUT_HANDLE")
    external fun STD_INPUT_HANDLE(): Int

    @CConstant("STD_OUTPUT_HANDLE")
    external fun STD_OUTPUT_HANDLE(): Int

    @CStruct("CONSOLE_SCREEN_BUFFER_INFO")
    interface CONSOLE_SCREEN_BUFFER_INFO : PointerBase {

        @get:CField("srWindow.Left")
        val Left: Short

        @get:CField("srWindow.Top")
        val Top: Short

        @get:CField("srWindow.Right")
        val Right: Short

        @get:CField("srWindow.Bottom")
        val Bottom: Short

    }

    @CFunction("GetStdHandle")
    external fun GetStdHandle(nStdHandle: Int): PointerBase?

    @CFunction("GetConsoleMode")
    external fun GetConsoleMode(hConsoleHandle: PointerBase?, lpMode: CIntPointer?): Boolean

    @CFunction("SetConsoleMode")
    external fun SetConsoleMode(hConsoleHandle: PointerBase?, dwMode: Int): Boolean

    @CFunction("GetConsoleScreenBufferInfo")
    external fun GetConsoleScreenBufferInfo(
        hConsoleOutput: PointerBase?,
        lpConsoleScreenBufferInfo: Long,
    ): Boolean

    @CFunction("WaitForSingleObject")
    external fun WaitForSingleObject(hHandle: PointerBase?, dwMilliseconds: Int): Int

    @CFunction("ReadConsoleInputW")
    external fun ReadConsoleInputW(
        hConsoleOutput: PointerBase?,
        lpBuffer: Pointer?,
        nLength: Int,
        lpNumberOfEventsRead: CIntPointer?,
    )
}

@Platforms(Platform.WINDOWS::class)
internal class TerminalInterfaceNativeImageWindows : TerminalInterfaceWindows() {
    private companion object {
        const val INPUT_RECORD_BYTE_SIZE = 20
        const val KEY_EVENT: Short = 0x0001
        const val MOUSE_EVENT: Short = 0x0002
    }

    override fun stdoutInteractive(): Boolean {
        val handle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_OUTPUT_HANDLE())
        return WinKernel32Lib.GetConsoleMode(handle, StackValue.get(CIntPointer::class.java))
    }

    override fun stdinInteractive(): Boolean {
        val handle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_INPUT_HANDLE())
        return WinKernel32Lib.GetConsoleMode(handle, StackValue.get(CIntPointer::class.java))
    }

    override fun getTerminalSize(): Size? {
        val csbi = StackValue.get(WinKernel32Lib.CONSOLE_SCREEN_BUFFER_INFO::class.java)
        val handle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_OUTPUT_HANDLE())
        return if (!WinKernel32Lib.GetConsoleScreenBufferInfo(handle, csbi.rawValue())) {
            null
        } else {
            Size(width = csbi.Right - csbi.Left + 1, height = csbi.Bottom - csbi.Top + 1)
        }
    }

    override fun getStdinConsoleMode(): UInt {
        val stdinHandle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_INPUT_HANDLE())
        val lpMode = StackValue.get(CIntPointer::class.java)
        if (!WinKernel32Lib.GetConsoleMode(stdinHandle, lpMode)) {
            throw RuntimeException("Error reading console mode")
        }
        return lpMode.read().toUInt()
    }

    override fun setStdinConsoleMode(dwMode: UInt) {
        val stdinHandle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_INPUT_HANDLE())
        if (!WinKernel32Lib.SetConsoleMode(stdinHandle, dwMode.toInt())) {
            throw RuntimeException("Error setting console mode")
        }
    }

    override fun readRawEvent(dwMilliseconds: Int): EventRecord? {
        val stdinHandle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_INPUT_HANDLE())
        val waitResult = WinKernel32Lib.WaitForSingleObject(stdinHandle, dwMilliseconds)
        if (waitResult != 0) {
            throw RuntimeException("Error reading from console input: waitResult=$waitResult")
        }
        val inputRecord = StackValue.get<Pointer>(INPUT_RECORD_BYTE_SIZE)
        val eventsRead = StackValue.get(CIntPointer::class.java)
        WinKernel32Lib.ReadConsoleInputW(stdinHandle, inputRecord, 1, eventsRead)
        if (eventsRead.read() == 0) {
            throw RuntimeException("Error reading from console input")
        }

        // native-image can't store word values like Pointer in object fields
        val event = inputRecord.add(4)
        return when (inputRecord.readShort(0)) {
            KEY_EVENT -> EventRecord.Key(
                bKeyDown = event.readInt(0) != 0,
                wVirtualKeyCode = event.readShort(6).toUShort(),
                uChar = event.readChar(10),
                dwControlKeyState = event.readInt(12).toUInt(),
            )

            MOUSE_EVENT -> EventRecord.Mouse(
                dwMousePositionX = event.readShort(0),
                dwMousePositionY = event.readShort(2),
                dwButtonState = event.readInt(4).toUInt(),
                dwControlKeyState = event.readInt(8).toUInt(),
                dwEventFlags = event.readInt(12).toUInt(),
            )

            else -> null // Ignore other event types like FOCUS_EVENT that we can't opt out of
        }
    }
}
