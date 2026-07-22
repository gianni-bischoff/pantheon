package gg.wildblood.faction

import net.minecraft.resources.ResourceLocation

object FactionId {
    fun fromDisplayName(name: String): ResourceLocation? {
        val cleaned = name.lowercase()
            .replace(" ", "_")
            .replace(Regex("[^a-z0-9_]"), "")
            .replace(Regex("_+"), "_")
            .trim('_')
        if (cleaned.isEmpty()) return null
        return ResourceLocation.fromNamespaceAndPath("pantheon", cleaned)
    }
}