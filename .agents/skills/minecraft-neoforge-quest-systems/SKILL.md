---
author: Gianni Bischoff
description: 'Use when building quest systems in Minecraft NeoForge 1.21.1: runtime quest creation, quest creators, quest lines, solo/group/faction quests, and static/daily/weekly quest cycles. Covers architecture patterns, data models, networking, progression tracking, and extensibility.'
license: MIT
metadata:
    github-path: skills/minecraft-neoforge-quest-systems
    github-ref: refs/heads/main
    github-repo: https://github.com/gianni-bischoff/gianni-skills
    github-tree-sha: e111ca6a82a4cc3056e7c72142fddb6a7691a678
    hermes:
        related_skills:
            - minecolonies-modding
        tags:
            - minecraft
            - neoforge
            - quests
            - gaming
            - modding
            - quest-system
name: minecraft-neoforge-quest-systems
platforms:
    - linux
    - macos
    - windows
version: 1.1.0
---
# Minecraft NeoForge 1.21.1 Quest Systems

## Overview

This skill provides proven architecture patterns for building quest systems in Minecraft NeoForge 1.21.1. It synthesizes analysis of four production quest mods — **FTB Quests** (1,722 commits, 378 files, 9,155 codegraph nodes), **Boundless** (135 commits, 987 files, 26,272 nodes), **Questlog** (123 common files, config-driven), and **NoreSuiteMods** (82 files, quest completion mode bridge) — into actionable patterns covering all requested use cases.

### Analyzed Repositories

| Repository | Focus | Key Takeaway |
|---|---|---|
| [FTB-Quests](https://github.com/FTBTeam/FTB-Quests) | Full quest framework, team-based progression | Most mature type/reward registry; team data model with merge logic; JSON5 data format; 14 task types, 13 reward types, 60+ packets |
| [Boundless](https://github.com/Revilo-Dev/Boundless) | Data-driven quest packs, in-game editor | Clean category→subcategory→quest hierarchy; SavedData for progress; `lockAfterDependency` inverse mode; 9,586-line editor |
| [Questlog](https://github.com/infernalstudios/Questlog) | JSON config-driven, Oblivion-style quest log | Per-player QuestManager; prerequisite/objective/failureCondition model; global quests; platform abstraction via ServiceLoader |
| [NoreSuiteMods](https://github.com/NorevexG/NoreSuiteMods) | Quest completion mode bridge (solo/team/allied) | ThreadLocal actor context; reflection-based FTB integration; offline completion queue |

## When to Use

- Building a quest mod for NeoForge 1.21.1 from scratch
- Adding runtime quest creation capabilities to an existing mod
- Implementing quest lines, solo/group/faction quest mechanics
- Adding daily/weekly/recurring quest cycles
- Designing quest creator/editor tools (in-game or data-driven)
- Extending FTB Quests with custom completion modes or quest types

## Architecture Patterns

### Pattern 1: Layered Quest Hierarchy (FTB-Quests / Boundless Hybrid)

The most extensible structure combines FTB's hierarchical object model with Boundless's clean category system:

```
QuestFile (root, singleton)
  └── ChapterGroup / QuestPack (optional grouping)
       └── Chapter / Category
            └── SubCategory (Boundless pattern, optional)
                 └── Quest
                      ├── Tasks / Objectives (list)
                      ├── Rewards (list)
                      ├── Dependencies (list of quest IDs)
                      ├── Failure Conditions (Questlog pattern)
                      └── Prerequisites (Questlog pattern)
```

**Key classes from FTB-Quests:**
- `BaseQuestFile` — root container, holds `ChapterGroup` list, `TeamData` map, `RewardTable` list, `Long2ObjectOpenHashMap<QuestObjectBase> questObjectMap` (global ID→object registry)
- `Chapter` — contains `List<Quest>`, `List<QuestLink>`, `List<ChapterImage>`, has `progressionMode`, `defaultRepeatable`, visibility flags
- `Quest` — contains `List<Task>`, `List<Reward>`, `List<QuestObject> dependencies`, `DependencyRequirement`, `canRepeat`, `repeatCooldown`, `x/y` position, `shape`, `size`, ~20 behavioral flags
- `QuestObject` (abstract) — base for all progressible objects: `isVisible(TeamData)`, `isCompletedRaw(TeamData)`, `onStarted()`, `onCompleted()`
- `QuestLink` — visual-only link to a quest in another chapter; delegates visibility/progress
- `TeamData` — per-team progress tracking (see Pattern 4)

**Key classes from Boundless:**
- `QuestData` — static registry of `Map<String, Quest>`, `Map<String, Category>`, `Map<String, SubCategory>`
- `QuestProgressState extends SavedData` — per-player: `Map<String, Map<String, QuestProgress>>` (player→quest→status)
- `QuestObjectiveState extends SavedData` — per-player: item progress, effect progress, input progress
- `KillCounterState extends SavedData` — per-player entity kill counts

**Key classes from Questlog:**
- `QuestManager` — per-player manager holding `Map<ResourceLocation, Quest>` (LinkedHashMap for insertion order)
- `ServerPlayerManager` — singleton, maintains `Map<UUID, QuestManager>` for all online players
- `Quest` — holds `List<Objective> prerequisites`, `List<Objective> objectives`, `List<Objective> failureConditions`, `List<Reward> rewards`, `QuestDisplayData display`, `boolean hasSentTrigger`, `boolean hasSentCompletion` (one-shot event guards)

### Pattern 2: Quest Object Model

```java
public class Quest {
    // Identity
    ResourceLocation id;
    
    // Display
    Component title;
    Component description;
    ItemStack icon;
    
    // Structure
    String category;
    String subCategory;
    List<ResourceLocation> dependencies;
    DependencyRequirement dependencyRequirement; // ALL_COMPLETED, ANY_COMPLETED
    boolean lockAfterDependency; // Boundless: quest locks AFTER dep is claimed (inverse mode)
    
    // Behavior flags
    boolean optional;
    boolean repeatable;
    boolean autoComplete;
    boolean hiddenUntilDependenciesComplete;
    
    // Temporal (for daily/weekly)
    QuestCycle cycle; // STATIC, DAILY, WEEKLY
    int repeatCooldownSeconds;
    
    // Completion mode (NoreSuiteMods pattern)
    QuestCompletionMode completionMode; // SOLO, TEAM, ALLIED
    
    // Content
    List<Objective> objectives;
    List<Reward> rewards;
    List<Objective> failureConditions; // Questlog pattern — quest can FAIL
    List<Objective> prerequisites; // Questlog pattern — must complete before quest "triggers"
    
    // One-shot event guards (Questlog pattern)
    boolean hasSentTrigger;   // prevents duplicate trigger events
    boolean hasSentCompletion; // prevents duplicate completion events
    boolean global; // Questlog: server-wide shared quest
}
```

### Pattern 3: Task/Objective Type Registry

FTB-Quests uses a static registry pattern that is clean and extensible:

```java
// Registry (FTB-Quests TaskTypes pattern)
public interface TaskTypes {
    Map<Identifier, TaskType> TYPES = new LinkedHashMap<>();
    
    static TaskType register(Identifier name, TaskType.Provider provider, 
                             Supplier<Icon<?>> iconSupplier) {
        return TYPES.computeIfAbsent(name, id -> new TaskType(id, provider, iconSupplier));
    }
    
    TaskType ITEM = register(id("item"), ItemTask::new, () -> Icon.getIcon("minecraft:item/diamond"));
    TaskType KILL = register(id("kill"), KillTask::new, () -> Icon.getIcon("minecraft:item/diamond_sword"));
    // ... more types
}

// Usage: third-party mods can register at class-load time
TaskType CUSTOM = TaskTypes.register(myId("custom"), MyTask::new, () -> MyIcon);
```

**Questlog uses a similar registry pattern** (`QuestObjectiveRegistry.create(jsonObject)` dispatches by type field, with `EditorMetadata` for autocomplete in the in-game editor).

**FTB-Quests also provides `CustomTask` + `CustomTaskEvent`** for code-based quest logic without subclassing:
```java
// In your mod's init:
ObjectCompletedEvent.QUEST.register(event -> { ... }); // platform-agnostic event
CustomTaskEvent.EVENT.register(event -> {
    CustomTask task = event.getTask();
    task.setCheck((data, player) -> { /* custom check logic */ });
    task.setCheckTimer(20); // check every 20 ticks
    task.setMaxProgress(100);
});
```

**Recommended objective types to support:**

| Category | Types (from all 4 mods combined) |
|---|---|
| Items | collect/hold, submit, craft, equip, use, drop |
| Entities | kill, breed, tame, approach, death (any source) |
| Blocks | mine, place, interact |
| World | visit biome, visit dimension, visit structure, visit position |
| Stats | advancement, statistic, XP/level threshold |
| Effects | have potion effect |
| Logic | AND, OR, NOT (composable objectives — Questlog pattern) |
| Custom | text input, mod-specific integrations, LevelUP |
| Special | observation (look at entity/block), checkmark (manual click), gamestage |

### Pattern 4: Progress Tracking

Two viable approaches, depending on team vs. individual progression:

**Team-based (FTB-Quests):**
```java
public class TeamData {
    UUID teamId;
    // task → progress value
    Long2LongMap taskProgress;
    // reward → claim timestamp
    Object2LongMap<QuestKey> claimedRewards;
    // quest → start timestamp
    Long2LongMap started;
    // quest → completion timestamp  
    Long2LongMap completed;
    // repeatable quest → completion count
    Long2IntMap completionCount;
    // repeatable quest → cooldown end time
    Long2LongMap questRepeatableTime;
    // per-player data within team
    Object2ObjectMap<UUID, PerPlayerData> perPlayerData;
    
    // Tristate caching for performance (BOOL_UNKNOWN/-1, BOOL_FALSE/0, BOOL_TRUE/1)
    Long2ByteMap areDependenciesCompleteCache;
    Long2ByteMap areDependenciesVisibleCache;
    Object2ByteMap<QuestKey> unclaimedRewardsCache;
    Long2BooleanMap exclusionCache;
}
```

**Team merge logic (FTB-Quests) — critical for group quests:**
- **Player → Party:** ALL progress data merged into party (`newTeamData.mergeData(oldTeamData)`)
- **Party → Player:** Only claimed rewards copied (`newTeamData.mergeClaimedRewards(oldTeamData)`) — player keeps progress but can't re-claim

**Individual (Boundless / Questlog):**
```java
public class QuestProgressState extends SavedData {
    // player UUID → quest ID → progress
    Map<String, Map<String, QuestProgress>> byPlayer;
    
    public static final class QuestProgress {
        String status; // INCOMPLETE, COMPLETED, REDEEMED, REJECTED
        int claimCount;
        boolean scrollRedeemed;
        boolean scrollCreated;
    }
}
```

**Questlog global quests** — server-wide shared state:
```java
// Global quests stored in separate file: global_quests.questlog.dat
// When any player updates a global quest, push to ALL online players
ServerPlayerManager.onGlobalQuestUpdated(questId, nbtData);
// Reset affects ALL players simultaneously
ServerPlayerManager.resetGlobalQuest(questId);
```

**Choose team-based when:** You need shared progression across party members (FTB-Quests uses FTBTeams).
**Choose individual when:** Each player tracks quests independently (Boundless, Questlog).
**Use global quests when:** Server-wide community goals (Questlog pattern).

### Pattern 5: Data Persistence

Three approaches observed:

| Mod | Definition Format | Definition Location | Progress Storage | Reload Command |
|---|---|---|---|---|
| FTB-Quests | JSON5 files | `config/ftbquests/quests/` | Per-team JSON5 in `world/ftbquests/` | `/ftbquests reload` |
| Boundless | JSON (Gson) | `config/boundless/questpacks/` | `SavedData` NBT (overworld) | `/boundless reload` |
| Questlog | JSON (Gson) | `config/questlog/` | Per-player compressed NBT files | `/questlog reload` |

**FTB-Quests IDs:** 64-bit `long`, serialized as 16-char hex strings (e.g., `"0000000000000001"`). Root file always id=1. Generated via `file.newID()`.

**Boundless JSON structure** supports 4 backward-compatible formats:
- New: `"completion": { "complete": [ { "collect": "...", "count": N } ] }`
- Legacy: `"completion": { "collect": "...", "count": N }`
- Array: `"completion": [ { "item": "...", "count": N } ]`
- Single: `"completion": { "item": "...", "count": N }`

**Recommendation:** Use `SavedData` for progress (survives world corruption). Use JSON5/JSON for quest definitions (human-editable, datapack-friendly).

### Pattern 6: Network Synchronization

**FTB-Quests** has 60+ packet types (~27 C→S, ~35 S→C). Key categories:
- `SyncQuestsMessage` — full quest file sync to client
- `SyncTeamDataMessage` — team progress sync
- `UpdateTaskProgressMessage` — incremental progress update
- `ObjectCompletedMessage` / `ObjectStartedMessage` — completion notifications
- `ClaimRewardMessage` / `ClaimRewardResponseMessage` — reward claiming
- `CreateObjectMessage` / `EditObjectMessage` / `DeleteObjectMessage` — editor operations
- `SubmitTaskMessage` — task submission from client
- `ClearRepeatCooldownMessage` — reset repeatable quest cooldown

**Boundless** uses a versioned `PayloadRegistrar` with packet chunking:
```java
PayloadRegistrar r = event.registrar("boundless").versioned("2");
r.playToServer(Redeem.TYPE, Redeem.CODEC, handler);
r.playToClient(SyncStatus.TYPE, SyncStatus.CODEC, handler);
// Large quest pack uploads chunked at QUEST_CHUNK_BYTES = 60000
```

**Questlog** uses platform-abstracted packets (15 types): `QuestSyncPacket` (bulk), `QuestDataPacket` (incremental), `QuestTriggeredPacket`, `QuestCompletedPacket`, `QuestDefinitionPacket`, `QuestRemovePacket`, `QuestOpenPacket`, `QuestEditModePacket`, `QuestRewardCollectPacket`, `QuestReadPacket`, `QuestResetPacket`, `QuestEditSavePacket`, `QuestEditRemovePacket`, `ChapterEditSavePacket`, `ChapterEditRemovePacket`.

**Sync strategy (all mods):** Full sync on login, incremental sync on progress change. Questlog queues packets received before client is ready and processes them on login via `handleDeferredDefinitions()` / `handleDeferredSync()`.

**NeoForge 1.21.1 networking:**
```java
// Register payloads
@SubscribeEvent
public void register(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar r = event.registrar(MOD_ID).versioned("1");
    r.playToServer(QuestActionPayload.TYPE, QuestActionPayload.CODEC, this::handleAction);
    r.playToClient(SyncQuestsPayload.TYPE, SyncQuestsPayload.CODEC, this::handleSync);
}

// Send to player
PacketDistributor.sendToPlayer(serverPlayer, new SyncQuestsPayload(data));

// Broadcast to all
PacketDistributor.sendToAllPlayers(new SyncQuestsPayload(data));
```

### Pattern 7: Platform Abstraction (Questlog)

Questlog uses `ServiceLoader` + `IPlatformHelper` for clean multi-loader support:

```java
// Common: interface
public interface IPlatformHelper {
    void sendPacketToClient(ServerPlayer player, QuestPacket packet);
    Path getConfigDir();
    boolean isModLoaded(String modId);
}

// Forge: implementation loaded via ServiceLoader
public class ForgePlatformHelper implements IPlatformHelper { ... }
// Fabric: implementation
public class FabricPlatformHelper implements IPlatformHelper { ... }

// Access: Services.PLATFORM singleton
```

## Use Case Implementation Guide

### 1. Runtime Quest Creation

**Pattern:** Build quest objects programmatically and inject into the quest file.

FTB-Quests approach — `BaseQuestFile.create()` factory:
```java
ServerQuestFile file = ServerQuestFile.getInstance();

// Factory method handles all object types
QuestObjectBase obj = file.create(file.newID(), QuestObjectType.CHAPTER, 1L, extra);
// Reads data, calls onCreated(), refreshIDMap(), markDirty()

// Or manual creation:
Chapter chapter = new Chapter(file.newID(), file, file.getDefaultChapterGroup(), "dynamic");
file.getDefaultChapterGroup().addChapter(chapter);

long questId = file.newID();
Quest quest = new Quest(questId, chapter);
chapter.addQuest(quest);
quest.setRawTitle(Component.literal("Daily Kill Challenge"));
quest.setCanRepeat(Tristate.TRUE);

Task killTask = new KillTask(file.newID(), quest);
quest.addTask(killTask);
((KillTask) killTask).setEntity("minecraft:zombie");
((KillTask) killTask).setValue(10);

// Critical: save and sync
file.saveNow();
file.refreshAllTeamData(); // syncs to all clients
```

**For a standalone mod**, maintain a runtime registry:
```java
public class RuntimeQuestRegistry {
    private static final Map<ResourceLocation, Quest> RUNTIME_QUESTS = new ConcurrentHashMap<>();
    
    public static Quest createQuest(ResourceLocation id, QuestBuilder builder) {
        Quest quest = builder.build(id);
        RUNTIME_QUESTS.put(id, quest);
        syncToAllPlayers(quest);
        return quest;
    }
    
    public static void removeQuest(ResourceLocation id) {
        RUNTIME_QUESTS.remove(id);
        sendRemovePacket(id);
    }
}
```

**Key lessons:**
- Always assign IDs from a central ID generator (FTB uses `file.newID()` returning `long`)
- After creating/editing quests, call `saveNow()` and sync to all clients
- Use `ConcurrentHashMap` for runtime quest storage to avoid CME during tick-based checks
- Questlog's `reload()` preserves saved progress: serialize existing state before clearing, then deserialize after reload

### 2. Quest Creator / Editor

**Boundless in-game editor** (9,586 lines): creating packs, editing categories/sub-categories/quests, reordering, duplicating, deleting, exporting zip. Key classes: `QuestEditorScreen`, `QuestSettingsScreen`.

**Questlog in-game editor**: `QuestEditorScreen`, `ChapterEditorScreen`, edit mode toggle via `/questlog edit`. Editor uses `EditorMetadata` from registered objective/reward types for autocomplete.

**FTB-Quests editor** is the quest book itself in edit mode, with `CreateObjectMessage` / `EditObjectMessage` / `DeleteObjectMessage` / `MoveMovableMessage` packets. Client sends edit request → server validates → creates/modifies → broadcasts response to all clients.

**Implementation pattern for quest creator:**
```java
public class QuestEditorScreen extends Screen {
    // Editor sends CreateObjectMessage to server
    // Server validates, creates quest in ServerQuestFile
    // Server broadcasts SyncQuestsMessage to all clients
    // Client quest book refreshes
    
    // Key: server is authority. Never create quests client-side.
}
```

**Command-based quest creation (FTB-Quests pattern):**
```java
dispatcher.register(Commands.literal("myquests")
    .requires(src -> src.hasPermission(2))
    .then(Commands.literal("create")
        .then(Commands.argument("id", StringArgumentType.string())
            .then(Commands.argument("title", StringArgumentType.string())
                .executes(ctx -> createQuest(ctx.getSource(), 
                    StringArgumentType.getString(ctx, "id"),
                    StringArgumentType.getString(ctx, "title"))))))
    .then(Commands.literal("delete")
        .then(Commands.argument("id", StringArgumentType.string())
            .executes(ctx -> deleteQuest(ctx.getSource(), 
                StringArgumentType.getString(ctx, "id")))))
    .then(Commands.literal("reload")
        .executes(ctx -> reloadQuests(ctx.getSource()))));
```

### 3. Quest Lines / Chapters

**FTB-Quests model:** `ChapterGroup → Chapter → Quest` with dependencies between quests within and across chapters. Quests have `x, y` coordinates for visual positioning in the quest book.

**Boundless model:** `QuestPack → Category → SubCategory → Quest` with dependency lists per quest. Quest chains are implicit — they emerge from the dependency graph. No explicit "quest line" concept.

**Boundless `lockAfterDependency`** — inverse dependency pattern: quest becomes unavailable AFTER the dependency is claimed. Useful for "you can only do this before completing X" scenarios.

**Implementation:**
```java
public class QuestLine {
    ResourceLocation id;
    Component title;
    Component description;
    ItemStack icon;
    List<Quest> quests; // ordered by dependency graph
    ProgressionMode progressionMode; // DEFAULT, LINEAR, FLEXIBLE
    boolean defaultRepeatable;
    
    // Visibility gating
    boolean hideUntilDependenciesVisible;
    boolean hideUntilDependenciesComplete;
    
    // Quest ordering within line
    public List<Quest> getOrderedQuests() {
        // Topological sort by dependencies
    }
}
```

**FTB-Quests `ProgressionMode` enum:**
- `DEFAULT` — quests visible based on individual settings
- `LINEAR` — quests only visible after all dependencies complete
- `FLEXIBLE` — tasks can be progressed even if deps aren't complete; quest auto-completes when deps are met if tasks are already done

**Dependency types (FTB-Quests `DependencyRequirement`):**
- `ALL_COMPLETED` — all dependencies must be completed
- `ANY_COMPLETED` — at least one dependency must be completed
- `minRequiredDependencies` — configurable minimum count
- `maxCompletableDeps` — for mutually exclusive questlines (see `Excludable` interface)

**Dependency validation:** FTB-Quests has `DependencyDepthException` (max depth 1000) and `DependencyLoopException`. Always validate quest dependency graphs for cycles.

### 4. Solo Quests

**Default mode in all analyzed mods.** Each player has independent progress.

**Implementation (Boundless pattern):**
```java
public class SoloQuestProgress {
    // Per-player UUID keyed
    Map<UUID, Map<ResourceLocation, QuestStatus>> playerProgress;
    
    // SavedData in overworld
    public static SoloQuestProgress get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(SoloQuestProgress::new, SoloQuestProgress::load),
            "solo_quests"
        );
    }
}
```

### 5. Group / Team Quests

**FTB-Quests** uses FTBTeams integration: `TeamData` is per-team, not per-player. All team members share quest progress. Team events: `TeamPlayerLoggedInEvent`, `PlayerChangedTeamEvent`, `TeamCreatedEvent`.

**Team merge on switch (FTB-Quests):**
- **Player → Party:** `newTeamData.mergeData(oldTeamData)` — all progress merged
- **Party → Player:** `newTeamData.mergeClaimedRewards(oldTeamData)` — only claimed rewards copied (prevents re-claiming)

**Per-team vs per-player rewards:** Rewards have a `team` Tristate field. When `team == TRUE`, reward is claimed once for the entire team. When `FALSE`, each player claims individually.

**NoreSuiteMods** adds `QuestCompletionMode` on top of FTB-Quests:
```java
public enum QuestCompletionMode {
    SOLO(false),    // individual progress only
    TEAM(false),    // share with team members
    ALLIED(true);  // share with team + allies
    
    private final boolean includeAllies;
}
```

**NoreSuiteMods `QuestShareService`** — the key pattern for group quest progress sharing:
- Uses `ThreadLocal<Boolean> APPLYING_SHARED_PROGRESS` to prevent infinite recursion
- Uses `ThreadLocal<Deque<ServerPlayer>> ACTOR_CONTEXT` to track which player triggered the progress
- `shareProgressDelta()` — propagates progress delta to all related players
- `shareFromFtbCompletion()` — propagates completion to all related players
- Uses `NoreTeamsApi.getRelatedPlayers(server, playerUUID, includeAllies)` to find targets
- Checks `canAcceptProgress()` before sharing: verifies `canStartTasks()` and `checkTaskSequence()`
- `PendingQuestCompletions` — stores offline completions in Properties file, applies when player logs in

**Implementation pattern for group quests:**
```java
public class GroupQuestService {
    private static final ThreadLocal<Boolean> SHARING = ThreadLocal.withInitial(() -> false);
    
    public static void shareProgress(ServerPlayer actor, Quest quest, long progressDelta) {
        if (SHARING.get()) return; // prevent recursion
        SHARING.set(true);
        try {
            Set<UUID> targets = getTeamMembers(actor.server, actor.getUUID());
            for (UUID targetId : targets) {
                if (targetId.equals(actor.getUUID())) continue;
                ServerPlayer target = actor.server.getPlayerList().getPlayer(targetId);
                if (target != null && !isCompleted(target, quest)) {
                    addProgress(target, quest, progressDelta);
                }
            }
        } finally {
            SHARING.set(false);
        }
    }
    
    // Offline completion queue (NoreSuiteMods pattern)
    public static void queueOfflineCompletion(MinecraftServer server, 
            UUID playerId, String questId) {
        // Store in Properties file, apply on next login
    }
}
```

### 6. Faction Quests

No analyzed mod implements true faction quests, but the patterns extend naturally:

**Faction quest = group quest + faction membership + faction reputation gating.**

```java
public class FactionQuest extends Quest {
    ResourceLocation factionId;
    int minReputation;
    FactionQuestScope scope; // FACTION_WIDE, FACTION_LEADER_ONLY, FACTION_MEMBER
    
    // Quest is only available to faction members
    public boolean canStart(ServerPlayer player) {
        return FactionApi.isMember(player, factionId) 
            && FactionApi.getReputation(player, factionId) >= minReputation;
    }
    
    // FACTION_WIDE: completing the quest advances progress for all faction members
    // FACTION_LEADER_ONLY: only the faction leader can claim the reward
}
```

**Integration points:**
- Use FTBTeams as the faction system (teams = factions)
- NoreSuiteMods' `QuestCompletionMode.ALLIED` pattern for cross-faction sharing
- NoreStages' stage gating for reputation-like progression blocking
- `FtbQuestBridge.onQuestCompleted()` pattern: quest completion → grant stage → unlock content

### 7. Static Quest Lines

Standard quest line that exists permanently. All analyzed mods support this by default.

**Key:** Quest definitions are loaded at server startup from JSON/JSON5 files. Progress persists in SavedData or per-team JSON files. No temporal logic needed.

### 8. Daily Quests

**No analyzed mod has built-in daily/weekly quests.** This is a gap. Implementation pattern:

```java
public class DailyQuestManager {
    private static final long ONE_DAY_TICKS = 24000L;
    
    // Scheduled task: regenerate daily quests at dawn or fixed interval
    public static void onServerTick(ServerTickEvent event) {
        MinecraftServer server = event.getServer();
        long day = server.overworld().getDayTime() / ONE_DAY_TICKS;
        
        if (day != lastQuestDay) {
            lastQuestDay = day;
            regenerateDailyQuests(server);
        }
    }
    
    // Or use real-time scheduling
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        long currentDay = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        Long lastLogin = playerDailyMap.get(player.getUUID());
        
        if (lastLogin == null || lastLogin < currentDay) {
            generateDailyQuestsForPlayer(player);
            playerDailyMap.put(player.getUUID(), currentDay);
        }
    }
    
    static void generateDailyQuestsForPlayer(ServerPlayer player) {
        // 1. Select from quest pool (random or weighted)
        // 2. Create runtime quest instances with daily cycle flag
        // 3. Set expiry timestamp (end of day)
        // 4. Sync to player
        // 5. Previous day's quests auto-complete or expire
    }
}
```

**FTB-Quests repeatable quests** are the closest analog:
- `Quest.canRepeat = Tristate.TRUE`
- `Quest.repeatCooldown` in seconds
- `TeamData.questRepeatableTime` tracks cooldown end
- `TeamData.completionCount` tracks how many times completed
- `ClearRepeatCooldownMessage` packet to reset cooldown

### 9. Weekly Quests

Same pattern as daily, with weekly reset interval. Use `Calendar` or day-of-week logic:

```java
public static long nextWeeklyReset() {
    Calendar c = Calendar.getInstance();
    c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.add(Calendar.WEEK_OF_YEAR, 1);
    return c.getTimeInMillis();
}
```

## NeoForge 1.21.1 Technical Reference

### Mod Entry Point
```java
@Mod(MOD_ID)
public class MyQuestMod {
    public static final String MOD_ID = "myquests";
    
    public MyQuestMod(IEventBus modBus, ModContainer container) {
        // Register items (quest book, etc.)
        ModItems.register(modBus);
        
        // Register network payloads
        modBus.addListener(MyNetwork::register);
        
        // Register config
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        // Register NeoForge event handlers
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(MyQuestTicker::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(MyQuestEvents::onPlayerLogin);
    }
    
    @SubscribeEvent
    public void onServerStart(ServerStartingEvent event) {
        QuestDataManager.loadQuests(event.getServer());
    }
    
    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent event) {
        QuestCommands.register(event.getDispatcher());
    }
}
```

### Key NeoForge Events for Quest Systems

| Event | Usage |
|---|---|
| `ServerStartingEvent` | Load quest data |
| `ServerStoppingEvent` | Save quest data |
| `PlayerLoggedInEvent` | Sync quests to player, check daily resets |
| `PlayerLoggedOutEvent` | Save player quest progress (Boundless forces `SavedData` save) |
| `PlayerTickEvent.Post` | Check quest objectives (Boundless: 20-tick interval) |
| `LivingDeathEvent` | Kill task progression (FTB: checks all `KillTask` instances) |
| `PlayerEvent.ItemCraftedEvent` | Craft task progression |
| `PlayerEvent.ItemPickupEvent` | Collect task progression |
| `EntityJoinLevelEvent` | Dimension task detection (FTB) |
| `PlayerContainerEvent.Open` | Inventory listener attachment (FTB) |
| `RegisterCommandsEvent` | Register quest commands |
| `OnDatapackSyncEvent` | Sync quest datapacks to clients |
| `ServerTickEvent.Post` | Custom task sync, deferred inventory detection (FTB) |
| `BuildCreativeModeTabContentsEvent` | Add quest book item to creative tab |

### SavedData Pattern (Boundless / Questlog)
```java
public class QuestProgressData extends SavedData {
    private final Map<UUID, Map<ResourceLocation, QuestStatus>> data = new HashMap<>();
    
    public static QuestProgressData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(QuestProgressData::new, QuestProgressData::load),
            "myquests_progress"
        );
    }
    
    public static QuestProgressData load(CompoundTag tag, HolderLookup.Provider provider) {
        // Deserialize from NBT
    }
    
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        // Serialize to NBT
        return tag;
    }
}
```

### Custom Packet Payload (NeoForge 1.21.1)
```java
public record QuestActionPayload(String questId, String action) implements CustomPacketPayload {
    public static final Type<QuestActionPayload> TYPE = 
        new Type<>(ResourceLocation.fromNamespaceAndPath("myquests", "action"));
    
    public static final StreamCodec<FriendlyByteBuf, QuestActionPayload> CODEC = 
        StreamCodec.composite(
            ByteBufCodecs.UTF8, QuestActionPayload::questId,
            ByteBufCodecs.UTF8, QuestActionPayload::action,
            QuestActionPayload::new
        );
    
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
```

## Best Practices (Synthesized)

1. **Server is authority.** All quest progress, creation, and edits happen server-side. Client sends action requests via packets; server validates and broadcasts updates.

2. **Use SavedData for progress, JSON for definitions.** SavedData survives world corruption; JSON is human-editable and datapack-friendly.

3. **Throttle tick-based quest checks.** Boundless checks every 20 ticks (1 second) per player, not every tick. Use `player.tickCount % CHECK_INTERVAL == 0`.

4. **ThreadLocal for shared progress.** When propagating progress to group members (NoreSuiteMods pattern), use `ThreadLocal<Boolean>` to prevent recursive sharing loops.

5. **Offline completion queue.** Store completions for offline players in a Properties file or NBT. Apply when they log in (NoreSuiteMods `PendingQuestCompletions`).

6. **ID generation.** Use a central ID generator. FTB uses `long` IDs via `file.newID()`. Questlog uses `ResourceLocation`. Choose one and be consistent.

7. **Extensible type registry.** Use the FTB-Quests `TaskTypes.register()` pattern. Third-party mods can add quest/task/reward types at class-load time without modifying your code.

8. **Quest reload preserves progress.** Before reloading quest definitions, serialize all progress. After reload, deserialize matching quests. Drop progress for deleted quests. (Questlog `reload()` pattern.)

9. **Reflection for cross-mod integration.** NoreSuiteMods uses `Reflector.call()` and cached `Method` lookups for FTB-Quests integration. Guard with `ModList.get().isLoaded()` checks. Cache reflection results to avoid repeated `Class.forName()` overhead.

10. **Dependency validation.** FTB-Quests has `DependencyDepthException` and `DependencyLoopException`. Always validate quest dependency graphs for cycles.

11. **One-shot event guards.** Questlog uses `hasSentTrigger` / `hasSentCompletion` boolean flags to prevent duplicate quest events from firing. Always guard event emission.

12. **Tristate caching for dependency checks.** FTB-Quests caches dependency completion as `BOOL_UNKNOWN`/`BOOL_FALSE`/`BOOL_TRUE` bytes. Invalidate cache on progress changes. Critical for performance with large quest trees.

13. **Packet chunking for large data.** Boundless uses `QUEST_CHUNK_BYTES = 60000` for quest pack uploads. Don't send everything in one packet.

14. **Platform abstraction for multi-loader.** Questlog uses `ServiceLoader` + `IPlatformHelper` to support Forge and Fabric from a single common codebase. Isolate all platform-specific code behind an interface.

## Gaps Identified

| Gap | Affects | Recommendation |
|---|---|---|
| No native daily/weekly quest support | All mods | Build `QuestCycle` enum + scheduled regeneration on top of existing systems |
| No faction quest system | All mods | Extend team/group patterns with faction membership + reputation gating |
| No quest versioning/migration | FTB-Quests, Boundless | Questlog has `QuestlogMigrator` — adopt similar migration pattern for schema changes |
| No quest difficulty scaling | All mods | Add difficulty multiplier to quest templates for dynamic quest generation |
| No quest reward balancing | All mods | Add reward weight system for auto-generated daily/weekly rewards |
| No cross-server quest sync | All mods | Quest definitions are per-world; consider a global quest pack sync system |
| No quest analytics | All mods | Track completion rates, average time-to-complete, abandonment points |
| Questlog targets 1.20.1 only | Questlog | Port to 1.21.1 or use as architecture reference only |
| Boundless has no team/group quests | Boundless | Add NoreSuiteMods-style completion mode layer |
| No quest sharing UI | All mods | NoreSuiteMods has `/norequest share` but no in-game UI for it |
| No failure condition support | FTB-Quests, Boundless | Questlog supports `failureConditions` — quests can fail, not just complete |

## Common Pitfalls

1. **Client-side quest creation.** Never create quests on the client. Always send a packet to the server, create there, sync back. Client-created quests will desync on reload.

2. **Forgetting to save after runtime quest creation.** Call `file.saveNow()` (FTB) or `state.setDirty()` (SavedData) after any quest data modification.

3. **Progress sharing infinite recursion.** When sharing progress to group members, the share call itself triggers progress events. Use `ThreadLocal<Boolean>` guard (NoreSuiteMods `APPLYING_SHARED_PROGRESS`).

4. **Tick-based quest checks on every tick.** Checking all quest objectives every tick is extremely expensive. Throttle to every 20 ticks minimum (Boundless pattern).

5. **Not handling offline players.** When a group quest completes and a member is offline, queue the completion (NoreSuiteMods `PendingQuestCompletions`). Otherwise progress is lost.

6. **Hardcoded task types.** Using if/else chains for task types instead of a registry. Always use the registry pattern for extensibility.

7. **Not validating quest dependency cycles.** Circular dependencies will cause `StackOverflowError`. FTB-Quests has explicit `DependencyLoopException` detection.

8. **Large packet payloads.** FTB-Quests chunks quest sync into multiple `SyncQuestsMessage` packets. Boundless uses `QUEST_CHUNK_BYTES = 60000` for quest pack uploads. Don't send everything in one packet.

9. **Duplicate event firing.** Without one-shot guards (Questlog `hasSentTrigger`/`hasSentCompletion`), quest completion events can fire multiple times if objectives are re-checked.

10. **Not caching dependency checks.** FTB-Quests uses Tristate byte caches (`BOOL_UNKNOWN`/`BOOL_FALSE`/`BOOL_TRUE`) for dependency completion. Without caching, every visibility check walks the entire dependency tree.

## Verification Checklist

- [ ] Quest definitions loaded from JSON/JSON5 in `config/<modid>/`
- [ ] Progress persisted via `SavedData` in overworld
- [ ] Server is authority for all quest state changes
- [ ] Network packets registered via `PayloadRegistrar` with version
- [ ] Task/objective types use registry pattern (extensible by third-party mods)
- [ ] Tick-based checks throttled (≥20 tick interval)
- [ ] Progress sharing uses ThreadLocal recursion guard
- [ ] Offline completions queued and applied on login
- [ ] Quest reload preserves existing progress (serialize before, deserialize after)
- [ ] Dependency graph validated for cycles (`DependencyLoopException`)
- [ ] One-shot event guards prevent duplicate trigger/completion events
- [ ] Dependency check caching (Tristate or equivalent) for large quest trees
- [ ] Commands registered with permission level 2 (op only)
- [ ] Client quest UI reads from synced data, not local files
- [ ] Large packet payloads chunked (≤60KB per packet)
- [ ] Cross-mod integration uses cached reflection with `ModList.get().isLoaded()` guard
