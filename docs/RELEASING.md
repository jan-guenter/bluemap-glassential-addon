# Releasing

The owner explicitly accepted the frozen candidate's visual result on
2026-08-16 after its exact-client six-cell comparison and isolated BlueMap
staging passed. The restart-scoped disabled control and physical-removal
rollback subsequently passed before publication. Publication of the exact
assets below as immutable prerelease `0.1.0-alpha.1` is authorized, including
the reviewed merge, annotated tag, Maven publication, and GitHub Release
required by this procedure. This candidate-specific authorization does not
authorize production deployment, alter production state, or establish a
supported production version.

The publication coordinates are:

- repository `jan-guenter/bluemap-glassential-addon`;
- Maven `io.github.jan-guenter:bluemap-glassential-addon:<version>`; and
- binary JAR, sources JAR, POM, Gradle module metadata, and `SHA256SUMS` release
  assets.

The frozen candidate asset identities are:

| Asset | Bytes | SHA-256 |
| --- | ---: | --- |
| `bluemap-glassential-addon-0.1.0-alpha.1.jar` | 162,440 | `a956e62f7b843391917b861c831545b07af43ccceaa0bb84465e7e0b14c49780` |
| `bluemap-glassential-addon-0.1.0-alpha.1-sources.jar` | 79,193 | `b2cc577972e8dec52c0ce1ea2a4c0321ec209c96b0476fab340f8bbc0125db9b` |
| `bluemap-glassential-addon-0.1.0-alpha.1.pom` | 1,355 | `ac64d9ec689cf7be1826a62e1dfb347fb63b4d04724b2ab11d0cb44f5dc3e7e7` |
| `bluemap-glassential-addon-0.1.0-alpha.1.module.json` | 2,847 | `f96bdd237f750d581fc79069814ded305851b76169e2a6e87b73d47eec057553` |
| `SHA256SUMS` | 464 | `ac35389832fbb972f80e6c9c86244445ab5b7a2576672191becce7514a36566f` |

Documentation and workflow hardening may occur after the candidate freeze only
if the release gate proves the five asset identities above unchanged. Release
authorization is limited to those exact five identities. Any changed release
asset requires a new freeze, technical gate, and explicit owner acceptance.

The binary JAR must contain the expanded add-on version. The sources JAR
deliberately preserves the single source-template literal `${version}` in
`bluemap.addon.json`; `verifySourcesJar` locks that exact template and rejects
any additional placeholder.

Before tagging:

1. regenerate/check the exact profile and gallery from the pinned artifacts;
2. run the full clean README gate and inspect JAR/source/POM/module contents;
3. run `actionlint .github/workflows/*.yml`;
4. freeze a clean candidate and independently audit its exact bytes;
5. complete isolated activation/gallery rendering, exact-client comparison,
   disabled control, and physical-removal rollback;
6. confirm the required BlueMap URL sanity check and explicit owner visual
   acceptance apply to those exact bytes;
7. create/review the public pull request, tag its merge with annotated
   `v<addon_version>`, and let the release workflow build twice, create a draft,
   attest both JARs, publish and verify Maven, then publish the prerelease;
8. update the private workspace release identity separately.

Never attach upstream JARs/assets, the gallery datapack, runtime logs,
screenshots, worlds, or reports to the public release.
