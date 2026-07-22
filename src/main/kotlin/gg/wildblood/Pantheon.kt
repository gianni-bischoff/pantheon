package gg.wildblood

import com.mojang.logging.LogUtils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import org.slf4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(Pantheon.MODID)
object Pantheon {
    const val MODID = "pantheon"
    val LOGGER: Logger = LogUtils.getLogger()

    val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(MODID)
    val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MODID)
    val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)

    val EXAMPLE_BLOCK: DeferredBlock<Block> =
        BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE))

    val EXAMPLE_BLOCK_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK)

    val EXAMPLE_ITEM: DeferredItem<Item> = ITEMS.registerSimpleItem(
        "example_item",
        Item.Properties().food(
            FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build(),
        ),
    )

    val EXAMPLE_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = CREATIVE_MODE_TABS.register("example_tab") { ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pantheon"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon { EXAMPLE_ITEM.get().defaultInstance }
            .displayItems { _, output ->
                output.accept(EXAMPLE_ITEM.get())
            }
            .build()
    }

    init {
        MOD_BUS.addListener(::commonSetup)
        MOD_BUS.addListener(::addCreative)

        BLOCKS.register(MOD_BUS)
        ITEMS.register(MOD_BUS)
        CREATIVE_MODE_TABS.register(MOD_BUS)

        NeoForge.EVENT_BUS.register(this)

        LOADING_CONTEXT.activeContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("HELLO FROM COMMON SETUP")

        if (Config.LOG_DIRT_BLOCK.get()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT))
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.get())

        Config.ITEM_STRINGS.get().forEach { item -> LOGGER.info("ITEM >> {}", item) }
    }

    private fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM)
        }
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        LOGGER.info("HELLO from server starting")
    }
}