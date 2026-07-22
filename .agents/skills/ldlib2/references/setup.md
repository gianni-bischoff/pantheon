# LDLib2 — Setup, Plugin & Registry

## Gradle / Maven

Target: **LDLib2 2.2.x on Minecraft 1.21.1 / NeoForge 21.1.x**. Kotlin 2.1.20+, JVM 21. LDLib2 itself uses `org.jetbrains.kotlin.plugin.lombok` + `io.freefair.lombok` and enables context parameters (`-Xcontext-parameters`) and when-guards (`-Xwhen-guards`) — your mod does not need these flags unless you use those language features.

Repository (add to `build.gradle`):

```groovy
repositories {
    maven { url = "https://maven.firstdark.dev/snapshots" }   // LDLib2
}
```

Dependency — **since 2.2.1** (the 1.21.1 line you want):

```groovy
dependencies {
    implementation("com.lowdragmc.ldlib2:ldlib2-neoforge-${minecraft_version}:${ldlib2_version}:all")
}
```

**Before 2.2.1** (older 1.21.1 releases) the classifier differed and `yoga` had to be added separately:

```groovy
dependencies {
    implementation("com.lowdragmc.ldlib2:ldlib2-neoforge-${minecraft_version}:${ldlib2_version}:all") { transitive = false }
    compileOnly("org.appliedenergistics.yoga:yoga:1.0.0")
}
```

Check the latest version badge at `https://maven.firstdark.dev/#/snapshots/com/lowdragmc`. The artifact is `ldlib2-neoforge-1.21.1`.

> 26.1.x line: drop the `:all` classifier — `implementation("com.lowdragmc.ldlib2:ldlib2-neoforge-${minecraft_version}:${ldlib2_version}")`. Do not mix 1.21.1 and 26.1 patterns.

## `@LDLibPlugin` — one-time LDLib2 setup

```kotlin
import com.lowdragmc.lowdraglib2.plugin.ILDLibPlugin
import com.lowdragmc.lowdraglib2.plugin.LDLibPlugin

@LDLibPlugin
class MyLDLibPlugin : ILDLibPlugin {
    override fun onLoad() {
        // LDLib2-specific registration, registry subscriptions, etc.
        // This runs once when LDLib2 loads; use it instead of reaching into LDLib2 internals from your @Mod constructor.
    }
}
```

`ILDLibPlugin` is a single-method interface (`fun onLoad()`); `@LDLibPlugin` is a runtime annotation discovered by LDLib2. Put your LDLib2-side init here, not in your NeoForge `@Mod` constructor.

## `@LDLRegister` / `@LDLRegisterClient` — LDLib2 auto-registry

LDLib2 maintains its own registries (e.g. `ldlib2:screen_test`, `ldlib2:menu_test`, editor view registries, node-graph node registries). These are **independent** of NeoForge's `DeferredRegister` / vanilla registries.

```kotlin
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient
import com.lowdragmc.lowdraglib2.registry.ILDLRegister
import com.lowdragmc.lowdraglib2.registry.ILDLRegisterClient

@LDLRegister(registry = "ldlib2:menu_test", name = "my_synced_menu")
class MyMenuTest : IMenuTest, ILDLRegister { /* server+client */ }

@LDLRegisterClient(registry = "ldlib2:screen_test", name = "my_screen")
class MyScreenTest : IScreenTest, ILDLRegisterClient { /* client only */ }
```

Annotation fields:
- `registry` — the LDLib2 registry id (ResourceLocation format, e.g. `"ldlib2:menu_test"`).
- `name` — unique key within that registry.
- `group` — optional category for grouping.
- `modID` — only register when this mod is installed (optional soft dependency).
- `priority` — iteration order (higher = earlier).
- `environment` — `RegistrationEnvironment.ALWAYS` (default), `CLIENT`, `SERVER`, or `MANUAL` (26.1+; replaces the deprecated `manual = true`).

Rules:
- The annotated class **must** implement `ILDLRegister` (for `@LDLRegister`) or `ILDLRegisterClient` (for `@LDLRegisterClient`). The interface carries the `onLoad`/registration hook the registry calls.
- `@LDLRegisterClient` registers **only on the client** — use for pure screen-side UI test classes or client-only editor views.
- Auto-registry scans the classpath; you do not call a `DeferredRegister.create(...)`. If you want to skip auto-registration and register manually, set `environment = RegistrationEnvironment.MANUAL` (26.1+) or `manual = true` (deprecated, 1.21.1) and call the registry's `register(...)` yourself inside your `@LDLibPlugin.onLoad()`.

## IDEA plugin (recommend to users)

"LDLib Dev Tool" (`https://plugins.jetbrains.com/plugin/28032-ldlib-dev-tool`) — code highlight, syntax check, code jumping, autocomplete for LDLib2 annotations and LSS. Strongly recommended when authoring LSS or using `@Configurable`/`@Persisted`/`@DescSynced` heavily.

## Version map (quick)

| MC | NeoForge | LDLib2 line | Kotlin | Notes |
|----|----------|-------------|--------|-------|
| 1.21.1 | 21.1.x | 2.2.x | 2.1.20 | `:all` classifier; `yoga` bundled for ≥2.2.1 |
| 1.21 (old) | 21.0.x | 2.1.x–2.2.0 | 2.1.20 | needs `transitive = false` + explicit `yoga` dep |
| 26.1.x | 26.1.x | 26.1.x+ | 2.1.20+ | no `:all` classifier; API drift — see migration doc |

When in doubt about a class/method for a specific LDLib2 version, clone `https://github.com/Low-Drag-MC/LDLib2` (branch `1.21` for 1.21.1) and grep the source — the API moves between minor versions.