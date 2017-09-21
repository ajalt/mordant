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
}
