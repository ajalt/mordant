package com.github.ajalt.mordant.internal.syscalls.nativeimage

import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.internal.Size
import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerWindows
import org.graalvm.nativeimage.Platform
import org.graalvm.nativeimage.Platforms
import org.graalvm.nativeimage.StackValue
import org.graalvm.nativeimage.c.CContext
import org.graalvm.nativeimage.c.constant.CConstant
import org.graalvm.nativeimage.c.function.CFunction
import org.graalvm.nativeimage.c.struct.CField
import org.graalvm.nativeimage.c.struct.CFieldAddress
import org.graalvm.nativeimage.c.struct.CStruct
import org.graalvm.nativeimage.c.type.CIntPointer
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

    @CConstant("STD_ERROR_HANDLE")
    external fun STD_ERROR_HANDLE(): Int

    @CConstant("ENABLE_PROCESSED_INPUT")
    external fun ENABLE_PROCESSED_INPUT(): Int

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

    @CStruct("uChar")
    interface UnionChar : PointerBase {
        @get:CFieldAddress("UnicodeChar")
        val UnicodeChar: Char

        @get:CFieldAddress("AsciiChar")
        val AsciiChar: Byte
    }

    @CStruct("COORD")
    interface COORD : PointerBase {
        @get:CField("X")
        var X: Short

        @get:CField("Y")
        var Y: Short
    }

    @CStruct("KEY_EVENT_RECORD")
    interface KEY_EVENT_RECORD : PointerBase {
        @get:CField("bKeyDown")
        val bKeyDown: Boolean

        @get:CField("wRepeatCount")
        val wRepeatCount: Short

        @get:CField("wVirtualKeyCode")
        val wVirtualKeyCode: Short

        @get:CField("wVirtualScanCode")
        val wVirtualScanCode: Short

        @get:CField("uChar")
        val uChar: UnionChar?

        @get:CField("dwControlKeyState")
        val dwControlKeyState: Int
    }

    @CStruct("MOUSE_EVENT_RECORD")
    interface MOUSE_EVENT_RECORD : PointerBase {
        @get:CField("dwMousePosition")
        var dwMousePosition: COORD

        @get:CField("dwButtonState")
        var dwButtonState: Int

        @get:CField("dwControlKeyState")
        var dwControlKeyState: Int

        @get:CField("dwEventFlags")
        var dwEventFlags: Int
    }

    @CStruct("Event")
    interface EventUnion : PointerBase {
        @get:CFieldAddress("KeyEvent")
        val KeyEvent: KEY_EVENT_RECORD

        @get:CFieldAddress("MouseEvent")
        val MouseEvent: MOUSE_EVENT_RECORD
        // ... other fields omitted until we need them
    }

    @CStruct("INPUT_RECORD")
    interface INPUT_RECORD : PointerBase {
        companion object {
            const val KEY_EVENT: Short = 0x0001
            const val MOUSE_EVENT: Short = 0x0002
            const val WINDOW_BUFFER_SIZE_EVENT: Short = 0x0004
            const val MENU_EVENT: Short = 0x0008
            const val FOCUS_EVENT: Short = 0x0010
        }

        @get:CField("EventType")
        val EventType: Short

        @get:CField("Event")
        val Event: EventUnion
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

    @CFunction("ReadConsoleInput")
    external fun ReadConsoleInput(
        hConsoleOutput: PointerBase?,
        lpBuffer: INPUT_RECORD,
        nLength: Int,
        lpNumberOfEventsRead: CIntPointer?,
    )
}

@Platforms(Platform.WINDOWS::class)
internal class SyscallHandlerNativeImageWindows : SyscallHandlerWindows() {

    override fun stdoutInteractive(): Boolean {
        val handle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_OUTPUT_HANDLE())
        return WinKernel32Lib.GetConsoleMode(handle, StackValue.get(CIntPointer::class.java))
    }

    override fun stderrInteractive(): Boolean {
        val handle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_ERROR_HANDLE())
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

    override fun getStdinConsoleMode(): UInt? {
        val stdinHandle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_INPUT_HANDLE())
        val lpMode = StackValue.get(CIntPointer::class.java)
        if (!WinKernel32Lib.GetConsoleMode(stdinHandle, lpMode)) return null
        return lpMode.read().toUInt()
    }

    override fun setStdinConsoleMode(dwMode: UInt): Boolean {
        val stdinHandle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_INPUT_HANDLE())
        return WinKernel32Lib.SetConsoleMode(stdinHandle, WinKernel32Lib.ENABLE_PROCESSED_INPUT())
    }

    override fun readRawEvent(dwMilliseconds: Int): EventRecord? {
        val stdinHandle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_INPUT_HANDLE())
        val waitResult = WinKernel32Lib.WaitForSingleObject(stdinHandle, dwMilliseconds)
        if (waitResult != 0) {
            return null
        }
        val inputEvents = StackValue.get(WinKernel32Lib.INPUT_RECORD::class.java)
        val eventsRead = StackValue.get(CIntPointer::class.java)
        WinKernel32Lib.ReadConsoleInput(stdinHandle, inputEvents, 1, eventsRead)
        if (eventsRead.read() == 0) {
            return null
        }
        return when (inputEvents.EventType) {
            WinKernel32Lib.INPUT_RECORD.KEY_EVENT -> {
                val keyEvent = inputEvents.Event.KeyEvent
                EventRecord.Key(
                    bKeyDown = keyEvent.bKeyDown,
                    wVirtualKeyCode = keyEvent.wVirtualKeyCode.toUShort(),
                    uChar = keyEvent.uChar!!.UnicodeChar,
                    dwControlKeyState = keyEvent.dwControlKeyState.toUInt(),
                )
            }

            WinKernel32Lib.INPUT_RECORD.MOUSE_EVENT -> {
                val mouseEvent = inputEvents.Event.MouseEvent
                EventRecord.Mouse(
                    dwMousePositionX = mouseEvent.dwMousePosition.X.toInt(),
                    dwMousePositionY = mouseEvent.dwMousePosition.Y.toInt(),
                    dwButtonState = mouseEvent.dwButtonState.toUInt(),
                    dwControlKeyState = mouseEvent.dwControlKeyState.toUInt(),
                    dwEventFlags = mouseEvent.dwEventFlags.toUInt(),
                )
            }

            else -> null
        }
    }
}
