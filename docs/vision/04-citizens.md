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
