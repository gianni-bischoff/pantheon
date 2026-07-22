# LDLib2 — Synchronization & Persistence

Package `com.lowdragmc.lowdraglib2.sync_data` (annotation processors) + `com.lowdragmc.lowdraglib2.gui.sync` (UI bindings). LDLib2 replaces hand-written `saveAdditional`/`load`/`getUpdateTag`/`handleUpdateTag` and custom network packets with annotation-driven field management on a `FieldManagedStorage`.

## The `ISyncPersistRPCBlockEntity` hub

Compose of `ISyncBlockEntity` + `IRPCBlockEntity` + `IPersistManagedHolder` + `IBlockEntityManaged`. Implement the combined interface to get all features at once, or pick a subset for fine-grained control.

```kotlin
class MyBlockEntity(pos: BlockPos, state: BlockState)
    : BlockEntity(MyBlockEntities.MY_BE.get(), pos, state), ISyncPersistRPCBlockEntity {

    @get:JvmName("getSyncStorage")          // Lombok needs the getter name stable
    val syncStorage = FieldManagedStorage(this)

    @Persisted @DescSynced @UpdateListener(methodName = "onIntChanged")
    var intValue = 10
    private fun onIntChanged(old: Int, new: Int) { /* runs on remote/client */ }

    @Persisted @DescSynced @RequireRerender
    var color = -1

    @Persisted @DropSaved
    var item = ItemStack.EMPTY

    @RPCMethod
    fun rpcMsg(msg: String) {
        if (level!!.isClientSide) {           // received
            LDLib2.LOGGER.info("got rpc: {}", msg)
        } else {                               // send
            rpcToTracking("rpcMsg", msg)
        }
    }
}
```

`FieldManagedStorage(this)` scans the class for LDLib2 annotations and wires persistence + sync + RPC. Without it, none of the annotations do anything.

## Annotations

### `@Persisted` — NBT save/load

Field value is written to / read from the BlockEntity's NBT. Optional `key = "tagName"` (default = field name). `subPersisted = true` serializes the field's **internal** value (for `final` instances): if the field implements `INBTSerializable<?>` LDLib2 uses its API; otherwise it serializes the field's own `@Persisted` children into a map.

```kotlin
@Persisted(key = "fluidAmount") var amount = 100
@Persisted(subPersisted = true) val stackHandler = ItemStackHandler(5)   // final + INBTSerializable
```

### `@DescSynced` — server → client sync

Field value (server side) is synced to the remote/client. Pair with `@Persisted` when you also want NBT save.

### `@RPCMethod` — cross-network method call

Annotate a method; call it via `rpcToServer(name, args...)`, `rpcToTracking(name, args...)`, `rpcToPlayer(player, name, args...)`. Parameter types must be sync-supported (see Types Support). If the first parameter is `RPCSender`, LDLib2 fills in sender info.

```kotlin
@RPCMethod fun rpcTestA(sender: RPCSender, msg: String) { /* ... */ }
@RPCMethod fun rpcTestB(stack: ItemStack) { /* ... */ }

fun fromClient(stack: ItemStack) = rpcToServer("rpcTestB", stack)
fun fromServerToTracking(msg: String) = rpcToTracking("rpcTestA", msg)
```

A single method can both send and receive by branching on `level.isClientSide` (see the `rpcMsg` example above).

### `@UpdateListener(methodName)` — react on the remote side

```kotlin
@DescSynced @UpdateListener(methodName = "onIntChanged")
var intValue = 10
private fun onIntChanged(old: Int, new: Int) { /* runs on client when server pushes a new value */ }
```

### `@LazyManaged` — manual dirty marking

The field is not auto-marked dirty on write; you must call `markDirty("fieldName")` (or the matching overload) when you want it synced/persisted. Useful for batched or infrequent updates.

```kotlin
@DescSynced @Persisted @LazyManaged var b = 0
fun setB(v: Int) { b = v; markDirty("b") }
```

### `@ReadOnlyManaged` — non-instantiable types

For "read-only" types (always non-null, immutable instance, no known constructor — e.g. `IManaged`, `INBTSerializable<?>`, or custom objects). Provide:

- `serializeMethod = "name"` — `Tag name(@Nonnull T obj)` returns a unique id.
- `deserializeMethod = "name"` — `T name(@Nonnull Tag tag)` creates a new instance from the id.
- `onDirtyMethod = "name"` (optional) — `boolean name()` self-controlled dirty check.

Sync process: (1) compare uid snapshot; if changed → dirty. (2) else check dirty via registered read-only type or `onDirtyMethod`. (3) if dirty, sync uid + value; remote rebuilds the instance via `deserializeMethod` then applies the value.

```kotlin
@Persisted @DescSynced
@ReadOnlyManaged(serializeMethod = "grpSer", deserializeMethod = "grpDeser")
val groups: MutableList<TestGroup> = ArrayList()

fun grpSer(g: List<TestGroup>) = IntTag.valueOf(g.size)
fun grpDeser(t: IntTag): List<TestGroup> = List(t.asInt) { TestGroup() }
```

If the element type implements `IPersistedSerializable` (→ `INBTSerializable`), `onDirtyMethod` is unnecessary — LDLib2 knows how to diff it.

### `@ConditionalSynced(methodName)` — gate sync

```kotlin
@Configurable @ConditionalSynced(methodName = "shouldSync")
var intField = 10
fun shouldSync(v: Int) = v > 0
```

### `@SkipPersistedValue(field = "...")` — gate persistence

```kotlin
@Persisted var intField = 10
@SkipPersistedValue(field = "intField")
fun skipInt(v: Int) = v == 10      // initial value → don't store
```

## BlockEntity-exclusive annotations

### `@DropSaved` — save into the broken-block item

Requires the `Block` to cooperate: in `setPlacedBy`, load `DataComponents.CUSTOM_DATA` into `loadManagedPersistentData`; in `getDrops`/`getCloneItemStack`, `saveManagedPersistentData(tag, true)` into `CustomData.of(tag)` on the drop/clone stack. See the Block example in the official `annotations` doc — the wiring is ~15 lines and must be done once per droppable block.

### `@RequireRerender` — re-render chunk on change

When the annotated field updates (synced from server), schedules a chunk render update. The BlockEntity must implement `IBlockEntityManaged` (included in `ISyncPersistRPCBlockEntity`). Equivalent to manually calling `level.sendBlockUpdated(pos, state, state, 1 shl 3)` from an `@UpdateListener`.

## RPC Packet (non-UI, global networking)

`@RPCMethod` on a BlockEntity works chunk-tracked (to players tracking the chunk). For UI-scoped RPC use the UI `RPCEvent`/`message` system (see `references/ui.md`); for arbitrary client↔server packets not tied to a UI or a chunk, use the lower-level `RPCPacket` system documented at `en/ldlib2/sync/rpc_packet.html`. Prefer `@RPCMethod` + `rpcTo*` over hand-rolled payloads.

## Types Support (sync/persistence)

Not every type syncs out of the box. "Read-only" types (no known constructor) need `@ReadOnlyManaged` or an explicit accessor. The official table is at `en/ldlib2/sync/types_support.html`. Common supported types: primitives + boxes, `String`, `ResourceLocation`, `ItemStack`, `FluidStack`, `CompoundTag`/`Tag`, enums, `Vector3f`/`Vector3i`, `Direction`, `Range`, `INBTSerializable<?>` (read-only). **Collections (`List`, `Map`, `Set`) and generic types are read-only** — for UI bindings supply `.syncType(type)` + `.initialValue(...)`; for `@Persisted` use `subPersisted = true` or register a custom `PersistedParser` (see `en/ldlib2/sync/PersistedParser.html`).

## Persistence parser customization

`PersistedParser` lets you register custom serialization for a type used in `@Persisted` fields. Register during `@LDLibPlugin.onLoad()`. Useful for third-party types that don't implement `INBTSerializable`. See `en/ldlib2/sync/PersistedParser.html`.

## Threading caveat

`useAsyncThread()` defaults to `true` on `ISyncPersistRPCBlockEntity` — dirty notifications (`notifyPersistence()`) may fire on a worker thread. LDLib2 handles the common paths safely, but don't touch unsynchronized Minecraft state (level, players, other BlockEntities) directly from a dirty callback without considering thread safety. When in doubt, schedule back onto the main thread.

## Gotchas

- Forgetting `FieldManagedStorage(this)` → annotations silently do nothing.
- Forgetting `@get:JvmName(...)` on the `syncStorage` val when Lombok is involved → reflection can't find the getter.
- `@DescSynced` without `@Persisted` syncs but doesn't save across restarts; usually you want both.
- Letting the client write a server-owned mutable (`IFluidHandler`, inventory) via a bidirectional UI binding — use s→c binding + RPC instead (see `FluidSlot.bind`).
- Read-only / generic collection bindings without `.syncType(type)` + `.initialValue(...)` → silent sync failure.