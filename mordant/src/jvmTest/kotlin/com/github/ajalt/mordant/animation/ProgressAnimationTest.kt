//package com.github.ajalt.mordant.animation
//
//import com.github.ajalt.mordant.internal.CSI
//import com.github.ajalt.mordant.rendering.Theme
//import com.github.ajalt.mordant.terminal.Terminal
//import com.github.ajalt.mordant.terminal.TerminalRecorder
//import com.github.ajalt.mordant.test.RenderingTest
//import com.github.ajalt.mordant.widgets.*
//import io.kotest.matchers.shouldBe
//import java.util.concurrent.*
//import kotlin.test.Test
//import kotlin.time.Duration.Companion.seconds
//import kotlin.time.TestTimeSource
//
//class ProgressAnimationTest : RenderingTest() {
//    //    @Test
////    fun throttling() {
////        val vt = TerminalRecorder()
////        val t = Terminal(terminalInterface = vt)
////        val pt = t.progressAnimation {
////            timeSource = { (now * TimeUnit.SECONDS.toNanos(1)).toLong() }
////            textFrameRate = 1
////            padding = 0
////            speed()
////            text("|")
////            timeRemaining()
////        }
////
////        pt.update(0, 1000)
////        now = 0.5
////        vt.clearOutput()
////        pt.update(40)
////        vt.normalizedBuffer() shouldBe " ---.-it/s|eta -:--:--"
////
////        now = 0.6
////        vt.clearOutput()
////        pt.update()
////        vt.normalizedBuffer() shouldBe " ---.-it/s|eta -:--:--"
////
////        now = 1.0
////        vt.clearOutput()
////        pt.update()
////        vt.normalizedBuffer() shouldBe "  40.0it/s|eta 0:00:24"
////
////        now = 1.9
////        vt.clearOutput()
////        pt.update()
////        vt.normalizedBuffer() shouldBe "  40.0it/s|eta 0:00:24"
////    }
////
//    @Test
//    fun animation() {
//        val vt = TerminalRecorder(width = 56)
//        val t = Terminal(
//            theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
//            terminalInterface = vt
//        )
//
//        val now = TestTimeSource()
//        val e = TestExecutor()
//        val p = progressBarLayout(spacing = 0) {
//            text("text.txt")
//            text("|")
//            percentage()
//            text("|")
//            progressBar()
//            text("|")
//            completed()
//            text("|")
//            speed()
//            text("|")
//            timeRemaining()
//        }.animateOnExecutor(t, e, now)
//        val pt = p.addTask(100, 0)
//        p.start()
//        vt.normalizedBuffer() shouldBe "text.txt|  0%|......|   0.0/100.0| ---.-it/s|eta -:--:--"
//
//        now += 10.seconds
//        vt.clearOutput()
//        pt.update { completed = 40 }
//        vt.normalizedBuffer() shouldBe "text.txt| 40%|##>...|  40.0/100.0|   4.0it/s|eta 0:00:15"
//
//        now += 10.seconds
//        vt.clearOutput()
//        pt.update()
//        vt.normalizedBuffer() shouldBe "text.txt| 40%|##>...|  40.0/100.0|   2.0it/s|eta 0:00:30"
//
//        now += 10.seconds
//        vt.clearOutput()
//        pt.updateTotal(200)
//        vt.normalizedBuffer() shouldBe "text.txt| 20%|#>....|  40.0/200.0|   1.3it/s|eta 0:02:00"
//
//        vt.clearOutput()
//        pt.restart()
//        vt.normalizedBuffer() shouldBe "text.txt|  0%|......|   0.0/200.0| ---.-it/s|eta -:--:--"
//
//        vt.clearOutput()
//        pt.clear()
//        vt.normalizedBuffer() shouldBe ""
//    }
//
//    private fun TerminalRecorder.normalizedBuffer(): String {
//        return output().substringAfter("${CSI}0J").substringAfter("${CSI}1A").trimEnd()
//    }
//}
//
//
//private class TestExecutor : ScheduledExecutorService {
//    private var pending: ScheduledFuture<*>? = null
//
//    fun run() {
//        pending?.get()
//        pending = null
//    }
//
//    override fun execute(command: Runnable) = error("not implemented")
//    override fun shutdown() {}
//    override fun shutdownNow(): MutableList<Runnable> = error("not implemented")
//    override fun isShutdown(): Boolean = error("not implemented")
//    override fun isTerminated(): Boolean = error("not implemented")
//    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = error("not implemented")
//    override fun <T : Any?> submit(task: Callable<T>): Future<T> = error("not implemented")
//    override fun <T : Any?> submit(task: Runnable, result: T): Future<T> = error("not implemented")
//    override fun submit(task: Runnable): Future<*> = error("not implemented")
//    override fun <T : Any?> invokeAll(tasks: MutableCollection<out Callable<T>>): MutableList<Future<T>> {
//        error("not implemented")
//    }
//
//    override fun <T : Any?> invokeAll(
//        tasks: MutableCollection<out Callable<T>>,
//        timeout: Long,
//        unit: TimeUnit,
//    ): MutableList<Future<T>> = error("not implemented")
//
//    @Suppress("WRONG_NULLABILITY_FOR_JAVA_OVERRIDE") // false positive
//    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>): T =
//        error("not implemented")
//
//    override fun <T : Any?> invokeAny(
//        tasks: MutableCollection<out Callable<T>>,
//        timeout: Long,
//        unit: TimeUnit,
//    ): T = error("not implemented")
//
//    override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*> {
//        return object : ScheduledFuture<Unit> {
//            private var done = false
//            override fun compareTo(other: Delayed?): Int = error("not implemented")
//            override fun getDelay(unit: TimeUnit): Long = error("not implemented")
//            override fun cancel(mayInterruptIfRunning: Boolean): Boolean = error("not implemented")
//            override fun isCancelled(): Boolean = error("not implemented")
//            override fun isDone(): Boolean = done
//            override fun get(timeout: Long, unit: TimeUnit) = error("not implemented")
//            override fun get() {
//                done = true
//                return command.run()
//            }
//        }.also { pending = it }
//    }
//
//    override fun <V : Any?> schedule(
//        callable: Callable<V>,
//        delay: Long,
//        unit: TimeUnit,
//    ): ScheduledFuture<V> = TODO()
//
//    override fun scheduleAtFixedRate(
//        command: Runnable,
//        initialDelay: Long,
//        period: Long,
//        unit: TimeUnit,
//    ): ScheduledFuture<*> = TODO()
//
//    override fun scheduleWithFixedDelay(
//        command: Runnable,
//        initialDelay: Long,
//        delay: Long,
//        unit: TimeUnit,
//    ): ScheduledFuture<*> = TODO()
//}
