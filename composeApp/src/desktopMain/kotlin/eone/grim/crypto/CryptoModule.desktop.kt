package eone.grim.crypto

actual object CryptoModule {
    actual fun generateKeyPair(bits: Int): Pair<PublicKey, PrivateKey> {
        TODO("Not yet implemented")
    }

    actual fun encrypt(
        m: ByteString,
        pub: PublicKey
    ): ByteString {
        TODO("Not yet implemented")
    }

    actual fun add(
        c1: ByteString,
        c2: ByteString,
        pub: PublicKey
    ): ByteString {
        TODO("Not yet implemented")
    }

    actual fun decrypt(
        c: ByteString,
        prv: PrivateKey
    ): ByteString {
        TODO("Not yet implemented")
    }

}