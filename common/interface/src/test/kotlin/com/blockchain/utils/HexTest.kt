package com.blockchain.utils

import org.amshove.kluent.`should equal`
import org.junit.Test

class HexTest {

    @Test
    fun `empty byte array to hex`() {
        byteArrayOf().toHex() `should equal` ""
    }

    @Test
    fun `1 byte array to hex`() {
        byteArrayOf(0).toHex() `should equal` "00"
    }

    @Test
    fun `1 byte, non-zero array to hex`() {
        byteArrayOf(1).toHex() `should equal` "01"
    }

    @Test
    fun `2 byte array to hex`() {
        byteArrayOf(1, 2).toHex() `should equal` "0102"
    }

    @Test
    fun `large value array to hex`() {
        byteArrayOf(0xff.toByte(), 0x7f).toHex() `should equal` "FF7F"
    }

    @Test
    fun `all values to hex`() {
        (0..255).map(Int::toByte).toByteArray().toHex() `should equal`
            "000102030405060708090A0B0C0D0E0F" +
            "101112131415161718191A1B1C1D1E1F" +
            "202122232425262728292A2B2C2D2E2F" +
            "303132333435363738393A3B3C3D3E3F" +
            "404142434445464748494A4B4C4D4E4F" +
            "505152535455565758595A5B5C5D5E5F" +
            "606162636465666768696A6B6C6D6E6F" +
            "707172737475767778797A7B7C7D7E7F" +
            "808182838485868788898A8B8C8D8E8F" +
            "909192939495969798999A9B9C9D9E9F" +
            "A0A1A2A3A4A5A6A7A8A9AAABACADAEAF" +
            "B0B1B2B3B4B5B6B7B8B9BABBBCBDBEBF" +
            "C0C1C2C3C4C5C6C7C8C9CACBCCCDCECF" +
            "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF" +
            "E0E1E2E3E4E5E6E7E8E9EAEBECEDEEEF" +
            "F0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF"
    }
}
