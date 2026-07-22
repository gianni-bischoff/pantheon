package gg.wildblood.season

import gg.wildblood.Pantheon
import gg.wildblood.blockentity.TempleBlockEntity
import gg.wildblood.faction.PantheonSavedData
import gg.wildblood.faction.SeasonState
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

object SeasonManager {
    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent.Pre) {
        val server = event.server
        val data = PantheonSavedData.get(server)
        data.season?.let { s ->
            if (s.phase == SeasonState.SeasonPhase.RUNNING && s.endsAt > 0 && System.currentTimeMillis() >= s.endsAt) {
                data.endSeason()
                Pantheon.LOGGER.info("Season {} ended (duration elapsed)", s.id)
            }
        }
        if (server.tickCount % 20 == 0) {
            data.factions.values.forEach { faction ->
                (server.overworld().getBlockEntity(faction.anchor) as? TempleBlockEntity)
                    ?.syncFrom(faction, data.season)
            }
        }
    }
}