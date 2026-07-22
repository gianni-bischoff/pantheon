package gg.wildblood.blockentity

import gg.wildblood.Pantheon
import gg.wildblood.block.ModBlocks
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object ModBlockEntities {
    val REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Pantheon.MODID)

    val TEMPLE: DeferredHolder<BlockEntityType<*>, BlockEntityType<TempleBlockEntity>> =
        REGISTRY.register("temple") { ->
            BlockEntityType.Builder.of(
                ::TempleBlockEntity,
                ModBlocks.TEMPLE.get(),
            ).build(null)
        }
}