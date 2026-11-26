import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalInterfaceProvider
import com.github.ajalt.mordant.terminal.terminalinterface.ffm.TerminalInterfaceProviderFfm
import java.util.ServiceLoader

fun main(args: Array<String>) {
    val expectFfm = "--expect-ffm" in args
    val provider = ServiceLoader.load(TerminalInterfaceProvider::class.java)
        .filterIsInstance<TerminalInterfaceProviderFfm>().single()
    val selected = Terminal().terminalInterface
    val ffm = provider.load()
    check((ffm != null) == expectFfm) { "Unexpected FFM provider result: $ffm" }
    if (ffm != null) check(selected.javaClass == ffm.javaClass)
    println("FFM available: ${ffm != null}; Terminal selected ${selected.javaClass.simpleName}")
}
