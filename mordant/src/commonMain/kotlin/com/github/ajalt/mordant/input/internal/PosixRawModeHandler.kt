@file:Suppress("SpellCheckingInspection")

package com.github.ajalt.mordant.input.internal

internal abstract class PosixRawModeHandler {
    protected class Termios(
        val iflag: UInt,
        val oflag: UInt,
        val cflag: UInt,
        val lflag: UInt,
        val cline: Byte,
        val cc: ByteArray,
        val ispeed: UInt,
        val ospeed: UInt,
    )

    protected abstract fun getStdinTermios(): Termios
    protected abstract fun setStdinTermios(termios: Termios)

    // https://www.man7.org/linux/man-pages/man3/termios.3.html
    // https://viewsourcecode.org/snaptoken/kilo/02.enteringRawMode.html
    fun enterRawMode(): AutoCloseable {
        val orig = getStdinTermios()
        val new = Termios(
            iflag = orig.iflag and (ICRNL or IGNCR or INPCK or ISTRIP or IXON).inv(),
            // we leave OPOST on so we don't change \r\n handling
            oflag = orig.oflag,
            cflag = orig.cflag or CS8,
            lflag = orig.lflag and (ECHO or ICANON or IEXTEN or ISIG).inv(),
            cline = orig.cline,
            cc = orig.cc.copyOf().also {
                it[VMIN] = 0 // min wait time on read
                it[VTIME] = 1 // max wait time on read, in 10ths of a second
            },
            ispeed = orig.ispeed,
            ospeed = orig.ospeed,
        )
        setStdinTermios(new)
        return AutoCloseable { setStdinTermios(orig) }
    }
}


private const val VINTR: Int = 0
private const val VQUIT: Int = 1
private const val VERASE: Int = 2
private const val VKILL: Int = 3
private const val VEOF: Int = 4
private const val VTIME: Int = 5
private const val VMIN: Int = 6
private const val VSWTC: Int = 7
private const val VSTART: Int = 8
private const val VSTOP: Int = 9
private const val VSUSP: Int = 10
private const val VEOL: Int = 11
private const val VREPRINT: Int = 12
private const val VDISCARD: Int = 13
private const val VWERASE: Int = 14
private const val VLNEXT: Int = 15
private const val VEOL2: Int = 16

private const val IGNBRK: UInt = 0x0000001u
private const val BRKINT: UInt = 0x0000002u
private const val IGNPAR: UInt = 0x0000004u
private const val PARMRK: UInt = 0x0000008u
private const val INPCK: UInt = 0x0000010u
private const val ISTRIP: UInt = 0x0000020u
private const val INLCR: UInt = 0x0000040u
private const val IGNCR: UInt = 0x0000080u
private const val ICRNL: UInt = 0x0000100u
private const val IUCLC: UInt = 0x0000200u
private const val IXON: UInt = 0x0000400u
private const val IXANY: UInt = 0x0000800u
private const val IXOFF: UInt = 0x0001000u
private const val IMAXBEL: UInt = 0x0002000u
private const val IUTF8: UInt = 0x0004000u

private const val OPOST: UInt = 0x0000001u
private const val OLCUC: UInt = 0x0000002u
private const val ONLCR: UInt = 0x0000004u
private const val OCRNL: UInt = 0x0000008u
private const val ONOCR: UInt = 0x0000010u
private const val ONLRET: UInt = 0x0000020u
private const val OFILL: UInt = 0x0000040u
private const val OFDEL: UInt = 0x0000080u
private const val NLDLY: UInt = 0x0000100u
private const val NL0: UInt = 0x0000000u
private const val NL1: UInt = 0x0000100u
private const val CRDLY: UInt = 0x0000600u
private const val CR0: UInt = 0x0000000u
private const val CR1: UInt = 0x0000200u
private const val CR2: UInt = 0x0000400u
private const val CR3: UInt = 0x0000600u
private const val TABDLY: UInt = 0x0001800u
private const val TAB0: UInt = 0x0000000u
private const val TAB1: UInt = 0x0000800u
private const val TAB2: UInt = 0x0001000u
private const val TAB3: UInt = 0x0001800u
private const val XTABS: UInt = 0x0001800u
private const val BSDLY: UInt = 0x0002000u
private const val BS0: UInt = 0x0000000u
private const val BS1: UInt = 0x0002000u
private const val VTDLY: UInt = 0x0004000u
private const val VT0: UInt = 0x0000000u
private const val VT1: UInt = 0x0004000u
private const val FFDLY: UInt = 0x0008000u
private const val FF0: UInt = 0x0000000u
private const val FF1: UInt = 0x0008000u

private const val CBAUD: UInt = 0x000100fu
private const val B0: UInt = 0x0000000u
private const val B50: UInt = 0x0000001u
private const val B75: UInt = 0x0000002u
private const val B110: UInt = 0x0000003u
private const val B134: UInt = 0x0000004u
private const val B150: UInt = 0x0000005u
private const val B200: UInt = 0x0000006u
private const val B300: UInt = 0x0000007u
private const val B600: UInt = 0x0000008u
private const val B1200: UInt = 0x0000009u
private const val B1800: UInt = 0x000000au
private const val B2400: UInt = 0x000000bu
private const val B4800: UInt = 0x000000cu
private const val B9600: UInt = 0x000000du
private const val B19200: UInt = 0x000000eu
private const val B38400: UInt = 0x000000fu
private const val EXTA: UInt = B19200
private const val EXTB: UInt = B38400
private const val CSIZE: UInt = 0x0000030u
private const val CS5: UInt = 0x0000000u
private const val CS6: UInt = 0x0000010u
private const val CS7: UInt = 0x0000020u
private const val CS8: UInt = 0x0000030u
private const val CSTOPB: UInt = 0x0000040u
private const val CREAD: UInt = 0x0000080u
private const val PARENB: UInt = 0x0000100u
private const val PARODD: UInt = 0x0000200u
private const val HUPCL: UInt = 0x0000400u
private const val CLOCAL: UInt = 0x0000800u

private const val ISIG: UInt = 0x0000001u
private const val ICANON: UInt = 0x0000002u
private const val XCASE: UInt = 0x0000004u
private const val ECHO: UInt = 0x0000008u
private const val ECHOE: UInt = 0x0000010u
private const val ECHOK: UInt = 0x0000020u
private const val ECHONL: UInt = 0x0000040u
private const val NOFLSH: UInt = 0x0000080u
private const val TOSTOP: UInt = 0x0000100u
private const val ECHOCTL: UInt = 0x0000200u
private const val ECHOPRT: UInt = 0x0000400u
private const val ECHOKE: UInt = 0x0000800u
private const val FLUSHO: UInt = 0x0001000u
private const val PENDIN: UInt = 0x0002000u
private const val IEXTEN: UInt = 0x0008000u
private const val EXTPROC: UInt = 0x0010000u

private const val TCSANOW: UInt = 0x0u
private const val TCSADRAIN: UInt = 0x1u
private const val TCSAFLUSH: UInt = 0x2u

