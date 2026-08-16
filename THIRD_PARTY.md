# Third-party and provenance inventory

| Component | Role | Exact identity | License | Bundled |
| --- | --- | --- | --- | --- |
| BlueMap | Compile-time ABI and adapted renderer mechanics | Backport `5.22-agent.backport-5.22-mc1.21.1-2`, commit `9be321df995a1103808621d529eb72773e719d4d` | MIT | License notice only |
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
