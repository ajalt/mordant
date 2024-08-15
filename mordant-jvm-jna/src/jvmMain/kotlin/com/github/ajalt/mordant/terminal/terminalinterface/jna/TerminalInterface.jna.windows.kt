package com.github.ajalt.mordant.terminal.terminalinterface.jna

import com.github.ajalt.mordant.rendering.Size
import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfaceWindows
import com.sun.jna.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions

// Interface definitions from
// https://github.com/java-native-access/jna/blob/master/contrib/platform/src/com/sun/jna/platform/win32/Kernel32.java
// copied here so that we don't need the entire platform dependency
@Suppress("FunctionName", "PropertyName", "ClassName", "unused")
private interface WinKernel32Lib : Library {
    companion object {
        const val STD_INPUT_HANDLE = -10
        const val STD_OUTPUT_HANDLE = -11
        const val STD_ERROR_HANDLE = -12
    }

    class HANDLE : PointerType()

    @Structure.FieldOrder("X", "Y")
    class COORD : Structure() {
        @JvmField
        var X: Short = 0

        @JvmField
        var Y: Short = 0
    }

    @Structure.FieldOrder("Left", "Top", "Right", "Bottom")
    class SMALL_RECT : Structure() {
        @JvmField
        var Left: Short = 0

        @JvmField
        var Top: Short = 0

        @JvmField
        var Right: Short = 0

        @JvmField
        var Bottom: Short = 0
    }

    @Structure.FieldOrder(
        "dwSize",
        "dwCursorPosition",
        "wAttributes",
        "srWindow",
        "dwMaximumWindowSize"
    )
    class CONSOLE_SCREEN_BUFFER_INFO : Structure() {
        @JvmField
        var dwSize: COORD? = null

        @JvmField
        var dwCursorPosition: COORD? = null

        @JvmField
        var wAttributes: Short = 0

        @JvmField
        var srWindow: SMALL_RECT? = null

        @JvmField
        var dwMaximumWindowSize: COORD? = null
    }

    class UnionChar : Union {
        constructor()

        constructor(c: Char) {
            setType(Char::class.javaPrimitiveType)
            UnicodeChar = c
        }

        constructor(c: Byte) {
            setType(Byte::class.javaPrimitiveType)
            AsciiChar = c
        }

        fun set(c: Char) {
            setType(Char::class.javaPrimitiveType)
            UnicodeChar = c
        }

        fun set(c: Byte) {
            setType(Byte::class.javaPrimitiveType)
            AsciiChar = c
        }

        @JvmField
        var UnicodeChar: Char = 0.toChar()

        @JvmField
        var AsciiChar: Byte = 0
    }


    @Structure.FieldOrder(
        "bKeyDown",
        "wRepeatCount",
        "wVirtualKeyCode",
        "wVirtualScanCode",
        "uChar",
        "dwControlKeyState"
    )
    class KEY_EVENT_RECORD : Structure() {
        @JvmField
        var bKeyDown: Boolean = false

        @JvmField
        var wRepeatCount: Short = 0

        @JvmField
        var wVirtualKeyCode: Short = 0

        @JvmField
        var wVirtualScanCode: Short = 0

        @JvmField
        var uChar: UnionChar? = null

        @JvmField
        var dwControlKeyState: Int = 0
    }

    @Structure.FieldOrder(
        "dwMousePosition", "dwButtonState", "dwControlKeyState", "dwEventFlags"
    )
    class MOUSE_EVENT_RECORD : Structure() {
        @JvmField
        var dwMousePosition: COORD? = null

        @JvmField
        var dwButtonState: Int = 0

        @JvmField
        var dwControlKeyState: Int = 0

        @JvmField
        var dwEventFlags: Int = 0
    }

    @Structure.FieldOrder("dwSize")
    class WINDOW_BUFFER_SIZE_RECORD : Structure() {
        @JvmField
        var dwSize: COORD? = null
    }

    @Structure.FieldOrder("dwCommandId")
    class MENU_EVENT_RECORD : Structure() {
        @JvmField
        var dwCommandId: Int = 0
    }

    @Structure.FieldOrder("bSetFocus")
    class FOCUS_EVENT_RECORD : Structure() {
        @JvmField
        var bSetFocus: Boolean = false
    }

    // https://learn.microsoft.com/en-us/windows/console/input-record-str
    @Structure.FieldOrder("EventType", "Event")
    class INPUT_RECORD : Structure() {
        companion object {
            const val KEY_EVENT: Short = 0x0001
            const val MOUSE_EVENT: Short = 0x0002
            const val WINDOW_BUFFER_SIZE_EVENT: Short = 0x0004
            const val MENU_EVENT: Short = 0x0008
            const val FOCUS_EVENT: Short = 0x0010
        }

        @JvmField
        var EventType: Short = 0

        @JvmField
        var Event: EventUnion? = null

        class EventUnion : Union() {
            @JvmField
            var KeyEvent: KEY_EVENT_RECORD? = null

            @JvmField
            var MouseEvent: MOUSE_EVENT_RECORD? = null

            @JvmField
            var WindowBufferSizeEvent: WINDOW_BUFFER_SIZE_RECORD? = null

            @JvmField
            var MenuEvent: MENU_EVENT_RECORD? = null

            @JvmField
            var FocusEvent: FOCUS_EVENT_RECORD? = null
        }

        override fun read() {
            readField("EventType")
            when (EventType) {
                KEY_EVENT -> Event!!.setType(KEY_EVENT_RECORD::class.java)
                MOUSE_EVENT -> Event!!.setType(MOUSE_EVENT_RECORD::class.java)
                WINDOW_BUFFER_SIZE_EVENT -> Event!!.setType(WINDOW_BUFFER_SIZE_RECORD::class.java)
                MENU_EVENT -> Event!!.setType(MENU_EVENT_RECORD::class.java)
                FOCUS_EVENT -> Event!!.setType(FOCUS_EVENT_RECORD::class.java)
            }
            super.read()
        }
    }


    fun GetStdHandle(nStdHandle: Int): HANDLE

    @Throws(LastErrorException::class)
    fun GetConsoleMode(hConsoleHandle: HANDLE, lpMode: IntByReference)

    fun GetConsoleScreenBufferInfo(
        hConsoleOutput: HANDLE,
        lpConsoleScreenBufferInfo: CONSOLE_SCREEN_BUFFER_INFO,
    ): Boolean

    @Throws(LastErrorException::class)
    fun SetConsoleMode(hConsoleHandle: HANDLE, dwMode: Int)

    // https://learn.microsoft.com/en-us/windows/win32/api/synchapi/nf-synchapi-waitforsingleobject
    fun WaitForSingleObject(hHandle: Pointer, dwMilliseconds: Int): Int

    @Throws(LastErrorException::class)
    fun ReadConsoleInput(
        hConsoleOutput: HANDLE,
        lpBuffer: Array<INPUT_RECORD?>,
        nLength: Int,
        lpNumberOfEventsRead: IntByReference,
    )
}


internal class TerminalInterfaceJnaWindows : TerminalInterfaceWindows() {
    private val kernel = Native.load(
        "kernel32", WinKernel32Lib::class.java, W32APIOptions.DEFAULT_OPTIONS
    )
    private val stdoutHandle = kernel.GetStdHandle(WinKernel32Lib.STD_OUTPUT_HANDLE)
    private val stdinHandle = kernel.GetStdHandle(WinKernel32Lib.STD_INPUT_HANDLE)
    private fun handleInteractive(handle: WinKernel32Lib.HANDLE): Boolean {
        return try {
            kernel.GetConsoleMode(handle, IntByReference())
            true
        } catch (e: LastErrorException) {
            false
        }
    }

    override fun stdoutInteractive(): Boolean = handleInteractive(stdoutHandle)
    override fun stdinInteractive(): Boolean = handleInteractive(stdinHandle)

    override fun getTerminalSize(): Size? {
        val csbi = WinKernel32Lib.CONSOLE_SCREEN_BUFFER_INFO()
        if (!kernel.GetConsoleScreenBufferInfo(stdoutHandle, csbi)) {
            return null
        }
        return csbi.srWindow?.run { Size(width = Right - Left + 1, height = Bottom - Top + 1) }
    }


    override fun getStdinConsoleMode(): UInt {
        val originalMode = IntByReference()
        kernel.GetConsoleMode(stdinHandle, originalMode)
        return originalMode.value.toUInt()
    }

    override fun setStdinConsoleMode(dwMode: UInt) {
        kernel.SetConsoleMode(stdinHandle, dwMode.toInt())
    }

    override fun readRawEvent(dwMilliseconds: Int): EventRecord? {
        val waitResult = kernel.WaitForSingleObject(stdinHandle.pointer, dwMilliseconds)
        if (waitResult != 0) {
            throw RuntimeException("Timeout reading from console input")
        }
        val inputEvents = arrayOfNulls<WinKernel32Lib.INPUT_RECORD>(1)
        val eventsRead = IntByReference()
        kernel.ReadConsoleInput(stdinHandle, inputEvents, inputEvents.size, eventsRead)
        val inputEvent = inputEvents[0]
            ?: throw RuntimeException("Error reading from console input")
        return when (inputEvent.EventType) {
            WinKernel32Lib.INPUT_RECORD.KEY_EVENT -> {
                val keyEvent = inputEvent.Event!!.KeyEvent!!
                EventRecord.Key(
                    bKeyDown = keyEvent.bKeyDown,
                    wVirtualKeyCode = keyEvent.wVirtualKeyCode.toUShort(),
                    uChar = keyEvent.uChar!!.UnicodeChar,
                    dwControlKeyState = keyEvent.dwControlKeyState.toUInt(),
                )
            }

            WinKernel32Lib.INPUT_RECORD.MOUSE_EVENT -> {
                val mouseEvent = inputEvent.Event!!.MouseEvent!!
                EventRecord.Mouse(
                    dwMousePositionX = mouseEvent.dwMousePosition!!.X,
                    dwMousePositionY = mouseEvent.dwMousePosition!!.Y,
                    dwButtonState = mouseEvent.dwButtonState.toUInt(),
                    dwControlKeyState = mouseEvent.dwControlKeyState.toUInt(),
                    dwEventFlags = mouseEvent.dwEventFlags.toUInt(),
                )
            }

            else -> null // Ignore other event types like FOCUS_EVENT that we can't opt out of
        }
    }
}

