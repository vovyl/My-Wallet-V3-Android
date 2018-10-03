package com.blockchain.nabu.metadata

import org.amshove.kluent.`should equal to`
import org.junit.Test

class NabuCredentialsMetadataTest {

    @Test
    fun `should be valid`() {
        NabuCredentialsMetadata("userId", "lifeTimeToken").isValid() `should equal to` true
    }

    @Test
    fun `empty id, should not be valid`() {
        NabuCredentialsMetadata("", "lifeTimeToken").isValid() `should equal to` false
    }

    @Test
    fun `empty token, should not be valid`() {
        NabuCredentialsMetadata("userId", "").isValid() `should equal to` false
    }
}