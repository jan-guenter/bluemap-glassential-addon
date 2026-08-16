# Glassential staging gallery

This deterministic datapack is the representative visual fixture for the exact
All the Mons 1.2.0 Glassential Renewed 3.4.5 plus Fusion 1.3.12 route. It has six
cells, 80 placements, and 83 exact observations; three observations lock the
missing diagonals in the layout triptych.

| Cell | Contract |
| ---: | --- |
| 1 | Matched L shapes for FULL `stone_glass`, SIMPLE `glass_ethereal`, and PIECED `gravity_glass` with three missing diagonals |
| 2 | Pane two-layer straight stack, mixed-ID L, T, and cross topology |
| 3 | Bottom/top/double slab runs, touching vertical slabs, and one waterlogged state |
| 4 | Both untinted colorable cube IDs, both tinted pane IDs with four distinct colors, and consistent lit/`EmitLight` pairs |
| 5 | Clear/tinted one-way glass with horizontal/vertical opaque faces, valid mimic swatches, and malformed-Mimic normalization to iron |
| 6 | Every exact vanilla glass-cube override plus stock door, trapdoor, lamp, empty clear-fluid glass, vanilla pane, and stone controls |

The census contains 69 custom-route placements, 11 stock controls, 13 explicit
block-entity payloads, and all 18 vanilla override IDs. Every pane observation
names its full property schema. Colorable and one-way NBT is applied with
`data merge block` after placement and verified independently from block state.

The exact All the Mons 1.2.0 client and the isolated BlueMap render passed this
complete representative comparison on 2026-08-16: untinted colorable cubes,
pane tint/light, one-way mimic faces/material/tint, vertical and mixed-boundary
pane behavior, and malformed-to-iron normalization all agreed. The owner then
explicitly accepted the frozen candidate's visual result. Any changed profile,
renderer, candidate artifact, or generated gallery must repeat that gate.

## Evidence and generation

The Glassential input is the operator-installed 702,249-byte
`Glassential-renewed-1.21.1-3.4.5.jar`, SHA-256
`1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471`.
The paired Fusion input is the 923,270-byte
`fusion-1.3.12-neoforge-mc1.21.1.jar`, SHA-256
`17f5215648a98bcde4134577b013200dbf363273ae282449c51408ae8346f2fa`.

Regenerate with optional byte-exact source verification, then check outputs:

```bash
python3 gallery/generate.py \
  --glassential-jar /absolute/path/Glassential-renewed-1.21.1-3.4.5.jar \
  --fusion-jar /absolute/path/fusion-1.3.12-neoforge-mc1.21.1.jar
python3 gallery/generate.py --check
(cd gallery && sha256sum --check SHA256SUMS)
python3 -m unittest discover -s gallery/tests -p 'test_*.py'
```

`cases.json` and `cases.tsv` are the canonical coordinate/state/NBT manifests.
The generator owns them, every function, `pack.mcmeta`, the load tag, and
`SHA256SUMS`; do not hand-edit generated output.

## Staging use

Package into `bluemap-glassential-gallery-atmons-1.2.0.zip`:

```bash
gallery/package.sh /absolute/output/directory
```

Install only in the authorized disposable All the Mons 1.2.0 staging world,
reload datapacks, and run:

```text
/function glassential_gallery:build
/function glassential_gallery:verify
/function glassential_gallery:status
/function glassential_gallery:pose
```

Mechanical verification requires `cells=6`, `placements=80`, `checked=83`,
`failed_cells=0`, and `failures=0`. The fixture occupies the overworld cuboid
from `189 100 201` through `235 106 231`; its review center is `212 100 216`.
Build force-loads the intersecting chunks. When disposable review is finished:

```text
/function glassential_gallery:release
```

`release` clears only that exact cuboid and removes its force-load range. It is
not a production-world cleanup command. The datapack contains first-party
functions/manifests plus factual IDs, states, NBT, coordinates, and vanilla
commands; it redistributes no Glassential or Fusion assets or code.
