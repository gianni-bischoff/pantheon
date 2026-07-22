package gg.wildblood.faction

import java.util.UUID

data class SeasonState(
    val id: UUID,
    val startedAt: Long,
    val endsAt: Long,
    val phase: SeasonPhase,
) {
    enum class SeasonPhase { CREATED, RUNNING, ENDED }
}