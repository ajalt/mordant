package com.github.ajalt.mordant.internal.nativeimage

import com.github.ajalt.mordant.internal.Size
import org.graalvm.nativeimage.Platform
import org.graalvm.nativeimage.Platforms
import org.graalvm.nativeimage.StackValue
import org.graalvm.nativeimage.c.CContext
import org.graalvm.nativeimage.c.constant.CConstant
import org.graalvm.nativeimage.c.function.CFunction
import org.graalvm.nativeimage.c.struct.CField
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

    @CFunction("GetConsoleScreenBufferInfo")
    external fun GetConsoleScreenBufferInfo(
        hConsoleOutput: PointerBase?,
        lpConsoleScreenBufferInfo: Long,
    ): Boolean

}

//@Platforms(Platform.WINDOWS::class)
//internal class NativeImageWin32MppImpls : MppImpls {
//
//    override fun stdoutInteractive(): Boolean {
//        val handle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_OUTPUT_HANDLE())
//        return WinKernel32Lib.GetConsoleMode(handle, StackValue.get(CIntPointer::class.java))
//    }
//
//    override fun stderrInteractive(): Boolean {
//        val handle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_ERROR_HANDLE())
//        return WinKernel32Lib.GetConsoleMode(handle, StackValue.get(CIntPointer::class.java))
//    }
//
//    override fun stdinInteractive(): Boolean {
//        val handle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_INPUT_HANDLE())
//        return WinKernel32Lib.GetConsoleMode(handle, StackValue.get(CIntPointer::class.java))
//    }
//
//    override fun getTerminalSize(): Size? {
//        val csbi = StackValue.get(WinKernel32Lib.CONSOLE_SCREEN_BUFFER_INFO::class.java)
//        val handle = WinKernel32Lib.GetStdHandle(WinKernel32Lib.STD_OUTPUT_HANDLE())
//        return if (!WinKernel32Lib.GetConsoleScreenBufferInfo(handle, csbi.rawValue())) {
//            null
//        } else {
//            Size(width = csbi.Right - csbi.Left + 1, height = csbi.Bottom - csbi.Top + 1)
//        }
//    }
//
//}
