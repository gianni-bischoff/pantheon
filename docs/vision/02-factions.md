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