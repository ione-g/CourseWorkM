// composeApp/src/androidMain/kotlin/eone/grim/crypto/CryptoModuleJvm.kt
package eone.grim.crypto

import java.math.BigInteger
import java.security.SecureRandom

private fun ByteString.toBigInt() = BigInteger(1, bytes)
private fun BigInteger.toByteString() = ByteString(toByteArray())
actual object CryptoModule {

    private val rnd get() = SecureRandom()

    actual fun generateKeyPair(bits: Int): Pair<PublicKey, PrivateKey> {
        val p = BigInteger(bits / 2, 100, rnd)
        val q = BigInteger(bits / 2, 100, rnd)
        val n = p * q
        val n2 = n * n
        val g = n + BigInteger.ONE
        val lambda = lcm(p - BigInteger.ONE, q - BigInteger.ONE)
        val mu = L(g.modPow(lambda, n2), n).modInverse(n)
        return PublicKey(n.toByteString(), g.toByteString(), n2.toByteString()) to
                PrivateKey(lambda.toByteString(), mu.toByteString(), n.toByteString())
    }

    actual fun encrypt(m: ByteString, pub: PublicKey): ByteString {
        val n  = pub.n.toBigInt()
        val n2 = pub.n2.toBigInt()
        val g  = pub.g.toBigInt()
        val mInt = m.toBigInt()
        require(mInt < n) { "message ≥ n" }
        val r = BigInteger(n.bitLength(), rnd).mod(n).let { if (it > BigInteger.ONE) it else BigInteger.valueOf(2) }
        val c = g.modPow(mInt, n2) * r.modPow(n, n2) % n2
        return c.toByteString()
    }

    actual fun add(c1: ByteString, c2: ByteString, pub: PublicKey): ByteString {
        val n2 = pub.n2.toBigInt()
        return (c1.toBigInt() * c2.toBigInt() % n2).toByteString()
    }

    actual fun decrypt(c: ByteString, prv: PrivateKey): ByteString {
        val n  = prv.n.toBigInt()
        val n2 = n * n
        val lambda = prv.lambda.toBigInt()
        val mu     = prv.mu.toBigInt()
        val u = c.toBigInt().modPow(lambda, n2)
        val m = (L(u, n) * mu) % n
        return m.toByteString()
    }

    private fun L(u: BigInteger, n: BigInteger) = (u - BigInteger.ONE) / n
    private fun lcm(a: BigInteger, b: BigInteger) = a / a.gcd(b) * b
}
