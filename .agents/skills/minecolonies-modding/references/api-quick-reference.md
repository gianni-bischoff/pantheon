# Minecolonies API Quick Reference

## Getting a Colony

```java
// By position (most common)
IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, blockPos);

// By ID
IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, world);

// All colonies
Collection<IColony> all = IColonyManager.getInstance().getAllColonies();
```

## Colony Access Patterns

```java
colony.getID();                          // int
colony.getCenter();                      // BlockPos (Town Hall)
colony.getName();                        // String
colony.getDimension();                   // ResourceKey<Level>
colony.getPermissions();                 // IPermissions
colony.getWorkManager();                 // IWorkManager
colony.getRequestManager();             // IRequestManager
colony.getResearchManager();            // IResearchManager
colony.getQuestManager();               // IQuestManager
colony.getCitizenManager();             // ICitizenManager
colony.getEventManager();               // IEventManager
colony.getAnimalManager();              // IAnimalManager
colony.getGraveManager();               // IGraveManager
colony.getVisitorManager();             // IVisitorManager
colony.getColonyConnectionManager();    // IColonyConnectionManager
colony.getTownHall();                    // BuildingTownHall (nullable)
colony.getBuildingManager();             // via colony
colony.isCoordInColony(world, pos);     // boolean
colony.getPackageManager();              // IColonyPackageManager
```

## Building Access

```java
// Get building at position
IBuilding building = colony.getBuildingManager().getBuilding(blockPos);

// Get all buildings
Collection<IBuilding> buildings = colony.getBuildingManager().getBuildings();

// Get buildings by type
IBuilding building = colony.getBuildingManager().getBuilding(blockPos, ModBuildings.baker.get());

// Building properties
building.getID();                        // BlockPos
building.getBuildingLevel();             // int (0-maxLevel)
building.getMaxBuildingLevel();          // int
building.getSchematicName();             // String (e.g., "baker")
building.getCustomName();                // String
building.getBuildingDisplayName();       // String
building.getStyle();                     // String
building.getColony();                    // IColony
building.getBuildingType();              // BuildingEntry

// Worker building (IBuildingWorker)
IBuildingWorker worker = (IBuildingWorker) building;
worker.getJobName();                     // String
worker.createJob(citizen);               // IJob<?>
worker.getHiringMode();                  // HiringMode
worker.assignCitizen(citizen);           // boolean
worker.getPrimarySkill();               // Skill
worker.getSecondarySkill();             // Skill
worker.canWorkDuringTheRain();          // boolean
worker.getMaxEquipmentLevel();          // int

// Modules
building.getModule(ModuleClass.class);   // Get specific module
building.getModules();                   // Collection<IBuildingModule>
```

## Citizen Access

```java
// Get all citizens
List<ICitizenData> citizens = colony.getCitizenManager().getCitizens();

// Get citizen by ID
ICitizenData citizen = colony.getCitizenManager().getCitizen(citizenId);

// Citizen properties
citizen.getName();                       // String
citizen.getId();                         // UUID
citizen.getHomeBuilding();               // IBuilding (nullable)
citizen.getWorkBuilding();              // IBuilding (nullable)
citizen.getJob();                         // IJob<?>
citizen.getJob(JobMiner.class);          // JobMiner (nullable if wrong type)
citizen.getSkills();                     // Map<Skill, Integer>
citizen.getSaturation();                // int
citizen.getEntity();                     // AbstractEntityCitizen
citizen.isChild();                        // boolean
citizen.getCitizenSkillHandler();        // ICitizenSkillHandler

// Assign/unassign
citizen.setJob(job);
citizen.setHomeBuilding(building);
```

## Request System

```java
IRequestManager rm = colony.getRequestManager();

// Create request
IRequest<Stack> request = rm.createRequest(building, new Stack(new ItemStack(Items.BREAD, 1)));

// Check state
RequestState state = request.getState(); // CREATED, PENDING, IN_PROGRESS, RESOLVED, OVERRULED, CANCELLED

// Update
rm.updateRequestState(request, RequestState.IN_PROGRESS);

// Cancel
rm.cancelRequest(request);
```

## Research

```java
IResearchManager rm = colony.getResearchManager();
ILocalResearchTree tree = rm.getResearchTree();
IResearchEffectManager effects = rm.getResearchEffects();

// Check if research is completed
boolean done = tree.hasResearch(new ResourceLocation("minecolonies", "basic_combat"));

// Get research state
ResearchState state = tree.getResearchState(researchId);

// Get all completed research
Set<ResourceLocation> completed = tree.getCompletedResearch();
```

## Events (Colony EventBus)

```java
// Subscribe to colony event
colony.getEventBus().subscribe(CitizenDiedModEvent.class, event -> {
    // Handle citizen death
});

// Global event bus
EventBus.getInstance().subscribe(ColonyManagerLoadedModEvent.class, event -> {
    // All colonies loaded
});
```

## Permissions

```java
IPermissions perms = colony.getPermissions();

// Check permission
boolean can = perms.hasPermission(player, Action.MANAGE_HUTS);

// Get rank
Rank rank = perms.getRank(player);

// Check if owner
boolean isOwner = perms.getRank(player).getId() == 0; // Owner rank ID is 0
```

## Networking

```java
// Send message to all subscribers
Network.getNetwork().sendToAll(message);

// Send to specific player
Network.getNetwork().sendToPlayer(message, player);

// Colony-specific: send to close subscribers
colony.getPackageManager().sendToCloseSubscribers(message);
```

## Skill Quick Reference

| Skill | Complimentary | Adverse |
|-------|-------------|---------|
| Athletics | Strength | Dexterity |
| Dexterity | Agility | Athletics |
| Strength | Athletics | Knowledge |
| Stamina | Agility | Knowledge |
| Agility | Dexterity | Stamina |
| Knowledge | Mana | Strength |
| Mana | Knowledge | Focus |
| Focus | Strength | Mana |

## Guard Types

| Guard Job | ID | Combat Style |
|----------|----|----|
| Ranger | `ranger` | Ranged (bow) |
| Knight | `knight` | Melee (sword) |
| Marksman | `marksman` | Ranged (crossbow) |
| Huscarl | `huscarl` | Melee (axe) |
| Cavalry | `cavalry` | Mounted melee |
| Druid | `druid` | Magic/potions |