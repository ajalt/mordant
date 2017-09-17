package com.github.ajalt.colorconvert

import com.github.ajalt.testing.softly
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RGBTest {
    @Test
    fun `RGB to HSV`() {
        softly {
            assertThat(RGB(0, 0, 0).toHSV()).isEqualTo(HSV(0, 0, 0))
            assertThat(RGB(140, 200, 100).toHSV()).isEqualTo(HSV(96, 50, 78))
            assertThat(RGB(96, 127, 83).toHSV()).isEqualTo(HSV(102, 35, 50))
            assertThat(RGB(255, 255, 255).toHSV()).isEqualTo(HSV(0, 0, 100))
        }
    }

    @Test
    fun `RGB to HSL`() {
        softly {
            assertThat(RGB(0, 0, 0).toHSL()).isEqualTo(HSL(0, 0, 0))
            assertThat(RGB(140, 200, 100).toHSL()).isEqualTo(HSL(96, 48, 59))
            assertThat(RGB(96, 127, 83).toHSL()).isEqualTo(HSL(102, 21, 41))
            assertThat(RGB(255, 255, 255).toHSL()).isEqualTo(HSL(0, 0, 100))
        }
    }

    @Test
    fun `RGB to Hex`() {
        softly {
            assertThat(RGB(0, 0, 0).toHex()).isEqualTo("000000")
            assertThat(RGB(140, 200, 100).toHex(true)).isEqualTo("#8cc864")
            assertThat(RGB(255, 255, 255).toHex()).isEqualTo("ffffff")
        }
    }

    @Test
    fun `Hex to RGB`() {
        softly {
            assertThat(RGB("000000")).isEqualTo(RGB(0, 0, 0))
            assertThat(RGB("#8CC864")).isEqualTo(RGB(140, 200, 100))
            assertThat(RGB("ffffff")).isEqualTo(RGB(255, 255, 255))
        }
    }

    @Test
    fun `RGB to Ansi16`() {
        softly {
            assertThat(RGB(0, 0, 0).toAnsi16()).isEqualTo(Ansi16(30))
            assertThat(RGB(128, 0, 0).toAnsi16()).isEqualTo(Ansi16(31))
            assertThat(RGB(0, 128, 0).toAnsi16()).isEqualTo(Ansi16(32))
            assertThat(RGB(128, 128, 0).toAnsi16()).isEqualTo(Ansi16(33))
            assertThat(RGB(0, 0, 128).toAnsi16()).isEqualTo(Ansi16(34))
            assertThat(RGB(128, 0, 128).toAnsi16()).isEqualTo(Ansi16(35))
            assertThat(RGB(0, 128, 128).toAnsi16()).isEqualTo(Ansi16(36))
            assertThat(RGB(170, 170, 170).toAnsi16()).isEqualTo(Ansi16(37))
            assertThat(RGB(255, 0, 0).toAnsi16()).isEqualTo(Ansi16(91))
            assertThat(RGB(0, 255, 0).toAnsi16()).isEqualTo(Ansi16(92))
            assertThat(RGB(255, 255, 0).toAnsi16()).isEqualTo(Ansi16(93))
            assertThat(RGB(0, 0, 255).toAnsi16()).isEqualTo(Ansi16(94))
            assertThat(RGB(255, 0, 255).toAnsi16()).isEqualTo(Ansi16(95))
            assertThat(RGB(0, 255, 255).toAnsi16()).isEqualTo(Ansi16(96))
            assertThat(RGB(255, 255, 255).toAnsi16()).isEqualTo(Ansi16(97))
        }
    }
}
