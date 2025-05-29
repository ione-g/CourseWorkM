package eone.grim.ElectionPart.model

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String = "",
    val order: Int = 0,
    val title: String = "",
    val type: Type = Type.SINGLE,
    val options: List<String> = emptyList(),
    val maxSelect: Int = 1,          // MULTI only
    val weight: Double = 1.0         // опц. для зважених голосів
) { enum class Type { YES_NO, SINGLE, MULTI, RANKED } }