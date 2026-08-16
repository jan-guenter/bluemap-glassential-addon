#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Generate the exact metadata-only Glassential 3.4.5/Fusion 1.3.12 profile.

Only independently derived identifiers, hashes, dimensions, schemas, and
counts are emitted. The operator-installed JSON, PNG, class, and JAR bytes are
never copied into this project.
"""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path, PurePosixPath
import struct
from typing import Any, Iterable
import zipfile


PROFILE_ROOT = Path("src/main/resources/bluemap-glassential/profiles")
PROFILE_DIRECTORY = PROFILE_ROOT / "glassential/3.4.5-fusion-1.3.12"
CATALOG_PATH = PROFILE_ROOT / "exact-artifacts.json"
PROFILE_PATH = PROFILE_DIRECTORY / "profile.json"
DEFINITIONS_PATH = PROFILE_DIRECTORY / "definitions.tsv"
DIRECT_MODELS_PATH = PROFILE_DIRECTORY / "direct-models.tsv"
RESOURCES_PATH = PROFILE_DIRECTORY / "required-resources.tsv"
TEXTURES_PATH = PROFILE_DIRECTORY / "textures.tsv"
HOST_RESOURCES_PATH = PROFILE_DIRECTORY / "host-resources.tsv"

GLASSENTIAL_FILENAME = "Glassential-renewed-1.21.1-3.4.5.jar"
GLASSENTIAL_SIZE = 702_249
GLASSENTIAL_SHA1 = "3a08f59f0930c8123fa1aacdfa0ba9fbdbb6e342"
GLASSENTIAL_SHA256 = (
    "1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471"
)
GLASSENTIAL_SHA512 = (
    "62ccb9057aab96ba656ec8ce357977360c1cc7761fedd7ac995a40b1f16e389c7"
    "5d753746840b11d30077b6b896938246fb281ec481e560a05084e22098c31d8"
)
FUSION_FILENAME = "fusion-1.3.12-neoforge-mc1.21.1.jar"
FUSION_SIZE = 923_270
FUSION_SHA1 = "79c0c6b6a2d9c9a04298df9a88bb71a93e885235"
FUSION_SHA256 = (
    "17f5215648a98bcde4134577b013200dbf363273ae282449c51408ae8346f2fa"
)
FUSION_SHA512 = (
    "a13d2a654988f021106f8a455134da1b515872e9122cf70bda064e663749f1c11"
    "aeddc3def23a621e236f2ffeefb6f56b15c2c63eeb2bca5f9833c5a2dc23a93"
)

MINECRAFT_CLIENT_SHA256 = (
    "499f6897d1837516680f3114072d8106e11c9adcd933fe5cf051b551089b0c99"
)

ALL_GLASSENTIAL_BLOCK_COUNT = 113
ALL_GLASSENTIAL_STATE_COUNT = 5_449
ROUTED_GLASSENTIAL_BLOCK_COUNT = 31
ROUTED_GLASSENTIAL_STATE_COUNT = 453
STOCK_GLASSENTIAL_BLOCK_COUNT = 82
STOCK_GLASSENTIAL_STATE_COUNT = 4_996
VANILLA_OVERRIDE_COUNT = 18
VANILLA_OVERRIDE_STATE_COUNT = 18
ROUTED_COUNT = 49
ROUTED_STATE_COUNT = 471
DIRECT_MODEL_COUNT = 95
FUSION_PROGRAM_COUNT = 93
ONE_WAY_PROGRAM_COUNT = 2
MODEL_COUNT = 100
PNG_COUNT = 39
CONNECTED_TEXTURE_COUNT = 34
MCMETA_COUNT = 34
RESOURCE_COUNT = 204
HOST_RESOURCE_COUNT = 22

ROUTED_GLASSENTIAL_NAMES = frozenset(
    {
        "colorable_glass",
        "colorable_glass_pane",
        "colorable_stained_glass",
        "colorable_stained_glass_pane",
        "glass_dark_ethereal",
        "glass_dark_ethereal_pane",
        "glass_dark_ethereal_reverse",
        "glass_dark_ethereal_reverse_pane",
        "glass_ethereal",
        "glass_ethereal_pane",
        "glass_ethereal_reverse",
        "glass_ethereal_reverse_pane",
        "glass_ghostly",
        "glass_ghostly_pane",
        "glass_light",
        "glass_light_pane",
        "glass_light_tinted",
        "glass_light_tinted_pane",
        "glass_redstone",
        "glass_redstone_pane",
        "glass_redstone_tinted",
        "glass_redstone_tinted_pane",
        "glass_slab",
        "gravity_glass",
        "ice_glass",
        "iron_glass",
        "obsidian_glass",
        "one_way_glass",
        "sandstone_glass",
        "stone_glass",
        "tinted_one_way_glass",
    }
)
COLORABLE_NAMES = frozenset(
    {
        "colorable_glass",
        "colorable_glass_pane",
        "colorable_stained_glass",
        "colorable_stained_glass_pane",
    }
)
ONE_WAY_NAMES = frozenset({"one_way_glass", "tinted_one_way_glass"})
VANILLA_OVERRIDE_NAMES = frozenset(
    {
        "black_stained_glass",
        "blue_stained_glass",
        "brown_stained_glass",
        "cyan_stained_glass",
        "glass",
        "gray_stained_glass",
        "green_stained_glass",
        "light_blue_stained_glass",
        "light_gray_stained_glass",
        "lime_stained_glass",
        "magenta_stained_glass",
        "orange_stained_glass",
        "pink_stained_glass",
        "purple_stained_glass",
        "red_stained_glass",
        "tinted_glass",
        "white_stained_glass",
        "yellow_stained_glass",
    }
)

SCHEMAS = {
    "propertyless": 1,
    "pane": 32,
    "colorable": 2,
    "colorable_pane": 64,
    "slab": 6,
    "one_way": 6,
}
LAYOUT_DIMENSIONS = {
    "full": (128, 128),
    "simple": (64, 64),
    "pieced": (80, 16),
}
PROGRAM_LAYOUT_COUNTS = {"full": 36, "simple": 54, "pieced": 3}
TEXTURE_LAYOUT_COUNTS = {"full": 22, "simple": 9, "pieced": 3, "plain": 5}
PREDICATE_TYPES = {
    "and",
    "is_direction",
    "is_same_block",
    "is_same_state",
    "match_state",
    "or",
}

# Exact Minecraft 1.21.1 client resources used as a separate host ABI. These
# bytes are validated at runtime but never copied into the add-on.
HOST_RESOURCES = (
    ("blockstate", "assets/minecraft/blockstates/black_stained_glass.json", 92, "4ca2acb7bcf0243bfc8ff6831cfc63e466c9b207913a9d21ab36a06d19ba6d25"),
    ("blockstate", "assets/minecraft/blockstates/blue_stained_glass.json", 91, "9b736f2da3d2560cd0c2d0899e8d3d157702d409a690a556891e0c25e04db656"),
    ("blockstate", "assets/minecraft/blockstates/brown_stained_glass.json", 92, "ffb3296e3ed3fed29788d7a086a1b5220f995292225fd3e95d83a021f7517111"),
    ("blockstate", "assets/minecraft/blockstates/cyan_stained_glass.json", 91, "26e2c22a84031c68ce83615acb65624e2520482d784d7ee5f2385553ebdea132"),
    ("blockstate", "assets/minecraft/blockstates/glass.json", 78, "1dbff97d8ac074c330024db2f2acc93eea26ffad0cd33e2f0442ca6e5145e9d5"),
    ("blockstate", "assets/minecraft/blockstates/gray_stained_glass.json", 91, "8bc07dc2fc36053a4a6fea2a798ecda2d747e0b91af5cf156175a679a255764d"),
    ("blockstate", "assets/minecraft/blockstates/green_stained_glass.json", 92, "ad5614c8066d8dd840a12ddb8e180271dfdb3e9b5b46ca160d1e29f4fa11a6bc"),
    ("blockstate", "assets/minecraft/blockstates/light_blue_stained_glass.json", 97, "31959a246bd718f7cd52affdf7cedd7dc9ae091f9d4a8227953d83d5bc172e78"),
    ("blockstate", "assets/minecraft/blockstates/light_gray_stained_glass.json", 97, "709371a89298b3944fa975d2729844574a02c2fe7702f0157db99e9e279c553c"),
    ("blockstate", "assets/minecraft/blockstates/lime_stained_glass.json", 91, "d5623dee377d8e7b2919d582ed6c2277b563a4848d82eb9c9c08e6063e08400f"),
    ("blockstate", "assets/minecraft/blockstates/magenta_stained_glass.json", 94, "708a98d39d20cf64704879fac750fd61cdda505ef11479236d3883a911dee182"),
    ("blockstate", "assets/minecraft/blockstates/orange_stained_glass.json", 93, "2448fdef559ba9d3e1e39b5ac07ef534223a8739c2dc09eb3356a7d80d5c1e9b"),
    ("blockstate", "assets/minecraft/blockstates/pink_stained_glass.json", 91, "28a5f79957fe60f2e1f7f3ad20e153eb55bfd30435b49433f1c53a6d58b0f91e"),
    ("blockstate", "assets/minecraft/blockstates/purple_stained_glass.json", 93, "0d904cb96bc4d4560a60e5d387c8b35bde4cb267b43a869ac374f61ac1d35592"),
    ("blockstate", "assets/minecraft/blockstates/red_stained_glass.json", 90, "bf0a679a5a2b3849544bc642c7dba47e0ca3d7285c1acf0d633c1f152f9f19ca"),
    ("blockstate", "assets/minecraft/blockstates/tinted_glass.json", 85, "495c5dcce129be9ffa89211444f272adde34eb360297dc5e3255a66c4b533cff"),
    ("blockstate", "assets/minecraft/blockstates/white_stained_glass.json", 92, "25b70ed75e066e90d725aecc1786061a6acfb058b9e280929c3fccbd74a52008"),
    ("blockstate", "assets/minecraft/blockstates/yellow_stained_glass.json", 93, "44d1e862eaef54f37e19564e616239ad4631246cf724e03bd55e98ff4728ca09"),
    ("model", "assets/minecraft/models/block/cube_all.json", 227, "be3205d629ffb9e03834c3d1d083f8a2c62e9f9ae80820755129408634e9144a"),
    ("model", "assets/minecraft/models/block/slab.json", 761, "bd869ebe3ba380d46349e5c6e988b9b1ccf1ab25212ab1de66e2fdcc067edc1d"),
    ("model", "assets/minecraft/models/block/slab_top.json", 733, "c02e81cd0b59698040db7a682d32d08ddeb0de64756e309d62ecfbda4af804f9"),
    ("texture", "assets/minecraft/textures/block/glass_pane_top.png", 116, "cf813fd7c62ff76a81a5d1a55df02bd9f7565be23539a8896b757eff468ec3de"),
)


def digest_bytes(raw: bytes, algorithm: str = "sha256") -> str:
    return hashlib.new(algorithm, raw).hexdigest()


def digest_path(path: Path, algorithm: str) -> str:
    value = hashlib.new(algorithm)
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def roster_digest(values: Iterable[str]) -> str:
    payload = "".join(f"{value}\n" for value in sorted(values)).encode("utf-8")
    return digest_bytes(payload)


def canonical_json(value: Any) -> bytes:
    return (
        json.dumps(value, ensure_ascii=True, sort_keys=True, indent=2) + "\n"
    ).encode("ascii")


def resource_path(key: str, kind: str, suffix: str) -> str:
    namespace, value = key.split(":", 1) if ":" in key else ("minecraft", key)
    path = PurePosixPath(value)
    if path.is_absolute() or any(part in {"", ".", ".."} for part in path.parts):
        raise ValueError(f"unsafe resource key: {key}")
    return f"assets/{namespace}/{kind}/{value}{suffix}"


def model_key(value: str, default_namespace: str = "minecraft") -> str:
    return value if ":" in value else f"{default_namespace}:{value}"


def model_path(key: str) -> str:
    return resource_path(key, "models", ".json")


def texture_path(key: str, suffix: str = ".png") -> str:
    return resource_path(key, "textures", suffix)


def _verify_identity(
    path: Path, *, filename: str, size: int, sha1: str, sha256: str, sha512: str
) -> None:
    if not path.is_file() or path.name != filename:
        raise ValueError(f"unexpected artifact path: {path}")
    if path.stat().st_size != size:
        raise ValueError(f"unexpected artifact size for {path}")
    for algorithm, expected in (
        ("sha1", sha1),
        ("sha256", sha256),
        ("sha512", sha512),
    ):
        actual = digest_path(path, algorithm)
        if actual != expected:
            raise ValueError(
                f"{path.name} {algorithm} changed: got {actual}, expected {expected}"
            )


def _application(value: Any, context: str) -> dict[str, Any]:
    if not isinstance(value, dict) or not isinstance(value.get("model"), str):
        raise ValueError(f"{context} model application changed")
    if not set(value).issubset({"model", "x", "y", "uvlock", "weight"}):
        raise ValueError(f"{context} model application keys changed")
    return value


def _applications(value: Any, context: str) -> list[dict[str, Any]]:
    if not isinstance(value, dict) or set(value) not in ({"variants"}, {"multipart"}):
        raise ValueError(f"{context} blockstate root changed")
    output: list[dict[str, Any]] = []
    if "variants" in value:
        variants = value["variants"]
        if not isinstance(variants, dict):
            raise ValueError(f"{context} variants changed")
        for selector, application in variants.items():
            rows = application if isinstance(application, list) else [application]
            for row in rows:
                output.append({"selector": selector, "apply": _application(row, context)})
    else:
        multipart = value["multipart"]
        if not isinstance(multipart, list):
            raise ValueError(f"{context} multipart changed")
        for part in multipart:
            if not isinstance(part, dict) or "apply" not in part:
                raise ValueError(f"{context} multipart part changed")
            rows = part["apply"] if isinstance(part["apply"], list) else [part["apply"]]
            for row in rows:
                output.append({"when": part.get("when"), "apply": _application(row, context)})
    return output


def _schema_for(name: str) -> tuple[str, str, str, int]:
    if name in ONE_WAY_NAMES:
        return "one_way", "one_way", "one_way", SCHEMAS["one_way"]
    if name == "glass_slab":
        return "slab", "slab", "fusion", SCHEMAS["slab"]
    if name.endswith("_pane"):
        schema = "colorable_pane" if name in COLORABLE_NAMES else "pane"
        return "pane", schema, "fusion_textured_regular", SCHEMAS[schema]
    if name in COLORABLE_NAMES:
        return "full", "colorable", "fusion", SCHEMAS["colorable"]
    return "full", "propertyless", "fusion", SCHEMAS["propertyless"]


def _validate_selectors(name: str, schema: str, value: dict[str, Any]) -> None:
    if schema == "propertyless" and set(value.get("variants", {})) != {""}:
        raise ValueError(f"{name} propertyless selector changed")
    if schema == "colorable" and set(value.get("variants", {})) != {
        "lit=false",
        "lit=true",
    }:
        raise ValueError(f"{name} colorable selector changed")
    if schema == "slab" and set(value.get("variants", {})) != {
        "type=bottom",
        "type=double",
        "type=top",
    }:
        raise ValueError(f"{name} slab selector changed")
    if schema == "one_way" and set(value.get("variants", {})) != {
        "opaque_face=down",
        "opaque_face=east",
        "opaque_face=north",
        "opaque_face=south",
        "opaque_face=up",
        "opaque_face=west",
    }:
        raise ValueError(f"{name} one-way selector changed")
    if schema in {"pane", "colorable_pane"}:
        if "multipart" not in value or not isinstance(value["multipart"], list):
            raise ValueError(f"{name} pane selector changed")
        expected = {"north", "east", "south", "west"}
        seen: set[str] = set()
        for part in value["multipart"]:
            when = part.get("when", {}) if isinstance(part, dict) else {}
            if isinstance(when, dict):
                seen.update(key for key in when if key in expected)
        if seen != expected:
            raise ValueError(f"{name} pane direction coverage changed")


def _texture_layout(archive: zipfile.ZipFile, path: str) -> tuple[str, int, int, str]:
    raw = archive.read(path)
    if raw[:8] != b"\x89PNG\r\n\x1a\n" or len(raw) < 24:
        raise ValueError(f"invalid PNG {path}")
    width, height = struct.unpack(">II", raw[16:24])
    metadata_path = f"{path}.mcmeta"
    if metadata_path not in archive.namelist():
        if (width, height) != (16, 16):
            raise ValueError(f"plain texture dimensions changed for {path}")
        return "plain", width, height, "-"
    metadata_raw = archive.read(metadata_path)
    metadata = json.loads(metadata_raw)
    fusion = metadata.get("fusion") if isinstance(metadata, dict) else None
    if (
        not isinstance(fusion, dict)
        or fusion.get("type") != "connecting"
        or fusion.get("layout") not in LAYOUT_DIMENSIONS
        or not set(fusion).issubset({"type", "layout", "render_type"})
    ):
        raise ValueError(f"Fusion metadata schema changed for {metadata_path}")
    layout = fusion["layout"]
    if (width, height) != LAYOUT_DIMENSIONS[layout]:
        raise ValueError(f"Fusion sheet dimensions changed for {path}")
    return layout, width, height, digest_bytes(metadata_raw)


def _resolved_textures(model: str, models: dict[str, dict[str, Any]]) -> dict[str, str]:
    value = models[model]
    result: dict[str, str] = {}
    parent = value.get("parent")
    if isinstance(parent, str):
        parent_key = model_key(parent)
        if parent_key in models:
            result.update(_resolved_textures(parent_key, models))
    textures = value.get("textures", {})
    if not isinstance(textures, dict):
        raise ValueError(f"texture map changed for {model}")
    for name, texture in textures.items():
        if isinstance(texture, str):
            result[name] = texture
    return result


def _literal_texture_keys(model: str, models: dict[str, dict[str, Any]]) -> set[str]:
    values = _resolved_textures(model, models)
    output: set[str] = set()
    for start in values.values():
        current = start
        seen: set[str] = set()
        while current.startswith("#"):
            name = current[1:]
            if name in seen or name not in values:
                raise ValueError(f"cyclic or missing texture reference for {model}")
            seen.add(name)
            current = values[name]
        output.add(model_key(current))
    return output


def _walk_predicates(value: Any, output: set[str]) -> None:
    if isinstance(value, dict):
        predicate = value.get("type")
        if predicate in PREDICATE_TYPES:
            output.add(predicate)
        for child in value.values():
            _walk_predicates(child, output)
    elif isinstance(value, list):
        for child in value:
            _walk_predicates(child, output)


def build_outputs(glassential: Path, fusion: Path) -> dict[Path, bytes]:
    _verify_identity(
        glassential,
        filename=GLASSENTIAL_FILENAME,
        size=GLASSENTIAL_SIZE,
        sha1=GLASSENTIAL_SHA1,
        sha256=GLASSENTIAL_SHA256,
        sha512=GLASSENTIAL_SHA512,
    )
    _verify_identity(
        fusion,
        filename=FUSION_FILENAME,
        size=FUSION_SIZE,
        sha1=FUSION_SHA1,
        sha256=FUSION_SHA256,
        sha512=FUSION_SHA512,
    )

    definitions: list[str] = []
    direct_models: dict[str, str] = {}
    operator_blockstates: set[str] = set()
    selected_models: set[str] = set()
    models: dict[str, dict[str, Any]] = {}
    texture_keys: set[str] = set()
    predicate_types: set[str] = set()

    with zipfile.ZipFile(glassential) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)):
            raise ValueError("Glassential JAR contains duplicate ZIP entries")
        available = set(names)
        all_blockstates = sorted(
            path
            for path in available
            if path.startswith("assets/glassential/blockstates/")
            and path.endswith(".json")
        )
        if len(all_blockstates) != ALL_GLASSENTIAL_BLOCK_COUNT:
            raise ValueError("Glassential blockstate roster changed")
        observed_names = {
            path.removeprefix("assets/glassential/blockstates/").removesuffix(".json")
            for path in all_blockstates
        }
        if not ROUTED_GLASSENTIAL_NAMES.issubset(observed_names):
            raise ValueError("routed Glassential blockstate roster changed")

        for name in sorted(ROUTED_GLASSENTIAL_NAMES):
            path = f"assets/glassential/blockstates/{name}.json"
            raw = archive.read(path)
            value = json.loads(raw)
            applications = _applications(value, name)
            shape, schema, mode, legal_states = _schema_for(name)
            _validate_selectors(name, schema, value)
            model_keys = {
                model_key(row["apply"]["model"], "glassential")
                for row in applications
            }
            expected_mode = "one_way" if mode == "one_way" else "fusion"
            for key in model_keys:
                previous = direct_models.setdefault(key, expected_mode)
                if previous != expected_mode:
                    raise ValueError(f"direct model mode conflict for {key}")
            operator_blockstates.add(path)
            definitions.append(
                "\t".join(
                    (
                        f"glassential:{name}",
                        shape,
                        schema,
                        mode,
                        str(legal_states),
                        digest_bytes(raw),
                        roster_digest(model_keys),
                    )
                )
            )

        host_blockstate_by_name = {
            path.split("/")[-1].removesuffix(".json"): (size, digest)
            for kind, path, size, digest in HOST_RESOURCES
            if kind == "blockstate"
        }
        if set(host_blockstate_by_name) != VANILLA_OVERRIDE_NAMES:
            raise ValueError("host vanilla blockstate ABI changed")
        for name in sorted(VANILLA_OVERRIDE_NAMES):
            key = f"minecraft:block/{name}"
            direct_models[key] = "fusion"
            size, blockstate_digest = host_blockstate_by_name[name]
            if size < 1:
                raise ValueError("invalid host blockstate size")
            definitions.append(
                "\t".join(
                    (
                        f"minecraft:{name}",
                        "full",
                        "propertyless",
                        "fusion",
                        "1",
                        blockstate_digest,
                        roster_digest({key}),
                    )
                )
            )

        if len(definitions) != ROUTED_COUNT or len(direct_models) != DIRECT_MODEL_COUNT:
            raise ValueError("routed definition or direct-model census changed")
        if sum(mode == "fusion" for mode in direct_models.values()) != FUSION_PROGRAM_COUNT:
            raise ValueError("Fusion direct-program census changed")
        if sum(mode == "one_way" for mode in direct_models.values()) != ONE_WAY_PROGRAM_COUNT:
            raise ValueError("one-way direct-program census changed")

        pending = list(direct_models)
        external_models: set[str] = set()
        while pending:
            key = pending.pop()
            if key in selected_models:
                continue
            path = model_path(key)
            if path not in available:
                external_models.add(key)
                continue
            selected_models.add(key)
            value = json.loads(archive.read(path))
            if not isinstance(value, dict):
                raise ValueError(f"model root changed for {path}")
            models[key] = value
            parent = value.get("parent")
            if isinstance(parent, str):
                pending.append(model_key(parent))
            glass_model = value.get("glass_model")
            if isinstance(glass_model, str):
                pending.append(model_key(glass_model))

        if len(selected_models) != MODEL_COUNT:
            raise ValueError("selected model closure changed")
        if external_models != {
            "minecraft:block/cube_all",
            "minecraft:block/slab",
            "minecraft:block/slab_top",
        }:
            raise ValueError(f"host model ABI changed: {sorted(external_models)}")

        # Resolve texture inheritance from each selected blockstate program.
        # Shared template models intentionally contain unresolved #pane/#edge
        # references until a direct child supplies them.
        for key in direct_models:
            texture_keys.update(_literal_texture_keys(key, models))
        external_textures = {
            key for key in texture_keys if texture_path(key) not in available
        }
        if external_textures != {"minecraft:block/glass_pane_top"}:
            raise ValueError(f"host texture ABI changed: {sorted(external_textures)}")
        operator_texture_keys = texture_keys - external_textures
        if len(operator_texture_keys) != PNG_COUNT:
            raise ValueError("operator texture census changed")

        layouts: dict[str, tuple[str, int, int, str]] = {}
        for key in sorted(operator_texture_keys):
            path = texture_path(key)
            layouts[key] = _texture_layout(archive, path)
        observed_layout_counts = {
            layout: sum(row[0] == layout for row in layouts.values())
            for layout in TEXTURE_LAYOUT_COUNTS
        }
        if observed_layout_counts != TEXTURE_LAYOUT_COUNTS:
            raise ValueError("texture layout census changed")

        program_layouts: dict[str, int] = {key: 0 for key in PROGRAM_LAYOUT_COUNTS}
        for key, mode in direct_models.items():
            value = models[key]
            if mode == "one_way":
                if value.get("loader") != "glassential:one_way_loader":
                    raise ValueError(f"one-way loader changed for {key}")
                continue
            loader = value.get("loader")
            if loader not in (None, "fusion:model"):
                raise ValueError(f"selected Fusion program loader changed for {key}")
            if loader is None and not key.startswith("glassential:block/pane/"):
                raise ValueError(f"ordinary selected model left pane route: {key}")
            connected_layouts = {
                layouts[texture][0]
                for texture in _literal_texture_keys(key, models)
                if texture in layouts and layouts[texture][0] != "plain"
            }
            if len(connected_layouts) != 1:
                raise ValueError(f"selected model connected-layout count changed for {key}")
            program_layouts[next(iter(connected_layouts))] += 1
            _walk_predicates(value.get("connections"), predicate_types)
        if program_layouts != PROGRAM_LAYOUT_COUNTS:
            raise ValueError("program layout census changed")
        if predicate_types != PREDICATE_TYPES:
            raise ValueError("Fusion predicate vocabulary changed")

        model_paths = {model_path(key) for key in selected_models}
        png_paths = {texture_path(key) for key in operator_texture_keys}
        metadata_paths = {
            f"{path}.mcmeta" for path in png_paths if f"{path}.mcmeta" in available
        }
        if len(metadata_paths) != MCMETA_COUNT:
            raise ValueError("Fusion metadata census changed")
        closure = sorted(operator_blockstates | model_paths | png_paths | metadata_paths)
        if len(closure) != RESOURCE_COUNT or len(closure) != len(set(closure)):
            raise ValueError("operator resource closure changed")
        resource_rows: list[str] = []
        for path in closure:
            if "/blockstates/" in path:
                kind = "blockstate"
            elif "/models/" in path:
                kind = "model"
            elif path.endswith(".png.mcmeta"):
                kind = "metadata"
            else:
                kind = "texture"
            raw = archive.read(path)
            resource_rows.append(f"{kind}\t{path}\t{len(raw)}\t{digest_bytes(raw)}")

    definitions_raw = ("\n".join(sorted(definitions)) + "\n").encode("ascii")
    direct_models_raw = (
        "\n".join(f"{key}\t{mode}" for key, mode in sorted(direct_models.items()))
        + "\n"
    ).encode("ascii")
    resources_raw = ("\n".join(resource_rows) + "\n").encode("ascii")
    textures_raw = (
        "\n".join(
            "\t".join((key, layout, str(width), str(height), metadata_digest))
            for key, (layout, width, height, metadata_digest) in sorted(layouts.items())
        )
        + "\n"
    ).encode("ascii")
    host_resources_raw = (
        "\n".join(
            f"{kind}\t{path}\t{size}\t{sha256}"
            for kind, path, size, sha256 in HOST_RESOURCES
        )
        + "\n"
    ).encode("ascii")
    if len(HOST_RESOURCES) != HOST_RESOURCE_COUNT:
        raise ValueError("host resource ABI census changed")

    catalog = {
        "schema": 1,
        "artifacts": [
            {
                "modId": "glassential",
                "version": "3.4.5",
                "filename": GLASSENTIAL_FILENAME,
                "size": GLASSENTIAL_SIZE,
                "sha1": GLASSENTIAL_SHA1,
                "sha256": GLASSENTIAL_SHA256,
                "sha512": GLASSENTIAL_SHA512,
            },
            {
                "modId": "fusion",
                "version": "1.3.12",
                "filename": FUSION_FILENAME,
                "size": FUSION_SIZE,
                "sha1": FUSION_SHA1,
                "sha256": FUSION_SHA256,
                "sha512": FUSION_SHA512,
            },
        ],
        "requiredForStaticRendering": ["glassential", "fusion"],
    }
    profile = {
        "schema": 2,
        "profileId": "glassential-fusion-3.4.5-1.3.12",
        "namespaceOwners": ["glassential", "minecraft-exact-18-override-allowlist"],
        "formatOwner": "fusion",
        "counts": {
            "allGlassentialBlocks": ALL_GLASSENTIAL_BLOCK_COUNT,
            "allGlassentialLegalStates": ALL_GLASSENTIAL_STATE_COUNT,
            "routedGlassentialBlocks": ROUTED_GLASSENTIAL_BLOCK_COUNT,
            "routedGlassentialLegalStates": ROUTED_GLASSENTIAL_STATE_COUNT,
            "stockGlassentialBlocks": STOCK_GLASSENTIAL_BLOCK_COUNT,
            "stockGlassentialLegalStates": STOCK_GLASSENTIAL_STATE_COUNT,
            "vanillaOverrideBlocks": VANILLA_OVERRIDE_COUNT,
            "routedBlocks": ROUTED_COUNT,
            "routedLegalStates": ROUTED_STATE_COUNT,
            "directRenderPrograms": DIRECT_MODEL_COUNT,
            "fusionPrograms": FUSION_PROGRAM_COUNT,
            "oneWayPrograms": ONE_WAY_PROGRAM_COUNT,
            "selectedModelClosure": MODEL_COUNT,
            "operatorTextures": PNG_COUNT,
            "connectedTextureSheets": CONNECTED_TEXTURE_COUNT,
            "fusionMetadata": MCMETA_COUNT,
            "operatorResourceClosure": RESOURCE_COUNT,
            "hostResourceAbi": HOST_RESOURCE_COUNT,
        },
        "stateSchemas": SCHEMAS,
        "textureLayouts": TEXTURE_LAYOUT_COUNTS,
        "programLayouts": PROGRAM_LAYOUT_COUNTS,
        "predicateTypes": sorted(PREDICATE_TYPES),
        "digests": {
            "allGlassentialBlockstateRoster": roster_digest(all_blockstates),
            "routedGlassentialBlockRoster": roster_digest(ROUTED_GLASSENTIAL_NAMES),
            "vanillaOverrideBlockRoster": roster_digest(VANILLA_OVERRIDE_NAMES),
            "directModelRoster": roster_digest(direct_models),
            "selectedModelRoster": roster_digest(selected_models),
            "operatorTextureRoster": roster_digest(operator_texture_keys),
            "operatorResourcePathClosure": roster_digest(closure),
            "definitions": digest_bytes(definitions_raw),
            "directModels": digest_bytes(direct_models_raw),
            "requiredResources": digest_bytes(resources_raw),
            "textures": digest_bytes(textures_raw),
            "hostResources": digest_bytes(host_resources_raw),
        },
        "hostResourceAbi": {
            "owner": "minecraft",
            "clientJarSha256": MINECRAFT_CLIENT_SHA256,
            "pathsAreOutsideOperatorClosure": True,
        },
        "textureOverridePolicy": "pixel-only-exact-dimensions",
        "failurePolicy": "route-wide-inactive-or-atomic-stock-fallback",
        "assetPolicy": "operator-installed-only",
    }
    return {
        CATALOG_PATH: canonical_json(catalog),
        PROFILE_PATH: canonical_json(profile),
        DEFINITIONS_PATH: definitions_raw,
        DIRECT_MODELS_PATH: direct_models_raw,
        RESOURCES_PATH: resources_raw,
        TEXTURES_PATH: textures_raw,
        HOST_RESOURCES_PATH: host_resources_raw,
    }


def write_or_check(outputs: dict[Path, bytes], check: bool) -> str:
    changed: list[str] = []
    expected = set(outputs)
    actual = set(PROFILE_ROOT.rglob("*")) if PROFILE_ROOT.is_dir() else set()
    unexpected = sorted(str(path) for path in actual if path.is_file() and path not in expected)
    if unexpected:
        raise ValueError("unexpected generated profile resources: " + ", ".join(unexpected))
    for path, raw in outputs.items():
        if not path.is_file() or path.read_bytes() != raw:
            changed.append(str(path))
            if not check:
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(raw)
    if check and changed:
        raise ValueError("generated profile is stale: " + ", ".join(changed))
    return "verified" if check else "generated"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--glassential", required=True, type=Path)
    parser.add_argument("--fusion", required=True, type=Path)
    parser.add_argument("--check", action="store_true")
    arguments = parser.parse_args()
    outputs = build_outputs(arguments.glassential, arguments.fusion)
    action = write_or_check(outputs, arguments.check)
    print(f"{action} exact Glassential/Fusion profile ({ROUTED_COUNT} routed blocks)")


if __name__ == "__main__":
    main()
