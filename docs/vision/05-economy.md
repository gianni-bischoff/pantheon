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
