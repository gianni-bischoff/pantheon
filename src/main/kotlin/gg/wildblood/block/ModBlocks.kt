package gg.wildblood.block

import gg.wildblood.Pantheon
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredRegister

object ModBlocks {
    val REGISTRY = DeferredRegister.createBlocks(Pantheon.MODID)

    val EXAMPLE_BLOCK: DeferredBlock<Block> =
        REGISTRY.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE))
}