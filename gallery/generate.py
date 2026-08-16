#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Generate the bounded ATM 1.2.0 Glassential review gallery."""

from __future__ import annotations

import argparse
from dataclasses import dataclass
import hashlib
import json
from pathlib import Path
import re
import sys
import zipfile


ROOT = Path(__file__).resolve().parent

PACK_VERSION = "1.2.0"
MINECRAFT_VERSION = "1.21.1"
GLASSENTIAL_VERSION = "3.4.5"
GLASSENTIAL_FILE = "Glassential-renewed-1.21.1-3.4.5.jar"
GLASSENTIAL_SIZE = 702_249
GLASSENTIAL_SHA256 = (
    "1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471"
)
FUSION_VERSION = "1.3.12"
FUSION_FILE = "fusion-1.3.12-neoforge-mc1.21.1.jar"
FUSION_SIZE = 923_270
FUSION_SHA256 = (
    "17f5215648a98bcde4134577b013200dbf363273ae282449c51408ae8346f2fa"
)

OBJECTIVE = "gsl_gallery"
NAMESPACE = "glassential_gallery"
FLOOR_Y = 100
CELL_HALF_SIZE = 6
REGION_MIN = (189, 100, 201)
REGION_MAX = (235, 106, 231)
POSE = (212, 122, 259, 180, 24)
CELL_ANCHORS = (
    (196, FLOOR_Y, 208),
    (212, FLOOR_Y, 208),
    (228, FLOOR_Y, 208),
    (196, FLOOR_Y, 224),
    (212, FLOOR_Y, 224),
    (228, FLOOR_Y, 224),
)

VANILLA_OVERRIDE_IDS = (
    "minecraft:glass",
    "minecraft:tinted_glass",
    "minecraft:white_stained_glass",
    "minecraft:orange_stained_glass",
    "minecraft:magenta_stained_glass",
    "minecraft:light_blue_stained_glass",
    "minecraft:yellow_stained_glass",
    "minecraft:lime_stained_glass",
    "minecraft:pink_stained_glass",
    "minecraft:gray_stained_glass",
    "minecraft:light_gray_stained_glass",
    "minecraft:cyan_stained_glass",
    "minecraft:purple_stained_glass",
    "minecraft:blue_stained_glass",
    "minecraft:brown_stained_glass",
    "minecraft:green_stained_glass",
    "minecraft:red_stained_glass",
    "minecraft:black_stained_glass",
)


@dataclass(frozen=True, order=True)
class Position:
    x: int
    y: int
    z: int

    def offset(self, relative: "Position") -> "Position":
        return Position(
            self.x + relative.x,
            self.y + relative.y,
            self.z + relative.z,
        )

    def command(self) -> str:
        return f"{self.x} {self.y} {self.z}"

    def payload(self) -> dict[str, int]:
        return {"x": self.x, "y": self.y, "z": self.z}


@dataclass(frozen=True)
class Observation:
    relative: Position
    block_state: str
    role: str
    route: str
    block_entity_snbt: str | None = None
    expected_block_entity_snbt: str | None = None
    placed: bool = True


@dataclass(frozen=True)
class GalleryCase:
    number: int
    case_id: str
    title: str
    anchor: Position
    notes: str
    client_gate: str | None
    observations: tuple[Observation, ...]

    @property
    def function_id(self) -> str:
        return f"{self.number:02d}_{self.case_id}"


def bool_text(value: bool) -> str:
    return "true" if value else "false"


def pane_state(
    block_id: str,
    *,
    north: bool = False,
    east: bool = False,
    south: bool = False,
    west: bool = False,
    waterlogged: bool = False,
    lit: bool | None = None,
) -> str:
    """Return one complete canonical IronBars-derived pane state."""
    properties = [
        f"east={bool_text(east)}",
        f"north={bool_text(north)}",
        f"south={bool_text(south)}",
        f"waterlogged={bool_text(waterlogged)}",
        f"west={bool_text(west)}",
    ]
    if lit is not None:
        properties.append(f"lit={bool_text(lit)}")
    return f"{block_id}[{','.join(properties)}]"


def obs(
    x: int,
    y: int,
    z: int,
    block_state: str,
    role: str,
    *,
    route: str = "custom",
    block_entity_snbt: str | None = None,
    expected_block_entity_snbt: str | None = None,
    placed: bool = True,
) -> Observation:
    return Observation(
        relative=Position(x, y, z),
        block_state=block_state,
        role=role,
        route=route,
        block_entity_snbt=block_entity_snbt,
        expected_block_entity_snbt=expected_block_entity_snbt,
        placed=placed,
    )


def air(x: int, y: int, z: int, role: str) -> Observation:
    return obs(
        x,
        y,
        z,
        "minecraft:air",
        role,
        route="negative",
        placed=False,
    )


def pane(
    x: int,
    y: int,
    z: int,
    block_id: str,
    role: str,
    *,
    route: str = "custom",
    block_entity_snbt: str | None = None,
    **states: bool,
) -> Observation:
    return obs(
        x,
        y,
        z,
        pane_state(block_id, **states),
        role,
        route=route,
        block_entity_snbt=block_entity_snbt,
    )


def color_nbt(color: int, emit_light: bool) -> str:
    return (
        f"{{Color:{color},EmitLight:{1 if emit_light else 0}b,"
        "EmitRedstone:0b,PassPlayer:0b,PassEntity:0b}"
    )


def cases() -> tuple[GalleryCase, ...]:
    ethereal_pane = "glassential:glass_ethereal_pane"
    dark_pane = "glassential:glass_dark_ethereal_pane"

    result = (
        GalleryCase(
            1,
            "layout_triptych",
            "FULL, SIMPLE, and PIECED corner selections",
            Position(*CELL_ANCHORS[0]),
            "Three L-shaped same-ID groups each keep the diagonal air so the "
            "three installed Fusion layouts can be compared on the same geometry.",
            None,
            (
                obs(-5, 1, -3, "glassential:stone_glass", "FULL L corner"),
                obs(-4, 1, -3, "glassential:stone_glass", "FULL horizontal arm"),
                obs(-5, 2, -3, "glassential:stone_glass", "FULL vertical arm"),
                air(-4, 2, -3, "FULL missing diagonal"),
                obs(-1, 1, -3, "glassential:glass_ethereal", "SIMPLE L corner"),
                obs(0, 1, -3, "glassential:glass_ethereal", "SIMPLE horizontal arm"),
                obs(-1, 2, -3, "glassential:glass_ethereal", "SIMPLE vertical arm"),
                air(0, 2, -3, "SIMPLE missing diagonal"),
                obs(3, 1, -3, "glassential:gravity_glass", "PIECED L corner"),
                obs(4, 1, -3, "glassential:gravity_glass", "PIECED horizontal arm"),
                obs(3, 2, -3, "glassential:gravity_glass", "PIECED vertical arm"),
                air(4, 2, -3, "PIECED missing diagonal"),
            ),
        ),
        GalleryCase(
            2,
            "pane_topologies",
            "Pane topology, vertical caps, and mixed-ID boundary",
            Position(*CELL_ANCHORS[1]),
            "The same fixture covers a two-layer straight stack, a mixed-ID L, "
            "and isolated T and cross networks with complete pane states.",
            "Compare vertical caps and the mixed-ID boundary with the exact 3.4.5 client.",
            (
                pane(-4, 1, -4, ethereal_pane, "straight lower north end", south=True),
                pane(-4, 1, -3, ethereal_pane, "straight lower middle", north=True, south=True),
                pane(-4, 1, -2, ethereal_pane, "straight lower south end", north=True),
                pane(-4, 2, -4, ethereal_pane, "straight upper north end", south=True),
                pane(-4, 2, -3, ethereal_pane, "straight upper middle", north=True, south=True),
                pane(-4, 2, -2, ethereal_pane, "straight upper south end", north=True),
                pane(0, 1, -4, ethereal_pane, "mixed L north end", south=True),
                pane(0, 1, -3, ethereal_pane, "mixed L corner", north=True, east=True),
                pane(1, 1, -3, dark_pane, "mixed L east end", west=True),
                pane(-1, 1, 1, ethereal_pane, "T west end", east=True),
                pane(0, 1, 0, ethereal_pane, "T north end", south=True),
                pane(0, 1, 1, ethereal_pane, "T center", north=True, east=True, west=True),
                pane(1, 1, 1, ethereal_pane, "T east end", west=True),
                pane(4, 1, 1, ethereal_pane, "cross center", north=True, east=True, south=True, west=True),
                pane(4, 1, 0, ethereal_pane, "cross north end", south=True),
                pane(5, 1, 1, ethereal_pane, "cross east end", west=True),
                pane(4, 1, 2, ethereal_pane, "cross south end", north=True),
                pane(3, 1, 1, ethereal_pane, "cross west end", east=True),
            ),
        ),
        GalleryCase(
            3,
            "slab_adjacency",
            "Bottom, top, double, vertical, and waterlogged slabs",
            Position(*CELL_ANCHORS[2]),
            "Horizontal same-type runs terminate at doubles; the vertical pair "
            "touches across a top-to-bottom boundary; one isolated bottom slab is waterlogged.",
            None,
            (
                obs(-4, 1, -2, "glassential:glass_slab[type=bottom,waterlogged=false]", "bottom run"),
                obs(-3, 1, -2, "glassential:glass_slab[type=bottom,waterlogged=false]", "bottom run"),
                obs(-2, 1, -2, "glassential:glass_slab[type=double,waterlogged=false]", "bottom-to-double boundary"),
                obs(0, 1, -2, "glassential:glass_slab[type=top,waterlogged=false]", "top run"),
                obs(1, 1, -2, "glassential:glass_slab[type=top,waterlogged=false]", "top run"),
                obs(2, 1, -2, "glassential:glass_slab[type=double,waterlogged=false]", "top-to-double boundary"),
                obs(4, 1, -2, "glassential:glass_slab[type=top,waterlogged=false]", "vertical lower top slab"),
                obs(4, 2, -2, "glassential:glass_slab[type=bottom,waterlogged=false]", "vertical upper bottom slab"),
                obs(4, 1, 2, "glassential:glass_slab[type=bottom,waterlogged=true]", "waterlogged bottom slab"),
            ),
        ),
        GalleryCase(
            4,
            "colorable_blocks",
            "Four colorable routes with exact tint and light behavior",
            Position(*CELL_ANCHORS[3]),
            "Every route persists an explicit 24-bit Color; exact Fusion 1.3.12 "
            "tints pane faces only, while cube faces remain untinted. EmitLight "
            "agrees with its lit state.",
            "Confirm both cube routes remain untinted, both pane routes apply "
            "their four distinct tints, and all lit/unlit output matches the "
            "exact 3.4.5 client.",
            (
                obs(-4, 1, -2, "glassential:colorable_glass[lit=false]", "clear cube red unlit", block_entity_snbt=color_nbt(0xFF0000, False)),
                obs(-3, 1, -2, "glassential:colorable_glass[lit=true]", "clear cube cyan lit", block_entity_snbt=color_nbt(0x00FFFF, True)),
                obs(-1, 1, -2, "glassential:colorable_stained_glass[lit=false]", "stained cube green unlit", block_entity_snbt=color_nbt(0x00FF00, False)),
                obs(0, 1, -2, "glassential:colorable_stained_glass[lit=true]", "stained cube yellow lit", block_entity_snbt=color_nbt(0xFFFF00, True)),
                pane(2, 1, -2, "glassential:colorable_glass_pane", "clear pane purple unlit", south=True, lit=False, block_entity_snbt=color_nbt(0x800080, False)),
                pane(2, 1, -1, "glassential:colorable_glass_pane", "clear pane orange lit", north=True, lit=True, block_entity_snbt=color_nbt(0xFF8000, True)),
                pane(4, 1, -2, "glassential:colorable_stained_glass_pane", "stained pane blue unlit", south=True, lit=False, block_entity_snbt=color_nbt(0x0000FF, False)),
                pane(4, 1, -1, "glassential:colorable_stained_glass_pane", "stained pane magenta lit", north=True, lit=True, block_entity_snbt=color_nbt(0xFF00FF, True)),
            ),
        ),
        GalleryCase(
            5,
            "one_way_mimics",
            "One-way orientation and stock mimic swatches",
            Position(*CELL_ANCHORS[4]),
            "Five one-way blocks cover horizontal and vertical opaque faces. "
            "Adjacent swatches lock static, texture-colored, and biome-tinted default-state mimics.",
            "Orbit every opaque face and compare material/tint delegation plus malformed-to-iron normalization with the exact 3.4.5 client.",
            (
                obs(-4, 1, -2, "glassential:one_way_glass[opaque_face=south]", "clear one-way south stone", block_entity_snbt='{Mimic:"minecraft:stone"}'),
                obs(-4, 1, 0, "minecraft:stone", "static stone mimic swatch", route="stock"),
                obs(-1, 1, -2, "glassential:one_way_glass[opaque_face=east]", "clear one-way east grass", block_entity_snbt='{Mimic:"minecraft:grass_block"}'),
                obs(-1, 1, 0, "minecraft:grass_block[snowy=false]", "biome-tinted grass mimic swatch", route="stock"),
                obs(2, 1, -2, "glassential:tinted_one_way_glass[opaque_face=up]", "tinted one-way up leaves", block_entity_snbt='{Mimic:"minecraft:oak_leaves"}'),
                obs(2, 1, 0, "minecraft:oak_leaves[distance=7,persistent=false,waterlogged=false]", "biome-tinted leaves mimic swatch", route="stock"),
                obs(5, 1, -2, "glassential:tinted_one_way_glass[opaque_face=north]", "tinted one-way north red glass", block_entity_snbt='{Mimic:"minecraft:red_stained_glass"}'),
                obs(5, 1, 0, "minecraft:red_stained_glass", "texture-colored glass mimic swatch"),
                obs(-4, 1, 2, "glassential:one_way_glass[opaque_face=west]", "malformed mimic canonicalizes to iron", block_entity_snbt='{Mimic:"not a valid id"}', expected_block_entity_snbt='{Mimic:"minecraft:iron_block"}'),
                obs(-4, 1, 4, "minecraft:iron_block", "malformed-mimic iron fallback swatch", route="stock"),
            ),
        ),
        GalleryCase(
            6,
            "overrides_and_controls",
            "Eighteen vanilla overrides and stock-path controls",
            Position(*CELL_ANCHORS[5]),
            "The palette contains every exact vanilla cube override. The final "
            "row keeps representative Glassential and vanilla stock paths beside it.",
            None,
            tuple(
                obs(x, 1, z, block, "exact vanilla cube override", route="custom")
                for block, (x, z) in zip(
                    VANILLA_OVERRIDE_IDS,
                    tuple(
                        (x, z)
                        for z in (-4, -2, 0)
                        for x in (-5, -3, -1, 1, 3, 5)
                    ),
                    strict=True,
                )
            )
            + (
                obs(-5, 2, -4, "minecraft:glass", "same-ID vanilla override connection", route="custom"),
                obs(-5, 1, 3, "glassential:glass_door[facing=north,half=lower,hinge=left,open=false,powered=false]", "stock Glassential door lower", route="stock"),
                obs(-5, 2, 3, "glassential:glass_door[facing=north,half=upper,hinge=left,open=false,powered=false]", "stock Glassential door upper", route="stock"),
                obs(-3, 1, 3, "glassential:glass_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]", "stock Glassential trapdoor", route="stock"),
                obs(-1, 1, 3, "glassential:glass_glowstone_lamp", "stock Glassential lamp", route="stock"),
                obs(1, 1, 3, "glassential:clear_fluid_glass", "stock empty clear-fluid glass", route="stock"),
                pane(3, 1, 3, "minecraft:glass_pane", "stock vanilla pane", route="stock"),
                obs(5, 1, 3, "minecraft:stone", "stock opaque control", route="stock"),
            ),
        ),
    )
    validate_cases(result)
    return result


def block_id(block_state: str) -> str:
    return block_state.split("[", 1)[0]


def counts(items: tuple[GalleryCase, ...]) -> dict[str, int]:
    observations = [observation for item in items for observation in item.observations]
    placements = [observation for observation in observations if observation.placed]
    return {
        "block_entity_placements": sum(
            observation.block_entity_snbt is not None for observation in placements
        ),
        "cells": len(items),
        "custom_route_placements": sum(
            observation.route == "custom" for observation in placements
        ),
        "glassential_pane_placements": sum(
            block_id(observation.block_state).startswith("glassential:")
            and "pane" in block_id(observation.block_state)
            for observation in placements
        ),
        "negative_space_observations": sum(
            observation.route == "negative" for observation in observations
        ),
        "observations": len(observations),
        "placements": len(placements),
        "stock_route_placements": sum(
            observation.route == "stock" for observation in placements
        ),
        "vanilla_override_placements": sum(
            observation.route == "custom"
            and block_id(observation.block_state).startswith("minecraft:")
            for observation in placements
        ),
        "waterlogged_placements": sum(
            "waterlogged=true" in observation.block_state for observation in placements
        ),
    }


def validate_cases(items: tuple[GalleryCase, ...]) -> None:
    if len(items) != 6:
        raise AssertionError(f"expected six gallery cells, found {len(items)}")
    if tuple(item.number for item in items) != tuple(range(1, 7)):
        raise AssertionError("gallery cells must be numbered 1 through 6")
    if len({item.case_id for item in items}) != len(items):
        raise AssertionError("duplicate gallery case ID")

    pane_pattern = re.compile(
        r"^[a-z0-9_.-]+:[a-z0-9_./-]+\["
        r"east=(?:false|true),north=(?:false|true),south=(?:false|true),"
        r"waterlogged=(?:false|true),west=(?:false|true)"
        r"(?:,lit=(?:false|true))?\]$"
    )
    absolute_positions: set[Position] = set()
    for item in items:
        local_positions: set[Position] = set()
        for observation in item.observations:
            if observation.relative in local_positions:
                raise AssertionError(
                    f"duplicate relative position in {item.case_id}: {observation.relative}"
                )
            local_positions.add(observation.relative)
            absolute = item.anchor.offset(observation.relative)
            if absolute in absolute_positions:
                raise AssertionError(f"gallery observation overlap at {absolute}")
            absolute_positions.add(absolute)
            if not (
                item.anchor.x - 5 <= absolute.x <= item.anchor.x + 5
                and FLOOR_Y + 1 <= absolute.y <= FLOOR_Y + 5
                and item.anchor.z - 5 <= absolute.z <= item.anchor.z + 5
            ):
                raise AssertionError(
                    f"observation outside bounded cell {item.case_id}: {absolute}"
                )
            if observation.route not in {"custom", "stock", "negative"}:
                raise AssertionError(f"unknown route marker: {observation.route}")
            if observation.placed:
                if observation.block_state == "minecraft:air":
                    raise AssertionError("placed observations cannot be air")
                if observation.route == "negative":
                    raise AssertionError("placed observations cannot be negative")
            else:
                if observation.block_state != "minecraft:air":
                    raise AssertionError("negative observations must expect air")
                if observation.route != "negative":
                    raise AssertionError("air observations must use the negative route")
            if "pane" in observation.block_state and not pane_pattern.fullmatch(
                observation.block_state
            ):
                raise AssertionError(
                    "pane observation lacks a complete canonical state: "
                    f"{observation.block_state}"
                )
            if observation.block_entity_snbt and not observation.placed:
                raise AssertionError("air observations cannot carry block-entity NBT")

    census = counts(items)
    if census["placements"] != 80 or census["observations"] != 83:
        raise AssertionError(
            "fixture census changed: "
            f"{census['placements']} placements / {census['observations']} observations"
        )
    overrides = {
        block_id(observation.block_state)
        for item in items
        for observation in item.observations
        if observation.route == "custom"
        and block_id(observation.block_state).startswith("minecraft:")
    }
    if overrides != set(VANILLA_OVERRIDE_IDS):
        raise AssertionError("fixture must contain the exact 18-ID vanilla override allowlist")


def source_block_ids(items: tuple[GalleryCase, ...]) -> set[str]:
    return {
        block_id(observation.block_state)
        for item in items
        for observation in item.observations
        if observation.placed
    }


def file_sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def verify_glassential_jar(path: Path, items: tuple[GalleryCase, ...]) -> None:
    if not path.is_file():
        raise ValueError(f"Glassential artifact does not exist: {path}")
    if path.stat().st_size != GLASSENTIAL_SIZE:
        raise ValueError("Glassential artifact size does not match the exact input")
    if file_sha256(path) != GLASSENTIAL_SHA256:
        raise ValueError("Glassential artifact SHA-256 does not match the exact input")
    with zipfile.ZipFile(path) as archive:
        names = set(archive.namelist())
        for source_id in sorted(source_block_ids(items)):
            namespace, name = source_id.split(":", 1)
            if namespace == "glassential":
                resource = f"assets/glassential/blockstates/{name}.json"
            elif source_id in VANILLA_OVERRIDE_IDS:
                resource = f"assets/minecraft/models/block/{name}.json"
            else:
                continue
            if resource not in names:
                raise ValueError(f"exact blockstate resource is missing: {resource}")
            json.loads(archive.read(resource))

        layouts = {
            "assets/glassential/textures/block/stone_glass.png.mcmeta": "full",
            "assets/glassential/textures/block/ethereal.png.mcmeta": "simple",
            "assets/glassential/textures/block/gravity_glass.png.mcmeta": "pieced",
        }
        for resource, expected in layouts.items():
            payload = json.loads(archive.read(resource))
            actual = payload.get("fusion", {}).get("layout")
            if actual != expected:
                raise ValueError(
                    f"layout contract changed for {resource}: {actual!r} != {expected!r}"
                )

        nbt_contracts = {
            "com/github/bigenergy/glassential/blocks/entity/ColorableGlassBlockEntity.class": (
                b"Color",
                b"EmitLight",
                b"EmitRedstone",
                b"PassPlayer",
                b"PassEntity",
            ),
            "com/github/bigenergy/glassential/blocks/entity/OneWayGlassBlockEntity.class": (
                b"Mimic",
            ),
        }
        for resource, literals in nbt_contracts.items():
            class_bytes = archive.read(resource)
            if any(literal not in class_bytes for literal in literals):
                raise ValueError(f"block-entity NBT contract changed for {resource}")


def verify_fusion_jar(path: Path) -> None:
    if not path.is_file():
        raise ValueError(f"Fusion artifact does not exist: {path}")
    if path.stat().st_size != FUSION_SIZE:
        raise ValueError("Fusion artifact size does not match the exact input")
    if file_sha256(path) != FUSION_SHA256:
        raise ValueError("Fusion artifact SHA-256 does not match the exact input")


def cases_json(items: tuple[GalleryCase, ...]) -> str:
    payload = {
        "cases": [
            {
                "anchor": item.anchor.payload(),
                "case_id": item.case_id,
                "client_gate": item.client_gate,
                "index": item.number,
                "notes": item.notes,
                "observations": [
                    {
                        "absolute": item.anchor.offset(observation.relative).payload(),
                        "block_entity_snbt": observation.block_entity_snbt,
                        "expected_block_entity_snbt": (
                            observation.expected_block_entity_snbt
                        ),
                        "block_state": observation.block_state,
                        "placed": observation.placed,
                        "relative": observation.relative.payload(),
                        "role": observation.role,
                        "route": observation.route,
                    }
                    for observation in item.observations
                ],
                "title": item.title,
            }
            for item in items
        ],
        "counts": counts(items),
        "evidence": {
            "all_the_mons": PACK_VERSION,
            "fusion": {
                "filename": FUSION_FILE,
                "sha256": FUSION_SHA256,
                "size_bytes": FUSION_SIZE,
                "version": FUSION_VERSION,
            },
            "glassential": {
                "filename": GLASSENTIAL_FILE,
                "sha256": GLASSENTIAL_SHA256,
                "size_bytes": GLASSENTIAL_SIZE,
                "version": GLASSENTIAL_VERSION,
            },
            "minecraft": MINECRAFT_VERSION,
        },
        "gallery": {
            "dimension": "minecraft:overworld",
            "floor_y": FLOOR_Y,
            "pose": {
                "pitch": POSE[4],
                "x": POSE[0],
                "y": POSE[1],
                "yaw": POSE[3],
                "z": POSE[2],
            },
            "region_max": {
                "x": REGION_MAX[0],
                "y": REGION_MAX[1],
                "z": REGION_MAX[2],
            },
            "region_min": {
                "x": REGION_MIN[0],
                "y": REGION_MIN[1],
                "z": REGION_MIN[2],
            },
        },
        "schema": 3,
    }
    return json.dumps(payload, indent=2, sort_keys=True) + "\n"


def tsv_text(value: str | None) -> str:
    if value is None:
        return ""
    return value.replace("\t", " ").replace("\r", " ").replace("\n", " ")


def cases_tsv(items: tuple[GalleryCase, ...]) -> str:
    header = (
        "case_index\tcase_id\ttitle\tanchor_x\tanchor_y\tanchor_z\t"
        "observation_index\tx\ty\tz\tblock_state\tblock_entity_snbt\t"
        "expected_block_entity_snbt\tplaced\troute\trole\tclient_gate\tnotes"
    )
    lines = [header]
    for item in items:
        for index, observation in enumerate(item.observations, start=1):
            absolute = item.anchor.offset(observation.relative)
            lines.append(
                "\t".join(
                    (
                        str(item.number),
                        item.case_id,
                        tsv_text(item.title),
                        str(item.anchor.x),
                        str(item.anchor.y),
                        str(item.anchor.z),
                        str(index),
                        str(absolute.x),
                        str(absolute.y),
                        str(absolute.z),
                        observation.block_state,
                        tsv_text(observation.block_entity_snbt),
                        tsv_text(observation.expected_block_entity_snbt),
                        bool_text(observation.placed),
                        observation.route,
                        tsv_text(observation.role),
                        tsv_text(item.client_gate),
                        tsv_text(item.notes),
                    )
                )
            )
    return "\n".join(lines) + "\n"


def generated_header() -> str:
    return "# Generated by gallery/generate.py; do not edit.\n"


def case_build_function(item: GalleryCase) -> str:
    lines = [generated_header().rstrip(), f"# Cell {item.number}: {item.title}"]
    for observation in item.observations:
        absolute = item.anchor.offset(observation.relative)
        state = observation.block_state if observation.placed else "minecraft:air"
        lines.append(f"setblock {absolute.command()} {state} replace")
        if observation.block_entity_snbt:
            lines.append(
                f"data merge block {absolute.command()} {observation.block_entity_snbt}"
            )
    return "\n".join(lines) + "\n"


def case_verify_function(item: GalleryCase) -> str:
    case_score = f"#case{item.number:02d}"
    lines = [
        generated_header().rstrip(),
        f"# Cell {item.number}: {item.title}",
        f"scoreboard players set {case_score} {OBJECTIVE} 0",
    ]
    for observation in item.observations:
        absolute = item.anchor.offset(observation.relative)
        lines.extend(
            (
                f"scoreboard players add #checked {OBJECTIVE} 1",
                f"scoreboard players set #observation {OBJECTIVE} 0",
                "execute unless block "
                f"{absolute.command()} {observation.block_state} run "
                f"scoreboard players set #observation {OBJECTIVE} 1",
            )
        )
        if observation.block_entity_snbt:
            expected_nbt = (
                observation.expected_block_entity_snbt
                or observation.block_entity_snbt
            )
            lines.append(
                "execute unless data block "
                f"{absolute.command()} {expected_nbt} run "
                f"scoreboard players set #observation {OBJECTIVE} 1"
            )
        lines.extend(
            (
                f"execute if score #observation {OBJECTIVE} matches 1 run "
                f"scoreboard players add #failures {OBJECTIVE} 1",
                f"execute if score #observation {OBJECTIVE} matches 1 run "
                f"scoreboard players add {case_score} {OBJECTIVE} 1",
            )
        )
    failure_message = json.dumps(
        {
            "color": "red",
            "text": f"Glassential gallery cell {item.number} failed: {item.title}",
        },
        separators=(",", ":"),
    )
    lines.extend(
        (
            f"execute if score {case_score} {OBJECTIVE} matches 1.. run "
            f"scoreboard players add #failed_cells {OBJECTIVE} 1",
            f"execute if score {case_score} {OBJECTIVE} matches 1.. run "
            f"tellraw @a {failure_message}",
        )
    )
    return "\n".join(lines) + "\n"


def clear_function() -> str:
    minimum = Position(*REGION_MIN).command()
    maximum = Position(*REGION_MAX).command()
    return (
        generated_header()
        + "execute in minecraft:overworld run forceload add "
        + f"{REGION_MIN[0]} {REGION_MIN[2]} {REGION_MAX[0]} {REGION_MAX[2]}\n"
        + f"execute in minecraft:overworld run fill {minimum} {maximum} minecraft:air replace\n"
    )


def build_function(items: tuple[GalleryCase, ...]) -> str:
    census = counts(items)
    lines = [
        generated_header().rstrip(),
        f"scoreboard objectives add {OBJECTIVE} dummy",
        f"function {NAMESPACE}:clear",
    ]
    for item in items:
        anchor = item.anchor
        lines.extend(
            (
                "execute in minecraft:overworld run fill "
                f"{anchor.x - CELL_HALF_SIZE} {FLOOR_Y} {anchor.z - CELL_HALF_SIZE} "
                f"{anchor.x + CELL_HALF_SIZE} {FLOOR_Y} {anchor.z + CELL_HALF_SIZE} "
                "minecraft:deepslate_tiles replace",
                "execute in minecraft:overworld run fill "
                f"{anchor.x - CELL_HALF_SIZE + 1} {FLOOR_Y} {anchor.z - CELL_HALF_SIZE + 1} "
                f"{anchor.x + CELL_HALF_SIZE - 1} {FLOOR_Y} {anchor.z + CELL_HALF_SIZE - 1} "
                "minecraft:smooth_stone replace",
                f"execute in minecraft:overworld run function "
                f"{NAMESPACE}:cell_build/{item.function_id}",
            )
        )
    lines.extend(
        (
            f"scoreboard players set #cells {OBJECTIVE} {census['cells']}",
            f"scoreboard players set #placements {OBJECTIVE} {census['placements']}",
            f"scoreboard players set #observations {OBJECTIVE} {census['observations']}",
            f"scoreboard players set #ready {OBJECTIVE} 1",
            f"function {NAMESPACE}:verify",
        )
    )
    return "\n".join(lines) + "\n"


def status_function() -> str:
    payload = [
        {"color": "aqua", "text": "Glassential gallery: "},
        {"text": "cells="},
        {"score": {"name": "#cells", "objective": OBJECTIVE}},
        {"text": ", placements="},
        {"score": {"name": "#placements", "objective": OBJECTIVE}},
        {"text": ", checked="},
        {"score": {"name": "#checked", "objective": OBJECTIVE}},
        {"text": ", failed_cells="},
        {"score": {"name": "#failed_cells", "objective": OBJECTIVE}},
        {"text": ", failures="},
        {"score": {"name": "#failures", "objective": OBJECTIVE}},
    ]
    return generated_header() + "tellraw @a " + json.dumps(
        payload, separators=(",", ":")
    ) + "\n"


def verify_function(items: tuple[GalleryCase, ...]) -> str:
    lines = [
        generated_header().rstrip(),
        f"scoreboard players set #checked {OBJECTIVE} 0",
        f"scoreboard players set #failed_cells {OBJECTIVE} 0",
        f"scoreboard players set #failures {OBJECTIVE} 0",
    ]
    lines.extend(
        f"execute in minecraft:overworld run function {NAMESPACE}:cell_verify/{item.function_id}"
        for item in items
    )
    lines.extend(
        (
            f"function {NAMESPACE}:status",
            f"execute if score #failures {OBJECTIVE} matches 0 run tellraw @a "
            '{"color":"green","text":"Glassential gallery verification passed."}',
            f"execute unless score #failures {OBJECTIVE} matches 0 run tellraw @a "
            '{"color":"red","text":"Glassential gallery verification failed."}',
        )
    )
    return "\n".join(lines) + "\n"


def generated_files(items: tuple[GalleryCase, ...]) -> dict[Path, bytes]:
    files: dict[Path, bytes] = {
        Path("cases.json"): cases_json(items).encode("utf-8"),
        Path("cases.tsv"): cases_tsv(items).encode("utf-8"),
        Path("datapack/pack.mcmeta"): (
            json.dumps(
                {
                    "pack": {
                        "description": (
                            "ATM 1.2.0 Glassential Renewed 3.4.5 + Fusion 1.3.12 "
                            "BlueMap review gallery"
                        ),
                        "pack_format": 48,
                    }
                },
                indent=2,
            )
            + "\n"
        ).encode("utf-8"),
        Path("datapack/data/minecraft/tags/function/load.json"): (
            json.dumps({"values": [f"{NAMESPACE}:load"]}, indent=2) + "\n"
        ).encode("utf-8"),
        Path(f"datapack/data/{NAMESPACE}/function/load.mcfunction"): (
            generated_header() + f"scoreboard objectives add {OBJECTIVE} dummy\n"
        ).encode("utf-8"),
        Path(f"datapack/data/{NAMESPACE}/function/clear.mcfunction"): (
            clear_function().encode("utf-8")
        ),
        Path(f"datapack/data/{NAMESPACE}/function/build.mcfunction"): (
            build_function(items).encode("utf-8")
        ),
        Path(f"datapack/data/{NAMESPACE}/function/verify.mcfunction"): (
            verify_function(items).encode("utf-8")
        ),
        Path(f"datapack/data/{NAMESPACE}/function/status.mcfunction"): (
            status_function().encode("utf-8")
        ),
        Path(f"datapack/data/{NAMESPACE}/function/pose.mcfunction"): (
            generated_header()
            + "execute in minecraft:overworld run tp @s "
            + " ".join(str(value) for value in POSE)
            + "\n"
        ).encode("utf-8"),
        Path(f"datapack/data/{NAMESPACE}/function/release.mcfunction"): (
            generated_header()
            + f"function {NAMESPACE}:clear\n"
            + f"scoreboard players set #ready {OBJECTIVE} 0\n"
            + "execute in minecraft:overworld run forceload remove "
            + f"{REGION_MIN[0]} {REGION_MIN[2]} {REGION_MAX[0]} {REGION_MAX[2]}\n"
        ).encode("utf-8"),
    }
    for item in items:
        files[
            Path(
                f"datapack/data/{NAMESPACE}/function/cell_build/{item.function_id}.mcfunction"
            )
        ] = case_build_function(item).encode("utf-8")
        files[
            Path(
                f"datapack/data/{NAMESPACE}/function/cell_verify/{item.function_id}.mcfunction"
            )
        ] = case_verify_function(item).encode("utf-8")

    checksum_lines = [
        f"{hashlib.sha256(content).hexdigest()}  {path.as_posix()}"
        for path, content in sorted(files.items(), key=lambda pair: pair[0].as_posix())
    ]
    files[Path("SHA256SUMS")] = ("\n".join(checksum_lines) + "\n").encode("ascii")
    return files


def existing_generated_paths() -> set[Path]:
    result = {
        path
        for path in (Path("cases.json"), Path("cases.tsv"), Path("SHA256SUMS"))
        if (ROOT / path).exists()
    }
    datapack = ROOT / "datapack"
    if datapack.exists():
        result.update(
            path.relative_to(ROOT) for path in datapack.rglob("*") if path.is_file()
        )
    return result


def write_or_check(files: dict[Path, bytes], check: bool) -> None:
    expected_paths = set(files)
    if check:
        differences: list[str] = []
        for path, content in sorted(files.items(), key=lambda pair: pair[0].as_posix()):
            target = ROOT / path
            if not target.is_file():
                differences.append(f"missing:{path.as_posix()}")
            elif target.read_bytes() != content:
                differences.append(f"changed:{path.as_posix()}")
        for path in sorted(existing_generated_paths() - expected_paths):
            differences.append(f"unexpected:{path.as_posix()}")
        if differences:
            raise ValueError("generated gallery differs: " + ", ".join(differences))
        return

    for path in sorted(existing_generated_paths() - expected_paths, reverse=True):
        (ROOT / path).unlink()
    for path, content in sorted(files.items(), key=lambda pair: pair[0].as_posix()):
        target = ROOT / path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(content)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--check",
        action="store_true",
        help="verify checked-in generated outputs instead of writing them",
    )
    parser.add_argument(
        "--glassential-jar",
        type=Path,
        help="optionally verify the exact Glassential JAR and fixture contracts",
    )
    parser.add_argument(
        "--fusion-jar",
        type=Path,
        help="optionally verify the exact Fusion JAR identity",
    )
    arguments = parser.parse_args()

    try:
        items = cases()
        if arguments.glassential_jar:
            verify_glassential_jar(arguments.glassential_jar, items)
        if arguments.fusion_jar:
            verify_fusion_jar(arguments.fusion_jar)
        write_or_check(generated_files(items), arguments.check)
    except (AssertionError, KeyError, OSError, ValueError, zipfile.BadZipFile) as error:
        print(f"gallery generation failed: {error}", file=sys.stderr)
        return 1

    census = counts(items)
    action = "checked" if arguments.check else "generated"
    print(
        f"{action} {census['cells']} cells, {census['placements']} placements, "
        f"and {census['observations']} exact observations"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
