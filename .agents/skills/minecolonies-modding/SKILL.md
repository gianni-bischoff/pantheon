---
author: Gianni Bischoff
description: Minecolonies modding for Minecraft NeoForge 1.21.1. Comprehensive guide for extending, modifying, and integrating with the Minecolonies colony management mod. Covers colony system, buildings, jobs, citizens, research, quests, request system, networking, commands, events, and AI.
license: MIT
metadata:
    github-path: skills/minecolonies-modding
    github-ref: refs/heads/main
    github-repo: https://github.com/gianni-bischoff/gianni-skills
    github-tree-sha: 2ce2c440b64f0ff0c03235b95e1fbb6312a90b08
    hermes:
        related_skills: []
        tags:
            - minecraft
            - minecolonies
            - neoforge
            - modding
            - java
            - kotlin
            - colony
            - npc
            - simulation
name: minecolonies-modding
platforms:
    - linux
    - macos
    - windows
version: 1.0.0
---
# Minecolonies Modding Skill

Comprehensive guide for modding Minecolonies on NeoForge 1.21.1. Covers all major subsystems: colony management, buildings, citizens, jobs, research, quests, request system, networking, commands, events, and AI. Based on analysis of the [ldtteam/minecolonies](https://github.com/ldtteam/minecolonies) source code (~2094 Java files).

## When to Use

- Creating addons or companion mods for Minecolonies
- Adding custom buildings, jobs, or citizen types
- Extending the research system with new branches/effects
- Creating custom quests or quest objectives
- Integrating external mods with the Minecolonies request/crafting system
- Adding custom commands for colony management
- Hooking into colony events (citizen added, building constructed, raids, etc.)
- Creating custom AI behaviors for citizens
- Modifying existing Minecolonies behavior through Forge events or Mixins

## Project Setup

### Build Dependencies

Minecolonies depends on several LDTteam mods. Add these to your `build.gradle`:

```groovy
dependencies {
    implementation "com.ldtteam:minecolonies:1.21.1-<version>"
    implementation "com.ldtteam:structurize:1.21.1-<version>"
    implementation "com.ldtteam:blockui:1.21.1-<version>"
    implementation "com.ldtteam:domumornamentum:1.21.1-<version>"
    implementation "com.ldtteam:multipiston:1.21.1-<version>"
}
```

### Minecraft/NeoForge Version

- **Minecraft**: 1.21.1 (NeoForge)
- **Java**: 17+ (project uses Java 17, NeoForge 1.21.1 may need 21)
- **Note**: The analyzed repo targets 1.20.1 with Forge 47.x, but the 1.21.1 port follows the same architecture with NeoForge mappings.

### Mod ID Convention

Minecolonies uses mod ID `minecolonies`. All ResourceLocations are namespaced `minecolonies:`.

## Architecture Overview

```
com.minecolonies/
├── api/                    # Public API interfaces (use these for addons)
│   ├── colony/             # IColony, ICitizenData, IBuilding, IJob, IColonyManager
│   ├── colony/buildings/   # IBuilding, IBuildingWorker, ModBuildings, BuildingEntry
│   ├── colony/jobs/        # IJob, ModJobs, JobEntry
│   ├── colony/managers/    # ICitizenManager, IEventManager, IAnimalManager, etc.
│   ├── colony/requestsystem/  # IRequestManager, IRequest, IRequestable, IRequestResolver
│   ├── colony/permissions/ # Rank, Action, IPermissions
│   ├── colony/workorders/  # IWorkOrder, IWorkManager, WorkOrderType
│   ├── research/           # IGlobalResearch, IResearchManager, IResearchEffect
│   ├── quests/             # IQuestManager, IQuestTemplate, IQuestInstance
│   ├── entity/             # AbstractEntityCitizen, Skill, ITickingStateAI
│   ├── entity/ai/          # State machine AI system, combat, pathfinding
│   ├── blocks/             # ModBlocks, AbstractBlockHut, AbstractColonyBlock
│   ├── items/              # ModItems, ModTags
│   ├── eventbus/           # Colony events, citizen events, building events
│   ├── network/            # IMessage (network packets)
│   └── crafting/           # IRecipeManager, IRecipeStorage, CraftingType
├── apiimp/                 # API implementation initializers
├── core/                   # Core implementation
│   ├── colony/             # Colony, ColonyView, managers, buildings, jobs
│   ├── colony/buildings/   # AbstractBuilding + 50+ worker buildings
│   ├── colony/buildings/modules/  # Building module system
│   ├── colony/jobs/        # 50+ job implementations
│   ├── colony/managers/    # 16 manager implementations
│   ├── colony/requestsystem/  # Request system implementation
│   ├── commands/           # /mc command tree
│   ├── entity/ai/workers/  # AI implementations per job type
│   ├── network/messages/   # Client/server network messages
│   ├── quests/             # Quest system implementation
│   ├── research/           # Research system implementation
│   └── generation/         # Data generators
```

## 1. Colony System

### Core Interface: `IColony`

The central hub for all colony data. Access via `IColonyManager` or from a `BlockPos`:

```java
// Get colony at position
IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, blockPos);

// Get colony by ID
IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, world);

// Key methods
colony.getCenter();           // BlockPos center (Town Hall)
colony.getName();             // Colony name
colony.getPermissions();      // IPermissions (ranks, actions)
colony.getWorkManager();      // IWorkManager (build orders)
colony.getRequestManager();   // IRequestManager (resource requests)
colony.getResearchManager();  // IResearchManager
colony.getQuestManager();     // IQuestManager (also via colony.getQuestManager())
colony.getCitizenManager();   // ICitizenManager
colony.getBuildingManager();  // via colony.getBuildingManager()
colony.getEventManager();     // IEventManager (raids, events)
colony.getAnimalManager();    // IAnimalManager
colony.getGraveManager();     // IGraveManager
colony.getVisitorManager();   // IVisitorManager
colony.getColonyConnectionManager(); // IColonyConnectionManager
colony.getTownHall();         // BuildingTownHall
colony.getID();               // int colony ID
colony.getDimension();        // ResourceKey<Level>
colony.isCoordInColony(world, pos); // Check if pos is within colony
```

### Colony Manager (`IColonyManager`)

```java
IColonyManager.getInstance().getAllColonies();    // Collection<IColony>
IColonyManager.getInstance().getColonyByWorld(id, world);
IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
```

### Creating a Colony (internal)

```java
IColony colony = IColonyManager.getInstance().createColony(world, pos, player, "ColonyName", "default");
```

## 2. Building System

### Key Interfaces

| Interface | Description |
|-----------|-------------|
| `IBuilding` | Base building interface — modules, request system, settings |
| `IBuildingWorker` | Worker building — has a job, can assign citizens |
| `IBuildingContainer` | Container/inventory access |
| `ICommonBuilding` | Shared building properties (level, style, schematic) |
| `ISchematicProvider` | Provides schematic info for structure placement |
| `IGuardBuilding` | Guard-specific building interface |
| `ITownHall` | Town Hall interface |
| `IWareHouse` | Warehouse interface |
| `IMysticalSite` | Mystical site interface |

### Building Registration (`ModBuildings`)

All buildings are registered as `BuildingEntry` via `RegistryObject<BuildingEntry>`. The full list:

| Building ID | Class | Job |
|-------------|-------|-----|
| `townhall` | BuildingTownHall | — |
| `builder` | BuildingBuilder | JobBuilder |
| `deliveryman` | BuildingDeliveryman | JobDeliveryman |
| `miner` | BuildingMiner | JobMiner |
| `lumberjack` | BuildingLumberjack | JobLumberjack |
| `farmer` | BuildingFarmer | JobFarmer |
| `fisherman` | BuildingFisherman | JobFisherman |
| `baker` | BuildingBaker | JobBaker |
| `cook` | BuildingCook | JobCook |
| `smeltery` | BuildingSmeltery | JobSmelter |
| `stonesmeltery` | BuildingStoneSmeltery | JobStoneSmeltery |
| `blacksmith` | BuildingBlacksmith | JobBlacksmith |
| `stonemason` | BuildingStonemason | JobStonemason |
| `sawmill` | BuildingSawmill | JobSawmill |
| `crusher` | BuildingCrusher | JobCrusher |
| `sifter` | BuildingSifter | JobSifter |
| `guardtower` | BuildingGuardTower | Guard jobs |
| `barracks` | BuildingBarracks | — |
| `barrackstower` | BuildingBarracksTower | Guard jobs |
| `archery` | BuildingArchery | JobArcherTraining |
| `combatacademy` | BuildingCombatAcademy | JobCombatTraining |
| `warehouse` | BuildingWareHouse | — |
| `home` (residence) | — | — |
| `library` | BuildingLibrary | JobStudent |
| `university` | BuildingUniversity | JobResearch |
| `school` | BuildingSchool | JobPupil/JobTeacher |
| `hospital` | BuildingHospital | JobHealer |
| `enchanter` | BuildingEnchanter | JobEnchanter |
| `florist` | BuildingFlorist | JobFlorist |
| `composter` | BuildingComposter | JobComposter |
| `chickenherder` | BuildingChickenHerder | JobChickenHerder |
| `cowboy` | BuildingCowboy | JobCowboy |
| `shepherd` | BuildingShepherd | JobShepherd |
| `swineherder` | BuildingSwineHerder | JobSwineHerder |
| `rabbithutch` | BuildingRabbitHutch | JobRabbitHerder |
| `beekeeper` | BuildingBeekeeper | JobBeekeeper |
| `stable` | BuildingStable | JobStablemaster |
| `plantation` | BuildingPlantation | JobPlanter |
| `fletcher` | BuildingFletcher | JobFletcher |
| `mechanic` | BuildingMechanic | JobMechanic |
| `glassblower` | BuildingGlassblower | JobGlassblower |
| `dyer` | BuildingDyer | JobDyer |
| `concretemixer` | BuildingConcreteMixer | JobConcreteMixer |
| `graveyard` | BuildingGraveyard | JobUndertaker |
| `netherworker` | BuildingNetherWorker | JobNetherWorker |
| `simplequarry` | — | JobQuarrier |
| `mediumquarry` | — | JobQuarrier |
| `alchemist` | BuildingAlchemist | JobAlchemist |
| `kitchen` | BuildingKitchen | JobChef |
| `gatehouse` | BuildingGateHouse | Guard jobs |
| `mysticalsite` | BuildingMysticalSite | — |
| `postbox` | PostBox | — |
| `stash` | Stash | — |
| `tavern` | — | — |

### Building Module System

Buildings are composed of **modules** (like a component system). Each module handles one aspect of building behavior. This is the **primary extension point** for buildings.

```java
// Module producer registration pattern
public static final BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> FARMER_WORK =
    new BuildingEntry.ModuleProducer<>("farmer_work",
        () -> new WorkerBuildingModule(ModJobs.farmer.get(), Skill.Stamina, Skill.Athletics, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
```

#### Key Module Types

| Module | Purpose |
|--------|---------|
| `WorkerBuildingModule` | Assigns a job + skill requirements to a building |
| `CraftingWorkerBuildingModule` | Crafting-specific worker module |
| `AbstractCraftingBuildingModule` | Base for crafting buildings (recipes, crafting types) |
| `SettingsModule` | GUI settings (toggles, int sliders, recipe modes) |
| `ItemListModule` | Filterable item lists (fuel, ore, saplings, etc.) |
| `EntityListModule` | Filterable entity lists (hostiles for guards) |
| `MinimumStockModule` | Minimum stock thresholds |
| `BedHandlingModule` | Bed assignment for citizens |
| `HomeBuildingModule` | Residence/home assignment |
| `LivingBuildingModule` | Living quarters behavior |
| `GuardBuildingModule` | Guard tower settings (patrol, follow, targets) |
| `MinerBuildingModule` | Miner-specific settings |
| `QuarryModule` | Quarry operation |
| `AnimalHerdingModule` | Animal herding behavior |
| `WarehouseModule` | Warehouse-specific behavior |
| `BuildingStatisticsModule` | Building stats tracking |
| `FurnaceUserModule` | Furnace interaction |
| `CourierAssignmentModule` | Courier delivery assignment |
| `DeliverymanAssignmentModule` | Deliveryman assignment |
| `TavernBuildingModule` | Tavern behavior |
| `ExpeditionLogModule` | Expedition logging |
| `RestaurantMenuModule` | Restaurant/tavern menu |
| `ChildrenBuildingModule` | Children/citizen spawning |

#### Creating a Custom Building

```java
public class BuildingCustomWorker extends AbstractBuilding
{
    private static final String SCHEMATIC_NAME = "customworker";
    private static final int MAX_LEVEL = 5;

    public BuildingCustomWorker(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public String getSchematicName() { return SCHEMATIC_NAME; }

    @Override
    public int getMaxBuildingLevel() { return MAX_LEVEL; }
}
```

#### Registering a Building

```java
// In your mod's deferred register
public static final DeferredRegister<BuildingEntry> BUILDINGS =
    DeferredRegister.create(ModBuildings.BUILDINGS, MOD_ID);

public static final RegistryObject<BuildingEntry> CUSTOM_WORKER =
    BUILDINGS.register("customworker", () ->
        new BuildingEntry.Builder()
            .setBuildingBlock(ModBlocks.CUSTOM_WORKER_BLOCK.get())
            .setBuildingProducer(BuildingCustomWorker::new)
            .setBuildingViewProducer(() -> BuildingCustomWorkerView::new)
            .setRegistryName(new ResourceLocation(MOD_ID, "customworker"))
            .addBuildingModuleProducer(BuildingModules.WORKER_DEFAULT)
            .addBuildingModuleProducer(BuildingModules.SETTINGS_CRAFTER_RECIPE)
            .createBuildingEntry());
```

## 3. Citizen & Job System

### Citizen Data (`ICitizenData`)

```java
citizenData.getName();              // Citizen name
citizenData.getId();                // UUID
citizenData.getHomeBuilding();       // IBuilding home
citizenData.getWorkBuilding();      // IBuilding workplace
citizenData.getJob();                // IJob<?> current job
citizenData.setJob(job);             // Assign job
citizenData.getJob(JobMiner.class);  // Typed job access (null if mismatch)
citizenData.getSkills();            // Map<Skill, Integer>
citizenData.getSaturation();        // Food saturation
citizenData.getEntity();            // AbstractEntityCitizen
citizenData.isChild();              // Is child citizen
citizenData.getCitizenSkillHandler(); // Skill handler
citizenData.getLastPosition();      // BlockPos last known position
```

### Skills (`com.minecolonies.api.entity.citizen.Skill`)

```java
public enum Skill {
    Athletics,     // complimentary: Strength, adverse: Dexterity
    Dexterity,    // complimentary: Agility, adverse: Athletics
    Strength,     // complimentary: Athletics, adverse: Knowledge
    Stamina,      // complimentary: Agility, adverse: Knowledge
    Agility,      // complimentary: Dexterity, adverse: Stamina
    Knowledge,    // complimentary: Mana, adverse: Strength
    Mana,         // complimentary: Knowledge, adverse: Focus
    Focus         // complimentary: Strength, adverse: Mana
}
```

### Jobs (`IJob` / `ModJobs`)

Each job is registered via `RegistryObject<JobEntry>`. Available jobs:

| Job | Job ID | Building | Skills |
|-----|--------|----------|--------|
| JobBuilder | `builder` | Builder | — |
| JobDeliveryman | `deliveryman` | Deliveryman | — |
| JobMiner | `miner` | Miner | — |
| JobLumberjack | `lumberjack` | Lumberjack | — |
| JobFarmer | `farmer` | Farmer | Stamina, Athletics |
| JobFisherman | `fisherman` | Fisherman | — |
| JobBaker | `baker` | Baker | — |
| JobCook | `cook` | Cook | — |
| JobSmelter | `smelter` | Smeltery | — |
| JobStoneSmeltery | `stonesmeltery` | StoneSmeltery | — |
| JobBlacksmith | `blacksmith` | Blacksmith | — |
| JobStonemason | `stonemason` | Stonemason | — |
| JobSawmill | `sawmill` | Sawmill | — |
| JobCrusher | `crusher` | Crusher | — |
| JobSifter | `sifter` | Sifter | — |
| JobFletcher | `fletcher` | Fletcher | — |
| JobMechanic | `mechanic` | Mechanic | — |
| JobGlassblower | `glassblower` | Glassblower | — |
| JobDyer | `dyer` | Dyer | — |
| JobConcreteMixer | `concretemixer` | ConcreteMixer | — |
| JobComposter | `composter` | Composter | Stamina, Athletics |
| JobChickenHerder | `chickenherder` | ChickenHerder | — |
| JobCowboy | `cowboy` | Cowboy | — |
| JobShepherd | `shepherd` | Shepherd | — |
| JobSwineHerder | `swineherder` | SwineHerder | — |
| JobRabbitHerder | `rabbitherder` | RabbitHutch | — |
| JobBeekeeper | `beekeeper` | Beekeeper | — |
| JobPlanter | `planter` | Plantation | — |
| JobStablemaster | `stablemaster` | Stable | — |
| JobEnchanter | `enchanter` | Enchanter | — |
| JobResearch | `researcher` | University | — |
| JobHealer | `healer` | Hospital | — |
| JobPupil | `pupil` | School | — |
| JobTeacher | `teacher` | School | — |
| JobStudent | `student` | Library | — |
| JobUndertaker | `undertaker` | Graveyard | — |
| JobNetherWorker | `netherworker` | NetherWorker | — |
| JobQuarrier | `quarrier` | Quarry | — |
| JobAlchemist | `alchemist` | Alchemist | — |
| JobChef | `chef` | Kitchen | — |
| JobResearch | `researcher` | University | — |
| Guard jobs: | | | |
| JobRanger | `ranger` | GuardTower | — |
| JobKnight | `knight` | GuardTower | — |
| JobMarksman | `marksman` | GuardTower | — |
| JobHuscarl | `huscarl` | GuardTower | — |
| JobCavalry | `cavalry` | GuardTower | — |
| JobDruid | `druid` | GuardTower | — |

### Creating a Custom Job

```java
public class JobCustomWorker extends AbstractJobCrafter
{
    public JobCustomWorker(final ICitizenData citizen) { super(citizen); }

    @Override
    public ResourceLocation getModel() { return new ResourceLocation(MOD_ID, "models/entity/custom_worker.json"); }

    @Override
    public JobEntry getJobRegistryEntry() { return ModJobs.CUSTOM_WORKER.get(); }

    @Override
    public void createAI() { super.createAI(); }
}
```

Register the job:

```java
public static final RegistryObject<JobEntry> CUSTOM_WORKER =
    JOBS.register("customworker", () ->
        new JobEntry.Builder()
            .setJobProducer(JobCustomWorker::new)
            .setJobViewProducer(() -> DefaultJobView::new)
            .setRegistryName(new ResourceLocation(MOD_ID, "customworker"))
            .createJobEntry());
```

## 4. AI System

### State Machine AI

Minecolonies uses a custom state machine system for citizen AI. Each job creates an AI that ticks via `ITickingStateAI`.

```java
public class EntityAIWorkCustomWorker extends AbstractEntityAICrafting<JobCustomWorker>
{
    public EntityAIWorkCustomWorker(final JobCustomWorker job)
    {
        super(job);
        // Register state transitions
        this.registerTarget(
            new AITarget(CitizenAIState.WORK, this::work, 1));
    }

    // State machine: IAIState -> work method -> returns next IAIState
    private IAIState work()
    {
        // Do work logic
        return CitizenAIState.WORK; // or IDLE, etc.
    }
}
```

### AI State Machine Classes

| Class | Purpose |
|-------|---------|
| `ITickRateStateMachine<IAIState>` | Core state machine |
| `TickingTransition` | State transition with condition + target |
| `AITarget` | Standard AI target (state, method, priority) |
| `AIEventTarget` | Event-driven target |
| `CitizenAIState` | Citizen AI states (IDLE, WORK, SLEEP, etc.) |
| `CombatAIStates` | Combat-specific states |

### AI Base Classes

| Class | Purpose |
|-------|---------|
| `AbstractEntityAIBasic` | Base AI for all workers |
| `AbstractEntityAICrafting` | Crafting worker AI |
| `AbstractEntityAIStructure` | Structure building AI |
| `AbstractEntityAIInteract` | Block interaction AI |
| `AbstractEntityAIFight` | Guard combat AI |
| `AbstractEntityAISkill` | Skill-based AI |

## 5. Request System

The request system is Minecolonies' internal logistics engine. Buildings request items, and the system routes requests to crafters, warehouses, or deliverymen.

### Key Interfaces

```java
IRequestManager requestManager = colony.getRequestManager();

// Create a request for an item
IRequest<? extends IRequestable> request = requestManager.createRequest(
    requester,      // The building making the request
    new Stack(new ItemStack(Items.BREAD, 1))  // What to request
);

// Request states: CREATED, PENDING, IN_PROGRESS, RESOLVED, OVERRULED, CANCELLED
request.getState();
request.getRequest();  // Get the IRequestable (Stack, Delivery, Tool, Food, etc.)

// Requestable types
new Stack(itemStack);       // Request a specific item stack
new Food();                 // Request any food
new Tool(ToolType.AXE, 2); // Request a tool
new Delivery(request, dest); // Request delivery of an item
new Pickup();               // Request item pickup
new Burnable();             // Request burnable fuel
```

### Request Resolvers

Resolvers fulfill requests. Types:

| Resolver | Purpose |
|----------|---------|
| `IPlayerRequestResolver` | Player fulfills manually |
| `IRetryingRequestResolver` | Retries failed requests |
| Crafting resolvers | Crafter buildings fulfill |
| Warehouse resolvers | Warehouse stock fulfills |

### Creating a Custom Request Resolver

```java
public class CustomRequestResolver implements IRequestResolver<Stack>
{
    @Override
    public boolean canResolveRequest(IRequestManager manager, IRequest<? extends IRequestable> request)
    {
        return request.getRequest() instanceof Stack;
    }

    @Override
    public void resolve(IRequestManager manager, IRequest<? extends Stack> request)
    {
        // Fulfill the request
    }
}
```

## 6. Research System

### Key Interfaces

```java
IResearchManager researchManager = colony.getResearchManager();
ILocalResearchTree researchTree = researchManager.getResearchTree();
IResearchEffectManager effects = researchManager.getResearchEffects();

// Get global research definitions
IGlobalResearchTree globalTree = IGlobalResearchTree.getInstance(); // server-side
IGlobalResearch research = globalTree.getResearch(new ResourceLocation("minecolonies", "basic_combat"));

research.getId();              // ResourceLocation ID
research.getCostList();        // List<IResearchCost> (items needed)
research.canResearch(building, localTree); // Check prerequisites
research.startResearch(localTree); // Begin research
```

### Research Cost Types

| Cost | Description |
|------|-------------|
| `ListItemCost` | Cost is specific item stacks |
| `SimpleItemCost` | Simple item + count |
| `TagItemCost` | Cost based on item tag |

### Research Requirements

| Requirement | Description |
|-------------|-------------|
| `BuildingResearchRequirement` | Requires building at a certain level |
| `BuildingAlternatesResearchRequirement` | Requires building OR alternate |
| `ResearchResearchRequirement` | Requires another research completed first |

### Research Effects (`ModResearchEffects`)

Research effects modify colony behavior. Common effect IDs in `ResearchConstants`:

```java
ResearchConstants.SHIELD_USAGE    // Guard shield usage
ResearchConstants.HUSCARL          // Unlock huscarl guard type
ResearchConstants.MARKSMAN         // Unlock marksman guard type
// etc.
```

### Adding Custom Research (Datapack/JSON)

Research is defined in datapack JSON files under `data/<namespace>/researches/`:

```json
{
  "research": "mymod:custom_research",
  "branch": "mymod:custom_branch",
  "requirements": [
    {"type": "building", "building": "minecolonies:guardtower", "level": 2}
  ],
  "costs": [
    {"type": "item", "item": "minecraft:diamond", "count": 4}
  ],
  "effects": [
    {"type": "mymod:custom_effect", "value": 1.0}
  ]
}
```

## 7. Quest System

### Key Interfaces

```java
IQuestManager questManager = colony.getQuestManager();
questManager.attemptAcceptQuest(questId, player);
questManager.completeQuest(questId);
questManager.deleteQuest(questId);
questManager.unlockQuest(questId);
questManager.isUnlocked(questId);
questManager.alterReputation(1.0);
```

### Quest Objectives (Core implementations)

| Objective | Description |
|-----------|-------------|
| `BreakBlockObjectiveTemplate` | Break specific blocks |
| `BuildBuildingObjectiveTemplate` | Build/upgrade a building |
| `DeliveryObjectiveTemplateTemplate` | Deliver items to NPC |
| `DialogueObjectiveTemplateTemplate` | Talk to NPC |
| `KillEntityObjectiveTemplate` | Kill entities |
| `PlaceBlockObjectiveTemplate` | Place blocks |
| `ResearchObjectiveTemplate` | Complete research |

### Quest Rewards (Core implementations)

| Reward | Description |
|--------|-------------|
| `HappinessRewardTemplate` | Increase citizen happiness |
| `ItemRewardTemplate` | Give items |
| `QuestReputationRewardTemplate` | Adjust quest reputation |
| `RaidAdjustmentRewardTemplate` | Modify raid behavior |
| `RelationshipRewardTemplate` | Adjust relationships |
| `ResearchCompleteRewardTemplate` | Complete research |
| `SkillRewardTemplate` | Improve citizen skills |
| `UnlockQuestRewardTemplate` | Unlock another quest |

### Quest Triggers

| Trigger | Description |
|---------|-------------|
| `CitizenQuestTriggerTemplate` | Triggered by citizen events |
| `QuestReputationTriggerTemplate` | Triggered by reputation threshold |
| `RandomQuestTriggerTemplate` | Random chance trigger |
| `StateQuestTriggerTemplate` | Colony state trigger |
| `UnlockQuestTriggerTemplate` | Triggered by quest unlock |
| `WorldDifficultyTriggerTemplate` | Based on world difficulty |

## 8. Colony Events (EventBus)

Minecolonies has its own event bus for colony-specific events. Subscribe via the colony's event bus.

### Available Events

| Event | When |
|-------|------|
| `ColonyCreatedModEvent` | Colony created |
| `ColonyDeletedModEvent` | Colony deleted |
| `ColonyNameChangedModEvent` | Colony renamed |
| `ColonyFlagChangedModEvent` | Colony flag changed |
| `ColonyTeamColorChangedModEvent` | Team color changed |
| `ColonyViewUpdatedModEvent` | Colony view updated (client) |
| `ColonyPlayerRankChangedModEvent` | Player rank changed |
| `BuildingAddedModEvent` | Building added to colony |
| `BuildingRemovedModEvent` | Building removed |
| `BuildingConstructionModEvent` | Building construction event |
| `CitizenAddedModEvent` | Citizen spawned/added |
| `CitizenRemovedModEvent` | Citizen removed |
| `CitizenDiedModEvent` | Citizen died |
| `CitizenJobChangedModEvent` | Citizen job changed |
| `PlayerEnteringModEvent` | Player enters colony |
| `PlayerLeavingModEvent` | Player leaves colony |
| `ColonyManagerLoadedModEvent` | Colony manager loaded |
| `ColonyManagerUnloadedModEvent` | Colony manager unloaded |
| `CustomRecipesReloadedEvent` | Custom recipes reloaded |

### Listening to Colony Events

```java
// Subscribe to colony event bus
colony.getEventBus().subscribe(CitizenAddedModEvent.class, event -> {
    ICitizenData citizen = event.getCitizen();
    // Handle new citizen
});

// Or use the global event bus
EventBus.getInstance().subscribe(ColonyManagerLoadedModEvent.class, event -> {
    // All colonies loaded
});
```

### Forge Event Integration

```java
@SubscribeEvent
public void onBlockBreak(BlockEvent.BreakEvent event)
{
    IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(
        (Level) event.getLevel(), event.getPos());
    if (colony != null)
    {
        // Block broken inside colony
    }
}
```

## 9. Networking

Minecolonies uses a client-server message system. Messages are in `com.minecolonies.core.network.messages`.

### Message Structure

```java
// Server -> Client messages (in messages/client/)
// ColonyViewMessage, ColonyViewCitizenViewMessage, ColonyViewBuildingViewMessage, etc.

// Client -> Server messages (in messages/server/)
// BuildRequestMessage, HireFireMessage, AssignFilterableItemMessage, etc.
// AbstractBuildingServerMessage, AbstractColonyServerMessage (base classes)
```

### Sending a Network Message

```java
// Send to all players tracking the colony
colony.getPackageManager().sendToCloseSubscribers(message);

// Send to a specific player
Network.getNetwork().sendToPlayer(message, player);
```

### Creating a Custom Message

```java
public class CustomActionMessage extends AbstractBuildingServerMessage
{
    private int actionId;

    public CustomActionMessage(BlockPos buildingId, int colonyId, int actionId)
    {
        super(colonyId, buildingId);
        this.actionId = actionId;
    }

    @Override
    public void onExecute(IColony colony, IBuilding building, Player player)
    {
        // Handle on server side
    }

    @Override
    public void serialize(FriendlyByteBuf buf)
    {
        super.serialize(buf);
        buf.writeInt(actionId);
    }

    @Override
    public void deserialize(FriendlyByteBuf buf)
    {
        super.deserialize(buf);
        actionId = buf.readInt();
    }
}
```

## 10. Commands

Minecolonies commands are under `/mc` with a tree structure.

### Command Categories

| Path | Commands |
|------|----------|
| `/mc colony` | info, list, teleport, delete, backup, export, claim, raid, raidsinfo, setabandoned, setdeletable, addofficer, changeowner, setrank, showclaim, reclaimchunks |
| `/mc citizen` | info, kill, list, modify, reload, spawnnew, teleport, track, walkto |
| `/mc rs` | reset, resetall (request system) |
| `/mc raid` | trigger raid |
| `/mc home` | teleport to colony |
| `/mc colony info` | Colony info |
| `/mc colony list` | List all colonies |

### Registering Custom Commands

```java
// Minecolonies uses Brigadier command framework
public static LiteralArgumentBuilder<CommandSourceStack> buildCommand()
{
    return Commands.literal("mycommand")
        .then(Commands.argument("colonyId", ColonyIdArgument.colonyId())
            .executes(context -> {
                int colonyId = context.getArgument("colonyId", Integer.class);
                // Execute command
                return 1;
            }));
}
```

## 11. Permissions System

### Ranks

| Rank | ID | Properties |
|------|----|-----------|
| Owner | — | Full access, colony creator |
| Officer | — | Colony manager, can manage |
| Friend | — | Limited access |
| Neutral | — | Default for visitors |
| Hostile | — | Can attack/be attacked |

### Actions (Permissions)

```java
Action.ACCESS_HUTS,  // Access hut GUIs
Action.PLACE_HUTS,    // Place building huts
Action.BREAK_HUTS,    // Break building huts
Action.EDIT_PERMISSIONS, // Edit rank permissions
Action.MANAGE_HUTS,   // Manage building settings
Action.RECEIVE_MESSAGES, // Receive colony messages
Action.PLACE_BLOCKS,  // Place blocks in colony
Action.BREAK_BLOCKS,  // Break blocks in colony
Action.OPEN_CONTAINER, // Open containers
Action.ATTACK_CITIZEN, // Attack citizens
Action.RALLY_GUARDS,   // Rally guards
Action.TELEPORT_TO_COLONY, // Teleport to colony
Action.EXPLODE,       // Explosions in colony
// etc.
```

### Checking Permissions

```java
boolean canAccess = colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS);
Rank rank = colony.getPermissions().getRank(player);
```

## 12. Colony Managers

Each colony has 16+ managers handling different aspects:

| Manager | Interface | Purpose |
|---------|-----------|---------|
| CitizenManager | ICitizenManager | Spawn, remove, track citizens |
| AnimalManager | IAnimalManager | Colony animals |
| EventManager | IEventManager | Raids, colony events |
| EventDescriptionManager | IEventDescriptionManager | Event descriptions |
| EventStructureManager | IEventStructureManager | Event structures |
| GraveManager | IGraveManager | Citizen graves |
| ColonyPackageManager | IColonyPackageManager | Colony chunk claiming |
| RegisteredStructureManager | IRegisteredStructureManager | Registered structures |
| ReproductionManager | IReproductionManager | Citizen reproduction |
| ResearchManager | IResearchManager | Research tree |
| StatisticsManager | IStatisticsManager | Colony statistics |
| TravellingManager | ITravellingManager | Citizen travel |
| VisitorManager | IVisitorManager | Visitors/tavern guests |
| ColonyConnectionManager | IColonyConnectionManager | Inter-colony connections |

## 13. Work Orders

Work orders drive building construction and upgrades.

```java
IWorkManager workManager = colony.getWorkManager();

// Work order types: BUILD, UPGRADE, REPAIR, REMOVE
WorkOrderType.BUILD, WorkOrderType.UPGRADE, WorkOrderType.REPAIR, WorkOrderType.REMOVE

// Create a build request
IBuilderWorkOrder workOrder = new WorkOrderBuilding(
    WorkOrderType.BUILD,
    building.getID(),
    building.getStructurePack(),
    building.getStructureName(),
    building.getPosition(),
    building.getBuildingLevel(),
    building.getStyle()
);
workManager.addWorkOrder(workOrder, false);
```

## 14. Crafting System

### Recipe Management

```java
IRecipeManager recipeManager = IColonyManager.getInstance().getRecipeManager();

// Custom recipes use tags for crafting type identification
// e.g., CRAFTING_BAKER tag identifies baker-specific recipes
```

### Crafting Types

Crafting types are identified by tags (e.g., `minecolonies:crafting_baker`). Each crafting building supports specific crafting types.

### Adding Custom Recipes

Recipes can be added via datapack JSON files or programmatically:

```java
// Custom recipe for a crafter building
IGenericRecipe recipe = IGenericRecipe.builder()
    .input(ItemStack(Items.WHEAT, 3))
    .output(ItemStack(Items.BREAD, 1))
    .type(CraftingType.CRAFTING)
    .build();
```

## 15. Building Styles & Blueprints

Minecolonies uses Structurize for blueprint management. Blueprints are stored in `src/main/resources/blueprints/minecolonies/<style>/`.

### Available Styles

acacia, ancientathens, birch, caledonia, cavern, colonial, darkoak, fortress, incan, jungle, lostcity, medievalbirch, medievaldarkoak, medievaloak, medievalspruce, nordic, original, pagoda, sandstone, shire, spacewars, truedwarven, warped, template

### Custom Styles

Add blueprints under `data/<your_mod>/blueprints/<style_name>/` with the same structure as vanilla styles.

## Common Modding Tasks

### Task: Add a Custom Worker Building

1. **Create the block** extending `AbstractBlockHut`
2. **Create the building** extending `AbstractBuilding` (or `AbstractBuildingStructureBuilder`)
3. **Create the job** extending `AbstractJobCrafter` (or `AbstractJob`)
4. **Create the AI** extending `AbstractEntityAICrafting` (or `AbstractEntityAIBasic`)
5. **Register block** in `DeferredRegister<Block>`
6. **Register building** in `DeferredRegister<BuildingEntry>` with `BuildingEntry.Builder`
7. **Register job** in `DeferredRegister<JobEntry>` with `JobEntry.Builder`
8. **Add building module producers** for worker assignment, settings, item lists
9. **Create blueprint** `.blueprint` file for each level
10. **Add localization** entries in lang JSON

### Task: Hook into Citizen Events

```java
@SubscribeEvent
public void onCitizenAdded(CitizenAddedModEvent event)
{
    ICitizenData citizen = event.getCitizen();
    IColony colony = event.getColony();
    // Your logic
}
```

### Task: Add Custom Research

1. Create a research JSON in `data/<your_mod>/researches/`
2. Define costs (items), requirements (buildings/other research), and effects
3. Add a custom `IResearchEffect` implementation if needed
4. Register the effect type in your mod initializer

### Task: Add Custom Quest

1. Create quest JSON in `data/<your_mod>/quests/`
2. Define objectives (dialogue, delivery, kill, build, etc.)
3. Define rewards (items, reputation, skills, research)
4. Define triggers (citizen events, reputation, random, etc.)
5. The quest system auto-loads from datapacks

### Task: Integrate with the Request System

```java
// Have your building request items
IBuilding building = ...; // your building
IRequest<? extends IRequestable> request = building.createRequest(
    new Stack(new ItemStack(Items.IRON_INGOT, 10)), true);

// Have your building fulfill requests (as a resolver)
building.registerResolver(new CustomRequestResolver());
```

## Pitfalls

1. **Client/Server separation**: Buildings have server-side `IBuilding` and client-side `IBuildingView`. Always use the correct side. Network messages must serialize properly.
2. **Module keys must be unique**: `BuildingEntry.ModuleProducer` keys are globally registered. Duplicate keys throw `RuntimeException`.
3. **NBT serialization**: All colony data must properly serialize/deserialize via `CompoundTag`. Missing fields cause data loss on world reload.
4. **Blueprint naming**: Blueprint files must match the `getSchematicName()` return value exactly.
5. **Job-Building linkage**: The `WorkerBuildingModule` links a job to a building. Make sure the `JobEntry` and `BuildingEntry` reference each other correctly.
6. **Research IDs**: Research IDs are `ResourceLocation`s. Always namespace them with your mod ID to avoid conflicts.
7. **Request system complexity**: The request system is async and stateful. Requests can be in various states and may be retried. Handle all states gracefully.
8. **Forge vs NeoForge**: The analyzed repo targets Forge 1.20.1. NeoForge 1.21.1 uses different package names (`net.neoforged` vs `net.minecraftforge`) and different event bus registration. The architecture is the same but imports change.
9. **Skill complementary/adverse**: Skills affect citizen productivity. Check `Skill.getComplimentary()` and `Skill.getAdverse()` when assigning skills to custom jobs.
10. **Permission checks**: Always check `colony.getPermissions().hasPermission(player, action)` before allowing modifications in a colony.

## Verification

- [ ] Building registers properly (check `ModBuildings` registry)
- [ ] Job registers properly (check `ModJobs` registry)
- [ ] Blueprint files exist for all levels (1 to maxLevel)
- [ ] Localization keys are defined in lang JSON
- [ ] NBT serialize/deserialize roundtrip works
- [ ] Client view (`IBuildingView`) syncs correctly
- [ ] Request system integration handles all `RequestState` values
- [ ] Commands registered with correct permission level
- [ ] Events subscribed on the correct bus (colony bus vs Forge bus)
- [ ] No duplicate module producer keys
