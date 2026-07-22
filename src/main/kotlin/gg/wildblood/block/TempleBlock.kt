package gg.wildblood.block

import gg.wildblood.blockentity.TempleBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class TempleBlock(properties: Properties) : Block(properties), EntityBlock {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        TempleBlockEntity(pos, state)

    override fun onDestroyedByPlayer(
        state: BlockState, level: Level, pos: BlockPos, player: net.minecraft.world.entity.player.Player,
        willHarvest: Boolean, fluid: net.minecraft.world.level.material.FluidState,
    ): Boolean {
        if (!player.isCreative) return false
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid)
    }
}