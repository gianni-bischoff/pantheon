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