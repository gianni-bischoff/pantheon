# Pantheon Vision — Docs Folder Structure

**Status:** Draft (pending user review)
**Date:** 2026-07-23

---

## 1. Goal

Turn the broad Pantheon vision ("a living world the player feels integrated into") into a graspable set of design docs, one per pillar, so the mod's direction is readable and each subsystem can be spec'd and built independently.

This document defines the **folder structure**, the **shared template** every pillar doc follows, and the **interlock** between pillars. It does not design the pillars themselves — each pillar doc does that.

## 2. The vision in one paragraph

A void world is the default. Each faction has a floating city-state, and building is restricted to your city's bounds so nobody sprawls across the void. Inside a city, **Minecolonies-style citizens live full lives** — they eat, work, produce, need, reproduce, form relationships. **Players are integrated into that civic, economic, and social life**, not spectators above it. A **fixed pantheon of gods** gives those actions purpose: factions align with a god, a daily vote empowers one, and the winning god's buffs steer what the city does that day. Gods are **LLM agents** that speak, react, and rule by mood; a **world-narrator LLM** generates events, quests, and crises across the whole world. The faith layer is the *why* behind the civic/economic/social engine — not the engine itself.

## 3. Out of scope for this document

- The detailed design of any individual pillar (that lives in its own doc).
- Implementation plans (those come from the writing-plans skill after a pillar spec is approved).
- Mayor election, skilltree — listed as deferred in the faction spec; not pillars in this structure.
- Custom dimension beyond the void world — the void world is the world, not a dimension you travel to.

## 4. Folder structure

```
docs/vision/
├── 00-overview.md           ← entry point: pitch, pillars, day-in-the-life, interlock, status
├── 01-world-and-cities.md   ← void world, floating city-states, build bounds, sky travel
├── 02-factions.md           ← links existing faction spec + vision-level context
├── 03-llm-and-gods.md       ← fixed pantheon, domains, daily vote, buffs, god-as-LLM-agent, world-narrator, LLM runtime
├── 04-citizens.md           ← Minecolonies integration: live/eat/work/produce/need/reproduce
├── 05-economy.md            ← jobs, production, needs fulfillment, inter-city trade
├── 06-social.md             ← NPC↔NPC relationships, reproduction, player↔NPC social
└── 07-quests.md             ← purposeful objectives tied to faith + city needs
```

Eight docs total. Ordering is logical, not by-priority: container (01) → identity (02) → purpose (03) → living population (04) → circulation (05) → fabric (06) → direction (07). Build order is decided per-pillar in implementation plans, not by filename.

### Placement note

The existing faction spec at `docs/superpowers/specs/2026-07-22-faction-gui-visual-identity-design.md` is an implementation-level design (GUI, color, scoreboard). The new `02-factions.md` is a **vision-level** doc — it links the existing spec rather than duplicating it, and adds only the high-level "what factions are for in the vision" context that wasn't in the original. The existing spec stays where it is; it is not moved.

## 5. Shared template for pillar docs

Every pillar doc (`01` through `07`) follows this structure so they're graspable at a glance and consistent to navigate:

```
# <Pillar name>

## Purpose          ← one paragraph: what this pillar is for in the vision
## Scope            ← what's in, what's explicitly out (edges)
## Dependencies     ← other pillars it needs, external mods (e.g. Minecolonies)
## Design           ← the meat: mechanics, data, systems
## Integration      ← how it connects to other pillars (links back to 00-overview)
## Open questions   ← unresolved decisions
```

`00-overview.md` does **not** follow this template — it has its own shape (see section 6).

## 6. Overview doc shape (`00-overview.md`)

The entry point. One page that makes the whole vision graspable and shows how the pillars interlock. Sections:

1. **The pitch** — the one-paragraph vision (section 2 of this doc, lightly expanded).
2. **The pillars** — one line per pillar with a link to its doc:
   - World & Cities — the container
   - Factions — who you belong to (built)
   - LLM & Gods — the purpose layer (fixed pantheon, daily vote, LLM agents + narrator)
   - Citizens — the living population (Minecolonies)
   - Economy — production, jobs, needs, trade
   - Social — relationships, reproduction, player↔NPC
   - Quests — purposeful objectives
3. **Day-in-the-life narrative** — walk through one in-game day from a player's POV, showing all pillars firing and interlocking. This makes the interlock *felt*, not just diagrammed. (The user's own description is the seed: go online, do your job, build on your home, trade, interact socially, do quests, explore the skies, interact with NPCs, feel integrated.)
4. **Interlock diagram** — text/ASCII map of how pillars steer and read each other, with a short paragraph per arrow so relationships are explicit. See section 7 of this doc for the arrows.
5. **Status** — which pillars are built (Factions), which are spec'd, which are open.
6. **Out of scope** — LLM cost/fallback strategy details (live in 03), mayor election, skilltree, custom dimension beyond void. Edges of the vision.

The overview is the **only** doc that describes cross-pillar interactions in depth. Each pillar doc's `Integration` section links back to the overview for context and gives only the arrows relevant to that pillar.

## 7. Pillar purposes and scopes

| # | Doc | Purpose (one paragraph) | Scope (in / out) |
|---|---|---|---|
| 01 | **World & Cities** | The container everything sits in. The void world is the default world; each faction's city-state floats in it. Build is restricted to inside a city's bounds so the void stays clean between cities. Sky travel connects the city-states. | **In:** world gen/config for void, city bounds definition + enforcement, inter-city travel. **Out:** city interiors (Citizens/Economy), faction identity (02). |
| 02 | **Factions** | Who you belong to and your city-state's identity. Already built (create/join/color/scoreboard). This doc links the existing implementation spec and adds vision-level context: factions are the civic unit the whole simulation runs on. | **In:** faction create/join/identity/color. **Out:** intra-faction roles (deferred), faction diplomacy (deferred). |
| 03 | **LLM & Gods** | The purpose layer. A fixed pantheon of named gods, each with a domain. Factions align with a god; a daily vote empowers one; the winning god's buffs steer the city that day. Each god is an LLM agent that speaks, reacts, and rules by mood. A world-narrator LLM watches the whole world and generates events, quests, crises. The LLM runtime (model choice, prompting, tool-calling, state, rate limits, fallbacks) lives here too. | **In:** pantheon definition, alignment, daily vote, god buffs, god-as-agent, world-narrator, LLM runtime/engineering. **Out:** the systems gods steer (those live in their own docs). |
| 04 | **Citizens** | The living population, via Minecolonies integration. Citizens live, eat, work, produce, need, reproduce. This pillar is the bridge: how Pantheon hooks into Minecolonies' citizen sim and exposes citizen state to the rest of the mod. | **In:** Minecolonies dependency + bridge, Pantheon hooks into citizen lifecycle, citizen state exposed to other pillars. **Out:** the sim itself (that's Minecolonies), citizen dialogue (03's LLM, not here). |
| 05 | **Economy** | The circulation that makes the city feel alive. Jobs, production, needs fulfillment, inter-city trade. Reads citizen state from Minecolonies (04); god buffs (03) steer what's produced and what's needed. | **In:** production/consumption loops, jobs as economic roles, needs fulfillment, inter-city trade. **Out:** jobs-as-quests (07), NPC relationships (06). |
| 06 | **Social** | The social fabric. NPC↔NPC relationships, reproduction, player↔NPC social interaction. Reads citizen state from Minecolonies (04); relationships feed back into reproduction and citizen needs. | **In:** relationship formation, reproduction, player↔NPC social. **Out:** economic production (05), faith (03). |
| 07 | **Quests** | The directed layer on top of the simulation. Quests are generated from faith directives (03) + citizen needs (04/05) + narrator events (03). They give players purposeful objectives that tie the pillars together. | **In:** quest generation, quest completion, quest state. **Out:** the sim state quests read (that's 04/05), the faith directives (that's 03). |

## 8. Interlock — the arrows

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

## 9. Day-in-the-life narrative (seed for 00-overview.md)

A player's typical day, from the user's description, annotated with which pillar fires:

> You log in. You're in **your faction's** city, floating in the **void**. The morning bell rings — **citizens** are heading to their jobs; the baker is already producing bread, the smith is smelting. You check the **temple**: today's **vote** is open, and your god — the Sun Lord — is trailing. You cast your vote and drop an offering. The Sun Lord (an **LLM agent**) acknowledges you in its own words, mood shifted by the offering. The vote closes at noon: the Sun Lord wins, and his buff — **increased harvest yield** — settles over the city. **Citizens** work faster in the fields; the **economy** ticks up. A **narrator event** fires: a merchant caravan from another city-state is stranded in the void. A **quest** appears: escort the caravan. You gear up, cross the **sky** to the other island, fight off the void wraiths, and bring the caravan home. The **economy** rewards you; the Sun Lord is pleased; your **citizens** eat well tonight. You spend the evening **building** an extension to your home — inside the bounds, the build is allowed — and chatting with an **NPC** neighbor whose child was born last week (**social**: reproduction). You log off feeling like you live there.

The overview doc expands this into a fuller narrative and marks each pillar firing.

## 10. Open questions

1. **World gen:** Is the void world a custom chunk generator, or the vanilla overworld with a void superflat preset + custom structures for the floating islands? Affects how 01 is spec'd.
2. **Build bounds enforcement:** Block-place/break event interception with a check against the faction's city bounds — but how are bounds defined (a radius from the Temple? a polygon? a claimed-chunk set)? Affects 01.
3. **Sky travel:** Airships, elytra-only, teleportation, or a custom vehicle? Affects 01 and the feel of the world.
4. **Minecolonies integration depth:** Do we use Minecolonies' town hall/happiness/needs system as-is and layer faith on top, or do we fork/patch their citizen behavior to be faith-aware? Affects 04 (citizens) and 05 (economy).
5. **Pantheon size:** How many gods? 6? 12? Affects 03's LLM cost (each god is an agent with persistent state).
6. **LLM cost/fallback:** What happens when the LLM is unavailable or too slow — do gods go silent, fall back to a scripted buff table, or degrade gracefully? Affects 03's runtime design.
7. **Single-server vs multi-server:** All city-states on one server (citizens + LLM agents on one box), or sharded? Affects 03's runtime and 04's inter-city trade.
8. **Factions spec relocation:** Should the existing `2026-07-22-faction-gui-visual-identity-design.md` eventually move into `docs/vision/` as `02-factions-implementation.md`, or stay where it is and just be linked? Affects only navigation, not design.