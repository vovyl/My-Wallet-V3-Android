package piuk.blockchain.androidcore.data.metadata

import com.blockchain.android.testutils.rxInit
import com.blockchain.serialization.BigDecimalAdaptor
import com.blockchain.serialization.JsonSerializable
import com.google.common.base.Optional
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Observable
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class MoshiMetadataRepositoryAdapterTest {

    @get:Rule
    val initSchedulers = rxInit {
        ioTrampoline()
    }

    data class ExampleClass(
        val field1: String,
        val field2: BigDecimal
    ) : JsonSerializable

    private val moshi: Moshi = Moshi.Builder().add(BigDecimalAdaptor()).build()

    @Test
    fun `can save json`() {
        val metadataManager = mock<MetadataManager> {
            on { saveToMetadata(any(), any()) } `it returns` Completable.complete()
        }
        MoshiMetadataRepositoryAdapter(metadataManager, moshi)
            .saveMetadata(ExampleClass("ABC", 123.toBigDecimal()), ExampleClass::class.java, 100)
            .test()
            .assertComplete()

        verify(metadataManager).saveToMetadata("""{"field1":"ABC","field2":"123"}""", 100)
    }

    @Test
    fun `can load json`() {
        val metadataManager = mock<MetadataManager> {
            on { fetchMetadata(199) } `it returns` Observable.just(
                Optional.of(
                    """{"field1":"DEF","field2":"456"}"""
                )
            )
        }
        MoshiMetadataRepositoryAdapter(metadataManager, moshi)
            .loadMetadata(199, ExampleClass::class.java)
            .test()
            .assertComplete()
            .values() `should equal` listOf(ExampleClass("DEF", 456.toBigDecimal()))
    }

    @Test
    fun `can load missing json`() {
        val metadataManager = mock<MetadataManager> {
            on { fetchMetadata(199) } `it returns` Observable.just(
                Optional.absent()
            )
        }
        MoshiMetadataRepositoryAdapter(metadataManager, moshi)
            .loadMetadata(199, ExampleClass::class.java)
            .test()
            .assertComplete()
            .values() `should equal` listOf()
    }

    @Test
    fun `bad json is an error`() {
        val metadataManager = mock<MetadataManager> {
            on { fetchMetadata(199) } `it returns` Observable.just(
                Optional.of(
                    """{"field1":"DEF","fie..."""
                )
            )
        }
        MoshiMetadataRepositoryAdapter(metadataManager, moshi)
            .loadMetadata(199, ExampleClass::class.java)
            .test()
            .assertErrorMessage("Unterminated string at path \$.field1")
    }
}
