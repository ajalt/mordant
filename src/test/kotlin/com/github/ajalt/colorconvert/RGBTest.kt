package com.github.ajalt.colorconvert

import com.github.ajalt.testing.softly
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RGBTest {
    @Test
    fun `RGB to HSV`() {
        softly {
            assertThat(RGB(0, 0, 0).toHSL()).isEqualTo(HSL(0, 0, 0))
            assertThat(RGB(140, 200, 100).toHSL()).isEqualTo(HSL(96, 48, 59))
            assertThat(RGB(96, 127, 83).toHSL()).isEqualTo(HSL(102, 21, 41))
            assertThat(RGB(255, 255, 255).toHSL()).isEqualTo(HSL(0, 0, 100))
        }
    }
}
