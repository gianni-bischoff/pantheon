package gg.wildblood.client.gui

import gg.wildblood.attachment.ModAttachments
import gg.wildblood.faction.FactionTeamManager
import gg.wildblood.faction.PantheonSavedData
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI
import com.lowdragmc.lowdraglib2.gui.ui.UI
import com.lowdragmc.lowdraglib2.gui.ui.element
import com.lowdragmc.lowdraglib2.gui.ui.dsl
import com.lowdragmc.lowdraglib2.gui.ui.elements.*
import com.lowdragmc.lowdraglib2.gui.sync.rpc.rpcEvent
import com.lowdragmc.lowdraglib2.gui.ui.layout.px
import dev.vfyjxf.taffy.style.FlexDirection
import com.lowdragmc.lowdraglib2.gui.ui.style.Stylesheet
import net.minecraft.world.entity.player.Player

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

    fun create(player: Player): ModularUI {
        val server = player.server
        val data = server?.let { PantheonSavedData.get(it) }
        val factionListStr = data?.factions?.values
            ?.joinToString("\n") { "${it.id}|${it.displayName}" } ?: ""

        val root = element({ cls = { +"panel_bg" } }) {
            label({ text("pantheon.gui.faction_select.title", true) })
            label({ text("pantheon.gui.faction_select.description", true) })

            val joinRpc = this.element.rpcEvent { factionIdStr: String ->
                val srv = player.server ?: return@rpcEvent
                val d = PantheonSavedData.get(srv)
                val rl = runCatching { net.minecraft.resources.ResourceLocation.parse(factionIdStr) }.getOrNull() ?: return@rpcEvent
                val f = d.factions[rl] ?: return@rpcEvent
                val oldFaction = d.findFactionByPlayer(player.uuid)
                d.assignPlayerToFaction(player.uuid, f.id)
                player.setData(ModAttachments.FACTION.get(), f.id)
                FactionTeamManager.syncFaction(srv, f)
                oldFaction?.let { FactionTeamManager.syncFaction(srv, it) }
                for (fac in listOfNotNull(f, oldFaction)) {
                    val anchorBe = srv.overworld().getBlockEntity(fac.anchor) as? gg.wildblood.blockentity.TempleBlockEntity
                    anchorBe?.syncFrom(fac)
                }
                player.closeContainer()
            }

            val factionContainer = com.lowdragmc.lowdraglib2.gui.ui.UIElement()
            factionContainer.layout { it.gapAll(4f).flexDirection(FlexDirection.COLUMN) }

            val factionSync = com.lowdragmc.lowdraglib2.gui.ui.elements.BindableValue<String>()
            factionSync.bind(
                com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder
                    .stringS2C { factionListStr }
                    .syncType(String::class.java)
                    .initialValue(factionListStr)
                    .s2cStrategy(com.lowdragmc.lowdraglib2.gui.sync.bindings.SyncStrategy.CHANGED_PERIODIC)
                    .remoteSetter { serialized ->
                        factionContainer.clearAllChildren()
                        for (entry in serialized.split('\n').filter { it.isNotEmpty() }) {
                            val (idStr, displayName) = entry.split("|", limit = 2)
                            val btn = com.lowdragmc.lowdraglib2.gui.ui.elements.Button()
                            btn.setText(displayName)
                            btn.setActive(true)
                            btn.addClass("faction-btn")
                            btn.layout { it.width(200f) }
                            btn.setOnClick { _ -> joinRpc.send(idStr) }
                            factionContainer.addChild(btn)
                        }
                    }
                    .build()
            )
            factionContainer.addChild(factionSync)
            dsl({ factionContainer }, {}, {})

            button({
                text("pantheon.gui.faction_select.skip", true)
                cls = { +"btn" }; active = true
                onServerClick = { _ -> player.closeContainer() }
            })
        }

        val stylesheet = Stylesheet.parse(LSS)
        return ModularUI.of(UI.of(root, stylesheet), player)
    }
}