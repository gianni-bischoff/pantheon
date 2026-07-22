package gg.wildblood.data

import gg.wildblood.Pantheon
import net.neoforged.neoforge.data.event.GatherDataEvent

/**
 * Data generation entrypoint.
 *
 * Register data providers (block/item models, blockstates, language, loot tables,
 * tags, recipes, advancements) here in response to [GatherDataEvent].
 *
 * Usage: run the `runData` Gradle task to regenerate everything under
 * `src/generated/resources/`.
 */
object ModDataGenerator {
    fun onGatherData(event: GatherDataEvent) {
        // val generator = event.generator
        // val existingFileHelper = event.existingFileHelper
        // generator.addProvider(MyProvider(generator, existingFileHelper))
    }
}