# Phase 0 — Foundation: Faction/Island Data Model, Season Lifecycle, Temple Block

**Status:** Draft (pending user review)
**Date:** 2026-07-22
**Phase:** 0 of 9 (see `docs/superpowers/specs/` roadmap)
**Depends on:** nothing (first phase)
**Blocks:** Phase 1 (World/Void), Phase 2 (Vote), Phase 3 (Gods stub), all downstream

---

## 1. Goal

Establish the canonical, always-available, server-authoritative state that every later Pantheon module reads and mutates:

- The **season** (id, start/end timestamps, lifecycle phase).
- The two **factions** (id, display name, island anchor `BlockPos`, assigned god id, member roster, mayor uuid, skillpoint pool, skilltree state).
- Per-player **attachments** (faction id, bank account id).
- A physical in-world **Temple block + block entity** that mirrors faction state for client display and click-interaction (Approach C: SavedData canonical, BE mirror).
- Admin **commands** to start/end a season and to create/assign factions.
- A **config** holding island anchor positions and season duration bounds.

Phase 0 delivers no gameplay — it is the data substrate. Quests, votes, economy, skilltrees, LLM all plug into this foundation in later phases.

## 2. Out of scope (explicitly deferred)

- Quest system, vote system, economy, skilltree effects, mayor election logic, god persona/LLM.
- Island worldgen / void no-build zone (Phase 1).
- Any LDLib2 ModularUI screen (Phase 2+; the Temple BE has no UI in Phase 0, only data mirroring).
- The god-vote / mayor-election / skillpoint-confirm vote flows (Phase 2).

## 3. Architecture (Approach C — Hybrid SavedData + BE mirror)

```
┌─────────────────────────────────────────────────────────────┐
│  PantheonSavedData  (world-level, Overworld-attached)        │
│  ─────────────────────────────────────────────────────────  │
│  season: SeasonState                                        │
│  factions: Map<FactionId, Faction>  (exactly 2 entries)     │
│  ─────────────────────────────────────────────────────────  │
│  Canonical, always-loaded, survives restart.                │
│  All mutations go through here; setDirty() after each.       │
└─────────────────────────────────────────────────────────────┘
        │  read on load/tick            │  write-through on mutation
        ▼                               ▼
┌─────────────────────────────────────────────────────────────┐
│  TempleBlockEntity  (one per faction, at island anchor)     │
│  ─────────────────────────────────────────────────────────  │
│  LDLib2 ISyncPersistRPCBlockEntity                           │
│  @Persisted @DescSynced mirrors: factionId, godId,          │
│     mayorUuid, memberCount, skillpointPool, seasonPhase      │
│  No gameplay logic. Read-only mirror + interaction point.   │
└─────────────────────────────────────────────────────────────┘
        │  @DescSynced server→client
        ▼
┌─────────────────────────────────────────────────────────────┐
│  Client  (sees god name, member count, skillpoints, phase)  │
└─────────────────────────────────────────────────────────────┘
```

**Sync rule (one-way):** `PantheonSavedData` is canonical. `TempleBlockEntity` reads from SavedData on chunk load and on every server tick (cheap; only 2 BEs). Every mutation goes through SavedData first, then the relevant Temple BE's mirror fields are updated and `markDirty`'d so LDLib2 pushes the change to tracking clients. The BE never owns state; it only reflects it.

**Why SavedData over a FactionBlockEntity-as-source-of-truth:** faction state must be queryable when the island chunk is unloaded (votes from afar, economy transfers, season-end checks). SavedData on the Overworld is the only always-loaded store. The Temple BE gives the in-world interaction point + LDLib2 `@DescSynced` auto-sync without being the source of truth.

## 4. Data model

### 4.1 `SeasonState`

```kotlin
data class SeasonState(
    val id: UUID,                       // unique per season
    val startedAt: Long,                // epoch millis
    val endsAt: Long,                   // epoch millis (0 = open-ended)
    val phase: SeasonPhase,             // CREATED → RUNNING → ENDED
) {
    enum class SeasonPhase { CREATED, RUNNING, ENDED }
}
```

- `CREATED`: season created by admin, god-vote window open (Phase 2), factions exist but no god assigned yet.
- `RUNNING`: god-vote resolved, god assigned, normal gameplay.
- `ENDED`: season over; state frozen, ready for wipe or new season.

### 4.2 `Faction`

```kotlin
data class Faction(
    val id: ResourceLocation,          // e.g. pantheon:sunkeep / pantheon:voidspire
    val displayName: String,           // "Sunkeep" / "Voidspire"
    val anchor: BlockPos,              // island center / temple block position
    val godId: ResourceLocation?,      // null until god-vote resolves (Phase 2/3)
    val members: MutableSet<UUID>,     // player UUIDs
    val mayor: UUID?,                  // null until mayor election (Phase 7)
    val skillpointPool: Int,           // island-wide skillpoints (spent via Phase 5)
    val skilltreeState: Map<ResourceLocation, Boolean>, // nodeId → purchased?
) {
    val memberCount: Int get() = members.size
}
```

- Exactly two factions per season (enforced at creation). **Faction ids are admin-chosen at creation** via `/pantheon faction create <id> <displayName> ...` — no hardcoded ids. The `<id>` must be a valid `ResourceLocation` (e.g. `pantheon:sunkeep`, `pantheon:voidspire`, or any namespace:path the admin picks). Display name is also admin-chosen.
- `godId`, `mayor`, `skillpointPool`, `skilltreeState` are placeholders in Phase 0 — they exist in the schema so later phases fill them without a migration. They're always null/0/empty in Phase 0 runtime.

### 4.3 Player attachments

Two NeoForge `AttachmentType`s registered to `NeoForgeRegistries.ATTACHMENT_TYPES`:

| Attachment | Type | Serializer | copyOnDeath | Default |
|---|---|---|---|---|
| `pantheon:faction` | `ResourceLocation?` (nullable) | codec `Codec.optionalFieldOf("faction").xmap(...)` | **true** (survives death) | `null` |
| `pantheon:bank_account` | `UUID?` (nullable) | codec `Codec.optionalFieldOf("bank").xmap(...)` | **true** | `null` |

- `faction` = which island the player belongs to (`null` = unaffiliated, picks faction at first join via Phase 2 god-vote or admin assign).
- `bank_account` = placeholder for Phase 6 economy; always `null` in Phase 0, exists in schema so Phase 6 doesn't need a migration.

Both use codec serialization (per NeoForge attachments doc). Nullable handled via `Codec.optionalFieldOf` → `Optional<ResourceLocation>` → `.orNull`.

### 4.4 `PantheonSavedData`

```kotlin
class PantheonSavedData : SavedData() {
    var season: SeasonState? = null
    val factions: MutableMap<ResourceLocation, Faction> = mutableMapOf()

    // Accessors (all mutate then setDirty()):
    fun startSeason(...) 
    fun endSeason()
    fun getFaction(id): Faction?
    fun ensureFaction(id, displayName, anchor): Faction
    fun assignPlayerToFaction(player, factionId)
    fun setMayor(factionId, uuid)
    fun setGod(factionId, godId)
    fun addSkillpoints(factionId, amount)
    fun purchaseSkill(factionId, nodeId)

    companion object {
        val FACTORY = Factory(::create, ::load)
        const val DATA_NAME = "pantheon"

        fun get(server: MinecraftServer): PantheonSavedData =
            server.overworld().dataStorage.computeIfAbsent(FACTORY, DATA_NAME)
    }
}
```

- Attached to Overworld (per NeoForge docs: only dimension never fully unloaded).
- `computeIfAbsent` lazy-creates on first access.
- Every mutator calls `setDirty()` at the end.
- Single `get(server)` entry point; all modules call `PantheonSavedData.get(server)`.

### 4.5 NBT schema (SavedData save/load)

```
pantheon.dat (in overworld/data/)
├── season: CompoundTag (optional)
│   ├── id: IntArray (UUID encoded)
│   ├── startedAt: Long
│   ├── endsAt: Long
│   └── phase: String ("CREATED"/"RUNNING"/"ENDED")
└── factions: ListTag
    └── [per faction]
        ├── id: String ("pantheon:sunkeep")
        ├── displayName: String
        ├── anchor: IntArray (BlockPos encoded: [x, y, z])
        ├── godId: String (optional)
        ├── members: ListTag<IntArray> (UUIDs)
        ├── mayor: IntArray (optional, UUID)
        ├── skillpointPool: Int
        └── skilltreeState: ListTag<CompoundTag>
            └── { nodeId: String, purchased: Byte }
```

Hand-written NBT (not Codec) for SavedData — matches the NeoForge doc pattern and avoids codec-optional-field gotchas. Codecs are used only for the player attachments (where NeoForge's attachment API expects them).

## 5. Temple block + block entity

### 5.1 `TempleBlock`

- `Block` subclass implementing `EntityBlock`.
- `BlockBehaviour.Properties`: strength 3.5F, requires correct tool, no drops by default (Temple is indestructible admin-placed; destruction handled in Phase 1 no-build zone).
- **Creative-only break:** override `getDestroyProgress` / `canHarvestBlock` (or use `BlockBehaviour.Properties.dynamicShape().noOcclusion()` + a `PlayerDestroyItem`/`BlockEvent.BreakEvent` cancel for non-creative). Simplest 1.21.1 approach: override `Block.playerWillDestroy(level, pos, state, player)` and `Block.playerDestroy(...)` — if `!player.isCreative`, cancel the break (restore the block, return early). This makes the Temple unbreakable except in creative mode, without needing the Phase 1 no-build zone.
- `newBlockEntity(pos, state)` → `TempleBlockEntity`.
- No `use` interaction in Phase 0 (UI deferred to Phase 2+). Right-click is a no-op for now.

### 5.2 `TempleBlockEntity`

```kotlin
class TempleBlockEntity(pos: BlockPos, state: BlockState)
    : BlockEntity(ModBlockEntities.TEMPLE.get(), pos, state), ISyncPersistRPCBlockEntity {

    @get:JvmName("getSyncStorage")
    val syncStorage = FieldManagedStorage(this)

    @Persisted @DescSynced var factionId: String = ""           // ResourceLocation.toString()
    @Persisted @DescSynced var godId: String = ""               // "" = unassigned
    @Persisted @DescSynced var mayorUuid: String = ""           // "" = no mayor
    @Persisted @DescSynced var memberCount: Int = 0
    @Persisted @DescSynced var skillpointPool: Int = 0
    @Persisted @DescSynced var seasonPhase: String = "CREATED"

    // Mirror update — called by SeasonManager each tick (server side)
    fun syncFrom(faction: Faction, season: SeasonState?) {
        factionId = faction.id.toString()
        godId = faction.godId?.toString() ?: ""
        mayorUuid = faction.mayor?.toString() ?: ""
        memberCount = faction.memberCount
        skillpointPool = faction.skillpointPool
        seasonPhase = season?.phase?.name ?: "CREATED"
        setChanged()
    }
}
```

- Stores strings (not `ResourceLocation`/`UUID`) because LDLib2 `@DescSynced` syncs primitives + `String` natively; avoids read-only-managed boilerplate for mirror-only fields.
- `syncFrom()` is the only writer; called by `SeasonManager` on tick (see §6). The BE never owns state.
- **Chunk-load re-sync:** `loadAdditional` calls `syncFrom` once on load by looking up its faction in `PantheonSavedData` (via `level.server`), so a freshly loaded BE mirrors current state immediately rather than waiting up to 1s for the next tick.
- No `@RPCMethod` in Phase 0 (no UI interactions yet).

### 5.3 Registration

- `ModBlocks.TEMPLE`: `DeferredBlock<TempleBlock>` — **keep `EXAMPLE_BLOCK` for now** (harmless, used by the creative tab icon); add `TEMPLE` alongside it.
- `ModBlockEntities.TEMPLE`: `DeferredHolder<BlockEntityType<*>, BlockEntityType<TempleBlockEntity>>` via `BlockEntityType.Builder.of(::TempleBlockEntity, ModBlocks.TEMPLE.get()).build(null)`.
- Creative tab: add `TEMPLE` to `ModCreativeTabs.EXAMPLE_TAB`'s `displayItems` so it's `/give`-able and visible. (Per open question §15.1 — defaulting to creative-tab-visible.)

### 5.4 Block model / resources

- `ModDataGenerator` is currently an empty stub (no providers wired). For Phase 0, **hand-author** the minimal JSON in `src/main/resources/`:
  - `assets/pantheon/blockstates/temple.json` — `variant` with single `model = "pantheon:block/temple"`.
  - `assets/pantheon/models/block/temple.json` — `parent = "minecraft:block/cube_all"`, `textures.all = "pantheon:block/temple"`.
  - `assets/pantheon/models/item/temple.json` — `parent = "pantheon:block/temple"` (for the item form / creative tab icon).
  - `assets/pantheon/textures/block/temple.png` — placeholder texture (copy `minecraft:block/stone` via resource-merge or a simple 16×16 PNG committed to the repo).
- When a later phase wires real datagen providers into `ModDataGenerator`, these hand-authored files move to `src/generated/resources/` via a `BlockStateProvider` + `ItemModelProvider`. Not Phase 0's concern.
- Lang entry: `block.pantheon.temple` = `"Temple"` (added to `en_us.json`, see §13).

## 6. SeasonManager (server-side, tick-driven)

```kotlin
object SeasonManager {
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        val server = event.server
        if (event.phase != TickEvent.Phase.END) return
        val data = PantheonSavedData.get(server)
        // 1. Season end check
        data.season?.let { s ->
            if (s.phase == SeasonState.SeasonPhase.RUNNING && s.endsAt > 0 && System.currentTimeMillis() >= s.endsAt) {
                endSeason(server)
            }
        }
        // 2. Mirror to Temple BEs (throttled — every 20 ticks = 1s)
        if (server.tickCount % 20 == 0) {
            data.factions.values.forEach { faction ->
                (server.overworld().getBlockEntity(faction.anchor) as? TempleBlockEntity)
                    ?.syncFrom(faction, data.season)
            }
        }
    }
}
```

- Registered on the NeoForge event bus (`NeoForge.EVENT_BUS.register(SeasonManager)` in `Pantheon`).
- Tick-end only, mirror throttled to 1 Hz (cheap; 2 BEs max).
- `endSeason()` sets `season.phase = ENDED`, setDirty, logs. No wipe logic in Phase 0 (admin decides post-season).
- If a Temple BE chunk is unloaded, the mirror write is skipped silently — SavedData stays canonical; BE re-syncs on next chunk load via the `loadAdditional` hook in §5.2.

## 7. Commands

### 7.1 `/pantheon season`

```
/pantheon season start [durationDays]   — creates a new season (CREATED phase), sets endsAt
/pantheon season end                    — ends the current season (ENDED phase)
/pantheon season info                   — prints current season id, phase, start/end
```

- `start` requires no existing season OR previous season ENDED. Generates new `UUID`, `startedAt = now`, `endsAt = now + durationDays*86400000` (0 if durationDays omitted = open-ended).
- Permission: OP level 2 (configurable later).

### 7.2 `/pantheon faction`

```
/pantheon faction create <id> <displayName> <x> <y> <z>   — creates faction at anchor (must be Temple block)
/pantheon faction assign <player> <factionId>            — sets player's faction attachment
/pantheon faction info <factionId>                        — prints faction state
/pantheon faction list                                   — lists both factions
```

- `create` verifies a `TempleBlock` exists at `<x> <y> <z>` (errors otherwise), creates the `Faction`, adds to SavedData.
- `assign` sets the player's `pantheon:faction` attachment and adds UUID to `faction.members`.
- Permission: OP level 2.

### 7.3 Wiring

`ModCommands.register(dispatcher)` builds the `pantheon` root literal with `season` and `faction` sub-literals. `Pantheon` registers a `RegisterCommandsEvent` listener on the NeoForge event bus that calls `ModCommands.register`.

## 8. Config additions

Add to `Config.kt`:

```kotlin
val ISLAND_ANCHOR_A: ModConfigSpec.ConfigValue<String> = BUILDER
    .comment("BlockPos of faction A's island anchor (Temple block). Format: 'x y z'")
    .define("islandAnchorA", "0 100 0")

val ISLAND_ANCHOR_B: ModConfigSpec.ConfigValue<String> = BUILDER
    .comment("BlockPos of faction B's island anchor (Temple block). Format: 'x y z'")
    .define("islandAnchorB", "1000 100 0")

val DEFAULT_SEASON_DURATION_DAYS: ModConfigSpec.IntValue = BUILDER
    .comment("Default season duration in days (0 = open-ended). Used by /pantheon season start with no arg.")
    .defineInRange("defaultSeasonDurationDays", 60, 0, 365)
```

- Config holds anchor positions as a convenience default; the canonical anchor is the `Faction.anchor` in SavedData (admin may override at `faction create`).
- Translation keys added to `en_us.json`: `pantheon.configuration.islandAnchorA`, `.islandAnchorB`, `.defaultSeasonDurationDays`.

## 9. Package layout (files to create/modify)

```
gg.wildblood
├── Pantheon.kt                      (MOD) — add SeasonManager registration, RegisterCommandsEvent listener
├── faction/
│   ├── Faction.kt                   data class
│   ├── SeasonState.kt               data class + enum
│   └── PantheonSavedData.kt         SavedData + accessors
├── block/
│   ├── ModBlocks.kt                 add TEMPLE DeferredBlock<TempleBlock>
│   └── TempleBlock.kt               Block + EntityBlock
├── blockentity/
│   ├── ModBlockEntities.kt          add TEMPLE DeferredHolder
│   └── TempleBlockEntity.kt         ISyncPersistRPCBlockEntity mirror
├── command/
│   └── ModCommands.kt               implement /pantheon season + /pantheon faction
├── config/
│   └── Config.kt                    add 3 config values
├── data/
│   └── ModDataGenerator.kt          add Temple blockstate/model/lang provider (if datagen route)
└── attachment/
    └── ModAttachments.kt            DeferredRegister<AttachmentType<?>> + FACTION, BANK_ACCOUNT
```

New package `gg.wildblood.faction` (canonical state) and `gg.wildblood.attachment` (player attachments). Both are sibling to existing packages per AGENTS.md layout rules.

## 10. Registration wiring in `Pantheon.kt`

```kotlin
private val REGISTRIES: List<DeferredRegister<*>> = listOf(
    ModBlocks.REGISTRY,
    ModItems.REGISTRY,
    ModCreativeTabs.REGISTRY,
    ModEntities.REGISTRY,
    ModBlockEntities.REGISTRY,
    ModAttachments.REGISTRY,   // NEW
)
```

`NeoForge.EVENT_BUS` registration in `init`:
```kotlin
NeoForge.EVENT_BUS.register(this)
NeoForge.EVENT_BUS.register(SeasonManager)   // NEW
NeoForge.EVENT_BUS.addListener(::onRegisterCommands)  // NEW
```

## 11. Testing

All tests are NeoForge **GameTests** (`@GameTest`, `@GameTestHolder`). No JUnit 5 unit tests — the user prefers in-game integration tests that exercise the real server environment (SavedData, commands, BE ticking). GameTests require `.nbt` structure templates in `data/pantheon/structure/`; these are minimal 1×1×1 empty structures (air only) generated by a build-time script and committed to `src/main/resources/`.

### 11.1 Build.gradle fix

The existing `gameTestServer` run config must add `setForceExit false` (per NeoForge docs) or the task always reports failure despite tests passing.

### 11.2 GameTests (`@GameTest`, server-side)

All tests in a single class `gg.wildblood.gametest.PantheonGameTests`, annotated `@GameTestHolder(Pantheon.MODID)` + `@PrefixGameTestTemplate(false)` (so template name = method name lowercase, no class prefix).

| Test method | Template | Verifies |
|---|---|---|
| `pantheon_season_start` | `pantheon:pantheon_season_start` | `/pantheon season start 7` → SavedData has `season` with `phase=CREATED`, `endsAt > startedAt` |
| `pantheon_season_end` | `pantheon:pantheon_season_end` | start then `end` → `phase=ENDED` |
| `pantheon_season_info` | `pantheon:pantheon_season_info` | `season info` prints season id/phase (no crash on no-season case) |
| `pantheon_faction_create` | `pantheon:pantheon_faction_create` | place Temple block at known pos → `/pantheon faction create pantheon:test_a TestA X Y Z` → SavedData has faction, anchor matches |
| `pantheon_faction_assign` | `pantheon:pantheon_faction_assign` | create faction → assign a fake player → player attachment is set, `faction.members` contains UUID |
| `pantheon_faction_info` | `pantheon:pantheon_faction_info` | `faction info` prints detail (no crash on missing faction) |
| `pantheon_faction_list` | `pantheon:pantheon_faction_list` | `faction list` prints header + entries |
| `pantheon_temple_mirror` | `pantheon:pantheon_temple_mirror` | create faction + Temple BE → tick 20 times → BE `factionId`/`memberCount` match SavedData |
| `pantheon_persistence` | `pantheon:pantheon_persistence` | start season, create factions, save world, reload → SavedData reloads with same state |

### 11.3 Structure templates

Nine `.nbt` files in `src/main/resources/data/pantheon/structure/`, each a 1×1×1 empty structure (palette = air only, no blocks/entities). Generated by `scripts/gen_structure_nbt.py` (committed to repo) and re-runnable when new tests are added.

### 11.4 Running tests

```bash
./gradlew runGameTestServer --no-daemon --no-configuration-cache
```

Exits 0 on success, non-zero on required-test failure. All 9 tests are `required = true` (default).

### 11.5 Manual client test

- Place Temple block, run `/pantheon faction create ...`, right-click block (no UI yet, but no crash). F3 debug or a temporary `/pantheon temple info` command can print BE mirror state for verification.

## 12. Error handling

- **SavedData corruption / missing fields:** `load()` uses `getInt`/`getString`/`getList` with defaults (0 / "" / empty list) per field; never throws on partial NBT. Logs a warning at WARN level listing which fields defaulted.
- **No season / no faction on command:** `season info` prints "No active season"; `faction info <bad id>` prints "No such faction". Commands never throw.
- **Temple BE missing at anchor:** `faction create` errors with "No Temple block at X Y Z". `SeasonManager` mirror write silently skips if BE is null or wrong type (chunk unloaded).
- **Duplicate season start:** `season start` when `season.phase != ENDED && season != null` errors with "Season already running (id ...)".
- **Duplicate faction create:** `faction create pantheon:sunkeep ...` when `sunkeep` exists errors with "Faction already exists".
- **Player already in a faction:** `faction assign` overwrites silently (admin override); logs at INFO.

## 13. Translation keys

Added to `src/main/resources/assets/pantheon/lang/en_us.json`:

```json
{
  "block.pantheon.temple": "Temple",
  "pantheon.configuration.islandAnchorA": "Island A Anchor (Temple block position)",
  "pantheon.configuration.islandAnchorB": "Island B Anchor (Temple block position)",
  "pantheon.configuration.defaultSeasonDurationDays": "Default Season Duration (days, 0=open)",
  "pantheon.command.season.start.success": "Season started (id: %s, ends: %s)",
  "pantheon.command.season.start.already_running": "Season already running (id: %s)",
  "pantheon.command.season.end.success": "Season ended (id: %s)",
  "pantheon.command.season.info.none": "No active season",
  "pantheon.command.season.info.current": "Season %s — phase: %s, started: %s, ends: %s",
  "pantheon.command.faction.create.success": "Faction %s created at %s",
  "pantheon.command.faction.create.no_temple": "No Temple block at %s",
  "pantheon.command.faction.create.exists": "Faction %s already exists",
  "pantheon.command.faction.assign.success": "%s assigned to %s",
  "pantheon.command.faction.info.none": "No such faction: %s",
  "pantheon.command.faction.info.detail": "Faction %s — god: %s, mayor: %s, members: %d, skillpoints: %d",
  "pantheon.command.faction.list.header": "Factions (%d):",
  "pantheon.command.faction.list.entry": "- %s (%s): %d members, god: %s"
}
```

## 14. Acceptance criteria

1. `./gradlew build --no-daemon --no-configuration-cache` succeeds.
2. `./gradlew gameTestServer --no-daemon --no-configuration-cache` passes all Phase 0 GameTests.
3. A server op can run `/pantheon season start 7`, `/pantheon faction create pantheon:test_a TestA <x> <y> <z>`, `/pantheon faction assign <player> pantheon:test_a`, `/pantheon season info`, `/pantheon faction info pantheon:test_a` and see correct output.
4. After a server restart, `/pantheon season info` and `/pantheon faction info` return the same state (SavedData persisted).
5. A Temple block placed in the world and linked to a faction shows mirrored `factionId`/`memberCount` on its BE (verifiable via F3 or a debug command).
6. No client-only class is referenced outside `gg.wildblood.client`.

## 15. Open questions for user review

1. **Temple block creative-tab placement:** ~~should it appear in the Pantheon creative tab~~ → **decided: yes, added to `EXAMPLE_TAB`** (per §5.3). Confirm you're OK with this.
2. **Faction ids:** ~~hardcoded `pantheon:sunkeep`/`pantheon:voidspire`~~ → **decided: admin-chosen at creation** (per §4.2). The `/pantheon faction create <id> ...` command takes any valid `ResourceLocation` as the id.
3. **Open-ended season:** `endsAt = 0` means no auto-end. Confirm this is desired (admin ends manually via `/pantheon season end`).
4. **Temple block destruction:** ~~normal breakable block~~ → **decided: creative-only break** (per §5.1). Non-creative players cannot break the Temple; creative players can.