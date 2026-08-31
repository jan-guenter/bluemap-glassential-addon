# Provenance and clean-room boundary

The profile derives from the exact operator-installed All the Mons 1.2.0
artifacts listed in README. The generator records only factual archive metadata
and independently derived structural observations: resource paths, sizes,
hashes, image dimensions, placed IDs, legal state schemas, model selections,
predicate/layout names, NBT keys, and aggregate counts.

The exact Glassential 3.4.5 descriptor declares MIT. No exact source archive or
commit has been independently correlated, so no source-level correspondence is
claimed. The exact Fusion 1.3.12 descriptor declares All Rights Reserved. This
repository redistributes neither artifact's code, classes, JSON, models,
textures, metadata, nor binaries regardless of those different licenses.

The interpreter, selectors, dynamic-state decoder, and stock-delegation guard
are independently authored against the bounded installed resource contract.
The repository starts from the MIT BlueMap Connected Glass Add-on scaffold at
peeled release commit `4a4eb5030d18f1e54cd5a8ad1c2dc093a187ac06`;
Glassential-specific profile facts, state/NBT semantics, tests, and gallery
replace the predecessor's generated content.

The current migration removes the duplicated `AxisVector`, `FusionDirection`,
`FusionTextureSelector`, and `TextureOrientation` implementations. Their exact
MIT replacements, plus `FusionTextureLayout`, are compiled from released
Fusion Resource Models `0.1.0-alpha.1` commit
`3ddd5d39bb7cc8664c242aedd849a636316075c2`, source tree
`6e85031ff2f0e7417a7a2fb0babbf7ed5a4f218a`. Four narrow 5.23 adapter helpers
are compiled from Adapter API `0.1.0-alpha.2` commit
`e81f08bc4bfbf02d810ec8949a019130e2e61634`, source tree
`2f974c9bb2ba13888d69682f86f30f58922d30eb`. Both source sets are MIT,
register no consumer IDs by themselves, and are package-audited in both JARs.

The BlueMap-facing emitter retains attribution for adapted MIT renderer
mechanics in source and `LICENSE-BlueMap`. Binary and sources JAR audits must
reject Glassential/Fusion namespaces, third-party assets/classes, nested
archives, and Minecraft/NeoForge/BlueMap implementation classes. The complete
factual record is `provenance/upstreams.json`.

The published 2026-08-16 alpha.1 candidate embeds its historical version of
that file verbatim, including
`"status": "unreleased-implementation"`. This value is the artifact-time marker
for the state in which the candidate bytes were frozen, not a mutable summary
of later operational or release status. The owner's subsequent 2026-08-16
visual acceptance and authorization to publish the exact candidate as
immutable prerelease `0.1.0-alpha.1` do not make the artifact-time marker
false. It must not be rewritten merely to retrofit staging, exact-client,
disabled-control, rollback, acceptance, or publication results into already
identified bytes. A future version may update it through the normal reviewed
build and release process.
