package eone.grim.ElectionPart.model

import kotlinx.serialization.Serializable

@Serializable data class Ballot(
    val uid: String = "",
    val ctMap: Map<String, String> = emptyMap(),   // qId → ciphertext
    val hash: String = "",
    val ts: Long = 0L
)