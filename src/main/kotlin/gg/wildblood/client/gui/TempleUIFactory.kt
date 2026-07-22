package gg.wildblood.client.gui

import gg.wildblood.Pantheon
import gg.wildblood.attachment.ModAttachments
import gg.wildblood.block.TempleBlock
import gg.wildblood.faction.Faction
import gg.wildblood.faction.FactionId
import gg.wildblood.faction.FactionTeamManager
import gg.wildblood.faction.PantheonSavedData
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI
import com.lowdragmc.lowdraglib2.gui.ui.UI
import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import com.lowdragmc.lowdraglib2.gui.ui.element
import com.lowdragmc.lowdraglib2.gui.ui.elements.*
import com.lowdragmc.lowdraglib2.gui.ui.Element
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents
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
        .faction-row {
            width: 100%;
            height: 24;
            padding-horizontal: 6;
            gap-all: 4;
            background: built-in(ui-gdp:RECT);
        }
        .faction-row:hover {
            background: built-in(ui-gdp:RECT_SOLID);
        }
    """.trimIndent()

    private fun swatchTexture(dye: DyeColor, selected: Boolean): GuiTextureGroup {
        val fill = ColorRectTexture(0xFF000000.toInt() or dye.mapColor.col)
        return if (selected) {
            GuiTextureGroup(fill, ColorBorderTexture(0xFFFFFFFF.toInt(), 3))
        } else {
            GuiTextureGroup(fill, ColorBorderTexture(0xFF555555.toInt(), 1))
        }
    }

    private fun dyeColor(index: Int): DyeColor = DyeColor.byId(index)

    fun createFactionUI(holder: BlockUIMenuType.BlockUIHolder): ModularUI {
        val server = holder.player.server
        val data = server?.let { PantheonSavedData.get(it) }
        val faction = data?.factions?.values?.find { it.anchor == holder.pos }

        val root = if (faction != null) {
            createManageFactionUI(holder, data!!, faction)
        } else {
            createNewFactionUI(holder, data)
        }

        val stylesheet = Stylesheet.parse(LSS)
        return ModularUI.of(UI.of(root, stylesheet), holder.player)
    }

    private fun createNewFactionUI(
        holder: BlockUIMenuType.BlockUIHolder,
        data: PantheonSavedData?,
    ) = element({ cls = { +"panel_bg" } }) {
        var name = ""
        var selectedColor = 0
        val player = holder.player
        val server = player.server
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
                    layout = { width(22.px); height(22.px) }
                    style = { background(swatchTexture(dyeColor(i), i == 0)) }
                }) {
                    swatchElements.add(this.element)
                    api {
                        setOnServerClick { _ ->
                            selectedColor = i
                            swatchElements.forEachIndexed { idx, el ->
                                el.style.background(swatchTexture(dyeColor(idx), idx == i))
                            }
                        }
                    }
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
            cls = { +"btn" }
        }) {
            api {
                setOnServerClick { _ ->
                    if (name.isBlank()) return@setOnServerClick
                    val id = FactionId.fromDisplayName(name) ?: return@setOnServerClick
                    val s = server ?: return@setOnServerClick
                    val d = data ?: PantheonSavedData.get(s)
                    val existing = d.factions[id]
                    if (existing != null && existing.anchor != BlockPos.ZERO) return@setOnServerClick
                    val f = d.createFaction(name, selectedColor, holder.pos) ?: return@setOnServerClick
                    FactionTeamManager.syncFaction(s, f)
                    s.overworld().setBlock(
                        holder.pos,
                        holder.blockState.setValue(TempleBlock.COLOR, DyeColor.byId(selectedColor)),
                        3
                    )
                    player.closeContainer()
                }
            }
        }
    }

    private fun createManageFactionUI(
        holder: BlockUIMenuType.BlockUIHolder,
        data: PantheonSavedData,
        faction: Faction,
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
                    layout = { width(22.px); height(22.px) }
                    style = { background(swatchTexture(dyeColor(i), i == selectedColor)) }
                }) {
                    swatchElements.add(this.element)
                    api {
                        setOnServerClick { _ ->
                            selectedColor = i
                            swatchElements.forEachIndexed { idx, el ->
                                el.style.background(swatchTexture(dyeColor(idx), idx == i))
                            }
                        }
                    }
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
                cls = { +"btn" }
            }) {
                api {
                    setOnServerClick { _ ->
                        val s = server ?: return@setOnServerClick
                        data.updateFaction(faction.id, name, selectedColor)
                        FactionTeamManager.syncFaction(s, faction)
                        s.overworld().setBlock(
                            holder.pos,
                            holder.blockState.setValue(TempleBlock.COLOR, DyeColor.byId(selectedColor)),
                            3
                        )
                        player.closeContainer()
                    }
                }
            }
        } else {
            button({
                text("pantheon.gui.faction_manage.close", true)
                cls = { +"btn" }
            }) {
                api {
                    setOnServerClick { _ -> player.closeContainer() }
                }
            }
        }
    }
}