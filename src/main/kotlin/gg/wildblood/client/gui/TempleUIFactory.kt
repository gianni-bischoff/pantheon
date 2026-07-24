package gg.wildblood.client.gui

import gg.wildblood.Pantheon
import gg.wildblood.block.TempleBlock
import gg.wildblood.faction.FactionId
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI
import com.lowdragmc.lowdraglib2.gui.ui.UI
import com.lowdragmc.lowdraglib2.gui.ui.UIElement
import com.lowdragmc.lowdraglib2.gui.ui.element
import com.lowdragmc.lowdraglib2.gui.ui.dsl
import com.lowdragmc.lowdraglib2.gui.ui.elements.*
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType
import com.lowdragmc.lowdraglib2.gui.ui.layout.px
import com.lowdragmc.lowdraglib2.gui.ui.layout.pct
import dev.vfyjxf.taffy.style.AlignContent
import dev.vfyjxf.taffy.style.AlignItems
import dev.vfyjxf.taffy.style.FlexDirection
import dev.vfyjxf.taffy.style.FlexWrap
import dev.vfyjxf.taffy.style.TaffyPosition
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture
import com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup
import com.lowdragmc.lowdraglib2.gui.ui.layout.pct
import com.lowdragmc.lowdraglib2.gui.ui.style.Stylesheet
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

object TempleUIFactory {

    private val DYE_COLORS = DyeColor.entries

    private val LSS = """
        .panel_bg {
            background: built-in(ui-gdp:BORDER);
            padding-all: 10;
            gap-all: 6;
            width: 90%;
            height: 90%;
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
            background: color-border(0xFFFFFFFF, 3);
        }
        .swatch.unselected {
            background: color-border(0xFF666666, 1);
        }
        .field {
            width: 100%;
            max-width: 220;
        }
        .scroller {
            width: 100%;
            flex-grow: 1;
            min-height: 60;
        }
    """.trimIndent()

    private fun swatchTexture(dye: DyeColor): ColorRectTexture =
        ColorRectTexture(0xFF000000.toInt() or dye.mapColor.col)

    fun createFactionUI(holder: BlockUIMenuType.BlockUIHolder): ModularUI {
        val be = holder.player.level().getBlockEntity(holder.pos) as? gg.wildblood.blockentity.TempleBlockEntity
        val content = if (be != null && be.factionId.isNotEmpty()) {
            createManageFactionUI(holder, be)
        } else {
            createNewFactionUI(holder, be)
        }

        val root = element({
            layout = { width(100.pct); height(100.pct); justifyContent(AlignContent.CENTER); alignItems(AlignItems.CENTER) }
        }) {
            dsl({ content }, {}, {})
        }

        val stylesheet = Stylesheet.parse(LSS)
        val ui = com.lowdragmc.lowdraglib2.gui.ui.UI.of(
            root, listOf(stylesheet),
            com.lowdragmc.lowdraglib2.gui.ui.UI.DynamicSizeProvider { size -> size }
        )
        return ModularUI.of(ui, holder.player)
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

        textField({ cls = { +"field" } }) {
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
        be: gg.wildblood.blockentity.TempleBlockEntity,
    ) = element({ cls = { +"panel_bg" } }) {
        var name = be.displayName
        var selectedColor = be.color
        val player = holder.player
        val isMayor = player.uuid.toString() == be.mayorUuid
        val isAdmin = player.hasPermissions(2)
        val canEdit = isMayor || isAdmin
        val swatchElements = mutableListOf<UIElement>()
        var nameField: com.lowdragmc.lowdraglib2.gui.ui.elements.TextField? = null

        label({ text("pantheon.gui.faction_manage.title", true) })

        if (canEdit) {
            var editBtn: com.lowdragmc.lowdraglib2.gui.ui.elements.Button? = null
            var saveBtn: com.lowdragmc.lowdraglib2.gui.ui.elements.Button? = null
            button({
                text("pantheon.gui.faction_manage.edit", true)
                cls = { +"btn" }; active = true
                layout = { pos { type(TaffyPosition.ABSOLUTE); top(4.px); right(4.px) } }
                onClick = {
                    nameField?.setActive(true)
                    swatchElements.forEach { it.setActive(true) }
                    editBtn?.setVisible(false)
                    saveBtn?.setVisible(true)
                }
            }) {
                editBtn = this.element
            }
            button({
                text("pantheon.gui.faction_manage.save", true)
                cls = { +"btn" }; active = true
                visible = false
                layout = { pos { type(TaffyPosition.ABSOLUTE); top(4.px); right(4.px) } }
                onClick = {
                    be.rpcToServer("rpcUpdateFaction", name, selectedColor)
                }
            }) {
                saveBtn = this.element
            }
        }

        label({ text("pantheon.gui.faction_create.name", true) })

        textField({ cls = { +"field" }; active = false }) {
            observer { name = it }
            dataSource { name }
            nameField = this.element
        }

        label({ text("pantheon.gui.faction_create.color", true) })

        element({ layout = { gap { all(3.px) }; wrap(FlexWrap.WRAP); flexDirection(FlexDirection.ROW) } }) {
            for (i in 0 until 16) {
                button({
                    noText(); active = false
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

        label({ text = Component.translatable("pantheon.gui.faction_manage.members", be.memberCount) })

        val memberContainer = com.lowdragmc.lowdraglib2.gui.ui.UIElement()
        memberContainer.layout { it.gapAll(2f).flexDirection(FlexDirection.COLUMN) }

        val memberSync = com.lowdragmc.lowdraglib2.gui.ui.elements.BindableValue<String>()
        memberSync.bind(
            com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder
                .stringS2C { be.memberNames }
                .syncType(String::class.java)
                .initialValue(be.memberNames)
                .s2cStrategy(com.lowdragmc.lowdraglib2.gui.sync.bindings.SyncStrategy.CHANGED_PERIODIC)
                .remoteSetter { names ->
                    memberContainer.clearAllChildren()
                    for (memberName in names.split('\n').filter { it.isNotEmpty() }) {
                        memberContainer.addChild(
                            com.lowdragmc.lowdraglib2.gui.ui.elements.Label().setText("• $memberName")
                        )
                    }
                }
                .build()
        )
        memberContainer.addChild(memberSync)

        scrollerView({ cls = { +"scroller" } }) {
            dsl({ memberContainer }, {}, {})
        }

        if (!canEdit) {
            button({
                text("pantheon.gui.faction_manage.close", true)
                cls = { +"btn" }; active = true
                onClick = { player.closeContainer() }
            })
        }
    }

    private fun dyeColor(index: Int): DyeColor = DyeColor.byId(index)
}