package gg.wildblood.login

import gg.wildblood.Pantheon
import gg.wildblood.attachment.ModAttachments
import gg.wildblood.faction.FactionTeamManager
import gg.wildblood.faction.PantheonSavedData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.util.FakePlayerFactory
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType

object FactionLoginHandler {
    val FACTION_SELECT_UI_ID = ResourceLocation.fromNamespaceAndPath(Pantheon.MODID, "faction_select")

    @SubscribeEvent
    fun onPlayerLogin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        val faction = player.getData(ModAttachments.FACTION.get())
        val server = player.server
        val data = PantheonSavedData.get(server)
        if (faction == null) {
            if (data.factions.isNotEmpty()) {
                PlayerUIMenuType.openUI(player, FACTION_SELECT_UI_ID)
            }
        } else {
            val f = data.factions[faction]
            if (f != null) {
                FactionTeamManager.addPlayerToFaction(server, f, player.uuid, player.scoreboardName)
            }
        }
    }
}