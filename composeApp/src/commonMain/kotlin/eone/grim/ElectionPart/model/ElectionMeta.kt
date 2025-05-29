package eone.grim.ElectionPart.model

import kotlinx.serialization.Serializable

@Serializable
data class ElectionMeta(
    val id: String = "",
    val title: String = "",
    val desc: String = "",
    val creatorUid: String = "",
    val startAt: Long = 0L,
    val endAt: Long = 0L,
    val status: Status = Status.DRAFT,
    val minShares: Int = 3,          // k-of-n для розшифрування
    val publicKey: String = ""      //TODO Transfer from election to meta
) { enum class Status { DRAFT, ACTIVE, CLOSED, TALLIED } }
