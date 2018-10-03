package com.blockchain.morph.ui.customviews

internal data class ThreePartText(val part1: String, val part2: String, val part3: String) {
    operator fun get(i: Int) =
        when (i) {
            0 -> part1
            1 -> part2
            2 -> part3
            else -> throw IllegalArgumentException()
        }
}
