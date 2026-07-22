package gg.wildblood.attachment

import gg.wildblood.Pantheon
import java.util.UUID
import net.minecraft.core.UUIDUtil
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries

object ModAttachments {
    val REGISTRY = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Pantheon.MODID)

    val FACTION: DeferredHolder<AttachmentType<*>, AttachmentType<ResourceLocation?>> =
        REGISTRY.register("faction") { ->
            AttachmentType.builder<ResourceLocation?> { -> null }
                .serialize(
                    ResourceLocation.CODEC.optionalFieldOf("faction")
                        .xmap(
                            { opt: java.util.Optional<ResourceLocation> -> opt.orElse(null) },
                            { rl: ResourceLocation? -> java.util.Optional.ofNullable(rl) },
                        )
                        .codec()
                )
                .copyOnDeath()
                .build()
        }

    val BANK_ACCOUNT: DeferredHolder<AttachmentType<*>, AttachmentType<UUID?>> =
        REGISTRY.register("bank_account") { ->
            AttachmentType.builder<UUID?> { -> null }
                .serialize(
                    UUIDUtil.CODEC.optionalFieldOf("bank")
                        .xmap(
                            { opt: java.util.Optional<UUID> -> opt.orElse(null) },
                            { uuid: UUID? -> java.util.Optional.ofNullable(uuid) },
                        )
                        .codec()
                )
                .copyOnDeath()
                .build()
        }
}