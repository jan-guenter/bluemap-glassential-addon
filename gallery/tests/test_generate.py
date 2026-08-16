# SPDX-License-Identifier: MIT
"""Contract tests for the deterministic representative gallery."""

from __future__ import annotations

import importlib.util
import json
from pathlib import Path
import sys
import unittest


GALLERY = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location(
    "glassential_gallery_generate", GALLERY / "generate.py"
)
assert SPEC is not None and SPEC.loader is not None
GENERATOR = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = GENERATOR
SPEC.loader.exec_module(GENERATOR)


class GalleryGeneratorTest(unittest.TestCase):
    def setUp(self) -> None:
        self.cases = GENERATOR.cases()

    def test_exact_representative_census(self) -> None:
        self.assertEqual(
            {
                "block_entity_placements": 13,
                "cells": 6,
                "custom_route_placements": 69,
                "glassential_pane_placements": 22,
                "negative_space_observations": 3,
                "observations": 83,
                "placements": 80,
                "stock_route_placements": 11,
                "vanilla_override_placements": 20,
                "waterlogged_placements": 1,
            },
            GENERATOR.counts(self.cases),
        )

    def test_exact_vanilla_override_allowlist_is_represented(self) -> None:
        represented = {
            GENERATOR.block_id(observation.block_state)
            for case in self.cases
            for observation in case.observations
            if observation.route == "custom"
            and GENERATOR.block_id(observation.block_state).startswith("minecraft:")
        }
        self.assertEqual(set(GENERATOR.VANILLA_OVERRIDE_IDS), represented)
        self.assertEqual(18, len(represented))

    def test_all_panes_have_complete_states(self) -> None:
        pane_states = [
            observation.block_state
            for case in self.cases
            for observation in case.observations
            if "pane" in GENERATOR.block_id(observation.block_state)
        ]
        self.assertEqual(23, len(pane_states))
        for state in pane_states:
            properties = state.split("[", 1)[1].removesuffix("]").split(",")
            self.assertEqual(
                ["east", "north", "south", "waterlogged", "west"],
                [property_.split("=", 1)[0] for property_ in properties[:5]],
            )
            if GENERATOR.block_id(state).startswith("glassential:colorable_"):
                self.assertEqual("lit", properties[5].split("=", 1)[0])

    def test_dynamic_block_entities_are_explicit_and_distinct(self) -> None:
        dynamic = [
            observation
            for case in self.cases
            for observation in case.observations
            if observation.block_entity_snbt is not None
        ]
        colors = [
            observation.block_entity_snbt
            for observation in dynamic
            if "Color:" in observation.block_entity_snbt
        ]
        mimics = [
            observation.block_entity_snbt
            for observation in dynamic
            if "Mimic:" in observation.block_entity_snbt
        ]
        self.assertEqual(8, len(colors))
        self.assertEqual(8, len(set(colors)))
        self.assertEqual(5, len(mimics))
        self.assertEqual(5, len(set(mimics)))
        build = GENERATOR.case_build_function(self.cases[3])
        self.assertEqual(8, build.count("data merge block"))
        verify = GENERATOR.case_verify_function(self.cases[3])
        self.assertEqual(8, verify.count("unless data block"))

        malformed = self.cases[4].observations[-2]
        self.assertEqual('{Mimic:"not a valid id"}', malformed.block_entity_snbt)
        self.assertEqual(
            '{Mimic:"minecraft:iron_block"}',
            malformed.expected_block_entity_snbt,
        )
        malformed_build = GENERATOR.case_build_function(self.cases[4])
        malformed_verify = GENERATOR.case_verify_function(self.cases[4])
        self.assertIn('{Mimic:"not a valid id"}', malformed_build)
        self.assertNotIn('{Mimic:"not a valid id"}', malformed_verify)
        self.assertIn('{Mimic:"minecraft:iron_block"}', malformed_verify)

    def test_client_gates_remain_pending_in_the_manifest(self) -> None:
        gates = [case.client_gate for case in self.cases if case.client_gate]
        self.assertEqual(3, len(gates))
        self.assertTrue(any("vertical caps" in gate for gate in gates))
        self.assertTrue(any("tints" in gate for gate in gates))
        self.assertTrue(any("material/tint delegation" in gate for gate in gates))
        self.assertTrue(any("malformed-to-iron" in gate for gate in gates))

    def test_generated_manifest_matches_checked_in_rows(self) -> None:
        payload = json.loads((GALLERY / "cases.json").read_text(encoding="utf-8"))
        self.assertEqual(GENERATOR.counts(self.cases), payload["counts"])
        self.assertEqual(6, len(payload["cases"]))
        self.assertEqual(
            84,
            len((GALLERY / "cases.tsv").read_text(encoding="utf-8").splitlines()),
        )

    def test_generation_is_byte_deterministic_and_asset_free(self) -> None:
        first = GENERATOR.generated_files(self.cases)
        second = GENERATOR.generated_files(GENERATOR.cases())
        self.assertEqual(first, second)
        for path in first:
            lowered = path.as_posix().lower()
            self.assertFalse(lowered.endswith((".class", ".jar", ".png")))
            self.assertNotIn("assets/glassential", lowered)
            self.assertNotIn("assets/fusion", lowered)


if __name__ == "__main__":
    unittest.main()
