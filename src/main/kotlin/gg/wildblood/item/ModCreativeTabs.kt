package gg.wildblood.item

import gg.wildblood.Pantheon
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

object ModCreativeTabs {
    val REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Pantheon.MODID)

    val EXAMPLE_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = REGISTRY.register("example_tab") { ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pantheon"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon { ModItems.EXAMPLE_ITEM.get().defaultInstance }
            .displayItems { _, output ->
                output.accept(ModItems.EXAMPLE_ITEM.get())
            }
            .build()
    }
}