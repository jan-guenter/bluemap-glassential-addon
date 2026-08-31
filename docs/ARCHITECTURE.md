# Architecture

## Shared source boundaries

The production JAR compiles two exact-pinned MIT source sets. Adapter API
`0.1.0-alpha.2` supplies only the BlueMap 5.23 runtime identity, registry,
resource-extension, and synthetic-dispatch helpers. Fusion Resource Models
`0.1.0-alpha.1` supplies only the five pure geometry/selector model sources.
The consumer keeps all Glassential-specific predicates, resource loading,
registration, activation, and rendering. Neither support-module JAR is a
runtime dependency, and no 5.22 render-core module is used.

## Activation

```text
BlueMap ResourcePack construction
  -> find the byte-exact Glassential Renewed 3.4.5 and Fusion 1.3.12 inputs
  -> validate the first-wins 204-path operator resource closure
  -> validate the separate 22-path Minecraft host ABI
  -> compile 93 bounded Fusion programs plus two one-way programs
  -> validate synthetic dispatch and registered state schemas
  -> activate glassential-fusion-3.4.5-1.3.12 process-wide
```

Structural resources are size/SHA-256 locked. The first physical resource
winner is claimed before reading, so an unreadable higher-priority pack entry
cannot fall through to a lower root. PNG bytes may differ only when decoded
dimensions match the exact catalog.

## Route and state boundary

The generated allowlist contains 31 Glassential IDs/453 states and 18 exact
propertyless vanilla glass-cube IDs. Its state schemas are:

- propertyless Fusion cubes;
- `lit` colorable cubes;
- four horizontal Boolean connections plus `waterlogged` for ordinary panes;
- those five pane properties plus `lit` for colorable panes;
- `type` plus `waterlogged` for the slab; and
- all six `opaque_face` directions for one-way glass.

Unknown IDs or invalid property maps use the original stock blockstate and
properties. The other 82 Glassential IDs remain outside dispatch.

## Model and dynamic-state interpretation

The active blockstate is evaluated by BlueMap, preserving multipart selection
and model/variant transforms. The bounded interpreter then evaluates only the
exact installed predicate/layout vocabulary. FULL, SIMPLE, and PIECED faces use
face-local neighbor frames; the five plain pane-edge textures bypass connected
sheet selection. Ordinary pane same-state comparisons include every property,
including `waterlogged` and `lit` where present.

Colorable routes read a 24-bit `Color` and Boolean `EmitLight` from the shared
Glassential colorable block-entity type. Only the pane templates have
tint-indexed faces, so the two panes apply `Color` while the two cube routes
remain untinted under exact Fusion 1.3.12. Ordinary new blocks save the mod's
white default, while a missing or malformed persisted `Color` is invalid; a
persisted light value inconsistent with `lit` is also invalid. Gameplay-only
`EmitRedstone`, `PassPlayer`, and `PassEntity` are not renderer behavior.

One-way routes read the `Mimic` block ID from their shared block-entity type.
Five faces use the installed glass program; `opaque_face` delegates to the
mimicked block's default-state stock model, material, and tint. Missing,
parse-invalid, or unregistered data normalizes to `minecraft:iron_block`, as
the exact block entity does; a valid one-way mimic is recursion guarded to the
configured base-glass face. Failures after a valid delegation target is chosen
trigger whole-block stock fallback.

## Failure policy

Tuple, resource, program, dispatch, or generated-texture failure makes the
whole route inactive. Invalid per-block state/NBT/resource observations reset
all partial triangles and map color, then invoke BlueMap's stock multipart path.
Capacity exceptions are never converted to fallback.

The add-on registers no blocks, items, entities, menus, commands, packets, or
required client resources. Removing its JAR and restarting must restore stock
BlueMap behavior; the disposable-host lifecycle exercised the separate
restart-scoped disable path on 2026-08-16, with physical-removal evidence
recorded in `docs/ROLLBACK.md`.
