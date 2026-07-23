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