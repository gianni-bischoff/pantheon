package gg.wildblood

import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import thedarkcolour.kotlinforforge.neoforge.forge.LOADING_CONTEXT

@Mod(value = Pantheon.MODID, dist = [Dist.CLIENT])
@EventBusSubscriber(modid = Pantheon.MODID, value = [Dist.CLIENT])
object PantheonClient {
    init {
        LOADING_CONTEXT.activeContainer.registerExtensionPoint(IConfigScreenFactory::class.java) { ->
            IConfigScreenFactory { container, parent -> ConfigurationScreen(container, parent) }
        }
    }

    @SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        Pantheon.LOGGER.info("HELLO FROM CLIENT SETUP")
        Pantheon.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
    }
}