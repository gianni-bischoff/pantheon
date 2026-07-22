package gg.wildblood.gametest

import gg.wildblood.Pantheon
import gg.wildblood.attachment.ModAttachments
import gg.wildblood.block.ModBlocks
import gg.wildblood.faction.FactionId
import gg.wildblood.faction.PantheonSavedData
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.common.util.FakePlayerFactory
import net.neoforged.neoforge.gametest.GameTestHolder
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate

@GameTestHolder(Pantheon.MODID)
@PrefixGameTestTemplate(false)
class PantheonGameTests {

    @GameTest(template = "pantheon_faction_create")
    fun pantheon_faction_create(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.factions.clear()
        val templePos = BlockPos(0, 1, 0)
        helper.setBlock(templePos, ModBlocks.TEMPLE.get())
        val absPos = helper.absolutePos(templePos)
        val f = data.createFaction("TestA", 1, absPos)
        if (f == null) { helper.fail("Faction not created"); return }
        if (f.displayName != "TestA") { helper.fail("Display name mismatch: ${f.displayName}"); return }
        if (f.color != 1) { helper.fail("Color mismatch: ${f.color}"); return }
        if (f.anchor != absPos) { helper.fail("Anchor mismatch: ${f.anchor} vs $absPos"); return }
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_assign")
    fun pantheon_faction_assign(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.factions.clear()
        val templePos = BlockPos(0, 1, 0)
        helper.setBlock(templePos, ModBlocks.TEMPLE.get())
        val absPos = helper.absolutePos(templePos)
        val f = data.createFaction("TestB", 2, absPos) ?: run { helper.fail("Faction not created"); return }
        val fakePlayer = FakePlayerFactory.getMinecraft(server.overworld())
        val factionId = f.id
        data.assignPlayerToFaction(fakePlayer.uuid, factionId)
        fakePlayer.setData(ModAttachments.FACTION.get(), factionId)
        if (fakePlayer.uuid !in f.members) { helper.fail("Player not in members"); return }
        val attached = fakePlayer.getData(ModAttachments.FACTION.get())
        if (attached != factionId) { helper.fail("Attachment mismatch: $attached"); return }
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_info")
    fun pantheon_faction_info(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.factions.clear()
        val source = server.createCommandSourceStack()
        val result = runCatching {
            server.commands.getDispatcher().execute("pantheon faction info \"pantheon:nonexistent\"", source)
        }
        if (result.isFailure) { helper.fail("faction info command crashed: ${result.exceptionOrNull()?.message}"); return }
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_list")
    fun pantheon_faction_list(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.factions.clear()
        val templePos = BlockPos(0, 1, 0)
        helper.setBlock(templePos, ModBlocks.TEMPLE.get())
        val absPos = helper.absolutePos(templePos)
        data.createFaction("TestC", 3, absPos) ?: run { helper.fail("Faction not created"); return }
        val source = server.createCommandSourceStack()
        val listResult = runCatching { server.commands.getDispatcher().execute("pantheon faction list", source) }
        if (listResult.isFailure) { helper.fail("faction list command failed"); return }
        if (data.factions.size != 1) { helper.fail("Expected 1 faction, got ${data.factions.size}"); return }
        helper.succeed()
    }

    @GameTest(template = "pantheon_temple_mirror")
    fun pantheon_temple_mirror(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.factions.clear()
        val templePos = BlockPos(0, 1, 0)
        helper.setBlock(templePos, ModBlocks.TEMPLE.get())
        val absPos = helper.absolutePos(templePos)
        val f = data.createFaction("TestD", 4, absPos) ?: run { helper.fail("Faction not created"); return }
        val fakePlayer = FakePlayerFactory.getMinecraft(server.overworld())
        data.assignPlayerToFaction(fakePlayer.uuid, f.id)
        val be = server.overworld().getBlockEntity(absPos) as? gg.wildblood.blockentity.TempleBlockEntity
            ?: run { helper.fail("Temple BE not found"); return }
        be.syncFrom(f)
        if (be.factionId != f.id.toString()) { helper.fail("factionId mismatch: ${be.factionId}"); return }
        if (be.memberCount != 1) { helper.fail("memberCount should be 1, got ${be.memberCount}"); return }
        if (be.color != 4) { helper.fail("color should be 4, got ${be.color}"); return }
        helper.succeed()
    }

    @GameTest(template = "pantheon_persistence")
    fun pantheon_persistence(helper: GameTestHelper) {
        val server = helper.level.server
        val templePos = helper.absolutePos(BlockPos(0, 1, 0))
        val data = PantheonSavedData()
        data.createFaction("TestE", 5, templePos) ?: run { helper.fail("Faction not created"); return }
        val tag = net.minecraft.nbt.CompoundTag()
        val registries = server.registryAccess()
        data.save(tag, registries)
        val reloaded = PantheonSavedData.load(tag, registries)
        val f = reloaded.factions[ResourceLocation.parse("pantheon:teste")]
            ?: run { helper.fail("Faction not reloaded"); return }
        if (f.displayName != "TestE") { helper.fail("Name mismatch: ${f.displayName}"); return }
        if (f.color != 5) { helper.fail("Color mismatch: ${f.color}"); return }
        if (f.anchor != templePos) { helper.fail("Anchor mismatch: ${f.anchor}"); return }
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_color")
    fun pantheon_faction_color(helper: GameTestHelper) {
        val server = helper.level.server
        val templePos = helper.absolutePos(BlockPos(0, 1, 0))
        val data = PantheonSavedData()
        data.createFaction("ColorTest", 11, templePos) ?: run { helper.fail("Faction not created"); return }
        val tag = net.minecraft.nbt.CompoundTag()
        data.save(tag, server.registryAccess())
        val reloaded = PantheonSavedData.load(tag, server.registryAccess())
        val f = reloaded.factions[FactionId.fromDisplayName("ColorTest")]
            ?: run { helper.fail("Faction not reloaded"); return }
        if (f.color != 11) { helper.fail("Color not preserved: ${f.color}"); return }
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_id_inference")
    fun pantheon_faction_id_inference(helper: GameTestHelper) {
        val id1 = FactionId.fromDisplayName("Sun Keep")
        if (id1?.toString() != "pantheon:sun_keep") { helper.fail("Expected pantheon:sun_keep, got $id1"); return }
        val id2 = FactionId.fromDisplayName("Void Spire!")
        if (id2?.toString() != "pantheon:void_spire") { helper.fail("Expected pantheon:void_spire, got $id2"); return }
        val id3 = FactionId.fromDisplayName("!!!@##")
        if (id3 != null) { helper.fail("Expected null for invalid name, got $id3"); return }
        helper.succeed()
    }
}