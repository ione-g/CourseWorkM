package eone.grim.ElectionPart.model

import eone.grim.crypto.PublicKey
import kotlinx.serialization.Serializable

@Serializable
data class Election(
    val id: String = "",
    val title: String = "",
    val publicKey: PublicKey,
    val active: Boolean = true,
)