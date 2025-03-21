package com.github.ajalt.mordant.terminal.terminalinterface.ffm

import com.github.ajalt.mordant.rendering.Size
import com.github.ajalt.mordant.terminal.TimeoutException
import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfaceWindows
import java.lang.foreign.*

private object WinLayouts {
    val HANDLE: AddressLayout = Layouts.POINTER
    val WORD: ValueLayout.OfShort = ValueLayout.JAVA_SHORT
    val DWORD: ValueLayout.OfInt = Layouts.INT
    val LPDWORD: AddressLayout = Layouts.POINTER
    val CHAR: ValueLayout.OfByte = ValueLayout.JAVA_BYTE
    val WCHAR: ValueLayout.OfChar = ValueLayout.JAVA_CHAR
}


@Suppress("FunctionName", "ClassName", "unused", "PropertyName")
private class WinKernel32Library {

    class COORD(override val segment: MemorySegment) : StructAccessor {
        object Layout : StructLayout() {
            val X by shortField()
            val Y by shortField()
        }

        constructor(arena: Arena) : this(arena.allocate(Layout.layout))

        val X by Layout.X
        val Y by Layout.Y
    }

    class SMALL_RECT(override val segment: MemorySegment) : StructAccessor {
        object Layout : StructLayout() {
            val Left by shortField()
            val Top by shortField()
            val Right by shortField()
            val Bottom by shortField()
        }

        constructor(arena: Arena) : this(arena.allocate(Layout.layout))

        val left by Layout.Left
        val top by Layout.Top
        val right by Layout.Right
        val bottom by Layout.Bottom
    }

    class CONSOLE_SCREEN_BUFFER_INFO(override val segment: MemorySegment) : StructAccessor {
        object Layout : StructLayout() {
            val dwSize by structField(COORD.Layout, ::COORD)
            val dwCursorPosition by structField(COORD.Layout, ::COORD)
            val wAttributes by wordField()
            val srWindow by structField(SMALL_RECT.Layout, ::SMALL_RECT)
            val dwMaximumWindowSize by structField(COORD.Layout, ::COORD)
        }

        constructor(arena: Arena) : this(arena.allocate(Layout.layout))

        val dwSize by Layout.dwSize
        val dwCursorPosition by Layout.dwCursorPosition
        val wAttributes by Layout.wAttributes
        val srWindow by Layout.srWindow
        val dwMaximumWindowSize by Layout.dwMaximumWindowSize
    }

    class MOUSE_EVENT_RECORD(override val segment: MemorySegment) : StructAccessor {
        object Layout : StructLayout() {
            val dwMousePosition by structField(COORD.Layout, ::COORD)
            val dwButtonState by dwordField()
            val dwControlKeyState by dwordField()
            val dwEventFlags by dwordField()
        }

        constructor(arena: Arena) : this(arena.allocate(Layout.layout))

        val dwMousePosition by Layout.dwMousePosition
        val dwButtonState by Layout.dwButtonState
        val dwControlKeyState by Layout.dwControlKeyState
        val dwEventFlags by Layout.dwEventFlags
    }

    class KEY_EVENT_RECORD(override val segment: MemorySegment) : StructAccessor {
        object Layout : StructLayout() {
            val bKeyDown by winBoolField()
            val wRepeatCount by wordField()
            val wVirtualKeyCode by wordField()
            val wVirtualScanCode by wordField()
            val uChar by customField<Char>(
                MemoryLayout.unionLayout(
                    WinLayouts.WCHAR.withName("UnicodeChar"),
                    WinLayouts.CHAR.withName("AsciiChar")
                ).withName("uChar")
            ) { segment, parent ->
                parent.layout.varHandle("uChar", "UnicodeChar").get(segment) as Char
            }
            val dwControlKeyState by dwordField()
        }

        constructor(arena: Arena) : this(arena.allocate(Layout.layout))

        val bKeyDown by Layout.bKeyDown
        val wRepeatCount by Layout.wRepeatCount
        val wVirtualKeyCode by Layout.wVirtualKeyCode
        val wVirtualScanCode by Layout.wVirtualScanCode
        val uChar by Layout.uChar
        val dwControlKeyState by Layout.dwControlKeyState
    }

    class WINDOW_BUFFER_SIZE_RECORD(override val segment: MemorySegment) : StructAccessor {
        object Layout : StructLayout() {
            val dwSize by structField(COORD.Layout, ::COORD)
        }

        constructor(arena: Arena) : this(arena.allocate(Layout.layout))

        val dwSize by Layout.dwSize
    }

    class MENU_EVENT_RECORD(override val segment: MemorySegment) : StructAccessor {
        object Layout : StructLayout() {
            val dwCommandId by dwordField()
        }

        constructor(arena: Arena) : this(arena.allocate(Layout.layout))

        val dwCommandId by Layout.dwCommandId
    }

    class FOCUS_EVENT_RECORD(override val segment: MemorySegment) : StructAccessor {
        object Layout : StructLayout() {
            val bSetFocus by winBoolField()
        }

        constructor(arena: Arena) : this(arena.allocate(Layout.layout))

        val bSetFocus by Layout.bSetFocus
    }

    class INPUT_RECORD(override val segment: MemorySegment) : StructAccessor {
        companion object {
            const val KEY_EVENT: Short = 0x0001
            const val MOUSE_EVENT: Short = 0x0002
            const val WINDOW_BUFFER_SIZE_EVENT: Short = 0x0004
            const val MENU_EVENT: Short = 0x0008
            const val FOCUS_EVENT: Short = 0x0010
        }

        object Layout : StructLayout() {
            val EventType by wordField()
            val padding by paddingField(2) // needed for alignment
            val Event by customField<Any>(
                MemoryLayout.unionLayout(
                    KEY_EVENT_RECORD.Layout.layout.withName("KeyEvent"),
                    MOUSE_EVENT_RECORD.Layout.layout.withName("MouseEvent"),
                    WINDOW_BUFFER_SIZE_RECORD.Layout.layout.withName("WindowBufferSizeEvent"),
                    MENU_EVENT_RECORD.Layout.layout.withName("MenuEvent"),
                    FOCUS_EVENT_RECORD.Layout.layout.withName("FocusEvent")
                ).withName("Event")
            ) { segment, _ -> segment }
        }

        constructor(arena: Arena) : this(arena.allocate(Layout.layout))

        val EventType by Layout.EventType
        val Event by Layout.Event
        val keyEvent
            get() = KEY_EVENT_RECORD(
                segment.offsetOf(
                    "Event",
                    Layout.layout,
                    KEY_EVENT_RECORD.Layout.layout
                )
            )
        val mouseEvent
            get() = MOUSE_EVENT_RECORD(
                segment.offsetOf(
                    "Event",
                    Layout.layout,
                    MOUSE_EVENT_RECORD.Layout.layout
                )
            )
    }

    object MethodHandles : MethodHandlesHolder() {
        init {
            System.loadLibrary("kernel32")
            System.loadLibrary("msvcrt")
        }

        val WaitForSingleObject by handle(WinLayouts.DWORD, WinLayouts.HANDLE, WinLayouts.DWORD)
        val GetStdHandle by handle(WinLayouts.HANDLE, WinLayouts.DWORD)
        val GetConsoleMode by handle(Layouts.INT, WinLayouts.HANDLE, WinLayouts.LPDWORD)
        val GetConsoleScreenBufferInfo by handle(Layouts.INT, WinLayouts.HANDLE, Layouts.POINTER)
        val SetConsoleMode by handle(Layouts.INT, WinLayouts.HANDLE, WinLayouts.DWORD)
        val ReadConsoleInputW by handle(
            Layouts.INT, WinLayouts.HANDLE, Layouts.POINTER, WinLayouts.DWORD, WinLayouts.LPDWORD
        )
    }

    fun WaitForSingleObject(hHandle: MemorySegment, dwMilliseconds: Int): Int {
        return MethodHandles.WaitForSingleObject.invokeExact(hHandle, dwMilliseconds) as Int
    }

    fun GetStdHandle(nStdHandle: Int): MemorySegment {
        return MethodHandles.GetStdHandle.invokeExact(nStdHandle) as MemorySegment
    }

    fun GetConsoleMode(
        hConsoleHandle: MemorySegment, lpMode: MemorySegment,
    ): Boolean {
        return MethodHandles.GetConsoleMode.invokeExact(hConsoleHandle, lpMode) as Int != 0
    }

    fun GetConsoleScreenBufferInfo(
        hConsoleOutput: MemorySegment, lpConsoleScreenBufferInfo: CONSOLE_SCREEN_BUFFER_INFO,
    ): Boolean {
        return MethodHandles.GetConsoleScreenBufferInfo.invokeExact(
            hConsoleOutput, lpConsoleScreenBufferInfo.segment
        ) as Int != 0
    }

    fun SetConsoleMode(hConsoleHandle: MemorySegment, dwMode: Int): Boolean {
        return MethodHandles.SetConsoleMode.invokeExact(hConsoleHandle, dwMode) as Int != 0
    }

    fun ReadConsoleInputW(
        hConsoleOutput: MemorySegment,
        lpBuffer: MemorySegment,
        nLength: Int,
        lpNumberOfEventsRead: MemorySegment,
    ): Boolean {
        return MethodHandles.ReadConsoleInputW.invokeExact(
            hConsoleOutput, lpBuffer, nLength, lpNumberOfEventsRead
        ) as Int != 0
    }
}


internal class TerminalInterfaceFfmWindows : TerminalInterfaceWindows() {
    private companion object {
        const val STD_INPUT_HANDLE: Int = -10
        const val STD_OUTPUT_HANDLE: Int = -11
        const val STD_ERROR_HANDLE: Int = -12
    }

    private val kernel = WinKernel32Library()
    private val stdoutHandle get() = kernel.GetStdHandle(STD_OUTPUT_HANDLE)
    private val stdinHandle get() = kernel.GetStdHandle(STD_INPUT_HANDLE)
    private val stderrHandle get() = kernel.GetStdHandle(STD_ERROR_HANDLE)
    private fun handleInteractive(handle: MemorySegment): Boolean {
        return Arena.ofConfined().use { arena ->
            kernel.GetConsoleMode(handle, arena.allocateInt())
        }
    }

    override fun stdoutInteractive(): Boolean = handleInteractive(stdoutHandle)
    override fun stdinInteractive(): Boolean = handleInteractive(stdinHandle)

    override fun getTerminalSize(): Size? = Arena.ofConfined().use { arena ->
        val csbi = WinKernel32Library.CONSOLE_SCREEN_BUFFER_INFO(arena)
        if (!kernel.GetConsoleScreenBufferInfo(stdoutHandle, csbi)) {
            return null
        }
        return csbi.srWindow.run { Size(width = right - left + 1, height = bottom - top + 1) }
    }


    override fun getStdinConsoleMode(): UInt = Arena.ofConfined().use { arena ->
        val originalMode = arena.allocateInt()
        kernel.GetConsoleMode(stdinHandle, originalMode)
        return originalMode.get(Layouts.INT, 0).toUInt()
    }

    override fun setStdinConsoleMode(dwMode: UInt) {
        kernel.SetConsoleMode(stdinHandle, dwMode.toInt())
    }

    override fun readRawEvent(dwMilliseconds: Int): EventRecord? = Arena.ofConfined().use { arena ->
        val stdin = stdinHandle
        val waitResult = kernel.WaitForSingleObject(stdin, dwMilliseconds)
        if (waitResult != 0) {
            throw TimeoutException()
        }
        val inputEvents = arena.allocate(WinKernel32Library.INPUT_RECORD.Layout.layout, 1)
        val eventsReadSeg = arena.allocateInt()
        if (!kernel.ReadConsoleInputW(stdin, inputEvents, 1, eventsReadSeg)
            || eventsReadSeg.get(Layouts.INT, 0) != 1
        ) {
            throw RuntimeException("Error reading from console input")
        }
        val inputEvent = inputEvents.elements(WinKernel32Library.INPUT_RECORD.Layout.layout)
            .map(WinKernel32Library::INPUT_RECORD).toList().single()

        return when (inputEvent.EventType) {
            WinKernel32Library.INPUT_RECORD.KEY_EVENT -> {
                val keyEvent = inputEvent.keyEvent
                EventRecord.Key(
                    bKeyDown = keyEvent.bKeyDown,
                    wVirtualKeyCode = keyEvent.wVirtualKeyCode.toUShort(),
                    uChar = keyEvent.uChar,
                    dwControlKeyState = keyEvent.dwControlKeyState.toUInt(),
                )
            }

            WinKernel32Library.INPUT_RECORD.MOUSE_EVENT -> {
                val mouseEvent = inputEvent.mouseEvent
                EventRecord.Mouse(
                    dwMousePositionX = mouseEvent.dwMousePosition.X,
                    dwMousePositionY = mouseEvent.dwMousePosition.Y,
                    dwButtonState = mouseEvent.dwButtonState.toUInt(),
                    dwControlKeyState = mouseEvent.dwControlKeyState.toUInt(),
                    dwEventFlags = mouseEvent.dwEventFlags.toUInt(),
                )
            }

            else -> null // Ignore other event types like FOCUS_EVENT that we can't opt out of
        }
    }
}

internal fun StructLayout.wordField() = scalarField<Short>(WinLayouts.WORD)
internal fun StructLayout.dwordField() = scalarField<Int>(WinLayouts.DWORD)
internal fun StructLayout.winBoolField() = scalarField(Layouts.INT) { it != 0 }
