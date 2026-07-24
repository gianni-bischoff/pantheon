package gg.wildblood.blockentity

import gg.wildblood.faction.Faction
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage
import com.lowdragmc.lowdraglib2.syncdata.storage.IManagedStorage
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib2.syncdata.annotation.RPCMethod
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender
import gg.wildblood.block.TempleBlock
import gg.wildblood.faction.FactionId
import gg.wildblood.faction.FactionTeamManager
import gg.wildblood.faction.PantheonSavedData
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class TempleBlockEntity(pos: BlockPos, state: BlockState)
    : BlockEntity(ModBlockEntities.TEMPLE.get(), pos, state), ISyncPersistRPCBlockEntity {

    private val syncStorage = FieldManagedStorage(this)

    override fun getSyncStorage(): IManagedStorage = syncStorage

    @Persisted @DescSynced var factionId: String = ""
    @Persisted @DescSynced var displayName: String = ""
    @Persisted @DescSynced var color: Int = 0
    @Persisted @DescSynced var godId: String = ""
    @Persisted @DescSynced var mayorUuid: String = ""
    @Persisted @DescSynced var memberCount: Int = 0
    @Persisted @DescSynced var memberNames: String = ""
    @Persisted @DescSynced var skillpointPool: Int = 0

    fun syncFrom(faction: Faction) {
        factionId = faction.id.toString()
        displayName = faction.displayName
        color = faction.color
        godId = faction.godId?.toString() ?: ""
        mayorUuid = faction.mayor?.toString() ?: ""
        memberCount = faction.memberCount
        memberNames = faction.members.joinToString("\n") { uuid ->
            level?.server?.profileCache?.get(uuid)?.orElse(null)?.name ?: uuid.toString()
        }
        skillpointPool = faction.skillpointPool
        setChanged()
    }

    @RPCMethod
    fun rpcCreateFaction(sender: RPCSender, name: String, colorIndex: Int) {
        if (level?.isClientSide != false) return
        val server = level!!.server ?: return
        val data = PantheonSavedData.get(server)
        val id = FactionId.fromDisplayName(name) ?: return
        val existing = data.factions[id]
        if (existing != null && existing.anchor != BlockPos.ZERO) return
        val f = data.createFaction(name, colorIndex, blockPos) ?: return
        FactionTeamManager.syncFaction(server, f)
        syncFrom(f)
        level!!.setBlock(blockPos, blockState.setValue(TempleBlock.COLOR, DyeColor.byId(colorIndex)), 3)
        sender.asPlayer()!!.closeContainer()
    }

    @RPCMethod
    fun rpcUpdateFaction(sender: RPCSender, name: String, colorIndex: Int) {
        if (level?.isClientSide != false) return
        val server = level!!.server ?: return
        val data = PantheonSavedData.get(server)
        val faction = data.factions.values.find { it.anchor == blockPos } ?: return
        val player = sender.asPlayer() ?: return
        val isMayor = faction.mayor == player.uuid
        val isAdmin = player.hasPermissions(2)
        if (!isMayor && !isAdmin) return
        data.updateFaction(faction.id, name, colorIndex)
        FactionTeamManager.syncFaction(server, faction)
        syncFrom(faction)
        level!!.setBlock(blockPos, blockState.setValue(TempleBlock.COLOR, DyeColor.byId(colorIndex)), 3)
        player.closeContainer()
    }

    @RPCMethod
    fun rpcJoinFaction(sender: RPCSender, factionIdStr: String) {
        if (level?.isClientSide != false) return
        val server = level!!.server ?: return
        val data = PantheonSavedData.get(server)
        val id = net.minecraft.resources.ResourceLocation.parse(factionIdStr)
        val faction = data.factions[id] ?: return
        val player = sender.asPlayer()!!
        val oldFaction = data.findFactionByPlayer(player.uuid)
        data.assignPlayerToFaction(player.uuid, id)
        player.setData(gg.wildblood.attachment.ModAttachments.FACTION.get(), id)
        FactionTeamManager.syncFaction(server, faction)
        oldFaction?.let { FactionTeamManager.syncFaction(server, it) }
        for (f in listOfNotNull(faction, oldFaction)) {
            val anchorBe = level!!.getBlockEntity(f.anchor) as? TempleBlockEntity
            anchorBe?.syncFrom(f)
        }
        player.closeContainer()
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        val server = level?.server
        if (server != null) {
            val data = PantheonSavedData.get(server)
            for (faction in data.factions.values) {
                if (faction.anchor == blockPos) {
                    syncFrom(faction)
                    break
                }
            }
        }
    }
}