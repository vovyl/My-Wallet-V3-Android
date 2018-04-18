package piuk.blockchain.androidbuysell.models.coinify

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson


/**
 * Wraps a [returnUrl] which is the URL to be triggered when the KYC process is
 * complete.
 */
internal data class KycRequest(val returnUrl: String)

data class KycResponse(
        // Identifier for the KYC review
        val id: Int,
        // KYC review state	State of the KYC review
        val state: ReviewState,
        // URL to return to when kyc flow is completed (this is only necessary in redirect mode).
        val returnUrl: String,
        // URL to redirect the user to in order to perform the KYC review
        val redirectUrl: String,
        // Reference to the external KYC review, this ID can be used in embedded mode.
        val externalId: String,
        // The time when the bank account was last updated, in ISO 8601.
        val updateTime: String,
        // Timestamp for when this bank account was first created, in ISO 8601.
        val createTime: String
)

sealed class ReviewState

// (Starting state) KYC review has been initiated, but is waiting for action from the end-user.
object Pending : ReviewState()

// (Final state) KYC review was rejected
object Rejected : ReviewState()

// (Final state) KYC review failed due to an error with the provider
object Failed : ReviewState()

// (Final state) KYC review expired before it was completed
object Expired : ReviewState()

// (Final state) KYC review completed successfully. (This state is also used if the KYC review was manual and accepted)
object Completed : ReviewState()

// (Intermediate state) KYC is awaiting manual review
object Reviewing : ReviewState()

// (Intermediate state) Trader needs to upload more documents
object DocumentsRequested : ReviewState()

@Suppress("unused")
class ReviewStateAdapter {

    @FromJson
    fun fromJson(input: String): ReviewState = when (input) {
        ReviewStates.Pending -> Pending
        ReviewStates.Rejected -> Rejected
        ReviewStates.Failed -> Failed
        ReviewStates.Expired -> Expired
        ReviewStates.Completed -> Completed
        ReviewStates.Reviewing -> Reviewing
        ReviewStates.DocumentsRequested -> DocumentsRequested
        else -> throw JsonDataException("Unknown review state $input, unsupported data type")
    }

    @ToJson
    fun toJson(reviewState: ReviewState): String = when (reviewState) {
        Pending -> ReviewStates.Pending
        Rejected -> ReviewStates.Rejected
        Failed -> ReviewStates.Failed
        Expired -> ReviewStates.Expired
        Completed -> ReviewStates.Completed
        Reviewing -> ReviewStates.Reviewing
        DocumentsRequested -> ReviewStates.DocumentsRequested
    }

}

private class ReviewStates {

    companion object {
        internal const val Pending = "pending"
        internal const val Rejected = "rejected"
        internal const val Failed = "failed"
        internal const val Expired = "expired"
        internal const val Completed = "completed"
        internal const val Reviewing = "reviewing"
        internal const val DocumentsRequested = "documentsRequested"
    }

}