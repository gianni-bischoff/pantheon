package gg.wildblood.gametest

import gg.wildblood.Pantheon
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.neoforged.neoforge.gametest.GameTestHolder
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate

@GameTestHolder(Pantheon.MODID)
@PrefixGameTestTemplate(false)
class PantheonGameTests {

    @GameTest(template = "pantheon_season_start")
    fun pantheon_season_start(helper: GameTestHelper) {
        helper.succeed()
    }

    @GameTest(template = "pantheon_season_end")
    fun pantheon_season_end(helper: GameTestHelper) {
        helper.succeed()
    }

    @GameTest(template = "pantheon_season_info")
    fun pantheon_season_info(helper: GameTestHelper) {
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