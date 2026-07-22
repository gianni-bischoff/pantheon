package gg.wildblood.item

import gg.wildblood.Pantheon
import gg.wildblood.block.ModBlocks
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems {
    val REGISTRY = DeferredRegister.createItems(Pantheon.MODID)

    val EXAMPLE_BLOCK_ITEM: DeferredItem<BlockItem> =
        REGISTRY.registerSimpleBlockItem("example_block", ModBlocks.EXAMPLE_BLOCK)

    val TEMPLE_ITEM: DeferredItem<BlockItem> =
        REGISTRY.registerSimpleBlockItem("temple", ModBlocks.TEMPLE)

    val EXAMPLE_ITEM: DeferredItem<Item> = REGISTRY.registerSimpleItem(
        "example_item",
        Item.Properties().food(
            FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build(),
        ),
    )
}