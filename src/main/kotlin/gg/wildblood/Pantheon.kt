package gg.wildblood

import com.mojang.logging.LogUtils
import gg.wildblood.attachment.ModAttachments
import gg.wildblood.block.ModBlocks
import gg.wildblood.blockentity.ModBlockEntities
import gg.wildblood.config.Config
import gg.wildblood.data.ModDataGenerator
import gg.wildblood.entity.ModEntities
import gg.wildblood.item.ModCreativeTabs
import gg.wildblood.item.ModItems
import gg.wildblood.season.SeasonManager
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Blocks
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegisterEvent
import org.slf4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/**
 * Pantheon mod root.
 *
 * This object is the single [Mod]-annotated entrypoint loaded by KotlinForForge.
 * All [DeferredRegister] hubs in `Mod*` companion objects are wired up here so
 * there is one obvious place to see what the mod registers.
 *
 * Per-feature logic should live in its own package (block, item, entity, ...).
 * Keep this file a thin registry/wiring hub - add feature code in dedicated
 * packages instead of growing this object.
 */
@Mod(Pantheon.MODID)
object Pantheon {
    const val MODID = "pantheon"
    val LOGGER: Logger = LogUtils.getLogger()

    /** All DeferredRegisters that should fire on the mod event bus. */
    private val REGISTRIES: List<DeferredRegister<*>> = listOf(
        ModBlocks.REGISTRY,
        ModItems.REGISTRY,
        ModCreativeTabs.REGISTRY,
        ModEntities.REGISTRY,
        ModBlockEntities.REGISTRY,
        ModAttachments.REGISTRY,
    )

    init {
        MOD_BUS.addListener(::commonSetup)
        MOD_BUS.addListener(::onRegister)
        MOD_BUS.addListener(::onRegisterPayloads)
        MOD_BUS.addListener(ModDataGenerator::onGatherData)

        REGISTRIES.forEach { it.register(MOD_BUS) }

        NeoForge.EVENT_BUS.register(this)
        NeoForge.EVENT_BUS.register(SeasonManager)

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

    private fun onRegister(event: RegisterEvent) {
        // Cross-registry fixups that need to run at register time go here.
        // Prefer adding entries directly to the relevant Mod* object over using this.
    }

    private fun onRegisterPayloads(event: RegisterPayloadHandlersEvent) {
        gg.wildblood.network.ModPayloads.register(event.registrar("1"))
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        LOGGER.info("HELLO from server starting")
    }
}