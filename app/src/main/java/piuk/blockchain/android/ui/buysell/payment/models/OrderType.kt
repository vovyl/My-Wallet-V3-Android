package piuk.blockchain.android.ui.buysell.payment.models

enum class OrderType {
    // Unknown payment type, not yet selected
    Buy,
    // Card only payment, trader hasn't been KYC'd fully yet
    BuyCard,
    // Sell is bank payment only
    Sell
}