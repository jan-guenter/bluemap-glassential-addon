# SPDX-License-Identifier: MIT
"""Unit coverage for deterministic fail-closed profile generation."""

from __future__ import annotations

import importlib.util
from pathlib import Path
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location(
    "generate_profile", ROOT / "tools/generate_profile.py"
)
assert SPEC is not None and SPEC.loader is not None
generate_profile = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(generate_profile)


class ProfileHelpersTest(unittest.TestCase):
    def test_roster_digest_is_sorted_and_newline_terminated(self) -> None:
        self.assertEqual(
            generate_profile.roster_digest(["z", "a"]),
            generate_profile.digest_bytes(b"a\nz\n"),
        )

    def test_resource_path_rejects_parent_escape(self) -> None:
        with self.assertRaisesRegex(ValueError, "unsafe resource key"):
            generate_profile.resource_path("glassential:../escape", "models", ".json")

    def test_identity_gate_rejects_wrong_filename_before_hashing(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "wrong.jar"
            path.write_bytes(b"")
            with self.assertRaisesRegex(ValueError, "unexpected artifact path"):
                generate_profile._verify_identity(  # noqa: SLF001 - exact helper test
                    path,
                    filename="expected.jar",
                    size=0,
                    sha1="",
                    sha256="",
                    sha512="",
                )

    def test_canonical_json_is_order_independent(self) -> None:
        self.assertEqual(
            generate_profile.canonical_json({"b": 2, "a": 1}),
            b'{\n  "a": 1,\n  "b": 2\n}\n',
        )

    def test_full_profile_constants_lock_exact_closure(self) -> None:
        self.assertEqual(49, generate_profile.ROUTED_COUNT)
        self.assertEqual(471, generate_profile.ROUTED_STATE_COUNT)
        self.assertEqual(31, generate_profile.ROUTED_GLASSENTIAL_BLOCK_COUNT)
        self.assertEqual(18, generate_profile.VANILLA_OVERRIDE_COUNT)
        self.assertEqual(95, generate_profile.DIRECT_MODEL_COUNT)
        self.assertEqual(93, generate_profile.FUSION_PROGRAM_COUNT)
        self.assertEqual(2, generate_profile.ONE_WAY_PROGRAM_COUNT)
        self.assertEqual(100, generate_profile.MODEL_COUNT)
        self.assertEqual(39, generate_profile.PNG_COUNT)
        self.assertEqual(34, generate_profile.MCMETA_COUNT)
        self.assertEqual(204, generate_profile.RESOURCE_COUNT)
        self.assertEqual(22, generate_profile.HOST_RESOURCE_COUNT)
        host_rows = "".join(
            f"{kind}\t{path}\t{size}\t{sha256}\n"
            for kind, path, size, sha256 in generate_profile.HOST_RESOURCES
        ).encode("ascii")
        self.assertEqual(
            "2b63c65a78eefce67a9cb51759d0f375f02ade20aa3f3c27aaad10555a9e6221",
            generate_profile.digest_bytes(host_rows),
        )

    def test_exact_shape_and_program_census(self) -> None:
        self.assertEqual(
            {"propertyless": 1, "pane": 32, "colorable": 2,
             "colorable_pane": 64, "slab": 6, "one_way": 6},
            generate_profile.SCHEMAS,
        )
        self.assertEqual(
            {"full": 36, "simple": 54, "pieced": 3},
            generate_profile.PROGRAM_LAYOUT_COUNTS,
        )
        self.assertEqual(
            {"full": 22, "simple": 9, "pieced": 3, "plain": 5},
            generate_profile.TEXTURE_LAYOUT_COUNTS,
        )


if __name__ == "__main__":
    unittest.main()
