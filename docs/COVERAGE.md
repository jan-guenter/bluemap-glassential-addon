# Coverage

## Exact product scope

| Route | IDs | Legal states |
| --- | ---: | ---: |
| Fusion full cubes | 17 | 19 |
| Fusion-textured panes | 11 | 416 |
| Fusion glass slab | 1 | 6 |
| One-way glass cubes | 2 | 12 |
| **Glassential custom subtotal** | **31** | **453** |
| Exact vanilla glass-cube overrides | 18 | 18 |
| **Total custom route** | **49** | **471** |
| Glassential stock path | 82 | 4,996 |
| **Complete Glassential roster** | **113** | **5,449** |

The 18 vanilla IDs are `glass`, `tinted_glass`, and all 16 stained-glass
cubes. They form a closed allowlist; vanilla panes remain stock.

## Resource and program closure

| Kind | Count |
| --- | ---: |
| Glassential blockstates | 31 |
| Selected models | 100 |
| PNG textures | 39 |
| Fusion metadata | 34 |
| **Strict operator paths** | **204** |
| Separate Minecraft host ABI paths | 22 |
| Fusion programs | 93 |
| One-way programs | 2 |

The Fusion layouts are 36 FULL, 54 SIMPLE, and three PIECED programs. Their 34
connected sheets comprise 22 FULL, nine SIMPLE, and three PIECED textures; five
additional pane-edge PNGs are plain.

## Representative gallery

The six-cell fixture has 80 placements and 83 exact observations: 69 custom
route placements, 11 stock controls, three negative-space observations, and 13
explicit block-entity payloads. It covers all 18 vanilla override IDs but is
not a 471-state visual matrix. Generated catalogs and unit tests own exhaustive
state/resource scope. The complete representative gallery passed both the
exact All the Mons 1.2.0 client comparison and the isolated BlueMap render on
2026-08-16.
