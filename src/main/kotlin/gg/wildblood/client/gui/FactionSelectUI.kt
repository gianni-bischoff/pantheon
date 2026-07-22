package gg.wildblood.client.gui

import gg.wildblood.Pantheon
import gg.wildblood.attachment.ModAttachments
import gg.wildblood.faction.FactionTeamManager
import gg.wildblood.faction.PantheonSavedData
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI
import com.lowdragmc.lowdraglib2.gui.ui.UI
import com.lowdragmc.lowdraglib2.gui.ui.element
import com.lowdragmc.lowdraglib2.gui.ui.elements.*
import com.lowdragmc.lowdraglib2.gui.ui.layout.px
import dev.vfyjxf.taffy.style.FlexDirection
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture
import com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup
import com.lowdragmc.lowdraglib2.gui.ui.style.Stylesheet
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.DyeColor

object FactionSelectUI {

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
        .faction-btn {
            width: 200;
            height: 24;
            padding-horizontal: 6;
            gap-all: 4;
            background: built-in(ui-gdp:RECT);
        }
        .faction-btn:hover {
            background: built-in(ui-gdp:RECT_SOLID);
        }
    """.trimIndent()

    fun create(player: ServerPlayer): ModularUI {
        val server = player.server!!
        val data = PantheonSavedData.get(server)

        val root = element({ cls = { +"panel_bg" } }) {
            label({ text("pantheon.gui.faction_select.title", true) })
            label({ text("pantheon.gui.faction_select.description", true) })

            element({ layout = { gap { all(4.px) }; flexDirection(FlexDirection.COLUMN) } }) {
                for (faction in data.factions.values) {
                    button({
                        cls = { +"faction-btn" }
                        layout = { width(200.px) }
                    }) {
                        api {
                            setOnServerClick { _ ->
                                val f = data.factions[faction.id] ?: return@setOnServerClick
                                data.assignPlayerToFaction(player.uuid, f.id)
                                player.setData(ModAttachments.FACTION.get(), f.id)
                                FactionTeamManager.addPlayerToFaction(server, f, player.uuid, player.scoreboardName)
                                player.closeContainer()
                            }
                        }
                    }
                }
            }

            button({
                text("pantheon.gui.faction_select.skip", true)
                cls = { +"btn" }
            }) {
                api {
                    setOnServerClick { _ -> player.closeContainer() }
                }
            }
        }

        val stylesheet = Stylesheet.parse(LSS)
        return ModularUI.of(UI.of(root, stylesheet), player)
    }
}