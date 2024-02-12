# Hexviewer Sample

This sample is a hex viewer like `xxd` which uses mordant for color and layout.

```
$ hexviewer picture.bmp
╭────────────┬─────────────────────────────────────────────────┬──────────────────╮
│ 0x00000000 │ 42 4d 56 04 00 00 00 00┆00 00 36 00 00 00 28 00 │ BMV␄······6···(· │
│ 0x00000010 │ 00 00 1f 00 00 00 0b 00┆00 00 01 00 18 00 00 00 │ ··␟···␋···␁·␘··· │
│ 0x00000020 │ 00 00 20 04 00 00 00 00┆00 00 00 00 00 00 00 00 │ ·· ␄············ │
│ 0x00000030 │ 00 00 00 00 00 00 e8 a2┆00 e8 a2 00 ee ba 40 ff │ ··············@· │
│ 0x00000040 │ ff ff ff ff ff c1 bf fa┆40 39 ef 24 1c ed 24 1c │ ········@9·$␜·$␜ │
│ 0x00000050 │ ed 24 1c ed 24 1c ed 24┆1c ed 5b 42 aa e8 a2 00 │ ·$␜·$␜·$␜·[B···· │
│ 0x00000060 │ e8 a2 00 e8 a2 00 e8 a2┆00 e8 a2 00 e8 a2 00 e8 │ ················ │
│ 0x00000070 │ a2 00 e8 a2 00 e8 a2 00┆7b 56 00 00 00 00 00 00 │ ········{V······ │
│ 0x00000080 │ 00 00 00 00 00 00 00 00┆00 00 07 05 00 74 51 00 │ ··········␇␅·tQ· │
│ 0x00000090 │ 33 24 00 00 00 00 e8 a2┆00 e8 a2 00 e8 a2 00 ff │ 3$·············· │
│ 0x00000360 │ 07 05 01 00 00 00 00 00┆00 01 01 07 12 0e 77 00 │ ␇␅␁······␁␁␇␒␎w· │
│ 0x00000370 │ 00 00 68 68 68 ea ab 18┆e8 a2 00 e8 a2 00 e8 a2 │ ··hhh··␘········ │
│ 0x00000380 │ 00 e8 a2 00 e8 a2 00 e8┆a2 00 e8 a2 00 e8 a2 00 │ ················ │
│ 0x00000390 │ e8 a2 00 00 00 00 24 1c┆ed 24 1c ed 24 1c ed ff │ ······$␜·$␜·$␜·· │
│ 0x000003a0 │ ff ff ff ff ff ff ff ff┆f4 d3 87 a6 74 00 1d 14 │ ············t·␝␔ │
│ 0x000003b0 │ 00 00 00 00 00 00 00 00┆00 00 00 00 00 24 1c ed │ ·············$␜· │
│ 0x000003d0 │ 00 00 08 08 08 f8 e5 b7┆eb ae 20 e8 a2 00 e8 a2 │ ··␈␈␈····· ····· │
│ 0x000003e0 │ 00 e8 a2 00 e8 a2 00 e8┆a2 00 e8 a2 00 e8 a2 00 │ ················ │
│ 0x000003f0 │ e8 a2 00 00 00 00 24 1c┆ed 24 1c ed 24 1c ed 24 │ ······$␜·$␜·$␜·$ │
│ 0x00000400 │ 1c ed 24 1c ed 24 1c ed┆e2 9e 07 e8 a2 00 e8 a2 │ ␜·$␜·$␜···␇····· │
│ 0x00000410 │ 00 e8 a2 00 00 00 00 00┆00 00 00 00 00 00 00 00 │ ················ │
│ 0x00000420 │ 00 00 00 00 00 00 00 00┆00 00 00 00 00 00 00 0d │ ···············␍ │
│ 0x00000430 │ 0d 0d 9f 9f 9f ff ff ff┆ff ff ff ff ff ff f8 e5 │ ␍␍·············· │
│ 0x00000450 │ e8 a2 00 00 00 00                               │ ······           │
╰────────────┴─────────────────────────────────────────────────┴──────────────────╯
```