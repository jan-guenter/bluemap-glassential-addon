# Rollback

The add-on owns no persisted world or BlueMap state. To restore stock behavior:

1. scale the disposable server to zero;
2. remove only the exact Glassential add-on JAR;
3. remove only its two BlueMap pack aliases and disposable
   `glassential_staging` rendered map output;
4. retain the exact original Glassential/Fusion JARs and gallery/map
   configuration for a clean stock comparison;
5. restart, force a stock BlueMap render, and verify the expected custom-route
   mismatch plus unchanged stock controls;
6. return the disposable Deployment to zero replicas.

The restart property
`bluemap.glassential.disabledProfiles=glassential-fusion-3.4.5-1.3.12`
provides a separate stock control but is not a same-JVM toggle. Physical JAR
removal plus restart is the rollback proof. Never delete a namespace, PVC, PV,
original mod JAR, or production map.

## Observed 2026-08-16 proof

The restart-scoped control retained the exact 162,440-byte candidate and both
original mod inputs. Its read-only runtime probe reported route
`glassential-fusion-3.4.5-1.3.12` as `INACTIVE`, detail `operator-disabled`,
with null Fusion, one-way, and total program catalogs. The gallery still
verified `6/80/83/0/0`, and its forced stock render finished with no pending
tasks. All 69 custom anchors changed from the accepted synthetic output to
stock geometry, while all 11 stock controls remained byte-signature identical.

At zero replicas, the physical rollback patch then removed the exact add-on,
both BlueMap aliases, and the previous rendered map output. Init reported
`GLASSENTIAL_ADDON_REMOVED`; filesystem checks confirmed those targets absent
while the original Glassential and Fusion JAR hashes remained exact. Minecraft
and BlueMap restarted cleanly without loading the add-on, and the attach probe
reported `GLASSENTIAL_RUNTIME_COUNTS class=not-loaded`. The gallery again
verified `6/80/83/0/0`, and the completed stock render was byte-identical to
the disabled-control output across all five rendered tiles and textures. No
relevant tile, capacity, out-of-memory, fallback, or add-on error occurred.

The Deployment was finally scaled to zero with no Minecraft Pods. Its PVC
identity and metadata were unchanged, and no namespace, PVC, PV, original mod
JAR, or production state was removed.
