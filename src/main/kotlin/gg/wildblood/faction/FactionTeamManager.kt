package gg.wildblood.faction

import java.util.UUID
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.DyeColor
import net.minecraft.world.scores.Scoreboard

object FactionTeamManager {
    private val DYE_TO_FORMATTING = mapOf(
        0 to ChatFormatting.WHITE,
        1 to ChatFormatting.GOLD,
        2 to ChatFormatting.LIGHT_PURPLE,
        3 to ChatFormatting.AQUA,
        4 to ChatFormatting.YELLOW,
        5 to ChatFormatting.GREEN,
        6 to ChatFormatting.LIGHT_PURPLE,
        7 to ChatFormatting.DARK_GRAY,
        8 to ChatFormatting.GRAY,
        9 to ChatFormatting.AQUA,
        10 to ChatFormatting.DARK_PURPLE,
        11 to ChatFormatting.DARK_BLUE,
        12 to ChatFormatting.DARK_RED,
        13 to ChatFormatting.DARK_GREEN,
        14 to ChatFormatting.DARK_RED,
        15 to ChatFormatting.BLACK,
    )

    fun syncFaction(server: MinecraftServer, faction: Faction) {
        val scoreboard = server.scoreboard
        val teamName = faction.id.toString()
        var team = scoreboard.getPlayerTeam(teamName)
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName)
        }
        team.displayName = Component.literal(faction.displayName)
        team.color = DYE_TO_FORMATTING[faction.color] ?: ChatFormatting.WHITE
        val currentMembers = team.players.toSet()
        for (uuid in faction.members) {
            val profile = server.profileCache?.get(uuid)?.orElse(null) ?: continue
            if (profile.name !in currentMembers) {
                scoreboard.addPlayerToTeam(profile.name, team)
            }
        }
        for (name in currentMembers) {
            val profile = server.profileCache?.get(name)?.orElse(null) ?: continue
            if (profile.id !in faction.members) {
                scoreboard.removePlayerFromTeam(name, team)
            }
        }
    }

    fun addPlayerToFaction(server: MinecraftServer, faction: Faction, playerUuid: UUID, playerName: String) {
        val scoreboard = server.scoreboard
        val teamName = faction.id.toString()
        val team = scoreboard.getPlayerTeam(teamName) ?: run {
            syncFaction(server, faction)
            scoreboard.getPlayerTeam(teamName)
        } ?: return
        if (playerName !in team.players) {
            scoreboard.addPlayerToTeam(playerName, team)
        }
    }
}