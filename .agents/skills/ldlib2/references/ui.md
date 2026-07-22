# LDLib2 — UI (ModularUI, layout, LSS, components, bindings, RPC, factories, Kotlin DSL)

Authoritative source: the **Agent Guide** at `https://low-drag-mc.github.io/LowDragMC-Doc/en/ldlib2/ui/agent_guide.html`. This file mirrors its decision tree and adds Kotlin-flavored detail. When the docs and this file disagree, the docs win.

## Top-level classes (package `com.lowdragmc.lowdraglib2.gui.ui`)

- `UIElement` — base element. `.layout {}`, `.style {}`, `.addClass()`, `.addChild()`, `.addChildren()`, `.addEventListener(...)`, `.transform {}`, `.lss(key, value)`.
- `UI` — `UI.of(root)` or `UI.of(root, stylesheet)`. Owns the element tree + style engine.
- `ModularUI` — runtime. `ModularUI.of(ui)` (client-only) or `ModularUI.of(ui, player)` (Menu). Also `ModularUI(ui, player)` constructor form.
- `ModularUIScreen`, `ModularUIContainerScreen`, `ModularUIContainerMenu` — vanilla-backed screen/menu helpers.
- `StylesheetManager` — built-in themes: `StylesheetManager.GDP`, `.MC`, `.MODERN`. `INSTANCE.getStylesheetSafe(id)` resolves one.
- `Sprites` / `MCSprites` — built-in texture sprites (`RECT`, `RECT_SOLID`, `BORDER`, `BORDER1`, `BORDER1_DARK`, ...).
- `UIEvents` — event type constants (`CLICK`, `MOUSE_DOWN`, `MOUSE_ENTER`, `MOUSE_LEAVE`, `ADDED`, ...).

## Step 1 — Client-only Screen vs server-synced Menu

**If the UI needs `Player` data or any server-side state → Menu UI with `ModularUI.of(ui, player)`.** A client-only `ModularUI.of(ui)` cannot carry server-authoritative data.

Open a client-only screen:

```kotlin
Minecraft.getInstance().setScreen(ModularUIScreen(createUI(), Component.empty()))
```

Menu UIs require either a registered UI factory (see Factories) or a manual `MenuType` + `AbstractContainerMenu` that implements `IModularUIHolderMenu`:

```kotlin
class MyMenu(...) : AbstractContainerMenu(...) {
    init {
        val mui = createUI(player)
        (this as? IModularUIHolderMenu)?.setModularUI(mui)
    }
}
class MyScreen(menu: MyMenu) : AbstractContainerScreen<MyMenu>(menu, pInv, title) {
    override fun init() {
        imageWidth = menu.modularUI.width.toInt()
        imageHeight = menu.modularUI.height.toInt()
        super.init()
    }
}
```

## Step 2 — Element tree (Kotlin DSL vs Java)

### Kotlin DSL (preferred in Kotlin projects)

Two-lambda builder: `{ spec }` (optional) then `{ init }`. Spec holds config (`id`, `focusable`, `visible`, `active`, `layout`, `style`, `cls`); init adds children/events/bindings.

```kotlin
import com.lowdragmc.lowdraglib2.gui.ui.element
import com.lowdragmc.lowdraglib2.gui.ui.elements.*
import com.lowdragmc.lowdraglib2.gui.ui.layout.px
import com.lowdragmc.lowdraglib2.gui.ui.layout.pct
import com.lowdragmc.lowdraglib2.gui.ui.row
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents

element({
    id = "my-panel"
    layout = { size(200.px); gap { all(4.px) }; padding { all(8.px) } }
    style = { background(Sprites.RECT); opacity(0.9f); tooltips("my.tip") }
    cls  = { +"active"; -"disabled" }
}) {
    label({ text("Title") })
    button({ text("OK"); onClick = { /* client-side */ } }) {
        events { e -> UIEvents.CLICK on { /* ... */ } }
    }
    row({ layout = { gap { all(2.px) } } }) {
        fluidSlot()
        itemSlot({ item = Items.APPLE.defaultInstance })
    }
}
```

DSL functions for children: `element`, `label`, `button`, `textField`, `textArea`, `toggle`, `switch`, `selector`, `progressBar`, `scrollerHorizontal`/`scrollerVertical`, `scrollerView`, `itemSlot`, `fluidSlot`, `inventorySlots`, `tab`/`tabView`, `toggleGroup`, `colorSelector`, `tagField`, `searchComponent`, `treeList`, `scene`, `graphView`, `codeEditor`, `inspector`, `template`, `dsl({ JavaElement() }) { ... }` (wrap a raw Java element). `row {}` / `column {}` preset flex direction.

### Java API

```java
var root = new UIElement();
root.addChildren(
    new Label().setText("Hello"),
    new Button().setText("OK").setOnClick(e -> { /* client */ }),
    new UIElement().layout(l -> l.width(80).height(80))
        .style(s -> s.background(SpriteTexture.of("mymod:textures/gui/icon.png")))
);
root.layout(l -> l.paddingAll(7).gapAll(5));
root.style(s -> s.background(Sprites.BORDER));
return ModularUI.of(UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)));
```

## Layout (Taffy — flexbox + grid)

Package `com.lowdragmc.lowdraglib2.gui.ui.layout`. Layout is computed by the Taffy engine (CSS Block, Flexbox, Grid). Properties:

- Size: `width(x)`, `height(x)`, `widthPercent(p)`, `heightPercent(p)`, `minWidth`, `maxWidth`, `aspectRate`. Units: `N.px`, `N.pct`, `auto`.
- Flex: `flexDirection(FlexDirection.ROW|COLUMN|ROW_REVERSE|COLUMN_REVERSE)`, `flexGrow`, `flexShrink`, `flexBasis`, `flex(1)` (= grow+shrink 1), `flexWrap`.
- Alignment: `alignItems`, `alignContent`, `justifyContent`, `alignSelf`.
- Box model: `paddingAll`/`paddingHorizontal`/`padding { all(...) }`, `marginAll`/`margin { top(...) }`, `borderAll` (avoid border; use a texture background).
- Position: `position(TaffyPosition.RELATIVE|ABSOLUTE)`, `pos { left(...); top(...) }`.
- Grid: `display(TaffyDisplay.GRID)`, `grid { templateColumns("1fr 1fr"); templateRows("auto 1fr"); row("1"); column("2") }`.
- Overflow: `overflowVisible(false)` clips content.

In Kotlin DSL the `layout = { ... }` block is a `TaffyLayoutStyleDsl` receiver — same property names. Outside the DSL use `element.layout { ... }` or `element.getLayout().width(150)`.

## Stylesheet (LSS — LDLib2 StyleSheet)

CSS-like, separates style from structure. Three ways to apply:

1. Inline on an element: `element.lss("width", 80)`, `element.lss("background", "sprite(mymod:textures/gui/icon.png)")`.
2. Element bindings: `root.lss("background", "built-in(ui-gdp:BORDER)")`.
3. A parsed stylesheet with selectors:

```kotlin
val lss = """
  #root { background: built-in(ui-gdp:BORDER); padding-all: 7; gap-all: 5; }
  .image { width: 80; height: 80; background: sprite(mymod:textures/gui/icon.png); }
  #root label { horizontal-align: center; }
""".trimIndent()
val ui = UI.of(root, Stylesheet.parse(lss))
```

Built-in themes: `StylesheetManager.GDP`, `.MC`, `.MODERN`. Apply via `UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP))`. At runtime you can swap: `mui.styleEngine.clearAllStylesheets(); mui.styleEngine.addStylesheet(...)`.

LSS value helpers: `sprite(rl)`, `built-in(id)`, `color(...)`, numbers, percentages. Texture syntax lives in `references/ui.md` → Textures below; full reference at `en/ldlib2/ui/textures/lss.html`.

## Events

`UIEvents` exposes typed event keys. Any `UIElement` can listen. Events: mouse (`MOUSE_DOWN`, `MOUSE_UP`, `CLICK`, `MOUSE_ENTER`, `MOUSE_LEAVE`, `MOUSE_WHEEL`, drag), keyboard (`KEY_DOWN`, `KEY_UP`), focus, lifecycle (`ADDED`, `REMOVED`), command.

Kotlin DSL:

```kotlin
element {
    events { e ->                                  // e is the element being built
        UIEvents.CLICK on { event -> /* ... */ }   // bubble phase
        UIEvents.MOUSE_ENTER on { it.currentElement.addClass("hover") }
    }
    events(capture = true) { UIEvents.CLICK on { it.stopPropagation() } }
    serverEvents { UIEvents.MOUSE_DOWN += { /* runs on server */ } }
    serverEvents(capture = true) { /* ... */ }
}
```

Java: `element.addEventListener(UIEvents.CLICK, listener)` / `element.addServerEventListener(UIEvents.CLICK, listener)`. The second arg `true` enables capture phase.

## Data bindings (critical — read carefully)

Three layers; mixing them up is the #1 bug source.

### A. Client-only — `dataSource` / `observer` / `TrackData`

No network. `IDataConsumer<T>` (display) + `IObservable<T>` (input). Most data components implement both.

```kotlin
var value = 0
label { dataSource({ Component.literal("v=$value") }) }              // display
textField { observer { value = it.toIntOrNull() ?: value }; dataSource { value.toString() } }
```

`TrackData<T>` (Kotlin) is a reactive holder implementing both provider + observer, usable with `by` delegation:

```kotlin
val td = TrackData("10.4")
var n by td.map({ it.toFloatOrNull() ?: 1f }, { it.toString() })
textField { observer(td); dataSource(td) }.asNumeric(0.3f, 100f)
button({ text("+10"); onClick = { n += 10f } })
```

### B. Server-synced (Menu UI) — `bind` / `bindS2C` / `bindC2S` via `DataBindingBuilder`

You only describe **server-side** getter/setter; LDLib2 wires the client side automatically through the component's `IBindable<T>` impl.

```kotlin
// server fields: var bool = true; var str = "hi"; var num = 0.5f
switch { bind(::bool) }                                  // bidirectional
textField { bind(::str) }
scrollerHorizontal({ layout = { width(100.pct) } }) { bind(::number) }
label { bindS2C({ Component.literal("s->c: $bool $str $num") }) }   // server→client read-only
textField { bindC2S({ v -> str = v }) }                              // client→server write-only
itemSlot { bind(itemHandler, 0) }                                    // inventory shorthand
fluidSlot { bind(fluidTank, 0) }                                     // fluid shorthand
```

Java equivalent:

```java
new Switch().bind(DataBindingBuilder.bool(() -> bool, v -> bool = v).build());
new TextField().bind(DataBindingBuilder.string(() -> str, v -> str = v).build());
new Label().bind(DataBindingBuilder.componentS2C(() -> Component.literal(str)).build());
new ItemSlot().bind(itemHandler, 0);
new FluidSlot().bind(fluidTank, 0);
```

`SyncStrategy`: `NONE`, `CHANGED_PERIODIC` (default, once/tick), `ALWAYS` (force every tick). Set via `.c2sStrategy(...)` / `.s2cStrategy(...)`.

### C. Custom / read-only types

`Collection<T>`, `Map`, `INBTSerializable`, and other **read-only** sync types need explicit type + initial value:

```kotlin
// List<String> candidates on the server
selector<String> {} // pseudo — needs a BindableValue<T> child to carry the sync
element {} .dsl({ BindableValue<List<String>>() }) {
    api {
        bind(
            bindings({ candidates }, { /* no-op */ })
                .syncType(object : TypeToken<List<String>>(){}.type)   // Java: TypeToken
                .initialValue(candidates)
                .c2sStrategy(SyncStrategy.NONE)
                .remoteSetter { sel -> sel.setCandidates(it) }
                .build()
        )
    }
}
```

Java:

```java
Type type = new TypeToken<List<String>>(){}.getType();
selector.addChild(new BindableValue<List<String>>().bind(
    DataBindingBuilder.create(() -> candidates, Consumers.nop())
        .syncType(type).initialValue(candidates)
        .c2sStrategy(SyncStrategy.NONE)
        .remoteSetter(selector::setCandidates)
        .build()));
```

Forgetting `.syncType(type)` + `.initialValue(...)` silently breaks sync for collections/generics. See `references/sync.md` for the full "Types Support" table.

### `remoteGetter` / `remoteSetter`

If you set either, LDLib2 **stops** auto-wiring `bindDataSource`/`bindObserver` — you take full responsibility for the client side. Use this to forward a synced value to a non-`IBindable` element (e.g. drive an element's width from a server float):

```kotlin
element {} .dsl({ BindableValue<Float>() }) {
    api { bind(DataBindingBuilder.floatValS2C { widthOnServer }
        .remoteSetter { w -> element.layout.width(w) }.build()) }
}
```

## RPCEvent & UI Message (interactions, not state)

Use bindings for persistent synced state; use RPC for one-shot actions/queries.

### `RPCEvent` (typed, bidirectional)

Direction is determined by **where `send` is called**, not the definition:

```kotlin
button {
    val rpc = element.rpcEvent { arg: String -> string = arg }   // lambda runs on receiver side
    events { UIEvents.MOUSE_DOWN += { rpc.send("rpc") } }        // client→server call
}
```

With return value:

```kotlin
val add = element.rpcEvent(Int::class.java, Int::class.java, Int::class.java) { a, b -> a + b }
events { UIEvents.CLICK += { add.send({ result -> /* client */ }, 1, 2) } }
```

Server→client push (server initiates):

```kotlin
val s2c = button.addRPCEvent(RPCEventBuilder.simple(Fluid::class.java) { fluid ->
    assert(LDLib2.isRemote()); button.setText(fluid.fluidType.description)
})
button.addServerEventListener(UIEvents.MOUSE_DOWN) { s2c.send(Fluids.LAVA) }
```

**Rule:** parameters passed to `send(...)` must match the builder's parameter types and order exactly, and the event must be attached via `addRPCEvent` / `element.rpcEvent { ... }` first.

### UI Message (lightweight, `CompoundTag` payload)

```kotlin
button {
    api {
        setOnServerClick { e ->
            e.currentElement.sendMessage("test", TagBuilder.compound().add("text", "hi").build())
        }
        onMessage("test") { self, msg -> (self as Button).setText(msg.getString("text")) }
    }
}
```

Use `message` for quick named packets (great in KubeJS); use a typed `RPCEvent` when you want a self-documenting signature.

### FluidSlot pattern (canonical safe interaction)

`FluidSlot.bind(tank, 0)` internally uses **s→c read-only binding** for display + **RPC** for click interactions — the client never writes the tank directly. Mimic this for any server-owned mutable resource; never let a bidirectional binding hand the client write access to a server inventory/tank.

## Factories (Block / Item / Player)

Pre-built helpers handle menu registration, client/server routing, and lifecycle. Prefer these over a manual `MenuType`.

### `BlockUIMenuType` — block right-click UI

```kotlin
class MyBlock(props: Properties) : Block(props), BlockUIMenuType.BlockUI {
    override fun useWithoutItem(state, level, pos, player, hit): InteractionResult {
        if (!level.isClientSide) BlockUIMenuType.openUI(player as ServerPlayer, pos)
        return InteractionResult.SUCCESS
    }
    override fun createUI(holder: BlockUIMenuType.BlockUIHolder): ModularUI {
        val root = element({ cls = { +"panel_bg" } }) { label({ text("Block UI") }); inventorySlots() }
        return ModularUI.of(UI.of(root, StylesheetManager.GDP), holder.player)
    }
}
```

Holder fields: `player`, `pos`, `blockState`.

### `HeldItemUIMenuType` — held item UI

Implement `HeldItemUIMenuType.HeldItemUI` on your `Item`; call `HeldItemUIMenuType.openUI((ServerPlayer) player, hand)` from `use(...)`. Holder fields: `player`, `hand`, `itemStack`.

### `PlayerUIMenuType` — arbitrary server trigger (command, keybind)

```kotlin
val UI_ID = ResourceLocation.fromNamespaceAndPath("mymod", "my_ui")
PlayerUIMenuType.register(UI_ID) { { p -> createUI(p) } }   // register once (init)
PlayerUIMenuType.openUI(serverPlayer, UI_ID)                 // open from server side
```

`openPlayerUI` must be called server-side; the UI is always considered valid (no block/item to validate).

## Components quick reference

| Component | Class | Key API |
|---|---|---|
| Base | `UIElement` | `.layout {}`, `.style {}`, `.addClass()`, `.addEventListener()` |
| Label | `Label` | `.setText()`, `.bind()` (s→c), `.textStyle {}` |
| Button | `Button` | `.setText()`, `.setOnClick()`, `.setOnServerClick()` |
| TextField | `TextField` | `.setText()`, `.bind()`, `.setNumbersOnlyInt(min,max)`, `.asNumeric(min,max)` |
| TextArea | `TextArea` | `.setText()`, `.bind()` |
| Toggle | `Toggle` | `.setText()`, `.bind()` |
| Switch | `Switch` | `.bind()` |
| Selector | `Selector<T>` | `.setCandidates()`, `.setSelected()`, `.bind()`, `.setOnValueChanged()` |
| ProgressBar | `ProgressBar` | `.setProgress()`, `.bind()`, `.label {}` |
| Scroller | `Scroller` / `Scroller.Horizontal` | `.bind()` |
| ItemSlot | `ItemSlot` | `.bind(handler, slot)`, `.setItem()`, `.setCanTake()` |
| FluidSlot | `FluidSlot` | `.bind(tank, slot)`, `.setFluid()` |
| InventorySlots | `InventorySlots` | (auto player inventory) |
| Tab / TabView | `Tab`, `TabView` | `.addTab()` |
| ToggleGroup | `ToggleGroup` | `.addToggle()` |
| ScrollerView | `ScrollerView` | (scrollable container) |
| SplitView | `SplitView` | (resizable split) |
| ColorSelector | `ColorSelector` | `.bind()` |
| TagField | `TagField` | `.bind()` |
| SearchComponent | `SearchComponent` | `.bind()`, `.searchUI {}` |
| TreeList | `TreeList` | — |
| Scene | `Scene` | (3D scene) |
| GraphView | `GraphView` | (node graph render) |
| CodeEditor | `CodeEditor` | — |
| Inspector | `Inspector` | `.inspect(IConfigurable)` |
| Template | `UITemplateElement` | load from XML |
| BindableValue | `BindableValue<T>` | `.bind()` — hidden sync carrier (`display: CONTENTS`) |
| Rich text | `Text` / `TextElement` | `.setText()` |

## Textures

Package `com.lowdragmc.lowdraglib2.gui.texture`. All implement `IGuiTexture`. Apply via `style { background(texture) }` or `.lss("background", "...")`.

| Texture | Class | LSS |
|---|---|---|
| Sprite (image) | `SpriteTexture` | `sprite(rl)` |
| Vanilla sprite | `VanillaSpriteTexture` | `vanilla-sprite(rl)` |
| Color rect | `ColorRectTexture` | `color-rect(0xFF0000FF)` |
| Color border | `ColorBorderTexture` | `color-border(...)` |
| SDF rect | `SDFRectTexture` | `sdf-rect(...)` |
| Resource rect | `RectTexture` | `rect(rl)` |
| Text | `TextTexture` | `text(...)` |
| Animation | `AnimationTexture` | `animation(...)` |
| Item stack | `ItemStackTexture` | `item-stack(...)` |
| Fluid stack | `FluidStackTexture` | `fluid-stack(...)` |
| Group | `GuiTextureGroup` | group multiple |
| Shader | `ShaderTexture` | custom shader |
| UI resource | `UIResourceTexture` | `ui-resource(...)` |

## Style animation

```kotlin
element {} .animationDsl {
    duration(1f); ease(Eases.QUAD_IN_OUT)
    style(PropertyRegistry.TRANSFORM_2D, Transform2D().scale(0.5f).translate(100f, 0f))
    style(PropertyRegistry.OPACITY, 0f)
    onFinished { e.animationDsl { /* reverse */ } }
}
```

`PropertyRegistry` holds animatable property keys (`OPACITY`, `TRANSFORM_2D`, `WIDTH`, `HEIGHT`, ...). Eases in `com.lowdragmc.lowdraglib2.math.interpolate.Eases`.

## HUD overlays

Client-only. Register in `RegisterGuiLayersEvent`:

```kotlin
val muiCache = Suppliers.memoize { ModularUI.of(UI.of(
    UIElement().layout { it.widthPercent(100).heightPercent(100).paddingAll(10) }
)) }
event.registerAboveAll(MyMod.id("my_hud"), ModularHudLayer { muiCache.get() })
```

## XML UI

Declarative tree + styles only; you still need code to load, bind, and wrap in `ModularUI`. Files live under `assets/<modid>/ui/`. XSD for autocomplete/validation: `https://raw.githubusercontent.com/Low-Drag-MC/LDLib2/refs/heads/1.21/ldlib2-ui.xsd`.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<ldlib2-ui xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:noNamespaceSchemaLocation="https://raw.githubusercontent.com/Low-Drag-MC/LDLib2/refs/heads/1.21/ldlib2-ui.xsd">
    <stylesheet location="ldlib2:lss/mc.lss"/>
    <root class="panel_bg">
        <label text="Hello from XML"/>
        <button text="Click me"/>
    </root>
</ldlib2-ui>
```

```kotlin
val xml = XmlUtils.loadXml(ResourceLocation.fromNamespaceAndPath("mymod", "my_ui.xml"))
val ui = UI.of(xml)
ui.select("#my_id"); ui.select(".cls > button")          // CSS-like queries
return ModularUI.of(ui, player)   // or ModularUI.of(ui) for client-only
```

## XEI (JEI/REI/EMI)

Integration lives in `com.lowdragmc.lowdraglib2.integration.xei`. LDLib2 provides unified handlers under `xei/jei`, `xei/rei`, `xei/emi`. Register recipe/category handlers against the XEI-neutral interfaces; LDLib2 routes to whichever mod is present. Full reference: `en/ldlib2/ui/xei_support.html`.

## KubeJS

`LDLib2UI.block/item/player(id) { event -> event.modularUI = ModularUI.of(UI.of(...), event.player) }` — must run on **both** sides; prefer `startup_scripts/` (runs on both). Do **not** build the UI eagerly at script load (resources aren't ready) — build inside the event lambda. Open via `LDLib2UIFactory.openBlockUI/openHeldItemUI/openPlayerUI(...)`.