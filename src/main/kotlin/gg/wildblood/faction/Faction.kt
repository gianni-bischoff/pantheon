package gg.wildblood.faction

import java.util.UUID
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation

data class Faction(
    val id: ResourceLocation,
    var displayName: String,
    var anchor: BlockPos,
    var color: Int = 0,
    var godId: ResourceLocation? = null,
    val members: MutableSet<UUID> = mutableSetOf(),
    var mayor: UUID? = null,
    var skillpointPool: Int = 0,
    val skilltreeState: MutableMap<ResourceLocation, Boolean> = mutableMapOf(),
) {
    val memberCount: Int get() = members.size
}