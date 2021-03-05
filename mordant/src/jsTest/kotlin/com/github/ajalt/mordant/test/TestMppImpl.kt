package com.github.ajalt.mordant.test

actual inline fun threadedTest(body: () -> Unit) = body()
