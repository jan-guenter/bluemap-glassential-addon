# BlueMap Glassential Add-on

This standalone MIT BlueMap add-on reproduces the static map appearance of the
exact Glassential Renewed 3.4.5 and Fusion 1.3.12 inputs installed by All the
Mons 1.2.0. The published `0.1.0-alpha.1` candidate passed its technical staging,
exact-client calibration, restart-scoped disabled-control, and physical
rollback gates on 2026-08-16. The owner explicitly accepted the candidate's
visual result on 2026-08-16 and authorized publication as the immutable
`0.1.0-alpha.1` prerelease. That acceptance does not authorize production
deployment or establish a supported production release.

Version `0.1.0-alpha.2` is the owner-accepted BlueMap 5.23 release candidate.
Its exact production JAR is 166,871 bytes with SHA-256
`9df99ffba26b1dd5a38452fb020e9a931b6a16a4ab4c374d85dad91cb9437e60`.
It preserves the accepted profile, gallery, and renderer behavior while moving
the adapter boundary to `bluemap523`. It compiles the exact Adapter API
`0.1.0-alpha.2` and released Fusion resource-model `0.1.0-alpha.1` source
modules. Neither standalone support-module JAR is installed.

## Exact contract

Activation requires both byte-exact operator-installed artifacts:

- `Glassential-renewed-1.21.1-3.4.5.jar`, 702,249 bytes, SHA-256
  `1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471`;
- `fusion-1.3.12-neoforge-mc1.21.1.jar`, 923,270 bytes, SHA-256
  `17f5215648a98bcde4134577b013200dbf363273ae282449c51408ae8346f2fa`.

The generated profile owns a bounded 49-ID/471-state route:

| Route | IDs | Legal states |
| --- | ---: | ---: |
| Glassential Fusion cubes | 17 | 19 |
| Glassential Fusion panes | 11 | 416 |
| Glassential Fusion slab | 1 | 6 |
| Glassential one-way cubes | 2 | 12 |
| Exact vanilla glass-cube overrides | 18 | 18 |
| **Total** | **49** | **471** |

The remaining 82 Glassential IDs and 4,996 legal states stay on BlueMap's
stock path. The exact vanilla allowlist is `glass`, `tinted_glass`, and the 16
stained-glass cubes; vanilla panes and every other vanilla ID remain stock.

The strict operator-installed closure has 204 paths: 31 blockstates, 100
selected models, 39 PNGs, and 34 Fusion metadata files. A separate 22-path
Minecraft host ABI supplies 18 vanilla blockstates, three host models, and one
host texture. None of those third-party resources is bundled.

## Rendering contract

The route contains 93 Fusion programs—36 `FULL`, 54 `SIMPLE`, and three
`PIECED`—plus two one-way programs. It supports the exact installed predicate
vocabulary (`and`, `or`, `is_direction`, `is_same_block`, `is_same_state`, and
`match_state`), ordinary pane multipart geometry, slab adjacency, and the five
plain pane-edge textures in the closure.

Four colorable block IDs persist a 24-bit block-entity `Color`. The two pane
models apply it to their tint-indexed faces; the two cube models have no tint
index under exact Fusion 1.3.12 and therefore remain untinted. Newly created
blocks persist the mod's white default; a missing or malformed persisted
`Color` is rejected for atomic stock fallback. `EmitLight` must agree with the
`lit` state. The two one-way IDs use `opaque_face` plus
block-entity `Mimic`; five faces use connected glass,
while the opaque face delegates to the default-state stock model/material/tint
of the mimicked block. Missing, malformed, or unregistered `Mimic` data follows
the exact client normalization to `minecraft:iron_block`; a one-way mimic is
recursion guarded to the configured base-glass face.

Tuple, structural-resource, host-ABI, registry, or compilation mismatch leaves
the whole route inactive. Invalid per-block state, color/light NBT, or render
input must discard partial geometry and use BlueMap's stock path; one-way
`Mimic` normalization follows the explicit client contract above.
`MaxCapacityReachedException` propagates unchanged. Dimension-preserving PNG
pixel overrides remain supported; structural overrides do not.

## Technical validation status

The published `0.1.0-alpha.1` production JAR is 162,440 bytes with SHA-256
`a956e62f7b843391917b861c831545b07af43ccceaa0bb84465e7e0b14c49780`.
On the reusable disposable host, the 2026-08-16 lifecycle established:

- an active exact-profile route with all 93 Fusion and two one-way programs
  after byte-exact input and BlueMap pack-root-order checks;
- all six gallery cells, 80 placements, and 83 observations verified with
  zero failed cells and zero failures, followed by a completed BlueMap render;
- agreement between the exact All the Mons 1.2.0 client and BlueMap for every
  representative gallery cell, including pane tint/light, untinted colorable
  cubes, slab adjacency, one-way mimic/default behavior, vertical pane caps,
  and all 18 vanilla overrides;
- an `operator-disabled` cold-restart control with no active catalog and a
  completed stock render; and
- physical add-on/alias removal, a clean restart with the runtime class absent,
  and a byte-identical stock rerender, after which the disposable Deployment
  was scaled to zero with no Pods.

The agent-side BlueMap sanity check passed while the active candidate was
rendered. The later rollback deliberately replaced that output with the stock
control, so it is not a current review endpoint. After reviewing the active
presentation and exact-client comparison, the owner explicitly accepted the
frozen candidate on 2026-08-16 and
authorized its immutable `0.1.0-alpha.1` prerelease publication. This is
release authorization only, not production deployment authorization. See
[docs/STAGING.md](docs/STAGING.md) and [docs/ROLLBACK.md](docs/ROLLBACK.md).

## Generate and validate

Java 21 and the exact local BlueMap feature-backport checkout are required.
Clone with `--recurse-submodules`, or initialize the three pinned support
checkouts before invoking Gradle:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit \
  modules/bluemap-addon-adapter-api \
  modules/bluemap-fusion-resource-models
```

Example inputs:

```bash
glassential_jar='/absolute/path/Glassential-renewed-1.21.1-3.4.5.jar'
fusion_jar='/absolute/path/fusion-1.3.12-neoforge-mc1.21.1.jar'

python3 tools/verify_pinned_artifacts.py \
  --glassential "$glassential_jar" --fusion "$fusion_jar"
python3 gallery/generate.py --check \
  --glassential-jar "$glassential_jar" --fusion-jar "$fusion_jar"
(cd gallery && sha256sum --check SHA256SUMS)
python3 -m unittest discover -s tools/tests -p 'test_*.py'
python3 -m unittest discover -s gallery/tests -p 'test_*.py'

gradle --no-daemon \
  -PbluemapSourcePath=/absolute/path/BlueMap \
  -PglassentialJar="$glassential_jar" \
  -PfusionJar="$fusion_jar" \
  clean prototypeCheck build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication
```

The binary and sources JAR gates admit only the exact Adapter API and Fusion
model source sets outside this repository's package. They reject upstream
runtime namespaces, assets, data, classes, and nested archives.

## Gallery and licensing

`gallery/` deterministically generates six representative cells with 80
placements and 83 exact observations. It covers the three layouts, pane
topology, slabs, all dynamic routes, every vanilla override, and stock
controls. It is representative rather than a 471-state visual matrix.

Project code is MIT. BlueMap-derived MIT renderer mechanics retain attribution.
The exact Glassential descriptor declares MIT and the exact Fusion descriptor
declares All Rights Reserved, but neither artifact or its assets is bundled.
No exact Glassential 3.4.5 source commit is correlated. See
[LICENSE-BlueMap](LICENSE-BlueMap), [THIRD_PARTY.md](THIRD_PARTY.md), and
[docs/PROVENANCE.md](docs/PROVENANCE.md).
