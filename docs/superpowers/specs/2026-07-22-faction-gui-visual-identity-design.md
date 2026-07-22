# Faction Management via Temple GUI + Visual Identity

**Status:** Draft (pending user review)
**Date:** 2026-07-22
**Depends on:** Phase 0 foundation (Faction, PantheonSavedData, TempleBlock, TempleBlockEntity, ModAttachments)
**Blocks:** Future phases (Quests, Economy, Skilltree, Mayor, LLM)

---

## 1. Goal

Transform the Temple block from a passive data mirror into the central faction management interface. Players create factions through a GUI (name + color), choose their faction on login, and see their faction's color reflected in nametags and the scoreboard. Remove all season-related code.

Concretely:
- **Remove** the season system entirely (SeasonState, SeasonManager, /pantheon season commands, season config, season GameTests, season NBT fields, season BE mirror fields).
- **Add** a `color` field to `Faction` (DyeColor index 0-15).
- **Add** a Temple block GUI (LDLib2 Menu UI via `BlockUIMenuType`): create faction (name + color palette + Create button) or edit existing faction (rename + recolor + member list + Save button).
- **Add** block color rendering: Temple block takes the faction's color via a blockstate property mapping to the 16 dye colors.
- **Add** on-login faction selection: if player has no faction, open a GUI listing all factions (color swatch + name) with a Skip button.
- **Add** scoreboard team sync: each faction = one vanilla scoreboard team with the matching color, auto-managed.
- **Make GUIs look good**: Minecraft-like, modern, using LDLib2 `StylesheetManager.MC` theme + custom LSS.

## 2. Out of scope

- Mayor election, god assignment, quest system, economy, skilltree, LLM — all deferred to later phases.
- Custom client-side nametag rendering — we use vanilla scoreboard teams which handle nametag coloring natively.
- Full RGB color picker — fixed 16-color palette (Minecraft dye colors) only.
- Faction deletion — admin can break the Temple block in creative, which unlinks the faction but doesn't delete it from SavedData (it persists, just loses its physical anchor). A `/pantheon faction delete` command is not in scope.

## 3. Season removal

### Files to delete
- `src/main/kotlin/gg/wildblood/faction/SeasonState.kt`
- `src/main/kotlin/gg/wildblood/season/SeasonManager.kt` (and the `season/` package)

### Files to modify
- `PantheonSavedData.kt` — remove `season` field, `startSeason()`, `endSeason()`, season NBT save/load block, `SeasonState` import.
- `TempleBlockEntity.kt` — remove `seasonPhase` field, remove `season` parameter from `syncFrom()`, remove `SeasonState` import, update `loadAdditional` to not pass `data.season`.
- `ModCommands.kt` — remove the entire `/pantheon season` subtree (lines 26-34, 55-94), remove `startSeason()`, `endSeason()`, `seasonInfo()` methods, remove `SeasonState`/`Config.DEFAULT_SEASON_DURATION_DAYS` imports.
- `Config.kt` — remove `DEFAULT_SEASON_DURATION_DAYS` value.
- `Pantheon.kt` — remove `SeasonManager` registration + import.
- `PantheonGameTests.kt` — remove `pantheon_season_start`, `pantheon_season_end`, `pantheon_season_info` tests + `SeasonState` import. Remove `data.season = null` lines from remaining tests. Remove `data.startSeason(7)` from `pantheon_persistence` test. Update `pantheon_persistence` to not assert on season. Update `pantheon_temple_mirror` to call `syncFrom(faction)` without season parameter.
- `en_us.json` — remove season-related translation keys.
- Structure templates — delete `pantheon_season_start.nbt`, `pantheon_season_end.nbt`, `pantheon_season_info.nbt`.

## 4. Faction data model changes

### `Faction.kt` — add color field

```kotlin
data class Faction(
    val id: ResourceLocation,
    val displayName: String,
    val anchor: BlockPos,
    val color: Int = 0,  // DyeColor ordinal (0-15), default white
    var godId: ResourceLocation? = null,
    val members: MutableSet<UUID> = mutableSetOf(),
    var mayor: UUID? = null,
    var skillpointPool: Int = 0,
    val skilltreeState: MutableMap<ResourceLocation, Boolean> = mutableMapOf(),
) {
    val memberCount: Int get() = members.size
}
```

### ID inference from name

`FactionId.fromDisplayName(name)` utility:
- Lowercase the name
- Replace spaces with underscores
- Strip characters not matching `[a-z0-9_]`
- Collapse multiple underscores
- Strip leading/trailing underscores
- Prefix with `pantheon:`
- Example: "Sun Keep!" → `pantheon:sun_keep`
- If the result is empty after stripping, return null (invalid name)

### `PantheonSavedData.kt` — add color to NBT + new accessors

- `save`: add `fTag.putInt("color", f.color)` in the faction serialization block.
- `load`: add `val color = fTag.getInt("color")` and pass to `Faction(... color = color ...)`.
- Add `createFaction(displayName: String, color: Int, anchor: BlockPos): Faction` — infers id from name, checks if a faction with that id already exists. If it does AND its anchor is `BlockPos.ZERO` (broken Temple), re-links it to the new anchor (updates anchor + color). If it exists AND has a valid anchor, throws/returns error. If it doesn't exist, creates a new faction. Syncs scoreboard team.
- Add `updateFaction(factionId: ResourceLocation, displayName: String?, color: Int?)` — updates name and/or color, syncs scoreboard team.
- Add `removeFactionAnchor(anchor: BlockPos)` — when Temple block is broken, sets the faction's anchor to `BlockPos.ZERO` (faction persists but loses its physical link). Does NOT delete the faction or remove members.

### Scoreboard team sync

A new `FactionTeamManager` object handles vanilla scoreboard team lifecycle:

```kotlin
object FactionTeamManager {
    fun syncFaction(server: MinecraftServer, faction: Faction) {
        val scoreboard = server.scoreboard
        val teamName = faction.id.toString()
        var team = scoreboard.getPlayerTeam(teamName)
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName)
        }
        team.displayName = Component.literal(faction.displayName)
        team.color = DYE_TO_FORMATTING[faction.color] ?: ChatFormatting.WHITE
        // Sync members
        val currentMembers = team.players.toSet()
        for (uuid in faction.members) {
            val name = server.profileCache?.get(uuid)?.orElse(null)?.name ?: continue
            if (name !in currentMembers) {
                scoreboard.addPlayerToTeam(name, team)
            }
        }
        for (name in currentMembers) {
            val uuid = server.profileCache?.get(name)?.orElse(null)?.id ?: continue
            if (uuid !in faction.members) {
                scoreboard.removePlayerFromTeam(name, team)
            }
        }
    }

    fun removeFaction(server: MinecraftServer, factionId: ResourceLocation) {
        val scoreboard = server.scoreboard
        val team = scoreboard.getPlayerTeam(factionId.toString()) ?: return
        scoreboard.removePlayerTeam(team)
    }
}
```

Called from `PantheonSavedData.createFaction`, `updateFaction`, and on player faction join.

### DyeColor → ChatFormatting mapping

```kotlin
val DYE_TO_FORMATTING = mapOf(
    0 to ChatFormatting.WHITE,       // white
    1 to ChatFormatting.GOLD,        // orange
    2 to ChatFormatting.LIGHT_PURPLE, // magenta
    3 to ChatFormatting.AQUA,        // light blue
    4 to ChatFormatting.YELLOW,      // yellow
    5 to ChatFormatting.GREEN,       // lime
    6 to ChatFormatting.LIGHT_PURPLE, // pink
    7 to ChatFormatting.DARK_GRAY,   // gray
    8 to ChatFormatting.GRAY,        // light gray
    9 to ChatFormatting.AQUA,        // cyan
    10 to ChatFormatting.DARK_PURPLE, // purple
    11 to ChatFormatting.DARK_BLUE,  // blue
    12 to ChatFormatting.DARK_BROWN, // brown (closest: DARK_RED)
    13 to ChatFormatting.DARK_GREEN, // green
    14 to ChatFormatting.DARK_RED,   // red
    15 to ChatFormatting.BLACK,      // black
)
```

Note: Minecraft's `ChatFormatting` has fewer colors than `DyeColor` (16 vs 16 but not 1:1 — some mappings are approximate). We use the standard mapping from `DyeColor` to `ChatFormatting` that vanilla itself uses for team colors where possible.

## 5. Temple block changes

### Blockstate property: COLOR

Add a `COLOR` property to `TempleBlock` using vanilla's `DyeColor` enum (0-15):

```kotlin
class TempleBlock(properties: Properties) : Block(properties), EntityBlock, BlockUIMenuType.BlockUI {
    companion object {
        val COLOR: EnumProperty<DyeColor> = EnumProperty.create("color", DyeColor::class.java)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(COLOR, DyeColor.WHITE).build())
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(COLOR)
    }
    // ...
}
```

- When a faction is created/updated, the block's `COLOR` property is set to match the faction's color.
- Blockstate JSON: 16 variants, each pointing to the same model but with a `tintindex` override, OR 16 variants each with a different `tintindex` tint. Simplest approach: one model with `tintindex: 0` and a blockstate file that sets `tintindex` per color variant via a model override. Actually, the simplest: use a single model with a `tintindex` and let the blockstate property drive a `tintindex` value via 16 model files (one per color, each with a different hardcoded tint in the model's `tintindex` element). Or use a single model + a custom `BlockColor` provider registered on the client.

**Chosen approach:** Register a client-side `BlockColor` provider (in `PantheonClient`) that returns the `DyeColor`'s RGB value for `tintIndex 0`. The model has `"tintindex": 0` on the `all` faces. One model, one blockstate (single variant), the color is driven by the blockstate property → `BlockColor` provider. This is the standard vanilla approach for tinted blocks (like grass, leaves).

### Block interaction: `BlockUIMenuType.BlockUI`

```kotlin
override fun useWithoutItem(
    state: BlockState, level: Level, pos: BlockPos,
    player: Player, hit: BlockHitResult,
): InteractionResult {
    if (!level.isClientSide && player is ServerPlayer) {
        BlockUIMenuType.openUI(player, pos)
    }
    return InteractionResult.SUCCESS
}

override fun createUI(holder: BlockUIMenuType.BlockUIHolder): ModularUI {
    return TempleUIFactory.createFactionUI(holder)
}
```

- `TempleUIFactory` (in `gg.wildblood.client.gui`) builds the LDLib2 UI based on whether a faction exists at this position.
- The factory needs server-side data (faction state), so it's a Menu UI (`ModularUI.of(ui, holder.player)`).

### Block break: unlink faction

When the Temple block is broken (creative only), call `PantheonSavedData.removeFactionAnchor(pos)` and `FactionTeamManager` cleanup. The faction persists in SavedData but loses its anchor. A new Temple block can be placed and the faction can be re-linked via the GUI (if admin enters the existing faction name, it re-links instead of creating a new one).

## 6. Temple GUI design

### 6.1 Create faction screen (no faction at this block)

```
┌─────────────────────────────────────────────┐
│              Create Faction                  │
│                                              │
│  Faction Name:                               │
│  ┌──────────────────────────────────────┐   │
│  │ Enter name...                         │   │
│  └──────────────────────────────────────┘   │
│                                              │
│  Faction Color:                              │
│  ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐  │
│  │  │ │  │ │  │ │  │ │  │ │  │ │  │ │  │  │
│  └──┘ └──┘ └──┘ └──┘ └──┘ └──┘ └──┘ └──┘  │
│  ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐  │
│  │  │ │  │ │  │ │  │ │  │ │  │ │  │ │  │  │
│  └──┘ └──┘ └──┘ └──┘ └──┘ └──┘ └──┘ └──┘  │
│                                              │
│  ID: pantheon:sun_keep                       │
│                                              │
│         ┌──────────────┐                     │
│         │   Create     │                     │
│         └──────────────┘                     │
└─────────────────────────────────────────────┘
```

- Name text field (bound to server-side `var name: String`)
- Color palette: `toggleGroup` of 16 `Toggle` elements, each with a `ColorRectTexture` background in the dye color
- ID preview label (updates as name is typed — `bindS2C` showing the inferred id)
- Create button: `setOnServerClick` → validates name non-empty, infers id, checks uniqueness, creates faction, sets block color, closes UI

### 6.2 Edit faction screen (faction exists at this block)

```
┌─────────────────────────────────────────────┐
│              Manage Faction                  │
│                                              │
│  Faction Name:                               │
│  ┌──────────────────────────────────────┐   │
│  │ Sun Keep                               │   │
│  └──────────────────────────────────────┘   │
│                                              │
│  Faction Color:                              │
│  ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐  │
│  │  │ │  │ │██│ │  │ │  │ │  │ │  │ │  │  │  (selected = orange)
│  └──┘ └──┘ └──┘ └──┘ └──┘ └──┘ └──┘ └──┘  │
│  ... (second row) ...                        │
│                                              │
│  Members (12):                               │
│  ┌──────────────────────────────────────┐   │
│  │ • Player1                              │   │
│  │ • Player2                              │   │
│  │ • Player3                              │   │
│  │ ...                                    │   │
│  └──────────────────────────────────────┘   │
│                                              │
│         ┌──────────────┐                     │
│         │    Save      │                     │
│         └──────────────┘                     │
└─────────────────────────────────────────────┘
```

- Same name field + color palette (pre-filled with current values)
- Member list: `scrollerView` with labels for each member name (read-only)
- Save button: `setOnServerClick` → updates faction name/color, syncs scoreboard team, updates block color, closes UI
- Non-admin players: name field read-only, color palette disabled, Save button hidden (replaced with "Close" button)

### 6.3 On-login faction selection screen

```
┌─────────────────────────────────────────────┐
│           Choose Your Faction                │
│                                              │
│  Select a faction to join, or skip to        │
│  remain factionless.                         │
│                                              │
│  ┌──────────────────────────────────────┐   │
│  │  ██ Sun Keep                    Join  │   │  (orange swatch + name + Join)
│  └──────────────────────────────────────┘   │
│  ┌──────────────────────────────────────┐   │
│  │  ██ Void Spire                 Join  │   │  (purple swatch + name + Join)
│  └──────────────────────────────────────┘   │
│                                              │
│         ┌──────────────┐                     │
│         │    Skip      │                     │
│         └──────────────┘                     │
└─────────────────────────────────────────────┘
```

- One button per faction (with color swatch + faction name + "Join" text)
- Clicking a faction button: sets player's `pantheon:faction` attachment, adds to `faction.members`, syncs scoreboard team, closes UI
- Skip button: closes UI, player remains factionless (prompt reappears on next login)
- This is a `PlayerUIMenuType` UI (no block/item needed)

### 6.4 LSS styling

Custom LSS applied via `StylesheetManager.MC` base + a custom stylesheet for modern polish:

```css
.panel_bg {
    background: built-in(ui-gdp:BORDER);
    padding-all: 10;
    gap-all: 6;
}
.panel_bg label {
    horizontal-align: center;
    color: 0xFFFFFFFF;
}
.color-swatch {
    width: 20;
    height: 20;
    border: 2;
    border-color: 0xFF000000;
}
.color-swatch.selected {
    border-color: 0xFFFFFFFF;
    border: 3;
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
.btn {
    width: 80;
    height: 20;
    background: built-in(ui-gdp:RECT);
}
.btn:hover {
    background: built-in(ui-gdp:RECT_SOLID);
}
```

## 7. On-login prompt flow

Register a `PlayerEvent.PlayerLoggedInEvent` listener on the NeoForge event bus (in a new `FactionLoginHandler` object):

```kotlin
object FactionLoginHandler {
    @SubscribeEvent
    fun onPlayerLogin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        val faction = player.getData(ModAttachments.FACTION.get())
        if (faction == null) {
            val server = player.server
            val data = PantheonSavedData.get(server)
            if (data.factions.isNotEmpty()) {
                PlayerUIMenuType.openUI(player, FACTION_SELECT_UI_ID)
            }
        } else {
            // Re-sync scoreboard team membership (in case player name changed)
            val data = PantheonSavedData.get(server)
            val f = data.factions[faction]
            if (f != null) {
                FactionTeamManager.syncFaction(server, f)
            }
        }
    }
}
```

- `FACTION_SELECT_UI_ID` = `ResourceLocation.fromNamespaceAndPath("pantheon", "faction_select")`
- Registered via `PlayerUIMenuType.register(FACTION_SELECT_UI_ID) { { p -> FactionSelectUI.create(p) } }` during mod init (in `Pantheon.kt` `init` block or `FMLCommonSetupEvent`).

## 8. Block color rendering (client)

In `PantheonClient.kt`, register a `BlockColor` provider during client setup:

```kotlin
@Mod(value = Pantheon.MODID, dist = [Dist.CLIENT])
object PantheonClient {
    init {
        LOADING_CONTEXT.activeContainer.registerExtensionPoint(IConfigScreenFactory::class.java) { ->
            IConfigScreenFactory { container, parent -> ConfigurationScreen(container, parent) }
        }
        MOD_BUS.addListener(::onClientSetup)
    }

    private fun onClientSetup(event: ClientPlayerNetworkEvent?) {
        // Register block color provider for Temple block tintindex 0
        Minecraft.getInstance().blockColors.register(
            { state, _, _, _, _ ->
                val dye = state.getValue(TempleBlock.COLOR)
                dye.mapColor.col
            },
            ModBlocks.TEMPLE.get()
        )
    }
}
```

Actually, the correct NeoForge 1.21.1 way to register `BlockColor` is via `RegisterColorHandlersEvent.Block` on the mod bus:

```kotlin
MOD_BUS.addListener { event: RegisterColorHandlersEvent.Block ->
    event.register({ state, _, _, _, tintIndex ->
        if (tintIndex == 0) {
            state.getValue(TempleBlock.COLOR).mapColor.col
        } else -1
    }, ModBlocks.TEMPLE.get())
}
```

The block model (`temple.json`) uses `tintindex: 0` on the `all` faces:

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "pantheon:block/temple"
  },
  "elements": [
    {
      "from": [0, 0, 0],
      "to": [16, 16, 16],
      "faces": {
        "down": { "texture": "#all", "tintindex": 0, "cullface": "down" },
        "up": { "texture": "#all", "tintindex": 0, "cullface": "up" },
        "north": { "texture": "#all", "tintindex": 0, "cullface": "north" },
        "south": { "texture": "#all", "tintindex": 0, "cullface": "south" },
        "west": { "texture": "#all", "tintindex": 0, "cullface": "west" },
        "east": { "texture": "#all", "tintindex": 0, "cullface": "east" }
      }
    }
  ]
}
```

The base texture (`temple.png`) should be a grayscale/white texture so the tint shows correctly.

Blockstate JSON changes to include the `color` property:

```json
{
  "variants": {
    "color=white": { "model": "pantheon:block/temple" },
    "color=orange": { "model": "pantheon:block/temple" },
    ...
    "color=black": { "model": "pantheon:block/temple" }
  }
}
```

All variants point to the same model — the tint is applied by the `BlockColor` provider based on the blockstate.

## 9. Package layout

```
gg.wildblood
├── Pantheon.kt                      MODIFY — remove SeasonManager, add FactionLoginHandler + PlayerUIMenuType registration
├── faction/
│   ├── Faction.kt                   MODIFY — add color field
│   ├── FactionId.kt                 CREATE — id inference utility
│   ├── FactionTeamManager.kt        CREATE — scoreboard team sync
│   └── PantheonSavedData.kt         MODIFY — remove season, add color, add createFaction/updateFaction/removeFactionAnchor
├── block/
│   ├── ModBlocks.kt                 MODIFY — TempleBlock now has COLOR property
│   └── TempleBlock.kt               MODIFY — add COLOR property, BlockUIMenuType.BlockUI, useWithoutItem, createUI
├── blockentity/
│   ├── ModBlockEntities.kt          (unchanged)
│   └── TempleBlockEntity.kt         MODIFY — remove seasonPhase, update syncFrom
├── client/
│   ├── PantheonClient.kt            MODIFY — register BlockColor provider for Temple tint
│   └── gui/
│       ├── TempleUIFactory.kt       CREATE — create/edit faction GUI (BlockUIMenuType)
│       └── FactionSelectUI.kt       CREATE — on-login faction selection GUI (PlayerUIMenuType)
├── command/
│   └── ModCommands.kt               MODIFY — remove season commands, keep faction commands
├── config/
│   └── Config.kt                    MODIFY — remove DEFAULT_SEASON_DURATION_DAYS
├── attachment/
│   └── ModAttachments.kt            (unchanged)
├── gametest/
│   └── PantheonGameTests.kt         MODIFY — remove season tests, add faction color tests
└── login/
    └── FactionLoginHandler.kt       CREATE — on-login faction selection prompt
```

New packages: `gg.wildblood.login` (login event handler). GUI factories go in `gg.wildblood.client.gui` (already exists).

## 10. Testing

### GameTests to add/modify

Keep these existing tests (modified to remove season references):
- `pantheon_faction_create` — update to use new `createFaction(name, color, anchor)` instead of command
- `pantheon_faction_assign` — update to verify scoreboard team membership
- `pantheon_faction_info` — no change
- `pantheon_faction_list` — no change
- `pantheon_temple_mirror` — update `syncFrom` call (no season param), assert `color` mirror field
- `pantheon_persistence` — remove season assertions, add color round-trip assertion

Remove:
- `pantheon_season_start`, `pantheon_season_end`, `pantheon_season_info` (delete + remove templates)

Add:
- `pantheon_faction_color` — create faction with color, verify NBT round-trip preserves color
- `pantheon_faction_id_inference` — verify `FactionId.fromDisplayName` produces correct ids
- `pantheon_scoreboard_team` — create faction, assign player, verify scoreboard team exists with correct color

### Manual testing

- Place Temple block → right-click → GUI opens with name field + color palette
- Enter name "Sun Keep", select orange, click Create → block turns orange, faction created
- Right-click again → edit screen shows name "Sun Keep", color orange, member list
- Change name to "Sun Kingdom", change color to yellow, click Save → block turns yellow, scoreboard team updates
- New player joins → faction selection GUI appears with "Sun Kingdom" (yellow swatch)
- Player clicks "Sun Kingdom" → nametag turns yellow, tab list shows team color
- Player skips → remains factionless, no nametag color
- Next login of same player → prompt appears again

## 11. Error handling

- **Empty faction name:** Create button shows error "Name cannot be empty", no faction created.
- **Invalid name (all special chars):** `FactionId.fromDisplayName` returns null → "Invalid name" error.
- **Duplicate faction id:** "A faction with this name already exists" error. (If the admin is re-linking a broken Temple, they must enter the exact existing name — the create flow checks if the inferred id matches an existing faction and offers to re-link instead.)
- **No factions on login:** If `data.factions` is empty, the selection GUI is not opened (nothing to choose).
- **Scoreboard team creation failure:** Log warning, continue without team (non-fatal — nametag just won't be colored).
- **Block break in survival:** Already prevented by `onDestroyedByPlayer` returning false for non-creative.

## 12. Translation keys

Add to `en_us.json`:

```json
  "pantheon.gui.faction_create.title": "Create Faction",
  "pantheon.gui.faction_create.name": "Faction Name",
  "pantheon.gui.faction_create.name_placeholder": "Enter name...",
  "pantheon.gui.faction_create.color": "Faction Color",
  "pantheon.gui.faction_create.id_preview": "ID: %s",
  "pantheon.gui.faction_create.create": "Create",
  "pantheon.gui.faction_create.error.empty_name": "Name cannot be empty",
  "pantheon.gui.faction_create.error.invalid_name": "Invalid name",
  "pantheon.gui.faction_create.error.exists": "A faction with this name already exists",
  "pantheon.gui.faction_manage.title": "Manage Faction",
  "pantheon.gui.faction_manage.members": "Members (%d):",
  "pantheon.gui.faction_manage.save": "Save",
  "pantheon.gui.faction_manage.close": "Close",
  "pantheon.gui.faction_select.title": "Choose Your Faction",
  "pantheon.gui.faction_select.description": "Select a faction to join, or skip to remain factionless.",
  "pantheon.gui.faction_select.join": "Join",
  "pantheon.gui.faction_select.skip": "Skip",
  "pantheon.gui.faction_select.no_factions": "No factions available yet."
```

Remove season-related keys:
- `pantheon.command.season.start.*`, `pantheon.command.season.end.*`, `pantheon.command.season.info.*`

## 13. Acceptance criteria

1. `./gradlew build --no-daemon --no-configuration-cache` succeeds.
2. `./gradlew runGameTestServer --no-daemon --no-configuration-cache` passes all remaining + new GameTests.
3. No `SeasonState` or `SeasonManager` references remain in the codebase.
4. Placing a Temple block and right-clicking opens the faction creation GUI.
5. Creating a faction via GUI sets the block's color (visible tint).
6. Right-clicking an existing faction's Temple block opens the manage GUI with editable name/color and member list.
7. A new player logging in with no faction sees the faction selection GUI.
8. Joining a faction via the selection GUI gives the player the faction's color in their nametag and tab list.
9. Scoreboard teams are auto-created and colored per faction.
10. No client-only class is referenced outside `gg.wildblood.client`.
11. GUIs look Minecraft-like and modern (panel backgrounds, styled buttons, colored swatches).

## 14. Open questions

1. **Re-linking broken factions:** When a Temple block is broken (creative) and a new one placed, should the admin be able to re-link to an existing faction by entering its name? The design says yes (create flow checks for existing id and re-links). Confirm this is desired.
2. **Texture:** The Temple block texture needs to be grayscale/white for the tint to show correctly. Currently it's a gold color (212,175,55). Should I replace it with a white 16×16 texture (or a stone-like gray pattern)?
3. **Member list in manage GUI:** Should it show offline members too, or only online? The design shows all members (read-only list of names). Confirm.