package gg.wildblood.block

import gg.wildblood.blockentity.TempleBlockEntity
import gg.wildblood.faction.PantheonSavedData
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.item.DyeColor
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType
import net.minecraft.server.level.ServerPlayer

class TempleBlock(properties: Properties) : Block(properties), EntityBlock, BlockUIMenuType.BlockUI {
    companion object {
        val COLOR: EnumProperty<DyeColor> = EnumProperty.create("color", DyeColor::class.java)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(COLOR, DyeColor.WHITE))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(COLOR)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        TempleBlockEntity(pos, state)

    override fun onDestroyedByPlayer(
        state: BlockState, level: Level, pos: BlockPos, player: Player,
        willHarvest: Boolean, fluid: FluidState,
    ): Boolean {
        if (!player.isCreative) return false
        if (!level.isClientSide) {
            level.server?.let { PantheonSavedData.get(it).removeFactionAnchor(pos) }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid)
    }

    override fun useWithoutItem(
        state: BlockState, level: Level, pos: BlockPos,
        player: Player, hit: BlockHitResult,
    ): InteractionResult {
        if (!level.isClientSide && player is ServerPlayer) {
            BlockUIMenuType.openUI(player, pos)
        }
        return InteractionResult.SUCCESS
    }

    override fun createUI(holder: BlockUIMenuType.BlockUIHolder): com.lowdragmc.lowdraglib2.gui.ui.ModularUI {
        return gg.wildblood.client.gui.TempleUIFactory.createFactionUI(holder)
    }
}