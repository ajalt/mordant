package com.github.ajalt.colorconvert

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RGBTest {
    @Test
    fun `RGB to HSV`() {
        assertThat(RGB(140, 200, 100).toHSL()).isEqualTo(HSL(96, 48, 59))
    }
}
