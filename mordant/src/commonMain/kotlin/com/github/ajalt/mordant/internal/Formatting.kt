package com.github.ajalt.mordant.internal

import kotlin.math.pow

private const val SI_PREFIXES = "KMGTEPZY"

/**
 * Return a list of all numbers in [nums] formatted as a string, and the unit they were reduced with.
 *
 * All numbers will be formatted to the same unit.
 *
 * @param precision The number of decimal places to include in the formatted numbers
 * @param nums The numbers to format
 */
internal fun formatMultipleWithSiSuffixes(
    precision: Int, truncateDecimals: Boolean, vararg nums: Double,
): Pair<List<String>, String> {
    require(precision >= 0) { "precision must be >= 0" }
    val largest = nums.max()
    var divisor = 1
    var prefix = ""
    for (s in SI_PREFIXES) {
        if (largest / divisor < 1000) break
        divisor *= 1000
        prefix = s.toString()
    }

    val exp = 10.0.pow(precision)
    val formatted = nums.map {
        val n = it / divisor
        val i = n.toInt()
        val d = ((n - i) * exp).toInt()
        when {
            truncateDecimals && (precision == 0 || divisor == 1 && d == 0) -> i.toString()
            else -> "$i.${d.toString().padEnd(precision, '0')}"
        }
    }
    return formatted to prefix
}

/** Return this number formatted as a string, suffixed with its SI unit */
internal fun Double.formatWithSiSuffix(precision: Int): String {
    return formatMultipleWithSiSuffixes(precision, false, this).let { it.first.first() + it.second }
}

/** Return the number of seconds represented by [nanos] as a `Double` */
internal fun nanosToSeconds(nanos: Double): Double = nanos / 1_000_000_000

/** Return the number of seconds represented by [nanos] as a `Double` */
internal fun nanosToSeconds(nanos: Long): Double = nanosToSeconds(nanos.toDouble())
