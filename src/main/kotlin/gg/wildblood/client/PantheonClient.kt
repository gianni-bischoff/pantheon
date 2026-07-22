package gg.wildblood.client

import gg.wildblood.Pantheon
import gg.wildblood.block.ModBlocks
import gg.wildblood.block.TempleBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.core.BlockPos
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import thedarkcolour.kotlinforforge.neoforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(value = Pantheon.MODID, dist = [Dist.CLIENT])
object PantheonClient {
    init {
        LOADING_CONTEXT.activeContainer.registerExtensionPoint(IConfigScreenFactory::class.java) { ->
            IConfigScreenFactory { container, parent -> ConfigurationScreen(container, parent) }
        }
        MOD_BUS.addListener(::onRegisterBlockColors)
    }

    private fun onRegisterBlockColors(event: RegisterColorHandlersEvent.Block) {
        event.register(
            { state: BlockState, _: BlockAndTintGetter?, _: BlockPos?, tintIndex: Int ->
                if (tintIndex == 0) state.getValue(TempleBlock.COLOR).mapColor.col else -1
            },
            ModBlocks.TEMPLE.get()
        )
    }
}