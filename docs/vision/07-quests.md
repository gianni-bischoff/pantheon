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
