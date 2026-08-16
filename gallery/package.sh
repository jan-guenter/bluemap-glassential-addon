#!/usr/bin/env bash
# SPDX-License-Identifier: MIT
set -euo pipefail

archive_name="bluemap-glassential-gallery-atmons-1.2.0.zip"

if [[ $# -gt 1 ]]; then
  echo "usage: $0 [/existing/output/directory]" >&2
  exit 2
fi

output_directory="$(realpath -m -- "${1:-.}")"
if [[ ! -d "$output_directory" ]]; then
  echo "output directory does not exist: $output_directory" >&2
  exit 2
fi

gallery_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
archive_temp="$(mktemp -d /tmp/bluemap-glassential-gallery.XXXXXX)"
cleanup() { rm -rf -- "$archive_temp"; }
trap cleanup EXIT

python3 "$gallery_root/generate.py" --check
(cd "$gallery_root" && sha256sum --check SHA256SUMS)

mkdir -p "$archive_temp/root"
cp -a "$gallery_root/datapack/." "$archive_temp/root/"
find "$archive_temp/root" -type f -exec chmod 0644 {} +
TZ=UTC find "$archive_temp/root" -exec touch -h -t 198001010000.00 {} +
(
  cd "$archive_temp/root"
  LC_ALL=C TZ=UTC find . -type f -printf '%P\n' | LC_ALL=C sort |
    LC_ALL=C TZ=UTC zip -q -X -9 "$archive_temp/$archive_name" -@
)
unzip -tq "$archive_temp/$archive_name"

install -m 0644 "$archive_temp/$archive_name" "$output_directory/$archive_name"
stat -c '%n %s bytes' "$output_directory/$archive_name"
sha256sum "$output_directory/$archive_name"
