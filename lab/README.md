# Disposable Glassential staging

These manifests target only the already authorized disposable
`bluemap-sophisticated-staging` host and PVC. This is not production deployment
authority. Keep `deployment/minecraft` at zero replicas while uploading or
patching, and never
delete the namespace, PVC, PV, original mod JARs, or production state.

Exact inputs already locked in `kubernetes/pins.yaml` are:

- Glassential Renewed 3.4.5: 702,249 bytes, SHA-256
  `1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471`;
- Fusion 1.3.12: 923,270 bytes, SHA-256
  `17f5215648a98bcde4134577b013200dbf363273ae282449c51408ae8346f2fa`;
- frozen add-on candidate: 162,440 bytes, SHA-256
  `a956e62f7b843391917b861c831545b07af43ccceaa0bb84465e7e0b14c49780`;
- deterministic six-cell gallery ZIP: 10,655 bytes, SHA-256
  `847067eab454eb646300c5f08887c6172de952f796dd090c7ab84d63aae304f0`.

The candidate and gallery pins are exact. Rebuild, re-audit, and re-pin either
payload if its generated inputs change.

The original Glassential and Fusion JARs remain under `/data/mods`. Two
`.zip`-named symlinks expose those exact files to BlueMap without duplicating
third-party bytes. The candidate and gallery are uploaded to the private
`/data/.bluemap-sophisticated-staging` directory and hash-verified before use.

## Completed 2026-08-16 lifecycle

The frozen inputs above completed the technical lifecycle on the disposable
host:

- exact init hashes passed, and BlueMap placed `zz-0246-glassential.zip`
  before `zz-0049-fusion.zip` and the corresponding original roots;
- a read-only probe reported the exact route `ACTIVE`, detail `exact-profile`,
  with 93 Fusion and two one-way programs;
- the gallery reported six cells, 80 placements, 83 observations, all 83
  checked, zero failed cells, and zero failures;
- the active render finished without relevant tile, capacity, out-of-memory,
  fallback, or inactive-route errors, and all 69 custom anchors used synthetic
  output while all 11 stock controls remained stock;
- the exact client and BlueMap agreed on all representative cases, followed by
  the owner's explicit visual acceptance;
- the restart-scoped disabled control reported `operator-disabled` with null
  catalogs and produced the expected stock mismatch; and
- physical removal deleted the exact add-on, both aliases, and prior map
  output, after which a clean stock startup and byte-identical stock rerender
  completed with `GlassentialRuntime` absent.

The final observed state was zero Deployment replicas and no Minecraft Pods.
The active public map output was deliberately replaced by the stock rollback
render, so the host is not currently presenting the accepted candidate. The
owner authorized immutable `0.1.0-alpha.1` prerelease publication, not
production deployment.

## Controlled sequence

Use kubeconfig `/root/.kube/guenter-cloud`, context `guenter.cloud`, and
namespace `bluemap-sophisticated-staging` for every command.

1. Reconfirm the frozen candidate/gallery identities and independent audit GO.
2. Validate the gallery ZIP identity against `gallery/package.sh` output.
3. Scale `deployment/minecraft` to zero and wait for all Minecraft Pods to
   disappear.
4. Delete only an old `glassential-artifact-loader` Pod, apply `config.yaml`,
   `pins.yaml`, and `artifact-loader.yaml`, then wait for `READY_FOR_UPLOAD`.
5. Copy candidate and gallery to `.part` paths in the private artifact
   directory, atomically rename them, create `.glassential-upload-complete`,
   and require `LOCAL_ARTIFACTS_VERIFIED`.
6. Delete the loader and wait for its Pod to disappear so the RWO PVC is free.
7. Run server-side dry runs for the Deployment, disabled-control, rollback,
   and Ingress patches. Confirm the merged Deployment remains at zero and has
   only the intended Glassential configuration.
8. Apply the Deployment patch and Ingress patch, then scale to one.
9. Require exact init hashes, BlueMap root priority, profile
   `glassential-fusion-3.4.5-1.3.12` active with 93 Fusion programs plus two
   one-way programs (95 total), gallery
   `6/80/83/0/0`, and a completed render.
10. Perform the exact-client comparisons from `docs/STAGING.md`, open the exact
    BlueMap URL for the lightweight agent sanity check, and request owner
    review.
11. At zero replicas, run the restart-scoped disabled control, then the
    physical-removal rollback proof.
12. Return the Deployment to zero and remove its Pods after evidence capture.

The strategic Deployment patches are intentionally not standalone manifests:

```bash
kubectl --kubeconfig /root/.kube/guenter-cloud --context guenter.cloud \
  -n bluemap-sophisticated-staging patch deployment minecraft \
  --type=strategic --patch-file kubernetes/deployment-patch.yaml

kubectl --kubeconfig /root/.kube/guenter-cloud --context guenter.cloud \
  -n bluemap-sophisticated-staging patch ingress \
  bluemap-sophisticated-review-public --type=strategic \
  --patch-file kubernetes/ingress-patch.yaml
```

The performance datapack maps the workspace staging policy to vanilla 1.21.1
gamerules, including `disableElytraMovementCheck=true`. Vanilla has no
`spawnerBlocksWork` gamerule; a flat, structure-free disposable world and the
bounded cleared fixture are the enforceable equivalent.
