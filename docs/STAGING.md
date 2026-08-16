# Disposable staging gate

The frozen 162,440-byte candidate with SHA-256
`a956e62f7b843391917b861c831545b07af43ccceaa0bb84465e7e0b14c49780`
passed the technical disposable-host gate on 2026-08-16. After reviewing the
active gallery presentation and exact-client comparison, the owner explicitly
accepted this exact candidate on 2026-08-16 and authorized immutable
`0.1.0-alpha.1` prerelease publication. Acceptance does not authorize
production deployment or production use. Use only the reusable disposable
BlueMap namespace/PVC authorized by the workspace; never production.

## Observed 2026-08-16 pass

Clean init verified the exact Glassential, Fusion, add-on, and gallery hashes.
BlueMap resolved the two explicit pack aliases in client-priority order:

```text
config/bluemap/packs/zz-0246-glassential.zip
config/bluemap/packs/zz-0049-fusion.zip
```

The read-only runtime probe reported route
`glassential-fusion-3.4.5-1.3.12` as `ACTIVE`, detail `exact-profile`, with 93
Fusion programs and two one-way programs (95 total). Gallery build and
verification reported `cells=6`, `placements=80`, `ready=1`, `checked=83`,
`failed_cells=0`, and `failures=0`. The forced BlueMap update finished with no
pending tasks and no relevant tile, capacity, out-of-memory, inactive-route,
or fallback error.

The exact All the Mons 1.2.0 client and BlueMap agreed on all six
representative cells: FULL/SIMPLE/PIECED selection; pane topology and vertical
caps; slab adjacency; untinted colorable cubes; four exact pane tints and
light states; one-way static, texture-colored, grass, foliage, recursive, and
malformed-to-iron cases; all 18 vanilla overrides; and every stock control.
The accepted BlueMap render contained synthetic geometry at all 69 custom
anchors and none at the 11 stock controls. An agent opened the exact active
BlueMap URL and confirmed that the intended map/view was not blank, black,
missing, or grossly broken before the owner's explicit visual acceptance.

The restart-scoped disabled control reported `INACTIVE`, detail
`operator-disabled`, and null program catalogs. Its stock render changed all
69 custom anchors while leaving all 11 stock controls byte-signature identical.
Physical removal then removed the add-on, both aliases, and prior rendered map
output. A clean restart reported `GLASSENTIAL_RUNTIME_COUNTS class=not-loaded`;
the stock rerender was byte-identical to the disabled output, and the original
Glassential/Fusion hashes remained exact. The final Deployment state was zero
replicas and no Pods. The active public output was deliberately replaced by
the rollback stock render, so it is no longer a live candidate-review endpoint.

## Reproduction and future owner-acceptance gate

Before any cluster mutation:

1. finish local implementation and the full clean README gate;
2. freeze and independently audit the exact candidate JAR;
3. package the deterministic gallery and record both exact sizes/SHA-256 values;
4. require every staging pin/annotation to match the frozen candidate and
   gallery identities, then re-run local YAML, hash, and server-dry-run checks;
5. keep the existing disposable Deployment at zero while changing its PVC;
6. configure the workspace's stable no-time/weather/ticks/mobs/damage staging
   baseline;
7. install only the exact candidate, exact original mod inputs, and gallery;
8. prove BlueMap pack-root priority and exact route activation from retained
   evidence.

## Runtime and gallery gate

The expected route ID is `glassential-fusion-3.4.5-1.3.12`. Run:

```text
function glassential_gallery:build
function glassential_gallery:verify
function glassential_gallery:status
bluemap force-update glassential_staging
bluemap tasks
```

Require `cells=6`, `placements=80`, `checked=83`, `failed_cells=0`, and
`failures=0`, followed by a completed render with no relevant tile, capacity,
out-of-memory, inactive-route, or fallback error.

Using the exact All the Mons 1.2.0 client, compare the same fixture and retain
observations for:

- FULL/SIMPLE/PIECED corner and missing-diagonal selection;
- pane straight/L/T/cross geometry, the two-layer stack's vertical caps, and
  the mixed-ID boundary;
- bottom/top/double and vertical slab adjacency;
- the two colorable cube routes remaining untinted, the two pane routes
  applying their four distinct tints, and all lit/unlit output;
- one-way opaque faces with static, texture-colored, grass, and foliage mimic
  controls, plus malformed-Mimic normalization to the iron fallback; and
- all 18 vanilla overrides plus unchanged door, trapdoor, lamp, empty
  clear-fluid glass, vanilla-pane, and stone controls.

Open the exact BlueMap review URL and perform the workspace-required lightweight
sanity check before requesting owner review. The recorded acceptance applies
only to the exact `0.1.0-alpha.1` identity above; any changed artifact, profile,
renderer, or generated gallery requires a fresh technical gate and explicit
owner visual acceptance.

Finally run a restart-scoped disabled control and the physical-removal rollback
from `docs/ROLLBACK.md`. Record only observed identities and results. No part of
this procedure authorizes production deployment.
