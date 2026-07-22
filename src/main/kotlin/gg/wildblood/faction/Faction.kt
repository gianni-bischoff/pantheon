package gg.wildblood.faction

import java.util.UUID
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation

data class Faction(
    val id: ResourceLocation,
    val displayName: String,
    val anchor: BlockPos,
    val godId: ResourceLocation? = null,
    val members: MutableSet<UUID> = mutableSetOf(),
    val mayor: UUID? = null,
    val skillpointPool: Int = 0,
    val skilltreeState: MutableMap<ResourceLocation, Boolean> = mutableMapOf(),
) {
    val memberCount: Int get() = members.size
}