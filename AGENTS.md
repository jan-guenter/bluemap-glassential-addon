# Agent guide

This is the independent BlueMap add-on repository for the exact Glassential
Renewed 3.4.5 plus Fusion 1.3.12 tuple in All the Mons 1.2.0. Read the
workspace and portfolio guides, this README, `docs/ARCHITECTURE.md`,
`docs/PROVENANCE.md`, and `docs/RELEASING.md` before changing it.

## Boundaries

- Java 21, Minecraft 1.21.1, and only BlueMap feature-backport commit
  `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac`, API commit
  `285c9a60eff3ac2b0cab308ce1058d1565be0971`.
- Compile the four Adapter API sources from gitlink
  `e81f08bc4bfbf02d810ec8949a019130e2e61634`, source tree
  `2f974c9bb2ba13888d69682f86f30f58922d30eb`, and the five Fusion model
  sources from released gitlink `3ddd5d39bb7cc8664c242aedd849a636316075c2`,
  source tree `6e85031ff2f0e7417a7a2fb0babbf7ed5a4f218a`. Never bundle either
  standalone module JAR.
- Own only the generated 49-ID/471-state route: 31 `glassential:*` IDs with
  453 legal states plus the exact 18 propertyless vanilla glass-cube IDs.
- Leave the other 82 Glassential IDs/4,996 legal states, vanilla panes, and
  every other namespace on BlueMap's stock path.
- Register no `fusion:*` blocks and add no runtime provider dependency on
  another add-on.
- Bundle no Glassential/Fusion runtime code, JSON, PNG, metadata, or JARs.
  The exact pinned MIT Fusion resource-model sources are deliberately compiled
  into this add-on; interpret only byte-exact operator-installed resources.
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

The owner accepted the aggregate BlueMap 5.23 integration view for release
candidate `0.1.0-alpha.2` on 2026-08-31. Its exact production JAR is 166,871
bytes with SHA-256
`9df99ffba26b1dd5a38452fb020e9a931b6a16a4ab4c374d85dad91cb9437e60`.
Publication is authorized; production deployment remains excluded.

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
