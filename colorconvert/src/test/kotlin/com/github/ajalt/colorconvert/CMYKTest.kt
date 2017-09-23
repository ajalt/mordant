package com.github.ajalt.colorconvert

import com.github.ajalt.testing.softly
import org.junit.Test

class CMYKTest {
    @Test
    fun `CMYK to RGB`() {
        softly {
            assertThat(CMYK(0, 0, 0, 0).toRGB()).isEqualTo(RGB(255, 255, 255))
            assertThat(CMYK(0, 0, 0, 100).toRGB()).isEqualTo(RGB(0, 0, 0))
            assertThat(CMYK(0, 100, 100, 0).toRGB()).isEqualTo(RGB(255, 0, 0))
            assertThat(CMYK(100, 0, 100, 0).toRGB()).isEqualTo(RGB(0, 255, 0))
            assertThat(CMYK(100, 100, 0, 0).toRGB()).isEqualTo(RGB(0, 0, 255))
            assertThat(CMYK(0, 0, 100, 0).toRGB()).isEqualTo(RGB(255, 255, 0))
            assertThat(CMYK(100, 0, 0, 0).toRGB()).isEqualTo(RGB(0, 255, 255))
            assertThat(CMYK(0, 100, 0, 0).toRGB()).isEqualTo(RGB(255, 0, 255))
            assertThat(CMYK(30, 0, 50, 22).toRGB()).isEqualTo(RGB(139, 199, 99))
        }
    }
}
