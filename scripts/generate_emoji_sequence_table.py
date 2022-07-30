import re

import requests

emoji_zwj_url = "https://unicode.org/Public/emoji/latest/emoji-zwj-sequences.txt"
emoji_seq_url = "https://unicode.org/Public/emoji/latest/emoji-sequences.txt"


def _parse_file(url: str, type_to_emit: str) -> list[tuple[list[int], str]]:
    """Download and parse a Unicode Emoji Sequences document

    Return tuples of ([code points], comment) for each sequence.
    """
    sequences = []
    text = requests.get(url).text
    for line in text.splitlines():
        m = re.fullmatch(r"(\d.+)\s+; (\w+)\s+;(.+)\s+# [^(]+\((.+)\)", line)
        if not m:
            continue
        (seq, typ, comment, emoji) = m.groups()
        if typ != type_to_emit:
            continue
        sequences.append(
            ([int(s, 16) for s in seq.split()], f"{comment.strip()} ({emoji.strip()})")
        )
    return sequences


def parse_zwj_sequences() -> list[tuple[list[int], str]]:
    """Parse the Emoji Sequences document and emit and sequences tagged as RGI_Emoji_Modifier_Sequence"""
    # example lines:
    # 1F9DD 200D 2642 FE0F        ; RGI_Emoji_ZWJ_Sequence  ; man elf                     # E5.0   [1] (🧝‍♂️)
    # 1F9DD 1F3FB 200D 2640 FE0F  ; RGI_Emoji_ZWJ_Sequence  ; woman elf: light skin tone  # E5.0   [1] (🧝🏻‍♀️)
    # 1F468 200D 1F466            ; RGI_Emoji_ZWJ_Sequence  ; family: man, boy            # E4.0   [1] (👨‍👦)
    # 1F468 200D 1F466 200D 1F466 ; RGI_Emoji_ZWJ_Sequence  ; family: man, boy, boy       # E4.0   [1] (👨‍👦‍👦)
    return _parse_file(emoji_zwj_url, "RGI_Emoji_ZWJ_Sequence")


def parse_general_sequences() -> list[tuple[list[int], str]]:
    """Parse the Emoji Sequences document and emit and sequences tagged as RGI_Emoji_Modifier_Sequence"""
    # example lines:
    # 2668 FE0F     ; Basic_Emoji                  ; hot springs                          # E0.6   [1] (♨️)
    # 1F6B3..1F6B5  ; Basic_Emoji                  ; no bicycles                          # E1.0   [3] (🚳..🚵)
    # 1F1FF 1F1FC   ; RGI_Emoji_Flag_Sequence      ; flag: Zimbabwe                       # E2.0   [1] (🇿🇼)
    # 261D 1F3FB    ; RGI_Emoji_Modifier_Sequence  ; index pointing up: light skin tone   # E1.0   [1] (☝🏻)
    return _parse_file(emoji_seq_url, "RGI_Emoji_Modifier_Sequence")


def main():
    seqs = sorted(parse_zwj_sequences() + parse_general_sequences())
    # Most emoji sequences start with codepoints in the supplementary plane, but a few of the skin tone sequences start
    # with older BMP codepoints.
    starts = [p[0][0] for p in seqs]
    r1 = [s for s in starts if s < 0x1F000]
    r2 = [s for s in starts if s >= 0x1F000]
    print(
        """package com.github.ajalt.mordant.internal.gen

import kotlin.native.concurrent.SharedImmutable

internal fun couldStartEmojiSeq(codepoint: Int): Boolean {"""
    )

    print(
        f"    return codepoint in {hex(min(r1))}..{hex(max(r1))} || codepoint in {hex(min(r2))}..{hex(max(r2))}"
    )

    print(
        """}

internal data class IntTrie(val children: MutableMap<Int, IntTrie>, val values: MutableSet<Int> = mutableSetOf()) {
    constructor(vararg children: Pair<Int, IntTrie>, values: MutableSet<Int> = mutableSetOf())
            : this(mutableMapOf(*children), values)
}

@SharedImmutable
internal val EMOJI_SEQUENCES: IntTrie = buildSeqTrie()

private fun buildSeqTrie(): IntTrie {
    val sequences = arrayOf("""
    )
    for s in seqs:
        print(f'        intArrayOf({", ".join(hex(it) for it in s[0])}), // {s[1]}')

    print(
        """    )

    val root = IntTrie()
    for (seq in sequences) {
        var node = root
        for (i in 0 until seq.lastIndex) {
            node = node.children.getOrPut(seq[i]) { IntTrie() }
        }
        node.values += seq.last()
    }
    return root
}"""
    )


if __name__ == "__main__":
    main()
