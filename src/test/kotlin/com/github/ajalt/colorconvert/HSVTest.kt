package com.github.ajalt.colorconvert

import com.github.ajalt.testing.softly
import org.junit.Test

class HSVTest {
    @Test
    fun `HSV to RGB`() {
        softly {
            assertThat(HSV(0, 0, 0).toRGB()).isEqualTo(RGB(0, 0, 0))
            assertThat(HSV(96, 50, 78).toRGB()).isEqualTo(RGB(139, 199, 99))
            assertThat(HSV(289, 85, 87).toRGB()).isEqualTo(RGB(187, 33, 222))
            assertThat(HSV(0, 0, 100).toRGB()).isEqualTo(RGB(255, 255, 255))
        }
    }
}
