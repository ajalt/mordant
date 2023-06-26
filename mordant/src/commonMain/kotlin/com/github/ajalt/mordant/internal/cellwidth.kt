package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.internal.gen.CELL_WIDTH_TABLE
import com.github.ajalt.mordant.internal.gen.EMOJI_SEQUENCES
import com.github.ajalt.mordant.internal.gen.IntTrie
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
    var sum = 0
    var sumSinceZwj = 0
    var zwjSeq: IntTrie? = null
    for (codepoint in codepointSequence(string)) {
        val width = cellWidth(codepoint)
        if (zwjSeq != null) {
            sumSinceZwj += width
            if (codepoint in zwjSeq.values) {
                sumSinceZwj = 0
            }
            zwjSeq = zwjSeq.children[codepoint]
            if (zwjSeq == null) {
                // all ZWJ sequences combine to one glyph, which is always an emoji, so add 2 for the width of the
                // emoji, plus the width of any codepoints since the end of the last complete sequence. Unfortunately,
                // some of these emoji are wider than two cells, but given that their size is font-dependant and usually
                // not cell-aligned anyway, there's no perfect solution here. Thanks, unicode.
                sum += sumSinceZwj + 2
                sumSinceZwj = 0
            } else {
                sumSinceZwj += width
            }
        } else {
            // We do a fast range check to skip ZWJ sequence processing for most codepoints
            if (couldStartEmojiSeq(codepoint)) {
                zwjSeq = EMOJI_SEQUENCES.children[codepoint]
            }
            if (zwjSeq == null) {
                sum += width
            }
        }
    }
    // If we were in a zwj sequence at the end of the string, add whatever was left to the sum
    return sum + sumSinceZwj

}
