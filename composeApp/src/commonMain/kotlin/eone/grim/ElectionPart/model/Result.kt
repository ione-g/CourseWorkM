package eone.grim.ElectionPart.model

import kotlinx.serialization.Serializable

@Serializable data class Result(
    val qId: String = "",
    val ctSum: String = "",
    val ptResult: Map<String, Int> = emptyMap(),   // option → голосів
    val talliedBy: List<String> = emptyList(),
    val talliedAt: Long = 0L
)