package gg.wildblood.client.gui

import gg.wildblood.Pantheon
import gg.wildblood.block.TempleBlock
import gg.wildblood.faction.Faction
import gg.wildblood.faction.FactionId
import gg.wildblood.faction.PantheonSavedData
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI
import com.lowdragmc.lowdraglib2.gui.ui.UI
import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import com.lowdragmc.lowdraglib2.gui.ui.element
import com.lowdragmc.lowdraglib2.gui.ui.elements.*
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType
import com.lowdragmc.lowdraglib2.gui.ui.layout.px
import dev.vfyjxf.taffy.style.FlexDirection
import dev.vfyjxf.taffy.style.FlexWrap
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture
import com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup
import com.lowdragmc.lowdraglib2.gui.ui.style.Stylesheet
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

object TempleUIFactory {

    private val DYE_COLORS = DyeColor.entries

    private val LSS = """
        .panel_bg {
            background: built-in(ui-gdp:BORDER);
            padding-all: 10;
            gap-all: 6;
        }
        .panel_bg label {
            horizontal-align: center;
            color: 0xFFFFFFFF;
        }
        .btn {
            width: 80;
            height: 20;
            background: built-in(ui-gdp:RECT);
        }
        .btn:hover {
            background: built-in(ui-gdp:RECT_SOLID);
        }
        .swatch {
            width: 22;
            height: 22;
        }
        .swatch.selected {
            border: 3;
            border-color: 0xFFFFFFFF;
        }
        .swatch.unselected {
            border: 1;
            border-color: 0xFF666666;
        }
    """.trimIndent()

    private fun swatchTexture(dye: DyeColor): ColorRectTexture =
        ColorRectTexture(0xFF000000.toInt() or dye.mapColor.col)

    fun createFactionUI(holder: BlockUIMenuType.BlockUIHolder): ModularUI {
        val server = holder.player.server
        val data = server?.let { PantheonSavedData.get(it) }
        val faction = data?.factions?.values?.find { it.anchor == holder.pos }
        val be = holder.player.level().getBlockEntity(holder.pos) as? gg.wildblood.blockentity.TempleBlockEntity

        val root = if (faction != null) {
            createManageFactionUI(holder, faction, be)
        } else {
            createNewFactionUI(holder, be)
        }

        val stylesheet = Stylesheet.parse(LSS)
        return ModularUI.of(UI.of(root, stylesheet), holder.player)
    }

    private fun createNewFactionUI(
        holder: BlockUIMenuType.BlockUIHolder,
        be: gg.wildblood.blockentity.TempleBlockEntity?,
    ) = element({ cls = { +"panel_bg" } }) {
        var name = ""
        var selectedColor = 0
        val swatchElements = mutableListOf<UIElement>()

        label({ text("pantheon.gui.faction_create.title", true) })
        label({ text("pantheon.gui.faction_create.name", true) })

        textField({ layout = { width(180.px) } }) {
            observer { name = it }
            dataSource { name }
        }

        label({ text("pantheon.gui.faction_create.color", true) })

        element({ layout = { gap { all(3.px) }; wrap(FlexWrap.WRAP); flexDirection(FlexDirection.ROW) } }) {
            for (i in 0 until 16) {
                button({
                    noText(); active = true
                    cls = { +"swatch"; +if (i == 0) "selected" else "unselected" }
                    layout = { width(22.px); height(22.px) }
                    style = { background(swatchTexture(dyeColor(i))) }
                    onClick = {
                        Pantheon.LOGGER.info("SWATCH CLICK {}", i)
                        selectedColor = i
                        swatchElements.forEachIndexed { idx, el ->
                            el.removeClass("selected")
                            el.removeClass("unselected")
                            el.addClass(if (idx == i) "selected" else "unselected")
                        }
                    }
                }) {
                    swatchElements.add(this.element)
                }
            }
        }

        label({}) {
            dataSource {
                val id = FactionId.fromDisplayName(name)
                Component.translatable("pantheon.gui.faction_create.id_preview", id?.toString() ?: "—")
            }
        }

        button({
            text("pantheon.gui.faction_create.create", true)
            cls = { +"btn" }; active = true
            onClick = {
                Pantheon.LOGGER.info("CREATE CLICK name={} color={} be={}", name, selectedColor, be)
                if (name.isNotBlank()) {
                    be?.rpcToServer("rpcCreateFaction", name, selectedColor)
                }
            }
        })
    }

    private fun createManageFactionUI(
        holder: BlockUIMenuType.BlockUIHolder,
        faction: Faction,
        be: gg.wildblood.blockentity.TempleBlockEntity?,
    ) = element({ cls = { +"panel_bg" } }) {
        var name = faction.displayName
        var selectedColor = faction.color
        val player = holder.player
        val server = player.server
        val isAdmin = player.hasPermissions(2)
        val swatchElements = mutableListOf<UIElement>()

        label({ text("pantheon.gui.faction_manage.title", true) })
        label({ text("pantheon.gui.faction_create.name", true) })

        textField({ layout = { width(180.px) } }) {
            observer { name = it }
            dataSource { name }
        }

        label({ text("pantheon.gui.faction_create.color", true) })

        element({ layout = { gap { all(3.px) }; wrap(FlexWrap.WRAP); flexDirection(FlexDirection.ROW) } }) {
            for (i in 0 until 16) {
                button({
                    noText(); active = true
                    cls = { +"swatch"; +if (i == selectedColor) "selected" else "unselected" }
                    layout = { width(22.px); height(22.px) }
                    style = { background(swatchTexture(dyeColor(i))) }
                    onClick = {
                        selectedColor = i
                        swatchElements.forEachIndexed { idx, el ->
                            el.removeClass("selected")
                            el.removeClass("unselected")
                            el.addClass(if (idx == i) "selected" else "unselected")
                        }
                    }
                }) {
                    swatchElements.add(this.element)
                }
            }
        }

        label({ text = Component.translatable("pantheon.gui.faction_manage.members", faction.memberCount) })

        element({ layout = { gap { all(2.px) }; flexDirection(FlexDirection.COLUMN) } }) {
            for (uuid in faction.members) {
                val profile = server?.profileCache?.get(uuid)?.orElse(null)
                if (profile != null) {
                    label({ text("• ${profile.name}", false) })
                }
            }
        }

        if (isAdmin) {
            button({
                text("pantheon.gui.faction_manage.save", true)
                cls = { +"btn" }; active = true
                onClick = {
                    be?.rpcToServer("rpcUpdateFaction", name, selectedColor)
                }
            })
        } else {
            button({
                text("pantheon.gui.faction_manage.close", true)
                cls = { +"btn" }; active = true
                onClick = { player.closeContainer() }
            })
        }
    }

    private fun dyeColor(index: Int): DyeColor = DyeColor.byId(index)
}