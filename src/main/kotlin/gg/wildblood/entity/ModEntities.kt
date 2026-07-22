package gg.wildblood.entity

import gg.wildblood.Pantheon
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.neoforged.neoforge.registries.DeferredRegister

object ModEntities {
    val REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, Pantheon.MODID)
}