#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Fail-closed review gate for exact Glassential 3.4.5/Fusion 1.3.12."""

from __future__ import annotations

import argparse
from pathlib import Path
import sys
import zipfile

import generate_profile


def _verify_mod_metadata(glassential: Path, fusion: Path) -> None:
    with zipfile.ZipFile(glassential) as archive:
        try:
            metadata = archive.read("META-INF/neoforge.mods.toml")
        except KeyError as error:
            raise ValueError("missing Glassential NeoForge metadata") from error
        if (b'modId = "glassential"' not in metadata
                or b'version = "3.4.5"' not in metadata
                or b'license = "MIT"' not in metadata):
            raise ValueError("Glassential NeoForge metadata identity changed")
        names = archive.namelist()
        if not any(name.startswith("assets/glassential/") for name in names):
            raise ValueError("Glassential archive has no installed resource root")
        if not any(
            name.startswith("com/github/bigenergy/glassential/") for name in names
        ):
            raise ValueError("Glassential archive has no expected implementation package")
        if any(name.startswith("com/supermartijn642/fusion/") for name in names):
            raise ValueError("Glassential archive unexpectedly embeds Fusion classes")

    with zipfile.ZipFile(fusion) as archive:
        names = archive.namelist()
        if "META-INF/neoforge.mods.toml" not in names:
            raise ValueError("Fusion archive has no NeoForge metadata")
        metadata = archive.read("META-INF/neoforge.mods.toml")
        if (b'modId = "fusion"' not in metadata
                or b'version = "1.3.12"' not in metadata
                or b'license = "All rights reserved"' not in metadata):
            raise ValueError("Fusion NeoForge metadata identity changed")
        if not any(name.startswith("com/supermartijn642/fusion/") for name in names):
            raise ValueError("Fusion archive has no expected implementation package")


def verify(glassential: Path, fusion: Path) -> None:
    outputs = generate_profile.build_outputs(glassential, fusion)
    generate_profile.write_or_check(outputs, check=True)
    _verify_mod_metadata(glassential, fusion)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--glassential", required=True, type=Path)
    parser.add_argument("--fusion", required=True, type=Path)
    args = parser.parse_args()
    try:
        verify(args.glassential, args.fusion)
    except (OSError, ValueError, zipfile.BadZipFile) as error:
        print(f"artifact verification failed: {error}", file=sys.stderr)
        return 1
    print(
        "Verified exact Glassential 3.4.5 + Fusion 1.3.12 artifacts, "
        "49 routed definitions, 471 legal states, the 204-path operator "
        "closure, and the 22-path host ABI."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
