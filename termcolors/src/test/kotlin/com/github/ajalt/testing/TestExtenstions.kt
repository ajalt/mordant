package com.github.ajalt.testing

import org.assertj.core.api.SoftAssertions

inline fun softly(block: SoftAssertions.() -> Unit) = SoftAssertions().apply(block).assertAll()
