# Third-party and provenance inventory

| Component | Role | Exact identity | License | Bundled |
| --- | --- | --- | --- | --- |
| BlueMap | Compile-time ABI and adapted renderer mechanics | Feature backport `5.22-feature.backport-5.23-stateless-java-web-server-46`, commit `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac` | MIT | License notice only |
| BlueMap Add-on Adapter API | Four narrow 5.23 adapter helpers | `0.1.0-alpha.2`, commit `e81f08bc4bfbf02d810ec8949a019130e2e61634`, source tree `2f974c9bb2ba13888d69682f86f30f58922d30eb` | MIT | Exact source compiled; no standalone JAR |
| BlueMap Fusion Resource Models | Pure Fusion geometry and selector model | `0.1.0-alpha.1`, commit `3ddd5d39bb7cc8664c242aedd849a636316075c2`, source tree `6e85031ff2f0e7417a7a2fb0babbf7ed5a4f218a` | MIT | Exact source compiled; no standalone JAR |
| BlueMap Connected Glass Add-on | First-party scaffold and independent Fusion interpreter substrate | peeled tag commit `4a4eb5030d18f1e54cd5a8ad1c2dc093a187ac06` | MIT | Source adapted; no binary/assets |
| Glassential Renewed | Operator-installed blocks/models/textures | `3.4.5`, 702,249 bytes, SHA-256 `1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471` | MIT | No |
| Fusion | Operator-installed model/texture format resources | `1.3.12`, 923,270 bytes, SHA-256 `17f5215648a98bcde4134577b013200dbf363273ae282449c51408ae8346f2fa` | All Rights Reserved | No |
| JUnit Jupiter | Tests | 5.11.4 BOM | EPL-2.0 | No |
| Checkstyle | Source style | 10.18.2 | LGPL-2.1-or-later | No |

The profile generator records only independently derived identities, paths,
sizes, hashes, dimensions, shapes, legal-state counts, and bounded schema
facts. No Glassential or Fusion source expression was copied or adapted.
Production and sources JAR gates reject their classes, archives, namespaces,
assets, and data resources.

BlueMap's complete MIT notice is retained in `LICENSE-BlueMap` and packaged as
`META-INF/LICENSE-BlueMap` in the binary and sources JARs. The machine-readable
record is `provenance/upstreams.json`.
