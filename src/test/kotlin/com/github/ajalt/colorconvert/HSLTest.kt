package com.github.ajalt.colorconvert

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HSLTest {
    @Test
    fun `HSL to RGB`() {
        assertThat(HSL(96, 48, 59).toRGB()).isEqualTo(RGB(140, 201, 100))
    }
}
