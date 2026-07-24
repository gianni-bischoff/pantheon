# Vision Docs Folder Structure Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the `docs/vision/` folder with 8 design docs (one overview + seven pillar docs) that decompose the Pantheon vision into graspable, independently-specable pieces.

**Architecture:** All content is derived from the approved spec at `docs/superpowers/specs/2026-07-23-vision-docs-structure-design.md`. The overview doc (`00-overview.md`) is the entry point with pitch, pillar list, day-in-the-life narrative, interlock diagram, status, and scope. Each pillar doc (`01`-`07`) follows a shared template (Purpose / Scope / Dependencies / Design / Integration / Open questions). This is a documentation-only deliverable — no code, no build, no tests.

**Tech Stack:** Markdown only.

## Global Constraints

- **Source of truth:** `docs/superpowers/specs/2026-07-23-vision-docs-structure-design.md`. Every doc's content traces back to a section of that spec. Do not invent new design decisions — if something is unresolved, it goes in that doc's `Open questions` section and must already be listed in spec section 10.
- **Shared template:** Every pillar doc (`01`-`07`) has exactly these sections in order: `## Purpose`, `## Scope`, `## Dependencies`, `## Design`, `## Integration`, `## Open questions`. No exceptions.
- **Overview doc does NOT use the template** — it has its own shape defined in spec section 6.
- **Pillar purposes and scopes** are copied verbatim from spec section 7's table. Do not rephrase.
- **Interlock arrows** are copied from spec section 8. The overview's interlock section links to the full arrow descriptions.
- **Day-in-the-life narrative** is seeded from spec section 9; the overview expands it with pillar-firing markers.
- **`02-factions.md` links, does not duplicate:** it references the existing implementation spec at `docs/superpowers/specs/2026-07-22-faction-gui-visual-identity-design.md` via a relative markdown link, and adds only vision-level context.
- **Open questions per pillar** are drawn from spec section 10 — only the questions relevant to that pillar. Questions that span pillars go in `00-overview.md`.
- **Cross-links:** every pillar doc's `Integration` section links back to `00-overview.md` and to the specific pillar docs it connects to (using the arrows from spec section 8). Use relative paths (`./01-world-and-cities.md`, etc.).
- **No placeholders:** every `## Design` section must contain real content derived from the spec, not "TBD" or "to be expanded." The spec defines purpose/scope/dependencies/interlock for each pillar — that material goes into `Design` and `Integration`. `Design` may note that deeper mechanics will be spec'd in a future pillar-specific design doc, but it must state the high-level design as understood from the spec.
- **Commit per task:** each task ends with a commit. Commit message style: `docs: <imperative summary>` per AGENTS.md.
- **No code changes:** this plan touches only `docs/vision/`. Do not modify `src/`, `build.gradle`, or any existing file outside `docs/vision/`.

---

## File Structure

```
docs/vision/
├── 00-overview.md           ← Task 1 — entry point, non-template shape
├── 01-world-and-cities.md   ← Task 2 — template
├── 02-factions.md           ← Task 3 — template, links existing spec
├── 03-llm-and-gods.md       ← Task 4 — template
├── 04-citizens.md           ← Task 5 — template
├── 05-economy.md            ← Task 6 — template
├── 06-social.md             ← Task 7 — template
└── 07-quests.md             ← Task 8 — template
```

Task 9 is verification + final commit. Eight content tasks + one verification task.

---

### Task 1: Overview doc (`00-overview.md`)

**Files:**
- Create: `docs/vision/00-overview.md`

**Interfaces:**
- Produces: the entry-point doc that all pillar docs link back to. Pillar docs (Tasks 2-8) reference this file as `./00-overview.md`.

- [ ] **Step 1: Create `docs/vision/` directory**

Run: `mkdir -p docs/vision`
Expected: directory exists, no output.

- [ ] **Step 2: Write `00-overview.md`**

Create `docs/vision/00-overview.md` with this content (derived from spec sections 2, 4, 6, 8, 9, 10):

````markdown
# Pantheon — Vision Overview

This is the entry point for the Pantheon mod's design. Each pillar of the vision has its own doc; this page ties them together.

## The pitch

A void world is the default. Each faction has a floating city-state, and building is restricted to your city's bounds so nobody sprawls across the void. Inside a city, **Minecolonies-style citizens live full lives** — they eat, work, produce, need, reproduce, form relationships. **Players are integrated into that civic, economic, and social life**, not spectators above it. A **fixed pantheon of gods** gives those actions purpose: factions align with a god, a daily vote empowers one, and the winning god's buffs steer what the city does that day. Gods are **LLM agents** that speak, react, and rule by mood; a **world-narrator LLM** generates events, quests, and crises across the whole world. The faith layer is the *why* behind the civic/economic/social engine — not the engine itself.

## The pillars

- [World & Cities](./01-world-and-cities.md) — the container: void world, floating city-states, build bounds, sky travel
- [Factions](./02-factions.md) — who you belong to; your city-state's identity (built)
- [LLM & Gods](./03-llm-and-gods.md) — the purpose layer: fixed pantheon, daily vote, LLM god-agents + world-narrator
- [Citizens](./04-citizens.md) — the living population, via Minecolonies integration
- [Economy](./05-economy.md) — production, jobs, needs, inter-city trade
- [Social](./06-social.md) — NPC↔NPC relationships, reproduction, player↔NPC social
- [Quests](./07-quests.md) — purposeful objectives tied to faith + city needs

## A day in the life

You log in. You're in **your faction's** city ([02 Factions](./02-factions.md)), floating in the **void** ([01 World & Cities](./01-world-and-cities.md)). The morning bell rings — **citizens** ([04 Citizens](./04-citizens.md)) are heading to their jobs; the baker is already producing bread, the smith is smelting. You check the temple: today's **vote** ([03 LLM & Gods](./03-llm-and-gods.md)) is open, and your god — the Sun Lord — is trailing. You cast your vote and drop an offering. The Sun Lord (an **LLM agent**) acknowledges you in its own words, mood shifted by the offering. The vote closes at noon: the Sun Lord wins, and his buff — increased harvest yield — settles over the city. Citizens work faster in the fields; the **economy** ([05 Economy](./05-economy.md)) ticks up. A **narrator event** fires: a merchant caravan from another city-state is stranded in the void. A **quest** ([07 Quests](./07-quests.md)) appears: escort the caravan. You gear up, cross the sky to the other island, fight off the void wraiths, and bring the caravan home. The economy rewards you; the Sun Lord is pleased; your citizens eat well tonight. You spend the evening **building** an extension to your home — inside the bounds, the build is allowed — and chatting with an **NPC** neighbor whose child was born last week ([06 Social](./06-social.md): reproduction). You log off feeling like you live there.

## How the pillars interlock

```
                         ┌──────────────────────────┐
                         │  03 LLM & Gods            │
                         │  (pantheon, vote, buffs,  │
                         │   god-agents, narrator)   │
                         └────────────┬─────────────┘
                          buffs steer │  events/quests spawn
                    ┌─────────────────┼─────────────────┐
                    ▼                 ▼                  ▼
              ┌─────────┐      ┌──────────┐       ┌──────────┐
              │ 04 Cit. │─────▶│ 05 Econ  │       │ 07 Quests│
              │ (state) │      │ (prod/   │◀──────│ (reads   │
              └────┬────┘      │  needs)  │       │  needs + │
                    │           └──────────┘       │  faith)  │
                    │                ▲              └──────────┘
                    ▼                │                  ▲
               ┌─────────┐           │                  │
               │ 06 Soc. │───────────┘                  │
               │ (relps, │  reproduction feeds needs    │
               │  repro) │                              │
               └─────────┘                              │
                                                         │
    01 World & Cities ───────────contains all of the above──┘
    02 Factions ────────────civic unit the sim runs on───┘
```

The arrows, in words:

1. **03 → 04 (gods steer citizens):** The winning god's buffs change citizen behavior for the day — productivity, needs, moods. Citizens (via the Minecolonies bridge) read these as modifiers.
2. **03 → 05 (gods steer economy):** God buffs modify production rates and needs — a harvest god boosts food output; a war god raises military needs. Economy applies these as multipliers.
3. **03 → 07 (narrator + faith spawn quests):** The world-narrator LLM generates events/crises that become quests; faith directives (god commands) also seed quests. Quests read these as generation sources.
4. **04 → 05 (citizens drive economy):** Citizens are the labor force. Their jobs, numbers, and health determine production and consumption. Economy reads citizen state from the Minecolonies bridge.
5. **04 → 06 (citizens are the social substrate):** Relationships form between citizens; the citizen pool is what reproduces. Social reads citizen state and writes relationship/repro state back.
6. **06 → 05 (reproduction feeds economy):** New citizens → more labor → more production and more needs. Economy reads population size from social/repro state.
7. **04/05 → 07 (quests read sim state):** Quests are generated from citizen needs (unmet needs → "fetch food" quests) and economic state (trade shortages → "run cargo to the other city" quests).
8. **01 contains everything:** World & Cities is the container — the void, the floating islands, the build bounds, the travel. Every other pillar runs inside it.
9. **02 is the civic unit:** Factions define the city-state. Citizens, economy, social, quests all scope to a faction. The daily vote is per-faction (which god your city aligns with / empowers).

## Status

- **Built:** [Factions](./02-factions.md) (implementation spec: [2026-07-22-faction-gui-visual-identity-design](../superpowers/specs/2026-07-22-faction-gui-visual-identity-design.md))
- **Spec'd (structure only):** this overview + all pillar docs (vision-level; implementation specs TBD per pillar)
- **Open:** every pillar except Factions needs an implementation-level spec before it can be built

## Out of scope

- LLM cost/fallback strategy details — live in [03 LLM & Gods](./03-llm-and-gods.md)
- Mayor election, skilltree — deferred in the faction spec; not pillars in this structure
- Custom dimension beyond the void world — the void world is the world, not a dimension you travel to

## Cross-pillar open questions

These span multiple pillars and are tracked here rather than in a single pillar doc:

1. **Single-server vs multi-server:** All city-states on one server (citizens + LLM agents on one box), or sharded? Affects 03's runtime and 05's inter-city trade.
2. **Factions spec relocation:** Should the existing `2026-07-22-faction-gui-visual-identity-design.md` eventually move into `docs/vision/` as `02-factions-implementation.md`, or stay where it is and just be linked? Affects only navigation, not design.
````

- [ ] **Step 3: Verify file exists and links are valid**

Run: `test -f docs/vision/00-overview.md && echo OK`
Expected: `OK`

Manually verify the relative links point to files that will exist after all tasks complete: `./01-world-and-cities.md` through `./07-quests.md`, and `../superpowers/specs/2026-07-22-faction-gui-visual-identity-design.md` (exists already).

- [ ] **Step 4: Commit**

```bash
git add docs/vision/00-overview.md
git commit -m "docs: add vision overview doc (00-overview)"
```

---

### Task 2: World & Cities (`01-world-and-cities.md`)

**Files:**
- Create: `docs/vision/01-world-and-cities.md`

**Interfaces:**
- Consumes: `00-overview.md` (links back to it)
- Produces: pillar doc referenced by all other pillars as the container

- [ ] **Step 1: Write `01-world-and-cities.md`**

Create `docs/vision/01-world-and-cities.md` with this content:

````markdown
# World & Cities

## Purpose

The container everything sits in. The void world is the default world; each faction's city-state floats in it. Build is restricted to inside a city's bounds so the void stays clean between cities. Sky travel connects the city-states.

## Scope

**In:** world gen/config for the void world, city bounds definition + enforcement, inter-city travel.

**Out:** city interiors (handled by [Citizens](./04-citizens.md) and [Economy](./05-economy.md)), faction identity ([02 Factions](./02-factions.md)).

## Dependencies

- **[02 Factions](./02-factions.md)** — the city-state is the faction's home; bounds are defined per-faction.
- NeoForge worldgen + chunk generator APIs (1.21.1) — to be spec'd in this pillar's implementation design.
- NeoForge block-place/break events — for build bounds enforcement.

## Design

The void world is the default world (not a custom dimension players travel to). Each faction's city-state is a floating island in the void. Building outside a city's bounds is blocked — no sprawl across the void between cities. Travel between city-states happens through the sky.

The high-level design, as understood from the vision:

- **World gen:** the world is void (empty) by default. Floating islands are placed as faction city-sites. Whether this is a custom chunk generator or a void superflat preset + custom structures is an open question (see below).
- **City bounds:** each city-state has a defined area where building is allowed. Outside that area, block place/break is denied. How bounds are defined (radius from the Temple, polygon, claimed-chunk set) is an open question.
- **Sky travel:** players move between city-states through the sky. The travel method (airships, elytra-only, teleportation, custom vehicle) is an open question affecting the feel of the world.

A full implementation-level design doc for this pillar is TBD.

## Integration

This pillar is the **container** — every other pillar runs inside it. See [00 Overview](./00-overview.md), arrow 8: "World & Cities contains everything." Specifically:

- [02 Factions](./02-factions.md) — each faction's city-state floats in this world; bounds are tied to faction identity.
- [04 Citizens](./04-citizens.md) — citizens live inside the city bounds.
- [05 Economy](./05-economy.md) — inter-city trade depends on sky travel between city-states.
- [07 Quests](./07-quests.md) — escort/caravan quests (like the day-in-the-life example) depend on sky travel.

## Open questions

1. **World gen:** Is the void world a custom chunk generator, or the vanilla overworld with a void superflat preset + custom structures for the floating islands? *(from spec section 10, Q1)*
2. **Build bounds enforcement:** Block-place/break event interception with a check against the faction's city bounds — but how are bounds defined (a radius from the Temple? a polygon? a claimed-chunk set)? *(from spec section 10, Q2)*
3. **Sky travel:** Airships, elytra-only, teleportation, or a custom vehicle? *(from spec section 10, Q3)*
````

- [ ] **Step 2: Verify file exists**

Run: `test -f docs/vision/01-world-and-cities.md && echo OK`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add docs/vision/01-world-and-cities.md
git commit -m "docs: add World & Cities pillar doc (01)"
```

---

### Task 3: Factions (`02-factions.md`)

**Files:**
- Create: `docs/vision/02-factions.md`

**Interfaces:**
- Consumes: `00-overview.md`, the existing implementation spec at `../superpowers/specs/2026-07-22-faction-gui-visual-identity-design.md`
- Produces: pillar doc referenced by all other pillars as the civic unit

- [ ] **Step 1: Write `02-factions.md`**

Create `docs/vision/02-factions.md` with this content:

````markdown
# Factions

## Purpose

Who you belong to and your city-state's identity. Already built (create/join/color/scoreboard). This doc links the existing implementation spec and adds vision-level context: factions are the civic unit the whole simulation runs on.

## Scope

**In:** faction create/join/identity/color.

**Out:** intra-faction roles (deferred), faction diplomacy (deferred).

## Dependencies

- Implementation-level design: [2026-07-22-faction-gui-visual-identity-design](../superpowers/specs/2026-07-22-faction-gui-visual-identity-design.md) — the existing spec covering GUI, color, scoreboard team sync. This doc does not duplicate it.
- **[01 World & Cities](./01-world-and-cities.md)** — each faction has a floating city-state in the void world.
- **[03 LLM & Gods](./03-llm-and-gods.md)** — factions align with a god from the pantheon.

## Design

Factions are the civic unit. Everything in the simulation — citizens, economy, social, quests — is scoped to a faction. The daily vote is per-faction: which god your city aligns with and empowers.

The implementation (faction create/join via Temple GUI, color identity, scoreboard team sync) is already designed and built. See the existing implementation spec linked above. This vision-level doc exists to make the faction pillar's *role in the whole vision* explicit: factions are not just a player-identity system, they are the container for the civic/economic/social life that the rest of the pillars populate.

**Vision-level context not in the implementation spec:**

- A faction's city-state is its physical home in the void world ([01](./01-world-and-cities.md)).
- A faction aligns with one god from the fixed pantheon ([03](./03-llm-and-gods.md)).
- A faction's citizens ([04](./04-citizens.md)), economy ([05](./05-economy.md)), and social fabric ([06](./06-social.md)) all scope to that faction.
- The daily vote that empowers a god is a per-faction action.

## Integration

Factions are the **civic unit** the simulation runs on. See [00 Overview](./00-overview.md), arrow 9: "Factions define the city-state. Citizens, economy, social, quests all scope to a faction." Specifically:

- [01 World & Cities](./01-world-and-cities.md) — the faction's city-state floats in the void; bounds are tied to the faction.
- [03 LLM & Gods](./03-llm-and-gods.md) — the faction aligns with a god; the daily vote is per-faction.
- [04 Citizens](./04-citizens.md) — citizens belong to a faction's city.
- [05 Economy](./05-economy.md) — production/needs/trade are per-faction.
- [06 Social](./06-social.md) — relationships and reproduction are within a faction's citizen pool.
- [07 Quests](./07-quests.md) — quests are generated per-faction from that faction's needs and faith.

## Open questions

1. **Factions spec relocation:** Should the existing `2026-07-22-faction-gui-visual-identity-design.md` eventually move into `docs/vision/` as `02-factions-implementation.md`, or stay where it is and just be linked? *(from spec section 10, Q8 — also tracked in 00-overview)*
````

- [ ] **Step 2: Verify file exists and link target exists**

Run: `test -f docs/vision/02-factions.md && test -f docs/superpowers/specs/2026-07-22-faction-gui-visual-identity-design.md && echo OK`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add docs/vision/02-factions.md
git commit -m "docs: add Factions pillar doc (02), links existing implementation spec"
```

---

### Task 4: LLM & Gods (`03-llm-and-gods.md`)

**Files:**
- Create: `docs/vision/03-llm-and-gods.md`

**Interfaces:**
- Consumes: `00-overview.md`
- Produces: pillar doc referenced by 04, 05, 07 (gods steer citizens/economy, narrator+faith spawn quests)

- [ ] **Step 1: Write `03-llm-and-gods.md`**

Create `docs/vision/03-llm-and-gods.md` with this content:

````markdown
# LLM & Gods

## Purpose

The purpose layer. A fixed pantheon of named gods, each with a domain. Factions align with a god; a daily vote empowers one; the winning god's buffs steer the city that day. Each god is an LLM agent that speaks, reacts, and rules by mood. A world-narrator LLM watches the whole world and generates events, quests, crises. The LLM runtime (model choice, prompting, tool-calling, state, rate limits, fallbacks) lives here too.

## Scope

**In:** pantheon definition, alignment, daily vote, god buffs, god-as-agent, world-narrator, LLM runtime/engineering.

**Out:** the systems gods steer (those live in their own docs — [04 Citizens](./04-citizens.md), [05 Economy](./05-economy.md), [07 Quests](./07-quests.md)).

## Dependencies

- **[02 Factions](./02-factions.md)** — factions align with a god; the daily vote is per-faction.
- **[04 Citizens](./04-citizens.md)** — god buffs are applied as modifiers to citizen behavior.
- **[05 Economy](./05-economy.md)** — god buffs modify production rates and needs.
- **[07 Quests](./07-quests.md)** — the narrator generates events/crises that become quests; faith directives seed quests.
- External: an LLM provider/model (choice TBD — see open questions).

## Design

Two LLM-driven layers, both living in this pillar:

**1. The pantheon (in-world faith system):**

- A **fixed pantheon of named gods**, each with a domain (e.g. sun, void, harvest, war). Gods persist forever — they're the cast.
- Factions align with one god. A **daily vote** per faction determines which god is empowered that day.
- The winning god's **buffs** steer the city: a harvest god boosts food output; a war god raises military needs. Buffs are applied as modifiers to citizens and economy.
- Gods are characters players interact with at the temple.

**2. The LLM agents (technical runtime):**

- **Each god is an LLM agent** with persistent state (mood, memory of offerings, relationship with the faction). The god speaks to its followers in its own words, reacts to votes and offerings, and adjusts buffs based on mood and events.
- A **world-narrator LLM** watches the whole world state and generates events, quests, and crises across all factions — the hand that shapes the story.
- The LLM runtime (model choice, prompting strategy, tool-calling for world interaction, persistent state, rate limits, fallbacks when the LLM is unavailable or too slow) is part of this pillar.

A full implementation-level design doc for this pillar is TBD.

## Integration

This pillar is the **purpose layer** — it steers the other pillars via buffs and generates quests via the narrator. See [00 Overview](./00-overview.md), arrows 1-3:

1. **03 → 04 (gods steer citizens):** The winning god's buffs change citizen behavior for the day — productivity, needs, moods. Citizens (via the Minecolonies bridge) read these as modifiers.
2. **03 → 05 (gods steer economy):** God buffs modify production rates and needs. Economy applies these as multipliers.
3. **03 → 07 (narrator + faith spawn quests):** The world-narrator LLM generates events/crises that become quests; faith directives (god commands) also seed quests. Quests read these as generation sources.

## Open questions

1. **Pantheon size:** How many gods? 6? 12? Affects the LLM cost (each god is an agent with persistent state). *(from spec section 10, Q5)*
2. **LLM cost/fallback:** What happens when the LLM is unavailable or too slow — do gods go silent, fall back to a scripted buff table, or degrade gracefully? *(from spec section 10, Q6)*
3. **Single-server vs multi-server:** All city-states + LLM agents on one server, or sharded? *(from spec section 10, Q7 — also tracked in 00-overview)*
````

- [ ] **Step 2: Verify file exists**

Run: `test -f docs/vision/03-llm-and-gods.md && echo OK`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add docs/vision/03-llm-and-gods.md
git commit -m "docs: add LLM & Gods pillar doc (03)"
```

---

### Task 5: Citizens (`04-citizens.md`)

**Files:**
- Create: `docs/vision/04-citizens.md`

**Interfaces:**
- Consumes: `00-overview.md`
- Produces: pillar doc referenced by 05, 06, 07 (citizen state drives economy, is the social substrate, and is read by quests)

- [ ] **Step 1: Write `04-citizens.md`**

Create `docs/vision/04-citizens.md` with this content:

````markdown
# Citizens

## Purpose

The living population, via Minecolonies integration. Citizens live, eat, work, produce, need, reproduce. This pillar is the bridge: how Pantheon hooks into Minecolonies' citizen sim and exposes citizen state to the rest of the mod.

## Scope

**In:** Minecolonies dependency + bridge, Pantheon hooks into citizen lifecycle, citizen state exposed to other pillars.

**Out:** the sim itself (that's Minecolonies), citizen dialogue (that's [03 LLM & Gods](./03-llm-and-gods.md)'s LLM, not here).

## Dependencies

- **Minecolonies** (external mod) — depended on directly. Reuse their citizen simulation, building system, needs. Pantheon adds the faith layer + faction/city-state wrapper on top.
- **[02 Factions](./02-factions.md)** — citizens belong to a faction's city.
- **[03 LLM & Gods](./03-llm-and-gods.md)** — god buffs are applied as modifiers to citizen behavior.
- **[05 Economy](./05-economy.md)** — economy reads citizen state (jobs, numbers, health) from this bridge.
- **[06 Social](./06-social.md)** — social reads citizen state and writes relationship/reproduction state back.

## Design

Citizens are Minecolonies-style: they live, eat, work, produce, need, reproduce. Rather than build a citizen simulation from scratch, Pantheon depends on Minecolonies directly and bridges into it.

The high-level design, as understood from the vision:

- **Minecolonies dependency:** Pantheon depends on Minecolonies (jit pack or jar). Their citizen sim — jobs, needs, happiness, reproduction — is reused as-is.
- **Pantheon bridge:** hooks into Minecolonies' citizen lifecycle to expose citizen state (population, jobs, health, needs) to the rest of the mod.
- **Faith-awareness:** god buffs from [03](./03-llm-and-gods.md) are applied as modifiers to citizen behavior — productivity, needs, moods. Whether we use Minecolonies' systems as-is and layer faith on top, or fork/patch their citizen behavior to be faith-aware, is an open question.

Citizen **dialogue** (citizens speaking, reacting, conversing with players in natural language) is not part of this pillar — that's the LLM layer in [03](./03-llm-and-gods.md).

A full implementation-level design doc for this pillar is TBD.

## Integration

This pillar is the **linchpin** — economy, social, and quests all read citizen state from here. See [00 Overview](./00-overview.md), arrows 1, 4, 5, 7:

1. **03 → 04 (gods steer citizens):** God buffs change citizen behavior for the day. Citizens read these as modifiers via the bridge.
4. **04 → 05 (citizens drive economy):** Citizens are the labor force. Their jobs, numbers, and health determine production and consumption. Economy reads citizen state from the Minecolonies bridge.
5. **04 → 06 (citizens are the social substrate):** Relationships form between citizens; the citizen pool is what reproduces. Social reads citizen state and writes relationship/repro state back.
7. **04/05 → 07 (quests read sim state):** Quests are generated from citizen needs (unmet needs → "fetch food" quests).

## Open questions

1. **Minecolonies integration depth:** Do we use Minecolonies' town hall/happiness/needs system as-is and layer faith on top, or do we fork/patch their citizen behavior to be faith-aware? *(from spec section 10, Q4)*
````

- [ ] **Step 2: Verify file exists**

Run: `test -f docs/vision/04-citizens.md && echo OK`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add docs/vision/04-citizens.md
git commit -m "docs: add Citizens pillar doc (04), Minecolonies integration"
```

---

### Task 6: Economy (`05-economy.md`)

**Files:**
- Create: `docs/vision/05-economy.md`

**Interfaces:**
- Consumes: `00-overview.md`
- Produces: pillar doc referenced by 07 (quests read economic state)

- [ ] **Step 1: Write `05-economy.md`**

Create `docs/vision/05-economy.md` with this content:

````markdown
# Economy

## Purpose

The circulation that makes the city feel alive. Jobs, production, needs fulfillment, inter-city trade. Reads citizen state from Minecolonies ([04](./04-citizens.md)); god buffs ([03](./03-llm-and-gods.md)) steer what's produced and what's needed.

## Scope

**In:** production/consumption loops, jobs as economic roles, needs fulfillment, inter-city trade.

**Out:** jobs-as-quests ([07 Quests](./07-quests.md)), NPC relationships ([06 Social](./06-social.md)).

## Dependencies

- **[04 Citizens](./04-citizens.md)** — citizens are the labor force; economy reads citizen state (jobs, numbers, health) from the Minecolonies bridge.
- **[03 LLM & Gods](./03-llm-and-gods.md)** — god buffs modify production rates and needs.
- **[06 Social](./06-social.md)** — reproduction feeds new citizens into the labor force, changing production and needs.
- **[01 World & Cities](./01-world-and-cities.md)** — inter-city trade depends on sky travel between city-states.

## Design

The economy is the circulation that makes the city feel alive: citizens produce, citizens need, trade flows between cities.

The high-level design, as understood from the vision:

- **Jobs as economic roles:** citizens work jobs (baker, smith, farmer, etc.) that produce goods. Jobs come from Minecolonies' citizen sim; Pantheon reads them as economic roles.
- **Production/consumption loops:** citizens produce goods and consume goods (they eat, they need tools, they need housing). Unmet needs drive quest generation ([07](./07-quests.md)).
- **God buffs as multipliers:** the winning god's buffs ([03](./03-llm-and-gods.md)) modify production rates and needs — a harvest god boosts food output; a war god raises military needs.
- **Inter-city trade:** city-states trade with each other. Trade shortages become quests ("run cargo to the other city"). Trade depends on sky travel ([01](./01-world-and-cities.md)).

A full implementation-level design doc for this pillar is TBD.

## Integration

This pillar is **steered by gods and driven by citizens.** See [00 Overview](./00-overview.md), arrows 2, 4, 6, 7:

2. **03 → 05 (gods steer economy):** God buffs modify production rates and needs. Economy applies these as multipliers.
4. **04 → 05 (citizens drive economy):** Citizens are the labor force. Their jobs, numbers, and health determine production and consumption.
6. **06 → 05 (reproduction feeds economy):** New citizens → more labor → more production and more needs. Economy reads population size from social/repro state.
7. **04/05 → 07 (quests read sim state):** Economic state (trade shortages) generates quests.

## Open questions

1. **Minecolonies integration depth:** Do we use Minecolonies' economy/needs system as-is and layer faith on top, or fork/patch? *(from spec section 10, Q4 — shared with 04 Citizens)*
````

- [ ] **Step 2: Verify file exists**

Run: `test -f docs/vision/05-economy.md && echo OK`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add docs/vision/05-economy.md
git commit -m "docs: add Economy pillar doc (05)"
```

---

### Task 7: Social (`06-social.md`)

**Files:**
- Create: `docs/vision/06-social.md`

**Interfaces:**
- Consumes: `00-overview.md`
- Produces: pillar doc referenced by 05 (reproduction feeds economy)

- [ ] **Step 1: Write `06-social.md`**

Create `docs/vision/06-social.md` with this content:

````markdown
# Social

## Purpose

The social fabric. NPC↔NPC relationships, reproduction, player↔NPC social interaction. Reads citizen state from Minecolonies ([04](./04-citizens.md)); relationships feed back into reproduction and citizen needs.

## Scope

**In:** relationship formation, reproduction, player↔NPC social.

**Out:** economic production ([05 Economy](./05-economy.md)), faith ([03 LLM & Gods](./03-llm-and-gods.md)).

## Dependencies

- **[04 Citizens](./04-citizens.md)** — citizens are the social substrate; relationships form between citizens; the citizen pool is what reproduces. Social reads citizen state from the Minecolonies bridge.
- **[05 Economy](./05-economy.md)** — reproduction feeds new citizens into the labor force, changing economy.
- **[03 LLM & Gods](./03-llm-and-gods.md)** — player↔NPC social interaction may involve LLM-driven dialogue (citizens speaking, reacting). The dialogue engine lives in 03; this pillar defines the social *structure* (relationships, reproduction).

## Design

The social fabric of the city: citizens form relationships, reproduce, and interact socially with players.

The high-level design, as understood from the vision:

- **NPC↔NPC relationships:** citizens form relationships with each other. Relationship state is tracked per-citizen.
- **Reproduction:** citizens reproduce — new citizens join the city's population. Reproduction feeds back into the economy ([05](./05-economy.md)) as new labor and new needs.
- **Player↔NPC social:** players interact socially with NPCs. The "alive" feeling comes from NPCs having relationships, families, and lives the player can see and participate in. NPC *dialogue* (natural language) is the LLM layer in [03](./03-llm-and-gods.md); this pillar defines the social *structure* the dialogue plays out over.

A full implementation-level design doc for this pillar is TBD.

## Integration

This pillar is the **social substrate over citizens.** See [00 Overview](./00-overview.md), arrows 5, 6:

5. **04 → 06 (citizens are the social substrate):** Relationships form between citizens; the citizen pool is what reproduces. Social reads citizen state and writes relationship/repro state back.
6. **06 → 05 (reproduction feeds economy):** New citizens → more labor → more production and more needs. Economy reads population size from social/repro state.

## Open questions

*(No pillar-specific open questions from the spec. Cross-pillar questions about Minecolonies integration depth (Q4) and single-vs-multi-server (Q7) are tracked in [00 Overview](./00-overview.md) and [04 Citizens](./04-citizens.md).)*
````

- [ ] **Step 2: Verify file exists**

Run: `test -f docs/vision/06-social.md && echo OK`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add docs/vision/06-social.md
git commit -m "docs: add Social pillar doc (06)"
```

---

### Task 8: Quests (`07-quests.md`)

**Files:**
- Create: `docs/vision/07-quests.md`

**Interfaces:**
- Consumes: `00-overview.md`
- Produces: pillar doc referenced by nothing downstream (quests are the terminal layer)

- [ ] **Step 1: Write `07-quests.md`**

Create `docs/vision/07-quests.md` with this content:

````markdown
# Quests

## Purpose

The directed layer on top of the simulation. Quests are generated from faith directives ([03](./03-llm-and-gods.md)) + citizen needs ([04](./04-citizens.md) / [05](./05-economy.md)) + narrator events ([03](./03-llm-and-gods.md)). They give players purposeful objectives that tie the pillars together.

## Scope

**In:** quest generation, quest completion, quest state.

**Out:** the sim state quests read (that's [04 Citizens](./04-citizens.md) / [05 Economy](./05-economy.md)), the faith directives (that's [03 LLM & Gods](./03-llm-and-gods.md)).

## Dependencies

- **[03 LLM & Gods](./03-llm-and-gods.md)** — the world-narrator generates events/crises that become quests; faith directives (god commands) also seed quests.
- **[04 Citizens](./04-citizens.md)** — unmet citizen needs generate quests (e.g. "fetch food").
- **[05 Economy](./05-economy.md)** — economic state (trade shortages) generates quests (e.g. "run cargo to the other city").
- **[01 World & Cities](./01-world-and-cities.md)** — escort/caravan quests depend on sky travel between city-states.

## Design

Quests are the directed layer that gives players purposeful objectives. They are generated from three sources, tying the pillars together:

The high-level design, as understood from the vision:

- **Faith directives:** the empowered god ([03](./03-llm-and-gods.md)) issues commands that become quests — "build a shrine," "defend the caravan," "convert the unbelievers."
- **Citizen needs:** unmet citizen needs ([04](./04-citizens.md)) generate quests — citizens are hungry → "fetch food"; citizens need tools → "supply the smith."
- **Narrator events:** the world-narrator LLM ([03](./03-llm-and-gods.md)) generates events and crises across the whole world that become quests — a merchant caravan stranded in the void → "escort the caravan."
- **Quest state:** quests are tracked per-faction; completion feeds back into the economy ([05](./05-economy.md)) and the god's mood ([03](./03-llm-and-gods.md)).

The day-in-the-life narrative in [00 Overview](./00-overview.md) shows all three sources firing: the Sun Lord's harvest buff (faith), citizens eating well (needs), and the stranded caravan (narrator event).

A full implementation-level design doc for this pillar is TBD.

## Integration

This pillar is the **terminal layer** — it reads from faith, citizens, and economy, and feeds completion back. See [00 Overview](./00-overview.md), arrows 3, 7:

3. **03 → 07 (narrator + faith spawn quests):** The world-narrator LLM generates events/crises that become quests; faith directives (god commands) also seed quests. Quests read these as generation sources.
7. **04/05 → 07 (quests read sim state):** Quests are generated from citizen needs (unmet needs → "fetch food" quests) and economic state (trade shortages → "run cargo to the other city" quests).

## Open questions

*(No pillar-specific open questions from the spec. The LLM cost/fallback question (Q6) and pantheon size (Q5) that affect quest generation volume are tracked in [03 LLM & Gods](./03-llm-and-gods.md).)*
````

- [ ] **Step 2: Verify file exists**

Run: `test -f docs/vision/07-quests.md && echo OK`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add docs/vision/07-quests.md
git commit -m "docs: add Quests pillar doc (07)"
```

---

### Task 9: Final verification

**Files:**
- Verify: all 8 files in `docs/vision/`

- [ ] **Step 1: Verify all 8 files exist**

Run:
```bash
ls docs/vision/
```
Expected output (8 files):
```
00-overview.md
01-world-and-cities.md
02-factions.md
03-llm-and-gods.md
04-citizens.md
05-economy.md
06-social.md
07-quests.md
```

- [ ] **Step 2: Verify every pillar doc has the shared template sections**

Run:
```bash
for f in docs/vision/0[1-7]*.md; do
  echo "=== $f ==="
  grep -E '^## (Purpose|Scope|Dependencies|Design|Integration|Open questions)' "$f"
done
```
Expected: each of the 7 pillar docs (01-07) shows exactly 6 `##` headers: Purpose, Scope, Dependencies, Design, Integration, Open questions — in that order.

- [ ] **Step 3: Verify the overview doc has its own shape (not the pillar template)**

Run:
```bash
grep -E '^## ' docs/vision/00-overview.md
```
Expected: headers include `The pitch`, `The pillars`, `A day in the life`, `How the pillars interlock`, `Status`, `Out of scope`, `Cross-pillar open questions` — and do NOT include `Purpose`/`Scope`/`Dependencies`/`Design`/`Integration`/`Open questions` (the pillar template).

- [ ] **Step 4: Verify all relative links resolve**

Run:
```bash
grep -oE '\]\(\./[^)]+\)' docs/vision/*.md | sed 's/.*](//;s/)//' | sort -u | while read link; do
  base=$(echo "$link" | sed 's#/[^/]*$##')
  file=$(echo "$link" | sed 's#^.*/##')
  dir="docs/vision/$base"
  [ -f "$dir/$file" ] && echo "OK: $link" || echo "BROKEN: $link"
done
```
Expected: all `./0X-*.md` links resolve (OK), and the `../superpowers/specs/2026-07-22-faction-gui-visual-identity-design.md` link from 02 resolves (OK).

- [ ] **Step 5: Verify no placeholder strings**

Run:
```bash
grep -rEn 'TBD|TODO|placeholder|to be expanded|fill in' docs/vision/*.md || echo "No placeholders found"
```
Expected: `No placeholders found` — note that "TBD" appearing in the phrase "A full implementation-level design doc for this pillar is TBD." is acceptable (it's an honest status, not a placeholder for content that should be in this doc). If the grep flags those, verify by eye that they are the honest-status usage, not missing content.

- [ ] **Step 6: Final commit (if any verification fixes were needed)**

If Steps 1-5 all pass with no fixes needed, this step is a no-op (all files already committed in Tasks 1-8). If fixes were needed, commit them:

```bash
git add docs/vision/
git commit -m "docs: fix vision docs verification issues"
```