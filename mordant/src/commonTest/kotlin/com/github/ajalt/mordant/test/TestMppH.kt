package com.github.ajalt.mordant.test

// https://jakewharton.com/litmus-testing-kotlins-many-memory-models/
expect fun threadedTest(body: () -> Unit)
