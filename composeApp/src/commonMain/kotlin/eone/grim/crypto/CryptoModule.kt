// composeApp/src/commonMain/kotlin/eone/grim/crypto/CryptoModule.kt
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package eone.grim.crypto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline


@Serializable
data class PublicKey(val n: ByteString, val g: ByteString, val n2: ByteString)
data class PrivateKey(val lambda: ByteString, val mu: ByteString, val n: ByteString)

expect object CryptoModule {

    fun generateKeyPair(bits: Int = 2048): Pair<PublicKey, PrivateKey>

    fun encrypt(m: ByteString, pub: PublicKey): ByteString
    fun add(c1: ByteString, c2: ByteString, pub: PublicKey): ByteString
    fun decrypt(c: ByteString, prv: PrivateKey): ByteString
}

@Serializable(with = ByteStringSerializer::class)
@JvmInline
value class ByteString(val bytes: ByteArray) {
    override fun toString() = bytes.joinToString("") { "%02x".format(it) }
    companion object {
        fun fromHex(hex: String): ByteString =
            ByteString(hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray())


    }
}

object ByteStringSerializer : KSerializer<ByteString> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ByteString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ByteString) =
        encoder.encodeString(value.toString())

    fun fromHex(hex: String): ByteString {
        if (!hex.equals("null")) {
            require(hex.matches(Regex("[0-9a-fA-F]+"))) { "Not a HEX string" }
            val clean = if (hex.length % 2 != 0) "0$hex" else hex
            return ByteString(
                ByteArray(clean.length / 2) { i ->
                    clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                }
            )
        } else {
            return ByteString(ByteArray(0))
        }
    }

    override fun deserialize(decoder: Decoder): ByteString =
        fromHex(decoder.decodeString())
}
