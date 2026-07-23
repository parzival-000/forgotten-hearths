from __future__ import annotations

import gzip
import json
import struct
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src" / "main" / "resources"
ASSETS = RESOURCES / "assets" / "forgotten_hearths"

BLOCKS = (
    "forgotten_hearth",
    "restoration_marker",
    "hearth_kettle",
    "mended_crock",
    "patchwork_quilt",
)
ITEMS = (
    "blackened_kettle",
    "mended_kettle",
    "cracked_crock",
    "mended_crock",
    "patchwork_cloth",
    "recipe_fragment",
    "hearth_porridge",
    "root_stew",
    "honeyed_oatcake",
    "hearth_kettle",
    "mended_crock_block",
    "patchwork_quilt",
)
GUIDED_ITEMS = (
    "blackened_kettle",
    "mended_kettle",
    "cracked_crock",
    "mended_crock",
    "patchwork_cloth",
)
BLOCK_ITEM_BLOCK_TRANSLATIONS = ("mended_crock_block",)


def load_json(path: Path, errors: list[str]):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exception:
        errors.append(f"Invalid JSON {path.relative_to(ROOT)}: {exception}")
        return {}


def require(path: Path, errors: list[str]):
    if not path.is_file():
        errors.append(f"Missing {path.relative_to(ROOT)}")


def mod_path(identifier: str, category: str, suffix: str) -> Path | None:
    if not identifier.startswith("forgotten_hearths:"):
        return None
    path = identifier.split(":", 1)[1]
    return ASSETS / category / f"{path}{suffix}"


def validate_model(identifier: str, errors: list[str]):
    path = mod_path(identifier, "models", ".json")
    if path is None:
        return
    require(path, errors)


def validate_texture(identifier: str, errors: list[str]):
    if identifier.startswith("#"):
        return
    path = mod_path(identifier, "textures", ".png")
    if path is None:
        return
    require(path, errors)


def main() -> int:
    errors: list[str] = []
    json_paths = sorted(RESOURCES.rglob("*.json"))
    documents = {path: load_json(path, errors) for path in json_paths}
    lang = documents.get(ASSETS / "lang" / "en_us.json", {})

    for block in BLOCKS:
        require(ASSETS / "blockstates" / f"{block}.json", errors)
        if f"block.forgotten_hearths.{block}" not in lang:
            errors.append(f"Missing block translation: {block}")

    for item in ITEMS:
        require(ASSETS / "items" / f"{item}.json", errors)
        if f"item.forgotten_hearths.{item}" not in lang:
            errors.append(f"Missing item translation: {item}")

    for item in GUIDED_ITEMS:
        for suffix in ("tooltip", "guide"):
            if f"item.forgotten_hearths.{item}.{suffix}" not in lang:
                errors.append(f"Missing {suffix} translation: {item}")

    for item in BLOCK_ITEM_BLOCK_TRANSLATIONS:
        if f"block.forgotten_hearths.{item}" not in lang:
            errors.append(f"Missing block-item fallback translation: {item}")

    for path, document in documents.items():
        relative = path.relative_to(ASSETS) if path.is_relative_to(ASSETS) else None
        if relative and relative.parts[0] == "blockstates":
            for variant in document.get("variants", {}).values():
                entries = variant if isinstance(variant, list) else [variant]
                for entry in entries:
                    if isinstance(entry, dict) and "model" in entry:
                        validate_model(entry["model"], errors)
        if relative and relative.parts[0] == "items":
            model = document.get("model", {})
            if isinstance(model, dict) and isinstance(model.get("model"), str):
                validate_model(model["model"], errors)
        if relative and relative.parts[:2] in (("models", "block"), ("models", "item")):
            parent = document.get("parent")
            if isinstance(parent, str):
                validate_model(parent, errors)
            for texture in document.get("textures", {}).values():
                if isinstance(texture, str):
                    validate_texture(texture, errors)

    png_paths = sorted(ASSETS.rglob("*.png"))
    for path in png_paths:
        data = path.read_bytes()
        if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
            errors.append(f"Invalid PNG {path.relative_to(ROOT)}")
            continue
        width, height = struct.unpack(">II", data[16:24])
        expected = (18, 18) if "mob_effect" in path.parts else (16, 16)
        if (width, height) != expected:
            errors.append(
                f"Unexpected PNG size {path.relative_to(ROOT)}: {width}x{height}, expected {expected[0]}x{expected[1]}"
            )

    structures = sorted((RESOURCES / "data" / "forgotten_hearths" / "structure").rglob("*.nbt"))
    if len(structures) != 3:
        errors.append(f"Expected three structure templates, found {len(structures)}")
    for path in structures:
        try:
            with gzip.open(path, "rb") as stream:
                if stream.read(1) != b"\x0a":
                    errors.append(f"Invalid NBT root tag in {path.relative_to(ROOT)}")
        except OSError as exception:
            errors.append(f"Invalid compressed NBT {path.relative_to(ROOT)}: {exception}")

    if errors:
        print("Resource validation failed:")
        for error in errors:
            print(f"- {error}")
        return 1

    print(f"Validated {len(json_paths)} JSON files, {len(png_paths)} PNG files, and {len(structures)} structure templates.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
