package com.pixeleye.sethpirith.ui.util

fun LimitCharacters(text: String, maxChars: Int): String {
    return if (text.length <= maxChars) {
        text
    } else {
        text.take(maxChars).trimEnd() + "..."
    }
}
