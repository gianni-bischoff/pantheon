package gg.wildblood.config

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.common.ModConfigSpec

/**
 * Pantheon common config.
 *
 * Values are accessed as [ModConfigSpec] typed properties on this object.
 * The built [SPEC] is registered with the mod container in [gg.wildblood.Pantheon].
 */
object Config {
    private val BUILDER = ModConfigSpec.Builder()

    val LOG_DIRT_BLOCK: ModConfigSpec.BooleanValue = BUILDER
        .comment("Whether to log the dirt block on common setup")
        .define("logDirtBlock", true)

    val MAGIC_NUMBER: ModConfigSpec.IntValue = BUILDER
        .comment("A magic number")
        .defineInRange("magicNumber", 42, 0, Int.MAX_VALUE)

    val MAGIC_NUMBER_INTRODUCTION: ModConfigSpec.ConfigValue<String> = BUILDER
        .comment("What you want the introduction message to be for the magic number")
        .define("magicNumberIntroduction", "The magic number is... ")

    val ITEM_STRINGS: ModConfigSpec.ConfigValue<List<String>> = BUILDER
        .comment("A list of items to log on common setup.")
        .defineListAllowEmpty("items", listOf("minecraft:iron_ingot"), { "" }) { obj ->
            obj is String && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(obj))
        }

    val SPEC: ModConfigSpec = BUILDER.build()
}