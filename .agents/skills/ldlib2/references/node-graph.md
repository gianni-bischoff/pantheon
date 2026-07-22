# LDLib2 — Node Graph Toolkit

Package `com.lowdragmc.lowdraglib2.nodegraphtoolkit`. A visual node editor framework (graph definition + runtime + editor UI). Used by LDLib2's own editor and by mods that need a node-based logic/data editor (recipe graphs, behaviour trees, shader graphs, etc.).

> Authoritative docs: `https://low-drag-mc.github.io/LowDragMC-Doc/en/ldlib2/node-graph-toolkit/`. Start at the **Getting Started** page, then read the pages you need. This file is a map + the most-used entry points.

## Sub-packages

- `api/` — `GraphDefinition`, `Node`, `Port`, `Variable`, `Blackboard`, `TypeHandle`, graph context.
- `model/` — serializable graph model (`graph/`, `node/`, `wire/`, `variable/`, `group/`, `wiget/`, `constant/`).
- `gui/` — `GraphView` widget, node UI, port UI, blackboard UI, command system, item library (node picker), dependency tracking.
- `editor/` — editor integration (project resource for a graph file).

## Core concepts

- **Graph** — a `GraphDefinition` holding nodes, wires (port connections), variables, and a blackboard.
- **Node** — a `NodeDefinition` with input/output **Ports**. Nodes execute logic when their inputs are satisfied (data-flow) or when triggered (control-flow).
- **Port** — typed connection point. Type matching uses **Type Handles** (see below).
- **Variable / Blackboard** — named graph-level values; blackboard is the shared variable store. Variables are exposed as ports on a special node.
- **Type Handle** — a typed reference describing what a port accepts/produces; enables type-safe connections and conversion.
- **Subgraph** — a node that wraps another graph; enables reuse and hierarchy.
- **Context / Block nodes** — context nodes provide per-graph context (e.g. the current entity, world, recipe); block nodes group statements (like a scope).

## Typical workflow

1. Define a `GraphDefinition` (code or JSON resource) listing node types, variable types, and the blackboard schema.
2. Register node types via `@LDLRegister(registry = "yourmod:graph_nodes", name = "...")` on classes implementing `NodeDefinition` (LDLib2's auto-registry — see `references/setup.md`).
3. Load a graph at runtime from a resource (JSON) or build it in code.
4. Execute the graph via the graph runtime, or present the `GraphView` widget inside a `ModularUI` for in-game editing.

## Key reference pages

| Topic | Doc URL (under `en/ldlib2/node-graph-toolkit/`) |
|---|---|
| Getting Started | `getting-started.html` |
| Graph Definition | `graph-definition.html` |
| Nodes and Ports | `nodes-and-ports.html` |
| Variables and Blackboard | `variables-and-blackboard.html` |
| Type Handles | `type-handles.html` |
| GraphView (UI widget) | `graph-view.html` |
| Editor Resources | `editor-resources.html` |
| Subgraphs | `subgraphs.html` |
| Context and Block Nodes | `context-and-block-nodes.html` |
| Commands and Customization | `commands-and-customization.html` |
| Glossary | `glossary.html` |

## Runnable examples in the repo

`src/main/java/com/lowdragmc/lowdraglib2/test/noddegraphtoolkit/` — `TestPreviewNode`, `ContextBlockTest`, `EvAccessorFloatNode`, `EvCodecValueBNode`, `EvWithoutSerializationNode`, `GraphCommandPolicyTest`, plus `src/main/java/com/lowdragmc/lowdraglib2/test/ui/TestGraphToolkit.java` and `TestGraphView.java` for the UI side. Clone `https://github.com/Low-Drag-MC/LDLib2` (branch `1.21`) and read these when you need exact signatures — the node-graph API is the most volatile part of LDLib2.

## Gotchas

- Node registration uses `@LDLRegister` on a **client or both-sides** registry depending on whether the node is editor-only or also evaluated server-side. Pick `@LDLRegister` vs `@LDLRegisterClient` deliberately.
- Port type matching is driven by `TypeHandle`s; two ports with the same JVM type but different handles will not connect. Register/resolve handles explicitly.
- Subgraphs share the blackboard with their parent unless you scope it — check the Subgraphs doc before assuming variable isolation.
- The `GraphView` widget is a `UIElement`; embed it in a `ModularUI` like any other component (see `references/ui.md`).