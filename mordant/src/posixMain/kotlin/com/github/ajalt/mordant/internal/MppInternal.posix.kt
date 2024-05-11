package com.github.ajalt.mordant.internal

import kotlinx.cinterop.*
import platform.posix.getcwd
import platform.posix.size_t

@OptIn(ExperimentalForeignApi::class)
internal actual fun cwd(): String = memScoped{
    val buf = allocArray<ByteVar>(4096)
    return getcwd(buf, 4096UL)?.toKString() ?: "??"
}
