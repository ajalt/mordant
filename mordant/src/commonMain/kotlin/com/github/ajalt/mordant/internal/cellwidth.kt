package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.internal.gen.CELL_WIDTH_TABLE
import com.github.ajalt.mordant.internal.gen.EMOJI_SEQUENCES
import com.github.ajalt.mordant.internal.gen.couldStartEmojiSeq


/*
 * This implementation uses a binary search of a lookup table, similar to Markus Kuhn's classic C
 * implementation of wcwidth. This function differs from his in a few ways. We generate the lookup
 * table from the latest unicode standard. wcwidth effectively requires two codespace searches for
 * each codepoint. We perform at most a single binary search, and for ASCII characters, we don't
 * perform any search at all. wcwidth also returns -1 for most control codes, which is wrong for all
 * the use cases you'd use wcwidth for. A BEL character does not suddenly make your line of text
 * shorter. We return 0 for control codes other than DEL and BS.
 */

/**
 * Return the width, in terminal cells, of the given unicode [codepoint].
 */
internal fun cellWidth(codepoint: Int): Int {
    if (codepoint in 0x20..0x7e) return 1 // fast path for printable ASCII
    if (codepoint == 0x08 || codepoint == 0x7f) return -1 // DEL and BS

    val table = CELL_WIDTH_TABLE
    var min = 0
    var mid: Int
    var max = table.lastIndex

    if (codepoint < table[0].low || codepoint > table[max].high) return 1

    while (max >= min) {
        mid = (min + max) / 2
        val entry = table[mid]
        when {
            codepoint > entry.high -> min = mid + 1
            codepoint < entry.low -> max = mid - 1
            else -> return table[mid].width.toInt()
        }
    }

    return 1
}

/** Return the width, in terminal cells, of the given [string]*/
internal fun stringCellWidth(string: String): Int {
    val input = codepointSequence(string).iterator()
    val pending = ArrayDeque<Int>()
    var sum = 0

    while (pending.isNotEmpty() || input.hasNext()) {
        val first = if (pending.isEmpty()) input.next() else pending.removeFirst()
        var node = if (couldStartEmojiSeq(first)) EMOJI_SEQUENCES.children[first] else null
        if (node == null) {
            sum += cellWidth(first)
            continue
        }

        // A complete sequence can also be the prefix of a longer one. Keep the lookahead
        // after the longest match so that it can start another sequence.
        var length = 0
        var matched = 0
        while (node != null) {
            if (length == pending.size) {
                if (!input.hasNext()) break
                pending.addLast(input.next())
            }
            val next = pending[length++]
            if (next in node.values || (length == 1 && next == 0xfe0f)) matched = length
            node = node.children[next]
        }

        repeat(matched) { pending.removeFirst() }
        sum += if (matched > 0) 2 else cellWidth(first)
    }
    return sum
}
