# Progress Bars

Mordant provides a simple way to create animated progress bars in your terminal.

# Basic Usage

You can use the [progressBarLayout] DSL to define the layout of your progress bar. Then you
can start the animation either on a thread with [animateOnThread], or
using coroutines with [animateOnCoroutine].`animateOnThread` is JVM-only, but `animateOnCoroutine`
is available on all platforms using the `mordant-coroutines` module.

Once the animation is started, you can update the progress bar by calling [update] and [advance].

=== "Example with Coroutines"

    ```kotlin
    val progress = progressBarLayout {
        marquee(terminal.theme.warning("my-file-download.bin"), width = 15)
        percentage()
        progressBar()
        completed(style = terminal.theme.success)
        speed("B/s", style = terminal.theme.info)
        timeRemaining(style = magenta)
    }.animateInCoroutine(terminal)

    launch { progress.execute() }

    // Update the progress as the download progresses
    progress.update { total = 3_000_000_000 }
    while (!progress.finished) {
        progress.advance(15_000_000)
        Thread.sleep(100)
    }
    ```

=== "Example with Threads"

    ```kotlin
    val progress = progressBarLayout {
        marquee(terminal.theme.warning("my-file-download.bin"), width = 15)
        percentage()
        progressBar()
        completed(style = terminal.theme.success)
        speed("B/s", style = terminal.theme.info)
        timeRemaining(style = magenta)
    }.animateOnThread(terminal)
    
    val future = progress.execute()
    
    // Update the progress as the download progresses
    progress.update { total = 3_000_000_000 }
    while (!progress.finished) {
        progress.advance(15_000_000)
        Thread.sleep(100)
    }
    
    // Optional: wait for the future to complete so that the final frame of the
    // animation is rendered before the program exits.
    future.get()
    ```

=== "Output"

    ![](img/progess_simple.gif)

# Changing Text While Animation is Running

You can pass data to the progress bar by using [progressBarContextLayout], which allows you to
set a [context][ProgressTaskUpdateScope.context] value that your progress bar can use to render
dynamic text.

=== "Example with Context"

    ```kotlin
    val progress = progressBarContextLayout<String> {
        text { "Status: $context" }
        progressBar()
        completed()
    }.animateInCoroutine(terminal, context = "Starting", total = 4, completed = 1)

    launch { progress.execute() }

    val states = listOf("Downloading", "Extracting", "Done")
    for (state in states) {
        delay(2.seconds)
        progress.update {
            context = state
            completed += 1
        }
    }
    ```

=== "Output"

    ![](img/progress_context.gif)

# Multiple Progress Bars

You can create multiple progress bars running at the same time using [MultiProgressBarAnimation].
Call [addTask] for each progress bar you want, passing in the layout for that bar. You can 
use the same layout for multiple tasks, or different layouts for some of them.

You can call [advance] and [update] on each task to update them separately.

The columns of the progress bars will have their widths aligned to the same size by default,
but you can change this by setting the `alignColumns` parameter in the layout.

=== "Example with Multiple Progress Bars"

    ```kotlin
    val overallLayout = progressBarLayout(alignColumns = false) {
        progressBar(width = 20)
        percentage()
        timeElapsed(compact = false)
    }
    val taskLayout = progressBarContextLayout<Int> {
        text(fps = animationFps, align = TextAlign.LEFT) { "〉 step $context" }
    }

    val progress = MultiProgressBarAnimation(terminal).animateInCoroutine()
    val overall = progress.addTask(overallLayout, total = 100)
    val tasks = List(3) { progress.addTask(taskLayout, total = 1, completed = 1, context = 0) }

    launch { progress.execute() }

    for (i in 1..100) {
        overall.advance()
        tasks[i % 3].update { context = i }
        delay(100)
    }
    ```

=== "Output"

    ![](img/progress_multi.gif)

!!! tip

    The progress animation will keep running until all tasks are [finished]. If you want to stop sooner,
    you can set all the tasks' `completed` equal to their `total`, or cancel the coroutine scope or
    future that the animation is running in.


[ProgressTaskUpdateScope.context]: api/mordant/com.github.ajalt.mordant.animation.progress/-progress-task-update-scope/context.html
[advance]:                         api/mordant/com.github.ajalt.mordant.animation.progress/advance.html
[finished]:                        api/mordant/com.github.ajalt.mordant.animation.progress/-progress-task/finished.html
[progressBarContextLayout]:        api/mordant/com.github.ajalt.mordant.widgets.progress/progress-bar-context-layout.html
[progressBarLayout]:               api/mordant/com.github.ajalt.mordant.widgets.progress/progress-bar-layout.html
[update]:                          api/mordant/com.github.ajalt.mordant.animation.progress/update.html
[MultiProgressBarAnimation]:       api/mordant/com.github.ajalt.mordant.animation.progress/-multi-progress-bar-animation/index.html
[addTask]:                         api/mordant/com.github.ajalt.mordant.animation.progress/-progress-bar-animation/add-task.html
