# Minecolonies Package & Class Reference

## Package Structure

```
com.minecolonies.api/
├── colony/
│   ├── IColony.java                          # Core colony interface
│   ├── IColonyManager.java                   # Colony manager singleton
│   ├── IColonyView.java                      # Client-side colony view
│   ├── ICitizenData.java                     # Citizen data interface
│   ├── ICitizenDataView.java                 # Client citizen view
│   ├── ICivilianData.java                    # Civilian base interface
│   ├── IVisitorData.java                     # Visitor data
│   ├── IVisitorViewData.java                 # Visitor client view
│   ├── IAnimalData.java                      # Animal data
│   ├── IGraveData.java                        # Grave data
│   ├── IColonyRelated.java                   # Colony-related marker
│   ├── IChunkmanagerCapability.java          # Chunk capability
│   ├── IColonyTagCapability.java             # Colony tag capability
│   ├── ColonyProgressType.java               # Progress type enum
│   ├── ColonyState.java                      # Colony state enum
│   ├── CompactColonyReference.java           # Lightweight colony ref
│   ├── GraveData.java                        # Grave data impl
│   ├── CitizenNameFile.java                  # Name generator
│   │
│   ├── buildings/
│   │   ├── IBuilding.java                    # Base building interface
│   │   ├── IBuildingWorker.java              # Worker building interface
│   │   ├── IBuildingContainer.java           # Container interface
│   │   ├── ICommonBuilding.java              # Common building props
│   │   ├── IGuardBuilding.java               # Guard building interface
│   │   ├── ISchematicProvider.java           # Schematic provider
│   │   ├── IRSComponent.java                 # Research system component
│   │   ├── IMysticalSite.java                # Mystical site interface
│   │   ├── ModBuildings.java                 # Building registry (50+ entries)
│   │   ├── HiringMode.java                   # Hiring mode enum
│   │   ├── registry/
│   │   │   ├── BuildingEntry.java             # Building registry entry (Builder pattern)
│   │   │   ├── IBuildingRegistry.java        # Registry interface
│   │   │   └── IBuildingDataManager.java     # Building data manager
│   │   ├── modules/
│   │   │   ├── IBuildingModule.java          # Base module interface
│   │   │   ├── AbstractBuildingModule.java    # Base module impl
│   │   │   ├── IBuildingModuleView.java      # Client module view
│   │   │   ├── AbstractBuildingModuleView.java
│   │   │   ├── IAssignsCitizen.java          # Citizen assignment module
│   │   │   ├── IAssignsJob.java              # Job assignment module
│   │   │   ├── ICraftingBuildingModule.java  # Crafting module
│   │   │   ├── ICreatesResolversModule.java   # Request resolver module
│   │   │   ├── IDefinesCoreBuildingStatsModule.java
│   │   │   ├── IEntityListModule.java        # Entity list module
│   │   │   ├── IHasRequiredItemsModule.java   # Required items module
│   │   │   ├── IItemListModule.java          # Item list module
│   │   │   ├── IMinimumStockModule.java       # Min stock module
│   │   │   ├── IPersistentModule.java        # Persistent module
│   │   │   ├── ISettingsModule.java          # Settings module
│   │   │   ├── ITickingModule.java           # Ticking module
│   │   │   ├── settings/                      # Setting types & factories
│   │   │   └── stat/                          # Building stats
│   │   ├── views/
│   │   │   ├── IBuildingView.java            # Client building view
│   │   │   └── IModuleContainerView.java     # Module container view
│   │   └── workerbuildings/
│   │       ├── ITownHall.java                # Town Hall interface
│   │       ├── ITownHallView.java            # Town Hall view
│   │       ├── IWareHouse.java               # Warehouse interface
│   │       └── IBuildingDeliveryman.java    # Deliveryman building
│   │
│   ├── jobs/
│   │   ├── IJob.java                          # Base job interface
│   │   ├── IJobView.java                      # Client job view
│   │   ├── IJobWithColonyFlag.java
│   │   ├── IJobWithExternalWorkStations.java
│   │   ├── ModJobs.java                       # Job registry (50+ entries)
│   │   └── registry/
│   │       ├── JobEntry.java                  # Job registry entry (Builder)
│   │       ├── IJobRegistry.java
│   │       └── IJobDataManager.java
│   │
│   ├── managers/
│   │   └── interfaces/                        # All manager interfaces
│   │       ├── ICitizenManager.java
│   │       ├── IAnimalManager.java
│   │       ├── IEventManager.java
│   │       ├── IGraveManager.java
│   │       ├── IVisitorManager.java
│   │       ├── IColonyPackageManager.java
│   │       ├── IRegisteredStructureManager.java
│   │       ├── IReproductionManager.java
│   │       ├── IStatisticsManager.java
│   │       ├── ITravellingManager.java
│   │       ├── IEntityManager.java
│   │       ├── IRaiderManager.java
│   │       └── IEventDescriptionManager.java
│   │
│   ├── permissions/
│   │   ├── IPermissions.java                  # Permission interface
│   │   ├── Action.java                        # Action enum (30 actions)
│   │   ├── Rank.java                          # Rank class
│   │   ├── ColonyPlayer.java                  # Player in colony
│   │   ├── PermissionEvent.java
│   │   └── Explosions.java
│   │
│   ├── requestsystem/                        # Request/logistics system
│   │   ├── manager/IRequestManager.java
│   │   ├── request/IRequest.java
│   │   ├── request/RequestState.java
│   │   ├── requestable/                       # Requestable types
│   │   │   ├── Stack.java                      # Item stack request
│   │   │   ├── Food.java                       # Food request
│   │   │   ├── Tool.java                       # Tool request
│   │   │   ├── Burnable.java                   # Fuel request
│   │   │   ├── Delivery.java                   # Delivery request
│   │   │   ├── Pickup.java                     # Pickup request
│   │   │   ├── MinimumStack.java
│   │   │   ├── StackList.java
│   │   │   ├── crafting/
│   │   │   │   ├── AbstractCrafting.java
│   │   │   │   ├── PrivateCrafting.java
│   │   │   │   └── PublicCrafting.java
│   │   │   └── deliveryman/
│   │   │       ├── AbstractDeliverymanRequestable.java
│   │   │       ├── Delivery.java
│   │   │       └── Pickup.java
│   │   ├── resolver/
│   │   │   ├── IRequestResolver.java
│   │   │   ├── IQueuedRequestResolver.java
│   │   │   ├── IRequestResolverProvider.java
│   │   │   ├── player/IPlayerRequestResolver.java
│   │   │   └── retrying/IRetryingRequestResolver.java
│   │   ├── requester/IRequester.java
│   │   ├── location/ILocation.java
│   │   ├── token/IToken.java
│   │   ├── factory/IFactoryController.java
│   │   ├── data/                              # Data stores
│   │   └── StandardFactoryController.java
│   │
│   ├── workorders/
│   │   ├── IWorkOrder.java
│   │   ├── IWorkOrderView.java
│   │   ├── IWorkManager.java
│   │   ├── IBuilderWorkOrder.java
│   │   ├── IServerWorkOrder.java
│   │   └── WorkOrderType.java                 # BUILD, UPGRADE, REPAIR, REMOVE
│   │
│   ├── connections/                           # Inter-colony connections
│   │   ├── IColonyConnectionManager.java
│   │   ├── ColonyConnection.java
│   │   ├── DiplomacyStatus.java
│   │   └── ConnectionEvent.java
│   │
│   ├── colonyEvents/                         # Colony events
│   │   ├── IColonyEvent.java
│   │   ├── IColonyRaidEvent.java
│   │   ├── IColonyCampFireRaidEvent.java
│   │   ├── IColonyEntitySpawnEvent.java
│   │   ├── IColonySpawnEvent.java
│   │   ├── IColonyStructureSpawnEvent.java
│   │   ├── EventStatus.java
│   │   └── descriptions/                      # Event descriptions
│   │
│   ├── interactionhandling/                   # Citizen interactions (chat)
│   │   ├── IInteractionResponseHandler.java
│   │   ├── AbstractInteractionResponseHandler.java
│   │   ├── ChatPriority.java
│   │   ├── InteractionValidatorRegistry.java
│   │   └── ModInteractionResponseHandlers.java
│   │
│   ├── modules/
│   │   ├── IModuleContainer.java
│   │   └── IBuildingModuleContainer.java
│   │
│   └── guardtype/                             # Guard types
│       ├── GuardType.java
│       └── registry/
│           ├── IGuardTypeRegistry.java
│           ├── IGuardTypeDataManager.java
│           └── ModGuardTypes.java
│
├── research/
│   ├── IResearchManager.java
│   ├── IGlobalResearch.java
│   ├── IGlobalResearchBranch.java
│   ├── IGlobalResearchTree.java
│   ├── ILocalResearch.java
│   ├── ILocalResearchTree.java
│   ├── IResearchCost.java
│   ├── IResearchEffect.java
│   ├── IResearchEffectManager.java
│   ├── IResearchRequirement.java
│   ├── ModResearchCosts.java
│   ├── ModResearchEffects.java
│   ├── ModResearchRequirements.java
│   ├── ResearchBranchType.java
│   ├── costs/
│   │   ├── ListItemCost.java
│   │   ├── SimpleItemCost.java
│   │   └── TagItemCost.java
│   ├── factories/                             # Factory interfaces
│   ├── requirements/
│   │   ├── BuildingResearchRequirement.java
│   │   ├── BuildingAlternatesResearchRequirement.java
│   │   └── ResearchResearchRequirement.java
│   └── util/
│       ├── ResearchConstants.java              # Effect IDs
│       └── ResearchState.java                 # Research state enum
│
├── quests/
│   ├── IQuestManager.java
│   ├── IQuestTemplate.java
│   ├── IQuestInstance.java
│   ├── IQuestGiver.java
│   ├── IQuestParticipant.java
│   ├── IQuestObjectiveTemplate.java
│   ├── IQuestRewardTemplate.java
│   ├── IQuestTriggerTemplate.java
│   ├── IQuestDialogueAnswer.java
│   ├── IObjectiveInstance.java
│   ├── FinishedQuest.java
│   ├── QuestParseConstant.java
│   └── registries/QuestRegistries.java
│
├── entity/
│   ├── citizen/
│   │   ├── AbstractEntityCitizen.java          # Base citizen entity
│   │   ├── Skill.java                          # Skill enum (8 skills)
│   │   ├── VisibleCitizenStatus.java
│   │   └── citizenhandlers/                    # Citizen behavior handlers
│   ├── ai/
│   │   ├── ITickingStateAI.java                # Ticking AI interface
│   │   ├── IStateAI.java
│   │   ├── JobStatus.java                      # Job status enum
│   │   ├── DesiredActivity.java
│   │   ├── Status.java
│   │   ├── statemachine/                        # State machine system
│   │   │   ├── states/IAIState.java
│   │   │   ├── states/IState.java
│   │   │   ├── states/CitizenAIState.java       # IDLE, WORK, SLEEP, etc.
│   │   │   ├── states/AIWorkerState.java
│   │   │   ├── states/EntityState.java
│   │   │   ├── tickratestatemachine/            # Tick-rate state machine
│   │   │   │   ├── ITickRateStateMachine.java
│   │   │   │   ├── TickRateStateMachine.java
│   │   │   │   ├── TickingTransition.java
│   │   │   │   └── TickRateConstants.java
│   │   │   ├── AITarget.java                    # State transition target
│   │   │   ├── AIEventTarget.java
│   │   │   ├── AIOneTimeEventTarget.java
│   │   │   └── basestatemachine/                # Base state machine
│   │   ├── combat/                              # Combat AI
│   │   │   ├── CombatAIStates.java
│   │   │   └── threat/                          # Threat system
│   │   └── workers/util/                        # Worker AI utilities
│   ├── mobs/                                    # Custom mobs (raiders, etc.)
│   ├── other/                                   # Other entities
│   └── pathfinding/                             # Pathfinding system
│
├── blocks/
│   ├── ModBlocks.java                           # Block registry
│   ├── AbstractColonyBlock.java                 # Base colony block
│   ├── AbstractBlockHut.java                    # Base hut block
│   ├── AbstractBlockMinecolonies.java          # Base mod block
│   ├── AbstractBlockMinecoloniesContainer.java
│   ├── AbstractBlockBarrel.java
│   ├── AbstractBlockMinecoloniesGrave.java
│   ├── AbstractBlockMinecoloniesRack.java
│   ├── decorative/                              # Decorative blocks
│   ├── huts/AbstractBlockMinecoloniesDefault.java
│   ├── interfaces/                              # Block interfaces
│   └── types/                                    # Block type enums
│
├── items/
│   ├── ModItems.java                             # Item registry
│   ├── ModTags.java                              # Item tags
│   ├── ModBannerPatterns.java
│   ├── IChiefSwordItem.java
│   ├── IMinecoloniesFoodItem.java
│   ├── ISupplyItem.java
│   ├── IBlockOverlayItem.java
│   ├── CheckedNbtKey.java
│   └── ItemBlockHut.java
│
├── eventbus/
│   ├── EventBus.java                             # Event bus singleton
│   ├── DefaultEventBus.java
│   ├── IModEvent.java
│   ├── AbstractModEvent.java
│   └── events/
│       ├── ColonyManagerLoadedModEvent.java
│       ├── ColonyManagerUnloadedModEvent.java
│       ├── CustomRecipesReloadedEvent.java
│       └── colony/
│           ├── ColonyCreatedModEvent.java
│           ├── ColonyDeletedModEvent.java
│           ├── ColonyNameChangedModEvent.java
│           ├── ColonyFlagChangedModEvent.java
│           ├── ColonyTeamColorChangedModEvent.java
│           ├── ColonyViewUpdatedModEvent.java
│           ├── ColonyPlayerRankChangedModEvent.java
│           ├── permissions/
│           │   ├── PlayerEnteringModEvent.java
│           │   └── PlayerLeavingModEvent.java
│           ├── buildings/
│           │   ├── BuildingAddedModEvent.java
│           │   ├── BuildingRemovedModEvent.java
│           │   └── BuildingConstructionModEvent.java
│           └── citizens/
│               ├── AbstractCitizenModEvent.java
│               ├── CitizenAddedModEvent.java
│               ├── CitizenRemovedModEvent.java
│               ├── CitizenDiedModEvent.java
│               └── CitizenJobChangedModEvent.java
│
├── network/
│   ├── IMessage.java                             # Network message interface
│   └── PacketUtils.java
│
├── crafting/
│   ├── IRecipeManager.java
│   ├── IRecipeStorage.java
│   ├── IGenericRecipe.java
│   ├── ItemStorage.java
│   └── registry/CraftingType.java
│
├── configuration/                                 # Config
├── creative/                                      # Creative tab
├── enchants/                                      # Enchantments
├── equipment/                                     # Equipment registry
├── inventory/                                     # Inventory system
│   ├── api/CombinedItemHandler.java
│   └── container/                                # Container types
├── loot/                                          # Loot tables
├── sounds/                                       # Sounds
├── tileentities/                                  # Tile entities
│   └── AbstractTileEntityColonyBuilding.java
├── compatibility/                                # Mod compatibility
│   ├── candb/                                    # Chisels & Bits
│   ├── dynamictrees/                             # Dynamic Trees
│   ├── newstruct/                                # Structurize
│   ├── resourcefulbees/                          # Resourceful Bees
│   └── tinkers/                                  # Tinkers Construct
└── util/                                         # Utilities
    └── constant/                                 # Constants