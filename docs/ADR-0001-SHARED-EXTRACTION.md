# ADR 0001: keep the Fusion interpreter repository-local

## Decision

Keep the adapted MIT Fusion interpreter source in this standalone Glassential
add-on for the first candidate. It owns only the exact generated 49-ID route
and provides no runtime service to other add-ons.

## Rationale

Rechiseled and Connected Glass are reviewed predecessors, but Glassential adds
different layout/predicate coverage, dynamic block-entity tint, one-way stock
delegation, slab adjacency, and a cross-namespace vanilla allowlist. Extracting
a shared installed provider before Glassential is independently accepted would
couple activation and rollback while freezing an unreviewed abstraction.

After Glassential is accepted, compare the reviewed implementations and extract
only a genuinely stable MIT source boundary if it reduces maintenance. Fusion
remains a resource format interpreted from operator-installed inputs, never an
installed block owner or runtime provider.
