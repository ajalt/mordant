package com.github.ajalt.colorconvert

/**
 * A color that can be converted to other representations.
 *
 * The conversion functions can return the object they're called on if it is already in the
 * correct format.
 *
 * Note that there is not a direct conversion between every pair of representations. In those cases,
 * the values may be converted through one or more intermediate representations. This may cause a
 * loss of precision.
 */
interface ConvertibleColor {
    /** Convert this color to Red-Green-Blue */
    fun toRGB(): RGB

    /**
     * Convert this color to an RGB hex string.
     *
     * @return A string in the form `"#ffffff"` if [withNumberSign] is true,
     *     or in the form `"ffffff"` otherwise.
     */
    fun toHex(withNumberSign: Boolean = false): String = toRGB().toHex(withNumberSign)

    /** Convert this color to Hue-Saturation-Luminosity */
    fun toHSL(): HSL = toRGB().toHSL()

    /** Convert this color to Hue-Saturation-Value */
    fun toHSV(): HSV = toRGB().toHSV()

    /** Convert this color to a 16-color ANSI code */
    fun toAnsi16(): Ansi16 = toRGB().toAnsi16()

    /** Convert this color to a 256-color ANSI code */
    fun toAnsi256(): Ansi256 = toRGB().toAnsi256()

    /** Convert this color to Cyan-Magenta-Yellow-Key */
    fun toCMYK(): CMYK = toRGB().toCMYK()
}
