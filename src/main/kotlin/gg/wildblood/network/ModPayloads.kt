package gg.wildblood.network

import gg.wildblood.Pantheon
import net.neoforged.neoforge.network.registration.PayloadRegistrar

/**
 * Custom network payload registration.
 *
 * Register play-to-client and play-to-server payloads via the provided
 * [PayloadRegistrar] in response to the RegisterPayloadHandlersEvent
 * (wired up in [Pantheon.init]).
 */
object ModPayloads {
    fun register(registrar: PayloadRegistrar) {
        // registrar.playBidirectional(...)
    }
}