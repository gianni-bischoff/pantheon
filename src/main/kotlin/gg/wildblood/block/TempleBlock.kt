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

    override fun playerWillDestroy(
        level: Level, pos: BlockPos, state: BlockState, player: net.minecraft.world.entity.player.Player,
    ): BlockState {
        if (!player.isCreative) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS)
            return state
        }
        return super.playerWillDestroy(level, pos, state, player)
    }
}