package com.blockchain.sunriver

import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw the Exception`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.junit.Test
import org.stellar.sdk.KeyPair

class HorizonKeyPairTest {

    @Test
    fun `toHorizonKeyPair can create a HorizonKeyPair Private key from a Private key`() {
        val pair = KeyPair.fromSecretSeed("SBQPDFUGLMWJYEYXFRM5TQX3AX2BR47WKI4FDS7EJQUSEUUVY72MZPJF")
        val hkp = pair.toHorizonKeyPair()

        hkp.accountId `should equal` "GDY47CJARRHHL66JH3RJURDYXAMIQ5DMXZLP3TDAUJ6IN2GUOFX4OJOC"
        hkp `should be instance of` HorizonKeyPair.Private::class
        String((hkp as HorizonKeyPair.Private).secret) `should equal`
            "SBQPDFUGLMWJYEYXFRM5TQX3AX2BR47WKI4FDS7EJQUSEUUVY72MZPJF"
    }

    @Test
    fun `toHorizonKeyPair can create a HorizonKeyPair Public key from a Public key`() {
        val pair = KeyPair.fromAccountId("GDY47CJARRHHL66JH3RJURDYXAMIQ5DMXZLP3TDAUJ6IN2GUOFX4OJOC")
        val hkp = pair.toHorizonKeyPair()

        hkp.accountId `should equal` "GDY47CJARRHHL66JH3RJURDYXAMIQ5DMXZLP3TDAUJ6IN2GUOFX4OJOC"
        hkp `should be instance of` HorizonKeyPair.Public::class
    }

    @Test
    fun `Private HorizonKeyPair can be neutered`() {
        val pair = KeyPair.fromSecretSeed("SBQPDFUGLMWJYEYXFRM5TQX3AX2BR47WKI4FDS7EJQUSEUUVY72MZPJF")
        val hkp: HorizonKeyPair.Public = pair.toHorizonKeyPair().neuter()

        hkp.accountId `should equal` "GDY47CJARRHHL66JH3RJURDYXAMIQ5DMXZLP3TDAUJ6IN2GUOFX4OJOC"
    }

    @Test
    fun `Private HorizonKeyPair neutered is self`() {
        val pair = KeyPair.fromAccountId("GDY47CJARRHHL66JH3RJURDYXAMIQ5DMXZLP3TDAUJ6IN2GUOFX4OJOC")
        val hkp = pair.toHorizonKeyPair()

        hkp.neuter() `should be` hkp
        hkp.accountId `should equal` "GDY47CJARRHHL66JH3RJURDYXAMIQ5DMXZLP3TDAUJ6IN2GUOFX4OJOC"
    }

    @Test
    fun `Private HorizonKeyPair back to a KeyPair`() {
        val pair = KeyPair.fromSecretSeed("SAFWTGXVS7ELMNCXELFWCFZOPMHUZ5LXNBGUVRCY3FHLFPXK4QPXYP2X")
        val pair2 = pair.toHorizonKeyPair().toKeyPair()

        pair2.accountId `should equal` "GDAHPZ2NSYIIHZXM56Y36SBVTV5QKFIZGYMMBHOU53ETUSWTP62B63EQ"
        String(pair2.secretSeed) `should equal` "SAFWTGXVS7ELMNCXELFWCFZOPMHUZ5LXNBGUVRCY3FHLFPXK4QPXYP2X"
        pair2.canSign() `should be` true
    }

    @Test
    fun `Public HorizonKeyPair back to a KeyPair`() {
        val pair = KeyPair.fromAccountId("GAVXVW5MCK7Q66RIBWZZKZEDQTRXWCZUP4DIIFXCCENGW2P6W4OA34RH")
        val pair2 = pair.toHorizonKeyPair().toKeyPair()

        pair2.accountId `should equal` "GAVXVW5MCK7Q66RIBWZZKZEDQTRXWCZUP4DIIFXCCENGW2P6W4OA34RH"
        pair2.canSign() `should be` false
    }

    @Test
    fun `createValidatedPublic - OK`() {
        HorizonKeyPair.createValidatedPublic("GAVXVW5MCK7Q66RIBWZZKZEDQTRXWCZUP4DIIFXCCENGW2P6W4OA34RH")
            .accountId `should equal` "GAVXVW5MCK7Q66RIBWZZKZEDQTRXWCZUP4DIIFXCCENGW2P6W4OA34RH"
    }

    @Test
    fun `createValidatedPublic - Bad account id checksum`() {
        {
            HorizonKeyPair.createValidatedPublic("GAVXVW5MCK7Q66RIBWZZKZEDQTRXWCZUP4DIIFXCCENGW2P6W4OA34RT")
        } `should throw the Exception` InvalidAccountIdException::class `with message`
            "Invalid Account Id, Checksum invalid"
    }

    @Test
    fun `createValidatedPublic - Bad length`() {
        {
            HorizonKeyPair.createValidatedPublic("G")
        } `should throw` InvalidAccountIdException::class
    }
}
