<h1 align="center">Mordant</h1>
<h4 align="center">Colorful styling for command-line applications</h4>

> /mɔː(ɹ)dənt/ A substance used to set (i.e. bind) colored dyes on fabrics <sup>[1](https://wikipedia.org/wiki/Mordant)</sup>

Mordant has:

* Easy colorful ANSI output with automatic detection of terminal capabilities
* Markdown rendering directly to the terminal
* Components for laying out our terminal output, including lists, tables, panels, and more
* Support for animating any widget, like progress bars and dashboards

##### This README documents Mordant 2.0, which is in beta. [You can read the docs for Mordant 1.0 here.](https://github.com/ajalt/mordant/blob/caec61d9ae667431cfe07e12eb426b005ee2cf06/README.md)

## Usage

Create a `Terminal` instance, and import any enum entries you want from `TextColors` and
`TextStyles`. The `println` function on your `Terminal` will detect your current terminal
capabilities and automatically downsample ANSI codes if necessary.

```kotlin
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*

val t = Terminal()
t.println(red("This text will be red on terminals that support color"))
```

![](.github/example_basic.png)

#### Multiple styles

```kotlin
import com.github.ajalt.mordant.rendering.TextColors.*
val t = Terminal()
t.println("${red("red")} ${white("white")} and ${blue("blue")}")
```

![](.github/example_multi.png)

#### Foreground and background colors

```kotlin
t.println((yellow on brightGreen)("this is easy to read, right?"))
```

![](.github/example_fg_bg.png)

#### Background color alone

```kotlin
t.println("The foreground ${brightBlue.bg("color will stay the")} same")
```

![](.github/example_bg.png)

#### Combine styles and colors

```kotlin
val style = (bold + white + underline)
t.println(style("You can save styles"))
t.println(style("to reuse"))
```

![](.github/example_styles.png)

#### Nest styles and colors

```kotlin
t.println(white("You ${(blue on yellow)("can ${(black + strikethrough)("nest")} styles")} arbitrarily"))
```

![](.github/example_nesting.png)

#### True color and other color spaces

```kotlin
import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb

t.println(rgb("#b4eeb4")("This will get downsampled on terminals that don't support truecolor"))
```

![](.github/example_rgb.png)
<p></p>

### Terminal color support detection

By default, `Terminal()` will try to detect ANSI support in the current stdout stream. If you'd
like to override the detection, you can pass a specific value to the `Terminal` constructor.

For example, to always output ANSI RGB color codes, even if stdout is currently directed to a file,
you can do this:

```kotlin
Terminal(AnsiLevel.TRUECOLOR)
```

## Tables

Use the `table` DSL to quickly create tables. Mordant handles ANSI styles and wide characters like
CJK and emojii.

```kotlin
val t = Terminal()
t.println(table {
    header { row("CJK", "Emojis") }
    body { row("모ㄹ단ㅌ", "🙊🙉🙈") }
})
```

![](.github/simple_table.png)

Mordant gives you lots of customization for your tables, including striped row styles, row and
column spans, and different border styles.

```kotlin
table {
    borderStyle = SQUARE_DOUBLE_SECTION_SEPARATOR
    align = RIGHT
    outerBorder = false
    column(0) {
        align = LEFT
        borders = ALL
        style = magenta
    }
    column(3) {
        borders = ALL
        style = magenta
    }
    header {
        style(magenta, bold = true)
        row("", "Projected Cost", "Actual Cost", "Difference")
    }
    body {
        rowStyles(blue, brightBlue)
        borders = TOM_BOTTOM
        row("Food", "$400", "$200", "$200")
        row("Data", "$100", "$150", "-$50")
        row("Rent", "$800", "$800", "$0")
        row("Candles", "$0", "$3,600", "-$3,600")
        row("Utility", "$145", "$150", "-$5")
    }
    footer {
        style(bold = true)
        row {
            cell("Subtotal")
            cell("$-3,455") { columnSpan = 3 }
        }
    }
    captionBottom("Budget courtesy @dril", TextStyle(dim = true))
}
```

![](.github/complex_table.png)

## Markdown

Mordant can render GitHub Flavored Markdown. Hyperlinks will even be clickable if you're on a
terminal that supports it, like recent versions of iTerm or Windows Terminal.

```kotlin
val t = Terminal()
t.printMarkdown(File("README.md").readText())
```

![](.github/markdown.png)

## Controlling the cursor

You can show and hide the cursor, move it around, and clear parts of the screen with the `cursor`
property on `Terminal`. If your terminal doesn't support cursor movements (like when output is
redirected to a file) these commands are no-ops.

```kotlin
val t = Terminal()
t.cursor.move {
    up(3)
    startOfLine()
    clearScreenAfterCursor()
}
t.cursor.hide(showOnExit = true)
```

## Animations

You can animate any widget like a table with `Terminal.animation`, or any regular
string with `Terminal.textAnimation`.

```kotln
val t = Terminal()
val a = t.textAnimation<Int> { frame ->
    (1..50).joinToString("") {
        val hue = (frame + it) * 3 % 360
        t.colors.hsv(hue, 100, 100)("━")
    }
}

t.cursor.hide(showOnExit = true)
repeat(120) {
    a.update(it)
    Thread.sleep(25)
}
``` 

![](.github/animation.svg)

## Progress bars

You can create customizable progress bars that automatically compute speed and time remaining.

```kotlin
val t = Terminal()
val progress = t.progressAnimation {
    text("my-file.iso")
    percentage()
    progressBar()
    completed()
    speed("B/s")
    timeRemaining()
}
```

![](.github/example_progress.png)

Call `progress.start` to animate the progress, and `progress.update` or `progress.advance` as your
task completes.

## Installation

Mordant is distributed through Maven Central.

```groovy
dependencies {
   implementation("com.github.ajalt.mordant:mordant:2.0.0-beta1")
}
```

<sup>In version 2.0, the maven coordinates changed. Make sure you're using the new coordinates if you're updating from an older version.</sup>

<sup>If you're using Maven instead of Gradle, use `<artifactId>mordant-jvm</artifactId>`</sup>

## License

    Copyright 2018-2021 AJ Alt

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
