package piuk.blockchain.androidcoreui.utils.extensions

import java.math.BigDecimal

private val boundaryValues =
    listOf(
        1000.toBigDecimal() to "1000",
        100.toBigDecimal() to "100",
        BigDecimal.TEN to "10",
        BigDecimal.ONE to "1.0",
        0.5.toBigDecimal() to "0.5",
        0.1.toBigDecimal() to "0.1",
        0.05.toBigDecimal() to "0.05",
        BigDecimal.ZERO to "0"
    )

fun BigDecimal.getBoundary(): String {
    var upper = boundaryValues[0]
    if (this >= upper.first) return upper.second
    for (i in 1 until boundaryValues.size) {
        val lower = boundaryValues[i]
        if (this >= lower.first) {
            return "${lower.second} - ${upper.second}"
        }
        upper = lower
    }
    return ""
}
