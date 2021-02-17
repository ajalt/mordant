package com.github.ajalt.mordant.test

import kotlin.native.concurrent.TransferMode.SAFE
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze

actual fun threadedTest(body: () -> Unit) {
    // Run once on the main thread
    body()

    // Run again on a background thread
    body.freeze()
    val worker = Worker.start()
    val future = worker.execute(SAFE, { body }) {
        runCatching(it)
    }
    future.result.getOrThrow()
}
