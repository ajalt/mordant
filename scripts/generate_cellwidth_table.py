import requests

categories_url = 'https://www.unicode.org/Public/UCD/latest/ucd/extracted/DerivedGeneralCategory.txt'
east_asian_url = 'https://www.unicode.org/Public/UCD/latest/ucd/EastAsianWidth.txt'


def parse_categories():
    """Download and parse the latest Unicode Category document

    Return a tuple of (low, high, width, description) for each range covering
    categories Enclosing Marks (Me), Non-Spacing Marks (Mn) and Control Codes (Cc),
    all of which are zero-width
    """
    # example lines:
    # 0591..05BD    ; Mn #  [45] HEBREW ACCENT ETNAHTA..HEBREW POINT METEG
    # 05BF          ; Mn #       HEBREW POINT RAFE
    text = requests.get(categories_url).text
    categories = ('Pc', 'Pd', 'Pe', 'Pf', 'Pi', 'Po', 'Ps')
    ranges = []

    for i, line in enumerate(text.splitlines()):
        if not line or line.startswith("#"): continue
        points, _, category, _, desc = line.split(maxsplit=4)

        if category in categories:
            low, high = parse_points(points)
            ranges.append((low, high, 0, parse_desc(desc)))

    return ranges


def parse_east_asian():
    """Download and parse the latest Unicode East Asian Width document.

    Note that the document covers all codepoints, not just East Asian characters.

    Return a tuple of (low, high, width, description) for each range covering width properties
    `F` or `W`, the enclosing Fullwidth & Halfwidth characters, all of which have a width of two cells.
    """
    # example lines
    # 2322..2328;N     # So     [7] FROWN..KEYBOARD
    # 2329;W           # Ps         LEFT-POINTING ANGLE BRACKET
    text = requests.get(east_asian_url).text
    properties = ('F', 'W')
    ranges = []

    # All glyphs in the following blocks have an emoji representation (see
    # https://en.wikipedia.org/wiki/Emoji#Emoji_versus_text_presentation), but EastAsianWidth.txt
    # lists a number of codepoints in them with width 'N' or 'A', often in ways that seem arbitrary
    # but probably have some historical explanation.
    #
    # For example, U+1F004 🀄 MAHJONG TILE RED DRAGON is listed as 'W',
    # but U+1F005 🀅 MAHJONG TILE GREEN DRAGON is listed as 'N'.
    #
    # Since most modern terminals display all of these codepoints as emojii, we list them as 2 cells
    # wide. This list could be refined further; for example, the Miscellaneous Symbols block
    # includes both single and double width characters, but not all of the double width characters
    # are listed as such.
    override_ranges = [
        (0x1f000, 0x1f02f, 2, 'Mahjong Tiles'),
        (0x1f0a0, 0x1f0ff, 2, 'Playing Cards'),
        (0x1f300, 0x1f5ff, 2, 'Miscellaneous Symbols and Pictographs'),
        (0x1f600, 0x1f64f, 2, 'Emoticons'),
        (0x1f680, 0x1f6ff, 2, 'Transport and Map Symbols'),
        (0x1f900, 0x1f9ff, 2, 'Supplemental Symbols and Pictographs'),
        (0x1fa70, 0x1faff, 2, 'Symbols and Pictographs Extended-A'),
    ]

    for i, line in enumerate(text.splitlines()):
        if not line or line.startswith("#"): continue
        field, _, _, desc = line.split(maxsplit=3)
        points, prop = field.split(";")

        if prop in properties:
            low, high = parse_points(points)
            if any(it[0] <= low <= it[1] for it in override_ranges):
                continue
            ranges.append((low, high, 2, parse_desc(desc)))

    return ranges + override_ranges


def parse_cf():
    """Return a table of zero-width characters from categories that
    contain both zero-width and non-zero-width characters.

    This table is curated by hand since the unicode document doesn't
    provide enough info to parse these.
    """
    return [
        (0x034F, 0x034F, 0, 'COMBINING GRAPHEME JOINER'),
        (0x200B, 0x200F, 0, 'ZERO WIDTH SPACE..RIGHT-TO-LEFT MARK'),
        (0x2028, 0x202E, 0, 'LINE SEPARATOR..RIGHT-TO-LEFT OVERRIDE'),
        (0x2060, 0x2063, 0, 'WORD JOINER..INVISIBLE SEPARATOR'),
    ]


def parse_desc(desc):
    if desc.startswith('['):
        return desc[desc.index(']') + 2:]
    return desc


def parse_points(points):
    if '..' in points:
        low, high = points.split('..')
        return int(low, 16), int(high, 16)
    else:
        point = int(points, 16)
        return point, point


def parse_all():
    combined = sorted(parse_categories(), key=lambda it: it[0])
    # concat adjacent ranges
    ranges = []
    iterator = iter(combined)
    prev = next(iterator)
    for low, high, width, desc in iterator:
        if width == prev[2] and prev[1] + 1 == low:
            p1, p2 = prev[3].split('..') if '..' in prev[3] else (prev[3], prev[3])
            d1, d2 = desc.split('..') if '..' in desc else (desc, desc)
            prev = prev[0], high, width, f'{p1}..{d2}'
        else:
            ranges.append(prev)
            prev = (low, high, width, desc)
    ranges.append(prev)
    return ranges


def main():
    print('''package com.github.ajalt.mordant.internal.gen

internal class CellWidthTableEntry(val low: Int, val high: Int, val width: Byte)

internal val CELL_WIDTH_TABLE : Array<CellWidthTableEntry> = arrayOf<CellWidthTableEntry>('''
          )
    for low, high, width, desc in parse_all():
        print(f"    '{hex(low)}'..'{hex(high)}',// {desc}".replace('0x', '\\u'))
    print(')')


if __name__ == '__main__':
    main()
