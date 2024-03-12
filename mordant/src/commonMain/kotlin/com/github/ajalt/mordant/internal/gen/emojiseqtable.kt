package com.github.ajalt.mordant.internal.gen

internal fun couldStartEmojiSeq(codepoint: Int): Boolean {
    return codepoint in 0x261d..0x2764 || codepoint in 0x1f344..0x1faf8
}

internal data class IntTrie(
    val children: MutableMap<Int, IntTrie>,
    val values: MutableSet<Int> = mutableSetOf(),
) {
    constructor(vararg children: Pair<Int, IntTrie>, values: MutableSet<Int> = mutableSetOf())
            : this(mutableMapOf(*children), values)
}

internal val EMOJI_SEQUENCES: IntTrie = buildSeqTrie()

private fun buildSeqTrie(): IntTrie {
    val root = IntTrie()
    for (sequences in arrayOf(sequences1(), sequences2(), sequences3(), sequences4())) {
        for (seq in sequences) {
            var node = root
            for (i in 0..<seq.lastIndex) {
                node = node.children.getOrPut(seq[i]) { IntTrie() }
            }
            node.values += seq.last()
        }
    }
    return root
}
