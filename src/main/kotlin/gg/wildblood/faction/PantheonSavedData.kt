package gg.wildblood.faction

import java.util.UUID
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntArrayTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.saveddata.SavedData

class PantheonSavedData : SavedData() {
    val factions: MutableMap<ResourceLocation, Faction> = mutableMapOf()

    fun createFaction(displayName: String, color: Int, anchor: BlockPos): Faction? {
        val id = FactionId.fromDisplayName(displayName) ?: return null
        val existing = factions[id]
        if (existing != null) {
            if (existing.anchor == BlockPos.ZERO) {
                existing.anchor = anchor
                existing.color = color
                setDirty()
                return existing
            }
            return null
        }
        val f = Faction(id = id, displayName = displayName, anchor = anchor, color = color)
        factions[id] = f
        setDirty()
        return f
    }

    fun updateFaction(factionId: ResourceLocation, displayName: String?, color: Int?) {
        val f = factions[factionId] ?: return
        if (displayName != null) f.displayName = displayName
        if (color != null) f.color = color
        setDirty()
    }

    fun removeFactionAnchor(anchor: BlockPos) {
        factions.values.find { it.anchor == anchor }?.let {
            it.anchor = BlockPos.ZERO
            setDirty()
        }
    }

    fun ensureFaction(id: ResourceLocation, displayName: String, anchor: BlockPos): Faction {
        val existing = factions[id]
        if (existing != null) return existing
        val f = Faction(id = id, displayName = displayName, anchor = anchor)
        factions[id] = f
        setDirty()
        return f
    }

    fun assignPlayerToFaction(playerUuid: UUID, factionId: ResourceLocation) {
        for (f in factions.values) {
            if (f.id != factionId && playerUuid in f.members) {
                f.members.remove(playerUuid)
                if (f.mayor == playerUuid) f.mayor = null
            }
        }
        factions[factionId]?.members?.add(playerUuid)
        setDirty()
    }

    fun findFactionByPlayer(playerUuid: UUID): Faction? =
        factions.values.find { playerUuid in it.members }

    fun setMayor(factionId: ResourceLocation, uuid: UUID) {
        factions[factionId]?.let { it.members.add(uuid); it.mayor = uuid }
        setDirty()
    }

    fun setGod(factionId: ResourceLocation, godId: ResourceLocation) {
        factions[factionId]?.godId = godId
        setDirty()
    }

    fun addSkillpoints(factionId: ResourceLocation, amount: Int) {
        factions[factionId]?.skillpointPool = (factions[factionId]?.skillpointPool ?: 0) + amount
        setDirty()
    }

    fun purchaseSkill(factionId: ResourceLocation, nodeId: ResourceLocation) {
        val f = factions[factionId] ?: return
        if (f.skillpointPool > 0 && !f.skilltreeState.getOrDefault(nodeId, false)) {
            f.skillpointPool -= 1
            f.skilltreeState[nodeId] = true
            setDirty()
        }
    }

    override fun save(tag: CompoundTag, registries: net.minecraft.core.HolderLookup.Provider): CompoundTag {
        val factionsList = ListTag()
        for (f in factions.values) {
            val fTag = CompoundTag()
            fTag.putString("id", f.id.toString())
            fTag.putString("displayName", f.displayName)
            fTag.put("anchor", IntArrayTag(intArrayOf(f.anchor.x, f.anchor.y, f.anchor.z)))
            fTag.putInt("color", f.color)
            f.godId?.let { fTag.putString("godId", it.toString()) }
            val membersList = ListTag()
            for (m in f.members) {
                membersList.add(IntArrayTag(intArrayOf(
                    (m.mostSignificantBits shr 32).toInt(),
                    m.mostSignificantBits.toInt(),
                    (m.leastSignificantBits shr 32).toInt(),
                    m.leastSignificantBits.toInt(),
                )))
            }
            fTag.put("members", membersList)
            f.mayor?.let { mayor ->
                fTag.put("mayor", IntArrayTag(intArrayOf(
                    (mayor.mostSignificantBits shr 32).toInt(),
                    mayor.mostSignificantBits.toInt(),
                    (mayor.leastSignificantBits shr 32).toInt(),
                    mayor.leastSignificantBits.toInt(),
                )))
            }
            fTag.putInt("skillpointPool", f.skillpointPool)
            val skilltreeList = ListTag()
            for ((nodeId, purchased) in f.skilltreeState) {
                val stTag = CompoundTag()
                stTag.putString("nodeId", nodeId.toString())
                stTag.putByte("purchased", if (purchased) 1 else 0)
                skilltreeList.add(stTag)
            }
            fTag.put("skilltreeState", skilltreeList)
            factionsList.add(fTag)
        }
        tag.put("factions", factionsList)
        return tag
    }

    companion object {
        val FACTORY: Factory<PantheonSavedData> = Factory(::create, ::load)
        const val DATA_NAME = "pantheon"

        fun create() = PantheonSavedData()

        fun load(tag: CompoundTag, registries: net.minecraft.core.HolderLookup.Provider): PantheonSavedData {
            val data = PantheonSavedData()
            val factionsList = tag.getList("factions", Tag.TAG_COMPOUND.toInt())
            for (i in 0 until factionsList.size) {
                val fTag = factionsList.getCompound(i)
                val id = ResourceLocation.parse(fTag.getString("id"))
                val displayName = fTag.getString("displayName")
                val anchorArr = fTag.getIntArray("anchor")
                val anchor = if (anchorArr.size == 3) BlockPos(anchorArr[0], anchorArr[1], anchorArr[2]) else BlockPos.ZERO
                val color = fTag.getInt("color")
                val godId = if (fTag.contains("godId")) ResourceLocation.parse(fTag.getString("godId")) else null
                val members = mutableSetOf<UUID>()
                val membersList = fTag.getList("members", Tag.TAG_INT_ARRAY.toInt())
                for (j in 0 until membersList.size) {
                    val mArr = membersList.getIntArray(j)
                    if (mArr.size == 4) {
                        members.add(UUID(
                            (mArr[0].toLong() shl 32) or (mArr[1].toLong() and 0xFFFFFFFFL),
                            (mArr[2].toLong() shl 32) or (mArr[3].toLong() and 0xFFFFFFFFL),
                        ))
                    }
                }
                val mayor = if (fTag.contains("mayor")) {
                    val mArr = fTag.getIntArray("mayor")
                    if (mArr.size == 4) UUID(
                        (mArr[0].toLong() shl 32) or (mArr[1].toLong() and 0xFFFFFFFFL),
                        (mArr[2].toLong() shl 32) or (mArr[3].toLong() and 0xFFFFFFFFL),
                    ) else null
                } else null
                val skillpointPool = fTag.getInt("skillpointPool")
                val skilltreeState = mutableMapOf<ResourceLocation, Boolean>()
                val stList = fTag.getList("skilltreeState", Tag.TAG_COMPOUND.toInt())
                for (k in 0 until stList.size) {
                    val stTag = stList.getCompound(k)
                    skilltreeState[ResourceLocation.parse(stTag.getString("nodeId"))] = stTag.getByte("purchased") == 1.toByte()
                }
                data.factions[id] = Faction(
                    id = id, displayName = displayName, anchor = anchor, color = color,
                    godId = godId, members = members, mayor = mayor,
                    skillpointPool = skillpointPool, skilltreeState = skilltreeState,
                )
            }
            return data
        }

        fun get(server: MinecraftServer): PantheonSavedData =
            server.overworld().dataStorage.computeIfAbsent(FACTORY, DATA_NAME)
    }
}