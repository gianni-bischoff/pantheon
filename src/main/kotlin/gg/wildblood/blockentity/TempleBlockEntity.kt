package gg.wildblood.blockentity

import gg.wildblood.faction.Faction
import gg.wildblood.faction.PantheonSavedData
import gg.wildblood.faction.SeasonState
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage
import com.lowdragmc.lowdraglib2.syncdata.storage.IManagedStorage
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class TempleBlockEntity(pos: BlockPos, state: BlockState)
    : BlockEntity(ModBlockEntities.TEMPLE.get(), pos, state), ISyncPersistRPCBlockEntity {

    private val syncStorage = FieldManagedStorage(this)

    override fun getSyncStorage(): IManagedStorage = syncStorage

    @Persisted @DescSynced var factionId: String = ""
    @Persisted @DescSynced var godId: String = ""
    @Persisted @DescSynced var mayorUuid: String = ""
    @Persisted @DescSynced var memberCount: Int = 0
    @Persisted @DescSynced var skillpointPool: Int = 0
    @Persisted @DescSynced var seasonPhase: String = "CREATED"

    fun syncFrom(faction: Faction, season: SeasonState?) {
        factionId = faction.id.toString()
        godId = faction.godId?.toString() ?: ""
        mayorUuid = faction.mayor?.toString() ?: ""
        memberCount = faction.memberCount
        skillpointPool = faction.skillpointPool
        seasonPhase = season?.phase?.name ?: "CREATED"
        setChanged()
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        val server = level?.server
        if (server != null) {
            val data = PantheonSavedData.get(server)
            for (faction in data.factions.values) {
                if (faction.anchor == blockPos) {
                    syncFrom(faction, data.season)
                    break
                }
            }
        }
    }
}