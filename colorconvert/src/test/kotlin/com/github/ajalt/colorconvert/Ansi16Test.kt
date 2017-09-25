package com.github.ajalt.colorconvert

import com.github.ajalt.testing.softly
import org.junit.Test

class Ansi16Test {
    @Test
    fun `Ansi16 to RGB`() {
        softly {
            assertThat(Ansi16(30).toRGB()).isEqualTo(RGB(0, 0, 0))
            assertThat(Ansi16(31).toRGB()).isEqualTo(RGB(128, 0, 0))
            assertThat(Ansi16(32).toRGB()).isEqualTo(RGB(0, 128, 0))
            assertThat(Ansi16(33).toRGB()).isEqualTo(RGB(128, 128, 0))
            assertThat(Ansi16(34).toRGB()).isEqualTo(RGB(0, 0, 128))
            assertThat(Ansi16(35).toRGB()).isEqualTo(RGB(128, 0, 128))
            assertThat(Ansi16(36).toRGB()).isEqualTo(RGB(0, 128, 128))
            assertThat(Ansi16(37).toRGB()).isEqualTo(RGB(170, 170, 170))
            assertThat(Ansi16(90).toRGB()).isEqualTo(RGB(85, 85, 85))
            assertThat(Ansi16(91).toRGB()).isEqualTo(RGB(255, 0, 0))
            assertThat(Ansi16(92).toRGB()).isEqualTo(RGB(0, 255, 0))
            assertThat(Ansi16(93).toRGB()).isEqualTo(RGB(255, 255, 0))
            assertThat(Ansi16(94).toRGB()).isEqualTo(RGB(0, 0, 255))
            assertThat(Ansi16(95).toRGB()).isEqualTo(RGB(255, 0, 255))
            assertThat(Ansi16(96).toRGB()).isEqualTo(RGB(0, 255, 255))
            assertThat(Ansi16(97).toRGB()).isEqualTo(RGB(255, 255, 255))
        }
    }

    @Test
    fun `Ansi16 to Ansi256`() {
        softly {
            assertThat(Ansi16(30).toAnsi256()).isEqualTo(Ansi256(0))
            assertThat(Ansi16(31).toAnsi256()).isEqualTo(Ansi256(1))
            assertThat(Ansi16(32).toAnsi256()).isEqualTo(Ansi256(2))
            assertThat(Ansi16(33).toAnsi256()).isEqualTo(Ansi256(3))
            assertThat(Ansi16(34).toAnsi256()).isEqualTo(Ansi256(4))
            assertThat(Ansi16(35).toAnsi256()).isEqualTo(Ansi256(5))
            assertThat(Ansi16(36).toAnsi256()).isEqualTo(Ansi256(6))
            assertThat(Ansi16(37).toAnsi256()).isEqualTo(Ansi256(7))
            assertThat(Ansi16(90).toAnsi256()).isEqualTo(Ansi256(8))
            assertThat(Ansi16(91).toAnsi256()).isEqualTo(Ansi256(9))
            assertThat(Ansi16(92).toAnsi256()).isEqualTo(Ansi256(10))
            assertThat(Ansi16(93).toAnsi256()).isEqualTo(Ansi256(11))
            assertThat(Ansi16(94).toAnsi256()).isEqualTo(Ansi256(12))
            assertThat(Ansi16(95).toAnsi256()).isEqualTo(Ansi256(13))
            assertThat(Ansi16(96).toAnsi256()).isEqualTo(Ansi256(14))
            assertThat(Ansi16(97).toAnsi256()).isEqualTo(Ansi256(15))
        }
    }
}
