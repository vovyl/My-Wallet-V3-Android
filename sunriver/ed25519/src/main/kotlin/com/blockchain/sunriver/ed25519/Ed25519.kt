package com.blockchain.sunriver.ed25519

import java.nio.charset.Charset
import java.util.Arrays
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Ed25519 derivation according to SLIP-0010
 * https://github.com/satoshilabs/slips/blob/master/slip-0010.md
 *
 *  - Only hardened key derivation is possible with ED25519, for this reason, this function hardens indexes for you.
 *  - For Ed25519 every 256-bit number (even 0) is a valid private key, so there are no hard to find and test edge
 *  cases, such as "parse256(IL) ≥ n or parse256(IL) + kpar (mod n) = 0" with secp256k1.
 *
 * @seed This is the seed that comes from BIP0039
 * @indexes Child indexes for the path. E.g. For path: m/1'/2'/3' will be 1, 2, 3.
 * These ints can be hardened or not, they will be hardened by this function regardless.
 */
@Suppress("LocalVariableName")
fun deriveEd25519PrivateKey(seed: ByteArray, vararg indexes: Int): ByteArray {

    val I = ByteArray(64)
    val Il = ByteArray(32)
    val Ir = ByteArray(32)
    val data = ByteArray(37)
    val hmacSha512 = Mac.getInstance("HmacSHA512")

    hmacSha512.run(
        key = "ed25519 seed".utf8Bytes(),
        data = seed,
        output = I
    )

    I copyHead32BytesInto Il

    indexes.forEach { index ->

        I copyTail32BytesInto Ir
        I.clear()

        data[0] = 0
        Il copy32BytesIntoOffset1of data
        index.hardened() copy4BytesIntoOffset33of data

        hmacSha512.run(
            key = Ir,
            data = data,
            output = I
        )
        data.clear(); Ir.clear()

        I copyHead32BytesInto Il
    }

    I.clear()
    return Il
}

private fun String.utf8Bytes() = toByteArray(Charset.forName("UTF-8"))

private infix fun ByteArray.copyHead32BytesInto(into: ByteArray) = System.arraycopy(this, 0, into, 0, 32)

private infix fun ByteArray.copyTail32BytesInto(into: ByteArray) = System.arraycopy(this, 32, into, 0, 32)

private infix fun ByteArray.copy32BytesIntoOffset1of(into: ByteArray) = System.arraycopy(this, 0, into, 1, 32)

private infix fun Int.copy4BytesIntoOffset33of(into: ByteArray) = into.ser32(this, 33)

/**
 * Writes a 32 bit int [i32] to a byte array starting [atIndex]
 */
private fun ByteArray.ser32(i32: Int, atIndex: Int) {
    this[atIndex] = (i32 shr 24).toByte()
    this[atIndex + 1] = (i32 shr 16).toByte()
    this[atIndex + 2] = (i32 shr 8).toByte()
    this[atIndex + 3] = i32.toByte()
}

private fun ByteArray.clear() = Arrays.fill(this, 0)

private fun Int.hardened() = this or -0x80000000

private fun Mac.run(key: ByteArray, data: ByteArray, output: ByteArray) {
    init(SecretKeySpec(key, algorithm))
    update(data)
    doFinal(output, 0)
}
