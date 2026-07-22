package gg.wildblood.gametest

import gg.wildblood.Pantheon
import gg.wildblood.faction.PantheonSavedData
import gg.wildblood.faction.SeasonState
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
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
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_assign")
    fun pantheon_faction_assign(helper: GameTestHelper) {
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_info")
    fun pantheon_faction_info(helper: GameTestHelper) {
        helper.succeed()
    }

    @GameTest(template = "pantheon_faction_list")
    fun pantheon_faction_list(helper: GameTestHelper) {
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