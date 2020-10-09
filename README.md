<h1 align="center">Mordant</h1>
<h4 align="center">Dead simple text styling for command-line applications</h4>

> /mɔː(ɹ)dənt/ A substance used to set (i.e. bind) colored dyes on fabrics <sup>[1](https://wikipedia.org/wiki/Mordant)</sup>

Mordant has:

* An easy, configuration-free, API
* Support for nesting styles and colors
* Automatic detection of terminal color support
* Support for 256 and 24-bit colors, with automatic downsampling
* Support for specifying colors in every color space supported by [colormath](https://github.com/ajalt/colormath)

<div align="center"><img src=".github/rainbow.gif"></div>

## Usage

```kotlin
val t = TermColors()
println(t.red("This text will be red on terminals that support color"))
```

<img src=".github/example_basic.png">

#### Multiple styles

```kotlin
with(TermColors()) {
    println("${red("red")} ${white("white")} and ${blue("blue")}")
}
```

<img src=".github/example_multi.png">

#### Foreground and background colors

```kotlin
with(TermColors()) {
    println((yellow on brightGreen)("this is easy to read, right?"))
}
```

<img src=".github/example_fg_bg.png">

#### Background color alone

```kotlin
with(TermColors()) {
    println("The foreground ${brightBlue.bg("color will stay the")} same")
}
```

<img src=".github/example_bg.png">

#### Combine styles and colors

```kotlin
with(TermColors()) {
    val style = (bold + white + underline)
    println(style("You can save styles"))
    println(style("to reuse"))
}
```

<img src=".github/example_styles.png">

#### Nest styles and colors

```kotlin
with(TermColors()) {
    println(white("You ${(blue on yellow)("can ${(black + strikethrough)("nest")} styles")} arbitrarily"))
}
```

<img src=".github/example_nesting.png">

#### True color and other color spaces
```kotlin
with(TermColors()) {
    println(rgb("#b4eeb4")("This will get downsampled on terminals that don't support truecolor"))
}
```

<img src=".github/example_rgb.png">
<p></p>

```kotlin
with(TermColors()) {
    for (v in 0..100 step 4) {
        for (h in 0..360 step 4) {
            print(hsv(h, 100, 100 - v).bg(" "))
        }
        println()
    }
}
```

<img src=".github/example_hsv.png">

### Terminal color support detection

By default, `TermColors()` will try to detect ANSI support in the current stdout stream. If you'd
like to override the detection, you can pass a specific value to the `TermColors` constructor.

For example, to always output ANSI RGB color codes, even if stdout is currently directed to a file,
you can do this:

```kotlin
TermColors(TermColors.Level.TRUECOLOR)
```

## API Documentation

API docs are [hosted on JitPack](https://jitpack.io/com/github/ajalt/mordant/1.2.0/javadoc/com/github/ajalt/mordant/TermColors.html).

## Installation

Mordant is distributed through Maven Central,
[Jcenter](https://bintray.com/ajalt/maven/mordant) and
[Jitpack](https://jitpack.io/#ajalt/mordant).

Gradle Groovy:
```groovy
dependencies {
    implementation 'com.github.ajalt:mordant:1.2.1'
}
```

Gradle Kotlin DSL:
```kotlin
dependencies {
    implementation("com.github.ajalt:mordant:1.2.1")
}
```

## License

    Copyright 2018-2019 AJ Alt

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
