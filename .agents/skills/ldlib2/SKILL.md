---
name: ldlib2
description: LDLib2 (LowDragLib2) API reference and modding patterns for Minecraft NeoForge 1.21.1. Use this whenever working in a project that depends on `com.lowdragmc.ldlib2:ldlib2-neoforge-1.21.1` — building ModularUI screens/menus, Kotlin UI DSL, LSS stylesheets, data bindings (client-only vs server-synced), RPCEvent/UI messages, `@Persisted`/`@DescSynced`/`@RPCMethod` annotations on BlockEntities, `ISyncPersistRPCBlockEntity`, `@Configurable` inspector UIs, UI factories (`BlockUIMenuType`/`HeldItemUIMenuType`/`PlayerUIMenuType`), textures, HUD overlays, or the Node Graph Toolkit. Also trigger when the user mentions LDLib2, LowDragLib2, ModularUI, LSS, or asks how to build a synced GUI with LDLib2. Consult this before guessing LDLib2 class names, method signatures, or annotation semantics — the API is version-specific and easy to get wrong.
---

# LDLib2 (for NeoForge 1.21.1)

LDLib2 (`com.lowdragmc.ldlib2`, mod id `ldlib2`, maven `com.lowdragmc.ldlib2:ldlib2-neoforge-1.21.1`) is a high-level library built on NeoForge 21.1.x that provides a CSS-like UI system (ModularUI + LSS), annotation-driven data sync/persistence, a configurable inspector, an in-game editor, and a node-graph toolkit. It is a full rewrite of LDLib and is **not** API-compatible with it.

> Versions: this skill targets **LDLib2 2.2.x on Minecraft 1.21.1 / NeoForge 21.1.x**. The `26.1.x` line targets MC 26.1 and has API differences — do not apply 1.21.1 patterns blindly to 26.1+ (see the migration doc). When a class or method name is uncertain, verify against the GitHub source (`https://github.com/Low-Drag-MC/LDLib2`, branch `1.21`) before writing code.

## When to consult the reference files

This file holds the decision tree and the most common, high-value patterns. Deeper detail lives in `references/` — read the relevant file **only when the task touches that area**:

| Task / topic | Read |
|---|---|
| Gradle/Maven setup, `@LDLibPlugin`, `@LDLRegister` auto-registry | `references/setup.md` |
| ModularUI, UIElement tree, layout (Taffy flex/grid), LSS, events, components, textures, HUD, factories, Kotlin DSL | `references/ui.md` |
| `@Persisted`, `@DescSynced`, `@RPCMethod`, `@ReadOnlyManaged`, `ISyncPersistRPCBlockEntity`, RPC packets | `references/sync.md` |
| `@Configurable`, `IConfigurable`, inspector, configurators | `references/configurable.md` |
| Node Graph Toolkit (graphs, nodes, ports, blackboard, GraphView) | `references/node-graph.md` |

Official docs (authoritative when this skill is silent): `https://low-drag-mc.github.io/LowDragMC-Doc/en/ldlib2/`. The repo also ships an **Agent Guide** at `en/ldlib2/ui/agent_guide.html` — use it as the canonical UI workflow.

## Project setup checklist

1. Add the maven repo and dependency (see `references/setup.md` for exact coordinates per LDLib2 version — the `:all` classifier and `transitive = false` differ between `<2.2.1` and `>=2.2.1`).
2. If you want IDE support, recommend the **LDLib Dev Tool** IntelliJ plugin (syntax check / autocomplete for LDLib2 annotations and LSS).
3. Register a `@LDLibPlugin class MyPlugin : ILDLibPlugin { override fun onLoad() { ... } }` for one-time LDLib2 setup (registry subscription, etc.). This is the LDLib2 equivalent of a NeoForge mod constructor — use it for LDLib2-specific registration, not vanilla `DeferredRegister`s.
4. Use `@LDLRegister(registry = "...", name = "...")` (both sides) or `@LDLRegisterClient` (client only) to auto-register classes into LDLib2's own registries (e.g. `ldlib2:screen_test`, `ldlib2:menu_test`). The class must implement `ILDLRegister` / `ILDLRegisterClient`. This is **separate** from NeoForge's `DeferredRegister`.

## Decision tree: what does the user want?

```
Is it a GUI task?
├── yes → references/ui.md
│   ├── Does it need server-side state / Player data? → Menu UI (ModularUI.of(ui, player))
│   │      otherwise → client-only Screen (ModularUI.of(ui))
│   ├── Kotlin project? → prefer the Kotlin DSL (element { ... } { ... })
│   ├── Need sync between client & server? → data bindings / RPCEvent (see also references/sync.md)
│   └── Opening from block / item / command? → use a UI Factory
└── no
    ├── Persisting/syncing BlockEntity data? → references/sync.md (ISyncPersistRPCBlockEntity + annotations)
    ├── Editing config/data objects in-game? → references/configurable.md
    ├── Building a visual node editor? → references/node-graph.md
    └── Build/registration plumbing? → references/setup.md
```

## UI: the single most important rule

**If the UI needs `Player` data or any server-side state, it MUST be a Menu-based UI created with `ModularUI.of(ui, player)`.** A client-only `ModularUI.of(ui)` cannot carry server-authoritative data. Picking the wrong variant is the #1 source of broken LDLib2 GUIs.

### Minimal templates

Client-only screen (Kotlin DSL):

```kotlin
fun createUI() = ModularUI.of(UI.of(
    element({ cls = { +"panel_bg" } }) { label({ text("Hello") }) },
    StylesheetManager.GDP
))
// open: Minecraft.getInstance().setScreen(ModularUIScreen(createUI(), Component.empty()))
```

Server-synced menu UI via the Player factory (no Block/Item needed):

```kotlin
val UI_ID = ResourceLocation.fromNamespaceAndPath("mymod", "my_ui")
PlayerUIMenuType.register(UI_ID) { { p -> createUI(p) } }   // register once during init
PlayerUIMenuType.openUI(serverPlayer, UI_ID)                // open from server side
```

Block / Item variants: implement `BlockUIMenuType.BlockUI` / `HeldItemUIMenuType.HeldItemUI` on your Block/Item and call `BlockUIMenuType.openUI((ServerPlayer) player, pos)` / `HeldItemUIMenuType.openUI((ServerPlayer) player, hand)` from the right-click handler. Full context fields and KubeJS bindings are in `references/ui.md`.

### Data binding — pick the right pattern

| Need | Client-only | Server-synced (Menu UI) |
|---|---|---|
| Display dynamic value | `label { dataSource({ ... }) }` | `label { bindS2C({ ... }) }` |
| React to user input | `textField { observer { v = it } }` | `textField { bind(::v) }` (bidirectional) |
| Write-only c→s | — | `textField { bindC2S({ v = it }) }` |
| Inventory / fluid | — | `itemSlot { bind(handler, 0) }`, `fluidSlot { bind(tank, 0) }` |
| One-shot action | local event | `serverEvents { UIEvents.MOUSE_DOWN += { ... } }` or `element.rpcEvent { arg: T -> ... }` |

`bindDataSource`/`bindObserver`/`TrackData` are **client-only** — they do not send packets. For anything touching server state use `bind` / `bindS2C` / `bindC2S` (which wrap `DataBindingBuilder`) or an `RPCEvent`. The `FluidSlot.bind(...)` implementation is the canonical example of combining s→c display binding with RPC for interactions — mimic that pattern rather than letting the client write to a tank directly.

When binding a `Collection<T>` or other **read-only** sync type, you must supply `.syncType(type)` and `.initialValue(...)` on the `DataBindingBuilder` (see `references/sync.md` → "Types Support"). Forgetting these silently breaks sync.

## Sync & persistence on BlockEntities

Implement `ISyncPersistRPCBlockEntity` and hold a `FieldManagedStorage`:

```kotlin
class MyBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(...), ISyncPersistRPCBlockEntity {
    @get:JvmName("getSyncStorage")
    val syncStorage = FieldManagedStorage(this)

    @Persisted @DescSynced            private var intValue = 10
    @Persisted @DescSynced @RequireRerender
                                       private var color = -1
    @Persisted @DropSaved              private val item = ItemStack.EMPTY

    @RPCMethod fun rpcMsg(msg: String) {
        if (level!!.isClientSide) {/* received on client */}
        else rpcToTracking("rpcMsg", msg)   /* send from server */
    }
}
```

Key annotations: `@Persisted` (NBT save/load), `@DescSynced` (server→client sync), `@RPCMethod` (call across network via `rpcToServer`/`rpcToTracking`/`rpcToPlayer`), `@DropSaved` (save into the dropped item), `@RequireRerender` (re-render chunk on change), `@UpdateListener(methodName)`, `@LazyManaged`, `@ReadOnlyManaged(serializeMethod=, deserializeMethod=)`, `@ConditionalSynced`, `@SkipPersistedValue`. Full semantics + gotchas in `references/sync.md`.

`useAsyncThread()` defaults to true — dirty notifications may fire off-thread. LDLib2 handles the common cases, but don't touch unsynchronized Minecraft state from a dirty callback without thinking.

## Configurable (in-game inspector)

Annotate fields on an `IConfigurable` object with `@Configurable` (plus `@ConfigNumber(range=, wheel=)`, etc.) and hand the object to an `Inspector` / `editor.inspectorView.inspect(obj)`. The parser builds the rows automatically. Override `buildConfigurator(group)` for custom layout. See `references/configurable.md`.

## Things to avoid

- Using `LDLib` (v1) class names — LDLib2 reorganized everything under `com.lowdragmc.lowdraglib2.*`.
- Calling `bindDataSource`/`bindObserver` expecting network sync — they are client-only.
- Letting the client write directly to a server `IFluidHandler`/inventory via a bidirectional binding — use s→c binding + RPC for the interaction (see `FluidSlot.bind`).
- Forgetting `.syncType(type)` + `.initialValue(...)` for read-only / generic collection bindings.
- Creating a Menu UI without passing `player` to `ModularUI.of`.
- Applying 1.21.1 LDLib2 patterns to the 26.1.x line without checking the migration doc.
- Mixing up `@LDLRegister` (LDLib2's own registry) with NeoForge `DeferredRegister` — they are independent systems.

## Pre-completion checklist (UI tasks)

- [ ] UI type matches need: client Screen has no `player` param; Menu UI has `player`.
- [ ] Data bindings correct: server data → `DataBindingBuilder.xxx().build()` + `.bind()` (or Kotlin `bind`/`bindS2C`/`bindC2S`); client-only → `dataSource`/`observer`.
- [ ] Menu UI factory registered (Block/Item/Player) or a manual `MenuType` wired up.
- [ ] A built-in stylesheet applied (`StylesheetManager.GDP` / `.MC` / `.MODERN`) or a custom LSS provided.
- [ ] XML UI files (if used) placed under `assets/<modid>/ui/` and loaded via `XmlUtils.loadXml(ResourceLocation)`.
- [ ] Return type matches request: `ModularUI` for full UIs, `UIElement` for reusable components.

After writing code, run the project's build (`./gradlew compileKotlin --no-daemon --no-configuration-cache` for a fast check, `./gradlew build` for a full pass) before claiming done.