package gg.wildblood.client

import gg.wildblood.Pantheon
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import thedarkcolour.kotlinforforge.neoforge.forge.LOADING_CONTEXT

/**
 * Client-only setup. Loaded exclusively on [Dist.CLIENT].
 *
 * - Registers the config screen factory so the in-game Mods list can open our config.
 * - Serves as the home for any client-side registration that must happen on the mod bus
 *   (renderers, key mappings, screen factories, etc.).
 */
@Mod(value = Pantheon.MODID, dist = [Dist.CLIENT])
@EventBusSubscriber(modid = Pantheon.MODID, value = [Dist.CLIENT])
object PantheonClient {
    init {
        LOADING_CONTEXT.activeContainer.registerExtensionPoint(IConfigScreenFactory::class.java) { ->
            IConfigScreenFactory { container, parent -> ConfigurationScreen(container, parent) }
        }
    }
}