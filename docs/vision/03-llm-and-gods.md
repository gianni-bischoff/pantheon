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