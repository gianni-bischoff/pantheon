# AGENTS.md

Guidelines for AI agents (and humans) working on **Pantheon**.
Read this before making any change to the codebase.

---

## Project snapshot

- **Mod:** Pantheon — seasonal faith/belief gameplay mod where the daily majority vote empowers a city god, unlocking buffs and goals.
- **Platform:** Minecraft **1.21.1** on **NeoForge 21.1.x**.
- **Language:** **Kotlin** (no Java sources). Loaded via **KotlinForForge 5.10.0** (`kotlinforforge-neoforge`), mod loader `kotlinforforge`.
- **Build:** Gradle (Groovy DSL) with `net.neoforged.moddev`. Kotlin Gradle plugin `org.jetbrains.kotlin.jvm` 2.2.20, JVM target 21.
- **Group id:** `gg.wildblood`. Mod id: `pantheon`.

### Key files
| Path | Purpose |
|------|---------|
| `build.gradle` | Plugins, KFF repo/dep, NeoForge runs, datagen config |
| `gradle.properties` | Versions (MC, NeoForge, KFF, mod metadata) |
| `src/main/templates/META-INF/neoforge.mods.toml` | Mod metadata template (properties expanded at build time) |
| `src/main/resources/pantheon.mixins.json` | Mixin config; package `gg.wildblood.mixin` |
| `src/main/kotlin/gg/wildblood/Pantheon.kt` | `@Mod` entrypoint and registry wiring hub |
| `.agents/skills/neoforge-docs/` | Local NeoForge 1.21.1 API reference — consult when unsure about API specifics |
| `.agents/skills/kotlin-patterns/` | Idiomatic Kotlin conventions — consult when writing/reviewing Kotlin |

---

## Build & verify commands

Run from the project root. Always run the relevant checks before considering a task done.

```bash
# Compile Kotlin + Java (fastest feedback loop)
./gradlew compileKotlin --no-daemon --no-configuration-cache

# Full build (compile + resources + jar)
./gradlew build --no-daemon --no-configuration-cache

# Launch the client (dev)
./gradlew runClient --no-daemon --no-configuration-cache

# Launch the dedicated server (dev)
./gradlew runServer --no-daemon --no-configuration-cache

# Regenerate data packs / assets (output → src/generated/resources)
./gradlew runData --no-daemon --no-configuration-cache

# Refresh dependencies after changing build.gradle
./gradlew --refresh-dependencies --no-daemon --no-configuration-cache
```

> **WSL note:** the project lives on `/mnt/c` (Windows filesystem mounted in WSL). Gradle's daemon and configuration cache occasionally throw `Input/output error` on cross-filesystem locks. Always pass `--no-daemon --no-configuration-cache`. If a build still fails with I/O errors, delete `.gradle/` (NOT `gradle/`) and retry.

### Before committing
1. `./gradlew build --no-daemon --no-configuration-cache` must succeed.
2. Prefer also running `./gradlew compileKotlin ...` after small edits for a fast check.
3. Never commit if the build is red.

---

## Code conventions (strict)

### Language
- **Kotlin only.** Do not add `.java` files. If a file exists in Java, convert it to Kotlin before extending it.
- Target **JVM 21** language features.
- Apply idiomatic Kotlin from `.agents/skills/kotlin-patterns/`: `val` over `var`, sealed classes for exhaustive hierarchies, `data class` for pure data, extension functions over utility statics, safe-call `?.` / Elvis `?:` over `!!`.

### Mod entrypoint pattern
- The `@Mod` class **must** be an `object` declared in `gg.wildblood.Pantheon`. KotlinForForge loads the object instance directly — no constructor parameters.
- Get the mod event bus via `thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS`, not `FMLJavaModLoadingContext`.
- Get the active container via `LOADING_CONTEXT.activeContainer`.
- Keep `Pantheon.kt` a thin wiring hub. Feature code lives in its package (see layout). Add new `DeferredRegister`s to the `REGISTRIES` list in `Pantheon.init`.

### Package layout (do not deviate)
```
gg.wildblood
├── Pantheon.kt              @Mod object, registry wiring, top-level event listeners
├── block/                   ModBlocks + custom Block subclasses
├── blockentity/             ModBlockEntities + BlockEntity classes
├── item/                     ModItems, ModCreativeTabs + custom Item subclasses
├── entity/                  ModEntities + custom Entity classes
├── client/
│   ├── PantheonClient.kt     @Mod(dist = CLIENT) client setup
│   └── gui/                 Client-only screens / HUD overlays
├── gui/                     Menus/containers shared by client + server
├── command/                 Brigadier commands
├── network/                 Custom payload registration + packet handlers
├── data/                    Data generation entrypoint (GatherDataEvent)
├── config/                  ModConfigSpec definitions
├── util/                    Side-safe helpers and constants
└── mixin/                   Mixin classes (referenced by pantheon.mixins.json)
```

- When adding a new feature, put it in the matching package. If none fits, create a new sibling package under `gg.wildblood` and document it here.
- Registry hubs (`ModBlocks`, `ModItems`, ...) are `object`s with a `val REGISTRY = DeferredRegister.create...(...)` and `val` entries. Register them by adding to `Pantheon.REGISTRIES`.

### Sidedness
- Client-only code goes under `gg.wildblood.client` and is annotated `@Mod(dist = [Dist.CLIENT])` or `@EventBusSubscriber(value = [Dist.CLIENT])`. Never reference `Minecraft`, `Screen`, renderers, etc. from outside the `client` package.
- Shared code (menus, payloads, data components) must be side-safe — no `Dist.CLIENT`-only class names in `gg.wildblood.gui`, `network`, `util`, etc.
- Prefer `runWhenOn(Dist.CLIENT) { ... }` from KFF over reflective `DistExecutor` calls.

### Registration
- Use `DeferredRegister.create(registryKey, Pantheon.MODID)` (the public `create` factory — the constructor is protected and Kotlin will reject it).
- For `DeferredHolder`/`DeferredItem`/`DeferredBlock` values, assign with `=` (not `by`) so the holder is preserved. Use the KFF `getValue` delegate only when you want the resolved entry at call sites.
- When a `register(...) { ... }` call has overload ambiguity, disambiguate with an arrow-start lambda: `register("name") { -> ... }`.

### Data generation
- Generated resources go to `src/generated/resources/` (already wired in `build.gradle`). Do **not** hand-edit files there — regenerate via `runData`.
- Hand-authored assets/data live in `src/main/resources/`.
- Datagen cache under `src/generated/**/.cache/` is gitignored.

### Resources & lang
- Every config key needs a matching translation in `src/main/resources/assets/pantheon/lang/en_us.json` including `.tooltip` (and `.button` where applicable) — NeoForge's `TranslationChecker` warns otherwise.
- Translation keys: `pantheon.configuration.<key>` for config, `block.pantheon.<id>` / `item.pantheon.<id>` / `itemGroup.pantheon` for content.

### NeoForge API lookup
- When unsure about exact signatures, class names, registration patterns, or JSON formats for 1.21.1, consult `.agents/skills/neoforge-docs/` (start at its `SKILL.md` index). Don't guess version-specific API behavior.

### Comments & style
- Do **not** add comments unless asked. Let naming and package structure carry intent.
- Use `kotlin.code.style=official` (already set via KFF conventions).
- Keep file headers free of license boilerplate unless a file derives from a licensed template.

---

## Git & commit rules

- Never commit unless explicitly asked.
- Stage only intended files. Never stage secrets, `.gradle/`, `build/`, `run/`, `.kotlin/`, `.opencode/`, or `graphify-out/` (all gitignored).
- Commit message style: `<type>: <imperative summary>` — e.g. `feat: add belief vote command`, `fix: correct tab icon NPE`. Wrap long bodies at 72 chars.
- Do not amend, force-push, or rewrite history unless asked.

---

## Things to avoid

- Adding `.java` files.
- Using `FMLJavaModLoadingContext` — use KFF's `MOD_BUS`.
- Calling the protected `DeferredRegister` constructor — use `DeferredRegister.create(...)`.
- Growing `Pantheon.kt` with feature logic — put features in their packages.
- Hand-editing `src/generated/resources/`.
- Skipping the build verification step.
- Client-only class references outside `gg.wildblood.client`.