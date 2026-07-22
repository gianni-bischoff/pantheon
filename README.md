# Pantheon

A seasonal faith/belief gameplay mod for Minecraft **1.21.1** on **NeoForge**.
Each day, players vote to decide which city god is empowered — the winner unlocks buffs, goals, and seasonal mechanics that shape where your city develops.

**Status:** scaffolded — feature development starting now.

---

## Tech stack

| | |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x |
| Mod loader | KotlinForForge 5.10.0 (`kotlinforforge`) |
| Language | Kotlin (JVM 21), no Java sources |
| Build | Gradle (Groovy DSL) + `net.neoforged.moddev` |

## Building

```bash
# Compile only (fast check)
./gradlew compileKotlin --no-daemon --no-configuration-cache

# Full build (compiles + resources + jar)
./gradlew build --no-daemon --no-configuration-cache
```

> On WSL, always pass `--no-daemon --no-configuration-cache`. If you hit `Input/output error` on Gradle cache locks, delete `.gradle/` and retry.

## Running

```bash
# Dev client
./gradlew runClient --no-daemon --no-configuration-cache

# Dev dedicated server
./gradlew runServer --no-daemon --no-configuration-cache

# Regenerate data/assets → src/generated/resources
./gradlew runData --no-daemon --no-configuration-cache
```

## Project layout

```
gg.wildblood
├── Pantheon.kt            @Mod object, registry wiring, event listeners
├── block/                 Blocks
├── blockentity/           Block entities
├── item/                  Items + creative tabs
├── entity/                Entities
├── client/                Client-only setup + screens
├── gui/                   Shared menus/containers
├── command/               Commands
├── network/               Custom payloads
├── data/                  Data generation
├── config/                ModConfigSpec
├── util/                  Side-safe helpers
└── mixin/                 Mixins
```

See [`AGENTS.md`](AGENTS.md) for the full development guidelines (build commands, code conventions, registration patterns, sidedness rules).

## License

All Rights Reserved unless otherwise noted. `TEMPLATE_LICENSE.txt` covers original NeoForged MDK template files.