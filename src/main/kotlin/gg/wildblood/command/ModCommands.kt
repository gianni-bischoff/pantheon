package gg.wildblood.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import gg.wildblood.attachment.ModAttachments
import gg.wildblood.block.ModBlocks
import gg.wildblood.faction.PantheonSavedData
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

object ModCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val root = Commands.literal("pantheon").requires { it.hasPermission(2) }

        root.then(Commands.literal("faction")
            .then(Commands.literal("create")
                .then(Commands.argument("id", StringArgumentType.string())
                    .then(Commands.argument("displayName", StringArgumentType.string())
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                            .executes { createFaction(it) }))))
            .then(Commands.literal("assign")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("factionId", StringArgumentType.string())
                        .executes { assignFaction(it) })))
            .then(Commands.literal("info")
                .then(Commands.argument("factionId", StringArgumentType.string())
                    .executes { factionInfo(it) }))
            .then(Commands.literal("list")
                .executes { factionList(it) }))

        dispatcher.register(root)
    }

    private fun createFaction(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val idStr = StringArgumentType.getString(ctx, "id")
        val displayName = StringArgumentType.getString(ctx, "displayName")
        val rl = runCatching { ResourceLocation.parse(idStr) }.getOrNull()
        if (rl == null) {
            source.sendFailure(Component.translatable("pantheon.command.faction.invalid_id", idStr))
            return 0
        }
        val pos = BlockPosArgument.getBlockPos(ctx, "pos")
        val level = source.level
        val blockState = level.getBlockState(pos)
        if (blockState.block != ModBlocks.TEMPLE.get()) {
            source.sendFailure(Component.translatable("pantheon.command.faction.create.no_temple", pos.toShortString()))
            return 0
        }
        val data = PantheonSavedData.get(source.server)
        if (data.factions.containsKey(rl)) {
            source.sendFailure(Component.translatable("pantheon.command.faction.create.exists", rl.toString()))
            return 0
        }
        data.ensureFaction(rl, displayName, pos)
        source.sendSuccess({ Component.translatable("pantheon.command.faction.create.success", displayName, pos.toShortString()) }, true)
        return 1
    }

    private fun assignFaction(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val player = EntityArgument.getPlayer(ctx, "player")
        val factionIdStr = StringArgumentType.getString(ctx, "factionId")
        val rl = runCatching { ResourceLocation.parse(factionIdStr) }.getOrNull()
        if (rl == null) {
            source.sendFailure(Component.translatable("pantheon.command.faction.invalid_id", factionIdStr))
            return 0
        }
        val data = PantheonSavedData.get(source.server)
        if (!data.factions.containsKey(rl)) {
            source.sendFailure(Component.translatable("pantheon.command.faction.info.none", rl.toString()))
            return 0
        }
        data.assignPlayerToFaction(player.uuid, rl)
        player.setData(ModAttachments.FACTION.get(), rl)
        source.sendSuccess({ Component.translatable("pantheon.command.faction.assign.success", player.name, rl.toString()) }, true)
        return 1
    }

    private fun factionInfo(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val factionIdStr = StringArgumentType.getString(ctx, "factionId")
        val rl = runCatching { ResourceLocation.parse(factionIdStr) }.getOrNull()
        if (rl == null) {
            source.sendFailure(Component.translatable("pantheon.command.faction.invalid_id", factionIdStr))
            return 0
        }
        val data = PantheonSavedData.get(source.server)
        val f = data.factions[rl]
        if (f == null) {
            source.sendFailure(Component.translatable("pantheon.command.faction.info.none", rl.toString()))
            return 0
        }
        val godStr = f.godId?.toString() ?: "none"
        val mayorStr = f.mayor?.toString() ?: "none"
        source.sendSuccess({ Component.translatable("pantheon.command.faction.info.detail", f.displayName, godStr, mayorStr, f.memberCount, f.skillpointPool) }, false)
        return 1
    }

    private fun factionList(ctx: CommandContext<CommandSourceStack>): Int {
        val source = ctx.source
        val data = PantheonSavedData.get(source.server)
        val factions = data.factions.values
        if (factions.isEmpty()) {
            source.sendSuccess({ Component.translatable("pantheon.command.faction.list.empty") }, false)
            return 0
        }
        source.sendSuccess({ Component.translatable("pantheon.command.faction.list.header", factions.size) }, false)
        for (f in factions) {
            val godStr = f.godId?.toString() ?: "none"
            source.sendSuccess({ Component.translatable("pantheon.command.faction.list.entry", f.displayName, f.id.toString(), f.memberCount, godStr) }, false)
        }
        return factions.size
    }
}