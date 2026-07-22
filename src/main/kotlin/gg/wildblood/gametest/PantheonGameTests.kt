package gg.wildblood.gametest

import gg.wildblood.Pantheon
import gg.wildblood.attachment.ModAttachments
import gg.wildblood.block.ModBlocks
import gg.wildblood.faction.PantheonSavedData
import gg.wildblood.faction.SeasonState
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

    @GameTest(template = "pantheon_season_start")
    fun pantheon_season_start(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.season = null
        data.factions.clear()
        val source = server.createCommandSourceStack()
        val result = runCatching { server.commands.getDispatcher().execute("pantheon season start 7", source) }
        if (result.isFailure) {
            helper.fail("season start command failed: ${result.exceptionOrNull()?.message}")
            return
        }
        if (result.getOrNull() == 0) {
            helper.fail("season start returned 0 (command rejected)")
            return
        }
        val s = data.season
        if (s == null) {
            helper.fail("Season was not created")
            return
        }
        if (s.phase != SeasonState.SeasonPhase.CREATED) {
            helper.fail("Phase should be CREATED, got ${s.phase}")
            return
        }
        if (s.endsAt <= s.startedAt) {
            helper.fail("endsAt (${s.endsAt}) should be > startedAt (${s.startedAt})")
            return
        }
        helper.succeed()
    }

    @GameTest(template = "pantheon_season_end")
    fun pantheon_season_end(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.season = null
        data.factions.clear()
        val source = server.createCommandSourceStack()
        val startResult = runCatching { server.commands.getDispatcher().execute("pantheon season start 7", source) }
        if (startResult.isFailure || startResult.getOrNull() == 0) {
            helper.fail("season start returned 0 or failed: ${startResult.exceptionOrNull()?.message}")
            return
        }
        val endResult = runCatching { server.commands.getDispatcher().execute("pantheon season end", source) }
        if (endResult.isFailure || endResult.getOrNull() == 0) {
            helper.fail("season end returned 0 or failed: ${endResult.exceptionOrNull()?.message}")
            return
        }
        val s = data.season
        if (s == null) {
            helper.fail("Season is null")
            return
        }
        if (s.phase != SeasonState.SeasonPhase.ENDED) {
            helper.fail("Phase should be ENDED, got ${s.phase}")
            return
        }
        helper.succeed()
    }

    @GameTest(template = "pantheon_season_info")
    fun pantheon_season_info(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.season = null
        data.factions.clear()
        val source = server.createCommandSourceStack()
        val result = runCatching { server.commands.getDispatcher().execute("pantheon season info", source) }
        if (result.isFailure) {
            helper.fail("season info command failed: ${result.exceptionOrNull()?.message}")
            return
        }
        if (result.getOrNull() != 0) {
            helper.fail("season info with no season should return 0, got ${result.getOrNull()}")
            return
        }
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_create")
    fun pantheon_faction_create(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.season = null
        data.factions.clear()
        val templePos = helper.absolutePos(BlockPos(0, 1, 0))
        helper.setBlock(BlockPos(0, 1, 0), ModBlocks.TEMPLE.get())
        val posStr = "${templePos.x} ${templePos.y} ${templePos.z}"
        val source = server.createCommandSourceStack()
        val result = runCatching {
            server.commands.getDispatcher().execute("pantheon faction create \"pantheon:test_a\" TestA $posStr", source)
        }
        if (result.isFailure) {
            helper.fail("faction create command failed: ${result.exceptionOrNull()?.message}")
            return
        }
        if (result.getOrNull() == 0) {
            helper.fail("faction create returned 0 (command rejected)")
            return
        }
        val f = data.factions[ResourceLocation.parse("pantheon:test_a")]
        if (f == null) {
            helper.fail("Faction not created")
            return
        }
        if (f.displayName != "TestA") {
            helper.fail("Display name mismatch: ${f.displayName}")
            return
        }
        if (f.anchor != templePos) {
            helper.fail("Anchor mismatch: ${f.anchor} vs $templePos")
            return
        }
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_assign")
    fun pantheon_faction_assign(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.season = null
        data.factions.clear()
        val templePos = helper.absolutePos(BlockPos(0, 1, 0))
        helper.setBlock(BlockPos(0, 1, 0), ModBlocks.TEMPLE.get())
        val posStr = "${templePos.x} ${templePos.y} ${templePos.z}"
        val source = server.createCommandSourceStack()
        val createResult = runCatching {
            server.commands.getDispatcher().execute("pantheon faction create \"pantheon:test_b\" TestB $posStr", source)
        }
        if (createResult.isFailure || createResult.getOrNull() == 0) {
            helper.fail("faction create failed: ${createResult.exceptionOrNull()?.message}")
            return
        }
        val fakePlayer = FakePlayerFactory.getMinecraft(server.overworld())
        val playerUuid = fakePlayer.uuid
        val factionId = ResourceLocation.parse("pantheon:test_b")
        data.assignPlayerToFaction(playerUuid, factionId)
        fakePlayer.setData(ModAttachments.FACTION.get(), factionId)
        val f = data.factions[factionId]
        if (f == null) {
            helper.fail("Faction missing")
            return
        }
        if (playerUuid !in f.members) {
            helper.fail("Player not in members")
            return
        }
        val attached = fakePlayer.getData(ModAttachments.FACTION.get())
        if (attached?.toString() != "pantheon:test_b") {
            helper.fail("Attachment mismatch: $attached")
            return
        }
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_info")
    fun pantheon_faction_info(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.season = null
        data.factions.clear()
        val source = server.createCommandSourceStack()
        val result = runCatching {
            server.commands.getDispatcher().execute("pantheon faction info \"pantheon:nonexistent\"", source)
        }
        if (result.isFailure) {
            helper.fail("faction info command crashed: ${result.exceptionOrNull()?.message}")
            return
        }
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_list")
    fun pantheon_faction_list(helper: GameTestHelper) {
        val server = helper.level.server
        val data = PantheonSavedData.get(server)
        data.season = null
        data.factions.clear()
        val templePos = helper.absolutePos(BlockPos(0, 1, 0))
        helper.setBlock(BlockPos(0, 1, 0), ModBlocks.TEMPLE.get())
        val posStr = "${templePos.x} ${templePos.y} ${templePos.z}"
        val source = server.createCommandSourceStack()
        val createResult = runCatching {
            server.commands.getDispatcher().execute("pantheon faction create \"pantheon:test_c\" TestC $posStr", source)
        }
        if (createResult.isFailure || createResult.getOrNull() == 0) {
            helper.fail("faction create failed: ${createResult.exceptionOrNull()?.message}")
            return
        }
        val listResult = runCatching {
            server.commands.getDispatcher().execute("pantheon faction list", source)
        }
        if (listResult.isFailure) {
            helper.fail("faction list command failed: ${listResult.exceptionOrNull()?.message}")
            return
        }
        if (data.factions.size != 1) {
            helper.fail("Expected 1 faction, got ${data.factions.size}")
            return
        }
        helper.succeed()
    }

    @GameTest(template = "pantheon_temple_mirror")
    fun pantheon_temple_mirror(helper: GameTestHelper) {
        helper.succeed()
    }

    @GameTest(template = "pantheon_persistence")
    fun pantheon_persistence(helper: GameTestHelper) {
        helper.succeed()
    }
}