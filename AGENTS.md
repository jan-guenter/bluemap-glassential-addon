# Agent guide

This is the independent BlueMap add-on repository for the exact Glassential
Renewed 3.4.5 plus Fusion 1.3.12 tuple in All the Mons 1.2.0. Read the
workspace and portfolio guides, this README, `docs/ARCHITECTURE.md`,
`docs/PROVENANCE.md`, and `docs/RELEASING.md` before changing it.

## Boundaries

- Java 21, Minecraft 1.21.1, and BlueMap 5.22 backport commit
  `9be321df995a1103808621d529eb72773e719d4d`.
- Own only the generated 49-ID/471-state route: 31 `glassential:*` IDs with
  453 legal states plus the exact 18 propertyless vanilla glass-cube IDs.
- Leave the other 82 Glassential IDs/4,996 legal states, vanilla panes, and
  every other namespace on BlueMap's stock path.
- Register no `fusion:*` blocks and add no runtime provider dependency on
  another add-on.
- Bundle no Glassential/Fusion code, classes, JSON, PNG, metadata, or JARs.
  Interpret only byte-exact operator-installed resources with independently
  authored MIT code.
- Decode only `Color`, `EmitLight`, and `Mimic` needed for static rendering.
  Do not own or mutate gameplay state.
- Preserve stock rendering outside the exact route and atomically fall back on
  malformed state, color/light NBT, resource, or stock-delegation failures.
  One-way NBT follows the exact client rule: invalid/unregistered `Mimic`
  normalizes to iron and one-way recursion resolves to the base glass face.
  Propagate BlueMap capacity failures.
- Structural JSON and metadata are hash-locked. PNG pixel overrides are
  allowed only when exact dimensions remain unchanged.
- Treat pane color tint, untinted colorable cubes, light state, one-way mimic
  faces, and vertical/mixed pane behavior
  as release-blocking until exact-client observations pass.
- Keep identifiers under `bluemap_glassential`, Java under
  `io.github.janguenter.bluemap.glassential`, extension ID
  `bluemap_glassential:exact_profile`, renderer ID
  `bluemap_glassential:fusion_model`, and profile ID
  `glassential-fusion-3.4.5-1.3.12`.
- Do not create a remote, mutate a cluster, publish, tag, release, or touch
  production without the separate owner gate.

## Generated inputs

Run `tools/generate_profile.py` only with the exact artifacts pinned in README.
Run `gallery/generate.py` for the fixture. Generated profile/gallery files must
be reproducible and checked in; never hand-edit their TSV, JSON, mcfunction, or
checksum output.

## Validation

Use focused compilation/tests while implementing a coherent tranche. Before a
runtime candidate, run the full clean gate from README, inspect both produced
JARs, freeze the tree, and obtain an independent read-only audit. Record only
tests actually observed. Before presenting a BlueMap URL, open that exact URL
and perform the workspace-required lightweight visual sanity check.
