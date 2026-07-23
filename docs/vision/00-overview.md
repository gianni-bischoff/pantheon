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
