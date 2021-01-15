package com.github.ajalt.mordant.internal

import java.util.concurrent.TimeUnit

private const val SI_PREFIXES = "KMGTEPZY"

/**
 * Return a list of all numbers in [nums] formatted as a string, and the unit they were reduced with.
 */
internal fun formatMultipleWithSiSuffixes(decimals: Int, vararg nums: Double): Pair<List<String>, String> {
    var n = nums.maxOrNull()!!
    var suffix = ""
    for (c in SI_PREFIXES) {
        if (n < 1000) {
            break
        }
        n /= 1000
        for (i in nums.indices) {
            nums[i] = nums[i] / 1000
        }
        suffix = c.toString()
    }
    return nums.map { num ->
        val s = num.toString()
        val len = s.indexOf('.').let { if (it >= 0) it + decimals + 1 else s.length }
        s.take(len)
    } to suffix
}

/** Return this number formatted as a string, suffixed with its SI unit */
internal fun Double.formatWithSiSuffix(decimals: Int): String {
    return formatMultipleWithSiSuffixes(decimals, this).let { it.first.first() + it.second }
}

/** Return the number of seconds represented by [nanos] as a `Double` */
internal fun nanosToSeconds(nanos: Double): Double = nanos / 1_000_000_000

/** Return the number of seconds represented by [nanos] as a `Double` */
internal fun nanosToSeconds(nanos: Long): Double = nanosToSeconds(nanos.toDouble())
