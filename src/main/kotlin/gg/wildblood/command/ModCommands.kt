package gg.wildblood.command

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack

/**
 * Custom server-side commands.
 *
 * Register commands by responding to the `RegisterCommandsEvent` on the
 * NeoForge event bus (wired up in [gg.wildblood.Pantheon.init]).
 */
object ModCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        // dispatcher.register(CommandBuilder.literal("pantheon")...)
    }
}