# Color conversions

This package allows you to convert between a number of color spaces. You
can convert from any supported color space to any other space.

## Supported color spaces

* RGB
* CMYK
* HSL
* HSV
* ANSI-16 color codes
* ANSI-256 color codes

Most conversions, including all conversions to and from RGB, run in a
single step. If there isn't a formula for a direct conversion between
two color spaces, the color will first be converted to RGB and then to
final color space.

## Usage

Each color space is represented with a data class, and contains
`.toXXX()` methods to convert to other spaces.

```kotlin
> RGB("#adcdef").toHSV()
HSV(h=211, s=28, v=94)

> RGB(12, 128, 255).toCMYK()
CMYK(c=95, m=50, y=0, k=0)

> HSL(180, 50, 50).toHex(withNumberSign = true)
"#40bfbf"
```

## Installation

If you just want to use Mordant, this dependency is added transitively,
there no need to declare it explicitly. However, if you want to use it
by itself, it is distributed with
[jitpack](https://jitpack.io/#ajalt/mordant).

First, add Jitpack to your gradle repositories.

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

Then add the dependency on colorconvert.

```groovy
dependencies {
   compile 'com.github.ajalt.mordant:colorconvert:<latest version>'
}
```
