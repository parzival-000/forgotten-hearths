from __future__ import annotations

import gzip
import struct
import zlib
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "forgotten_hearths" / "textures"
STRUCTURES = ROOT / "src" / "main" / "resources" / "data" / "forgotten_hearths" / "structure" / "homestead"


def rgba(hex_color: str, alpha: int = 255) -> tuple[int, int, int, int]:
    value = int(hex_color.removeprefix("#"), 16)
    return (value >> 16, (value >> 8) & 255, value & 255, alpha)


def canvas(width: int = 16, height: int = 16) -> list[list[tuple[int, int, int, int]]]:
    return [[(0, 0, 0, 0) for _ in range(width)] for _ in range(height)]


def fill(image, color):
    for y in range(len(image)):
        for x in range(len(image[0])):
            image[y][x] = color


def rect(image, x0, y0, x1, y1, color):
    for y in range(max(0, y0), min(len(image), y1 + 1)):
        for x in range(max(0, x0), min(len(image[0]), x1 + 1)):
            image[y][x] = color


def pixel(image, x, y, color):
    if 0 <= y < len(image) and 0 <= x < len(image[0]):
        image[y][x] = color


def png(path: Path, image) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    height = len(image)
    width = len(image[0])
    raw = b"".join(b"\x00" + b"".join(bytes(px) for px in row) for row in image)

    def chunk(kind: bytes, payload: bytes) -> bytes:
        body = kind + payload
        return struct.pack(">I", len(payload)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    data = b"\x89PNG\r\n\x1a\n"
    data += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    data += chunk(b"IDAT", zlib.compress(raw, 9))
    data += chunk(b"IEND", b"")
    path.write_bytes(data)


def stone_texture(base: str, mortar: str, accent: str, moss: str | None = None, glow: str | None = None):
    image = canvas()
    fill(image, rgba(base))
    mortar_color = rgba(mortar)
    accent_color = rgba(accent)
    for y in (4, 9, 14):
        rect(image, 0, y, 15, y, mortar_color)
    for row, y0 in enumerate((0, 5, 10)):
        offset = 3 if row % 2 else 7
        for x in (offset, offset + 8):
            rect(image, x, y0, x, min(y0 + 3, 15), mortar_color)
    for x, y in ((1, 2), (12, 1), (5, 7), (14, 11), (3, 12), (9, 6), (7, 15)):
        pixel(image, x, y, accent_color)
    if moss:
        moss_color = rgba(moss)
        for x, y in ((0, 0), (1, 0), (1, 1), (10, 4), (11, 4), (15, 9), (14, 10), (2, 14)):
            pixel(image, x, y, moss_color)
    if glow:
        glow_color = rgba(glow)
        for x, y in ((6, 11), (7, 11), (8, 11), (9, 11), (7, 10), (8, 10)):
            pixel(image, x, y, glow_color)
    return image


def item_kettle(mended: bool):
    image = canvas()
    outline = rgba("22201E")
    shadow = rgba("3B3834" if not mended else "704229")
    metal = rgba("545653" if not mended else "A85F36")
    light = rgba("777A70" if not mended else "D98A51")
    bright = rgba("98998F" if not mended else "F0B06C")
    rect(image, 5, 2, 10, 2, outline)
    rect(image, 4, 3, 4, 7, outline)
    rect(image, 11, 3, 11, 7, outline)
    pixel(image, 5, 3, outline)
    pixel(image, 10, 3, outline)
    rect(image, 6, 4, 9, 4, outline)
    rect(image, 5, 5, 10, 6, shadow)
    rect(image, 4, 7, 11, 12, outline)
    rect(image, 3, 8, 12, 11, outline)
    rect(image, 4, 8, 11, 11, metal)
    rect(image, 5, 7, 10, 11, metal)
    rect(image, 5, 8, 6, 10, light)
    pixel(image, 6, 8, bright)
    rect(image, 12, 8, 14, 9, outline)
    rect(image, 12, 8, 13, 8, light)
    rect(image, 5, 12, 10, 13, shadow)
    if mended:
        pixel(image, 8, 7, rgba("F2C078"))
        pixel(image, 8, 8, rgba("F2C078"))
        pixel(image, 9, 9, rgba("F2C078"))
        pixel(image, 9, 10, rgba("F2C078"))
    return image


def item_crock(mended: bool):
    image = canvas()
    outline = rgba("4B2D25")
    dark = rgba("6B3B2D")
    clay = rgba("A95F43")
    light = rgba("D18862")
    rect(image, 5, 3, 10, 3, outline)
    rect(image, 4, 4, 11, 5, outline)
    rect(image, 5, 4, 10, 4, dark)
    rect(image, 4, 6, 11, 12, outline)
    rect(image, 3, 8, 12, 11, outline)
    rect(image, 4, 8, 11, 11, clay)
    rect(image, 5, 6, 10, 12, clay)
    rect(image, 5, 7, 5, 10, light)
    pixel(image, 6, 7, light)
    rect(image, 5, 13, 10, 13, outline)
    seam = rgba("E2C184") if mended else rgba("2C211F")
    for x, y in ((8, 6), (8, 7), (7, 8), (8, 9), (8, 10), (7, 11), (7, 12)):
        pixel(image, x, y, seam)
    return image


def cloth_texture():
    image = canvas()
    edge = rgba("5B403A")
    seam = rgba("C8AE7A")
    colors = [rgba("8F4944"), rgba("C47B50"), rgba("657A61"), rgba("61748B"), rgba("B99D69")]
    rect(image, 2, 5, 13, 12, edge)
    rect(image, 3, 4, 12, 11, edge)
    for y in range(5, 11):
        for x in range(4, 12):
            image[y][x] = colors[((x - 4) // 2 + ((y - 5) // 2) * 2) % len(colors)]
    rect(image, 4, 7, 11, 7, seam)
    rect(image, 7, 5, 7, 10, seam)
    rect(image, 3, 11, 12, 12, rgba("70504A"))
    pixel(image, 13, 10, rgba("B99D69"))
    return image


def quilt_texture():
    image = canvas()
    fill(image, rgba("604641"))
    colors = [rgba("914D48"), rgba("BE744F"), rgba("657B63"), rgba("64768B"), rgba("B79C68")]
    seam = rgba("D0B77F")
    for y in range(1, 15):
        for x in range(1, 15):
            image[y][x] = colors[((x // 4) + (y // 4) * 2) % len(colors)]
    for i in (4, 8, 12):
        rect(image, i, 1, i, 14, seam)
        rect(image, 1, i, 14, i, seam)
    rect(image, 0, 0, 15, 0, rgba("4C3937"))
    rect(image, 0, 15, 15, 15, rgba("4C3937"))
    return image


def recipe_fragment():
    image = canvas()
    paper = rgba("D7C598")
    edge = rgba("877252")
    ink = rgba("5A4738")
    rect(image, 3, 2, 12, 13, paper)
    rect(image, 3, 2, 12, 2, edge)
    rect(image, 3, 13, 12, 13, edge)
    rect(image, 3, 2, 3, 13, edge)
    for y, length in ((5, 6), (7, 7), (9, 5), (11, 6)):
        rect(image, 5, y, 4 + length, y, ink)
    pixel(image, 12, 3, (0, 0, 0, 0))
    pixel(image, 3, 12, (0, 0, 0, 0))
    return image


def bowl_food(liquid: str, garnish: str, accent: str, porridge: bool):
    image = canvas()
    outline = rgba("3E2920")
    wood = rgba("74452C")
    rim = rgba("A9693F")
    rect(image, 3, 6, 12, 6, outline)
    rect(image, 2, 7, 13, 9, outline)
    rect(image, 3, 7, 12, 9, rim)
    rect(image, 4, 7, 11, 8, rgba(liquid))
    rect(image, 3, 10, 12, 10, outline)
    rect(image, 4, 10, 11, 12, wood)
    rect(image, 5, 13, 10, 13, outline)
    if porridge:
        for x, y in ((5, 7), (8, 8), (10, 7)):
            pixel(image, x, y, rgba(garnish))
        pixel(image, 7, 7, rgba(accent))
    else:
        for x, y in ((5, 7), (9, 8), (10, 7)):
            pixel(image, x, y, rgba(garnish))
        for x, y in ((7, 8), (11, 8)):
            pixel(image, x, y, rgba(accent))
    pixel(image, 6, 3, rgba("D7D0C1"))
    pixel(image, 6, 4, rgba("D7D0C1"))
    pixel(image, 9, 4, rgba("D7D0C1"))
    pixel(image, 9, 5, rgba("D7D0C1"))
    return image


def oatcake():
    image = canvas()
    edge = rgba("8F5C2D")
    cake = rgba("D9A94E")
    honey = rgba("F2C34A")
    rect(image, 3, 5, 12, 11, edge)
    rect(image, 4, 4, 11, 12, edge)
    rect(image, 4, 5, 11, 11, cake)
    for x, y in ((5, 6), (8, 5), (10, 8), (6, 10), (9, 11)):
        pixel(image, x, y, honey)
    return image


def block_kettle_texture():
    image = canvas()
    fill(image, rgba("8C4D2F"))
    rect(image, 0, 0, 15, 2, rgba("5B3428"))
    rect(image, 0, 13, 15, 15, rgba("633627"))
    rect(image, 3, 3, 5, 12, rgba("B9683D"))
    rect(image, 6, 4, 7, 10, rgba("D58B58"))
    for x, y in ((2, 5), (10, 3), (12, 8), (8, 12), (14, 4)):
        pixel(image, x, y, rgba("70402F"))
    return image


def dark_metal_texture():
    image = canvas()
    fill(image, rgba("292A28"))
    rect(image, 0, 0, 15, 2, rgba("454641"))
    rect(image, 0, 13, 15, 15, rgba("171817"))
    rect(image, 3, 3, 4, 12, rgba("555650"))
    return image


def block_crock_texture():
    image = canvas()
    fill(image, rgba("9C563D"))
    rect(image, 0, 0, 15, 2, rgba("63382D"))
    rect(image, 0, 13, 15, 15, rgba("71402F"))
    rect(image, 3, 3, 5, 12, rgba("C57955"))
    for x, y in ((9, 1), (9, 2), (8, 3), (8, 4), (9, 5), (9, 6), (8, 7), (8, 8), (7, 9), (7, 10), (8, 11), (8, 12), (7, 13), (7, 14)):
        pixel(image, x, y, rgba("DEC08A"))
    return image


def crock_inside_texture():
    image = canvas()
    fill(image, rgba("321F1B"))
    rect(image, 2, 2, 13, 4, rgba("573127"))
    return image


def marker_texture(kind: str):
    image = canvas()
    fill(image, rgba("4B5049"))
    rect(image, 0, 12, 15, 15, rgba("343936"))
    if kind == "kettle":
        rect(image, 4, 6, 11, 12, rgba("262928"))
        rect(image, 6, 4, 9, 6, rgba("161817"))
        pixel(image, 12, 8, rgba("75766C"))
    elif kind == "crock":
        rect(image, 4, 5, 11, 12, rgba("6B4338"))
        for x, y in ((8, 6), (7, 7), (8, 8), (7, 9), (6, 10), (6, 11)):
            pixel(image, x, y, rgba("24201F"))
    else:
        for y in range(5, 13):
            for x in range(2, 14):
                image[y][x] = rgba("604A49" if (x + y) % 3 else "857166")
        rect(image, 3, 7, 12, 7, rgba("2D302D"))
    return image


def comfort_icon():
    image = canvas(18, 18)
    ember = rgba("F4A24A")
    dark = rgba("7B3C2A")
    glow = rgba("FFD37A")
    rect(image, 4, 12, 13, 14, dark)
    rect(image, 5, 14, 12, 15, rgba("4E332D"))
    for x, y in ((8, 4), (7, 5), (9, 5), (6, 7), (8, 6), (10, 7), (7, 9), (9, 9), (8, 11)):
        pixel(image, x, y, ember)
    for x, y in ((8, 7), (8, 8), (9, 8)):
        pixel(image, x, y, glow)
    return image


def generate_textures() -> None:
    block = ASSETS / "block"
    item = ASSETS / "item"
    effect = ASSETS / "mob_effect"

    png(block / "hearth_forgotten.png", stone_texture("5A5D58", "343734", "6D7068", "52634A"))
    png(block / "hearth_discovered.png", stone_texture("5F605A", "353735", "777267", "5A694D", "8A4A32"))
    png(block / "hearth_rekindled.png", stone_texture("6B6257", "37322F", "8A7967", None, "E77835"))
    png(block / "hearth_restored.png", stone_texture("7A6655", "43362D", "A38A70", None, "FFAD47"))
    png(block / "hearth_inside_cold.png", stone_texture("242725", "171918", "343936", "38473A"))
    png(block / "hearth_inside_ember.png", stone_texture("2A2420", "161312", "43342A", None, "FF8D38"))
    png(block / "marker_kettle.png", marker_texture("kettle"))
    png(block / "marker_crock.png", marker_texture("crock"))
    png(block / "marker_quilt.png", marker_texture("quilt"))
    png(block / "hearth_kettle.png", block_kettle_texture())
    png(block / "hearth_kettle_iron.png", dark_metal_texture())
    png(block / "mended_crock.png", block_crock_texture())
    png(block / "mended_crock_inside.png", crock_inside_texture())
    png(block / "patchwork_quilt.png", quilt_texture())

    png(item / "blackened_kettle.png", item_kettle(False))
    png(item / "mended_kettle.png", item_kettle(True))
    png(item / "cracked_crock.png", item_crock(False))
    png(item / "mended_crock.png", item_crock(True))
    png(item / "patchwork_cloth.png", cloth_texture())
    png(item / "recipe_fragment.png", recipe_fragment())
    png(item / "hearth_porridge.png", bowl_food("D8BD72", "8F3F3C", "F0DDA0", True))
    png(item / "root_stew.png", bowl_food("7F3F29", "E17A2E", "6E8A4F", False))
    png(item / "honeyed_oatcake.png", oatcake())
    png(effect / "comforted.png", comfort_icon())


TAG_END = 0
TAG_INT = 3
TAG_LONG = 4
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10


def nbt_string(value: str) -> bytes:
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def named_tag(tag_type: int, name: str, payload: bytes) -> bytes:
    return bytes([tag_type]) + nbt_string(name) + payload


def payload_int(value: int) -> bytes:
    return struct.pack(">i", value)


def payload_long(value: int) -> bytes:
    return struct.pack(">q", value)


def payload_string(value: str) -> bytes:
    return nbt_string(value)


def payload_list(tag_type: int, values: list[bytes]) -> bytes:
    return bytes([tag_type]) + struct.pack(">i", len(values)) + b"".join(values)


def payload_compound(entries: list[tuple[int, str, bytes]]) -> bytes:
    return b"".join(named_tag(tag_type, name, payload) for tag_type, name, payload in entries) + bytes([TAG_END])


class Structure:
    def __init__(self, variant: int):
        self.variant = variant
        self.blocks: dict[tuple[int, int, int], tuple[str, dict[str, str], dict[str, object] | None]] = {}

    def set(self, x, y, z, name, properties=None, nbt=None):
        self.blocks[(x, y, z)] = (name, properties or {}, nbt)

    def box(self, x0, y0, z0, x1, y1, z1, name, properties=None):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    self.set(x, y, z, name, properties)

    def write(self, path: Path, size=(11, 7, 11)):
        palette: list[tuple[str, tuple[tuple[str, str], ...]]] = []
        palette_index: dict[tuple[str, tuple[tuple[str, str], ...]], int] = {}
        block_payloads = []

        for pos in sorted(self.blocks):
            name, properties, nbt = self.blocks[pos]
            key = (name, tuple(sorted(properties.items())))
            state = palette_index.get(key)
            if state is None:
                state = len(palette)
                palette_index[key] = state
                palette.append(key)

            entries = [
                (TAG_LIST, "pos", payload_list(TAG_INT, [payload_int(value) for value in pos])),
                (TAG_INT, "state", payload_int(state)),
            ]
            if nbt:
                nbt_entries = []
                for key_name, value in nbt.items():
                    if isinstance(value, str):
                        nbt_entries.append((TAG_STRING, key_name, payload_string(value)))
                    elif isinstance(value, int):
                        nbt_entries.append((TAG_INT, key_name, payload_int(value)))
                    else:
                        raise TypeError(f"Unsupported NBT value: {value!r}")
                entries.append((TAG_COMPOUND, "nbt", payload_compound(nbt_entries)))
            block_payloads.append(payload_compound(entries))

        palette_payloads = []
        for name, properties in palette:
            entries = [(TAG_STRING, "Name", payload_string(name))]
            if properties:
                property_entries = [(TAG_STRING, key, payload_string(value)) for key, value in properties]
                entries.append((TAG_COMPOUND, "Properties", payload_compound(property_entries)))
            palette_payloads.append(payload_compound(entries))

        root = payload_compound([
            (TAG_INT, "DataVersion", payload_int(4903)),
            (TAG_LIST, "size", payload_list(TAG_INT, [payload_int(value) for value in size])),
            (TAG_LIST, "palette", payload_list(TAG_COMPOUND, palette_payloads)),
            (TAG_LIST, "blocks", payload_list(TAG_COMPOUND, block_payloads)),
            (TAG_LIST, "entities", payload_list(TAG_COMPOUND, [])),
        ])
        path.parent.mkdir(parents=True, exist_ok=True)
        with path.open("wb") as raw_stream:
            with gzip.GzipFile(filename="", mode="wb", compresslevel=9, fileobj=raw_stream, mtime=0) as stream:
                stream.write(bytes([TAG_COMPOUND]) + nbt_string("") + root)


def build_homestead(variant: int) -> Structure:
    palettes = [
        {
            "foundation": "minecraft:mossy_cobblestone",
            "floor": "minecraft:spruce_planks",
            "wall": "minecraft:oak_planks",
            "post": "minecraft:stripped_oak_log",
            "roof": "minecraft:spruce_stairs",
            "ridge": "minecraft:spruce_slab",
        },
        {
            "foundation": "minecraft:cobblestone",
            "floor": "minecraft:oak_planks",
            "wall": "minecraft:mud_bricks",
            "post": "minecraft:oak_log",
            "roof": "minecraft:oak_stairs",
            "ridge": "minecraft:oak_slab",
        },
        {
            "foundation": "minecraft:cobbled_deepslate",
            "floor": "minecraft:spruce_planks",
            "wall": "minecraft:spruce_planks",
            "post": "minecraft:spruce_log",
            "roof": "minecraft:dark_oak_stairs",
            "ridge": "minecraft:dark_oak_slab",
        },
    ][variant]

    house = Structure(variant)
    house.box(1, 1, 1, 9, 5, 9, "minecraft:air")
    house.box(1, 0, 1, 9, 0, 9, palettes["foundation"])
    house.box(2, 0, 2, 8, 0, 8, palettes["floor"])

    for x, z in ((1, 1), (1, 9), (9, 1), (9, 9)):
        house.box(x, 1, z, x, 3, z, palettes["post"])

    for y in range(1, 4):
        for x in range(2, 9):
            if (x not in (4, 5) or y == 3):
                house.set(x, y, 1, palettes["wall"])
            if not (x == 5 and y < 3):
                house.set(x, y, 9, palettes["wall"])
        for z in range(2, 9):
            if not (z == 5 and y == 2):
                house.set(1, y, z, palettes["wall"])
            if not (z in (4, 5) and y == 2):
                house.set(9, y, z, palettes["wall"])

    for z in range(0, 11):
        for x, y, facing in ((0, 4, "east"), (10, 4, "west"), (1, 5, "east"), (9, 5, "west"), (2, 6, "east"), (8, 6, "west")):
            if (x, z) not in ((10, 7), (1, 2), (8, 5)):
                house.set(x, y, z, palettes["roof"], {"facing": facing, "half": "bottom", "shape": "straight", "waterlogged": "false"})
        for x in range(3, 8):
            if (x, z) not in ((4, 4), (5, 4), (6, 8)):
                house.set(x, 6, z, palettes["ridge"], {"type": "bottom", "waterlogged": "false"})

    house.box(8, 1, 2, 8, 5, 2, "minecraft:mossy_cobblestone" if variant == 0 else "minecraft:cobblestone")
    house.set(8, 6, 2, "minecraft:cobblestone_wall")
    house.set(
        7,
        1,
        3,
        "forgotten_hearths:forgotten_hearth",
        {"facing": "south", "stage": "forgotten"},
        {"id": "forgotten_hearths:forgotten_hearth", "variant": variant, "stage": 0, "tasks": 0},
    )
    house.set(7, 1, 4, "forgotten_hearths:restoration_marker", {"facing": "south", "task": "kettle"})
    house.set(3, 1, 3, "forgotten_hearths:restoration_marker", {"facing": "south", "task": "crock"})
    house.set(3, 1, 7, "forgotten_hearths:restoration_marker", {"facing": "south", "task": "quilt"})
    house.set(
        2,
        1,
        7,
        "minecraft:barrel",
        {"facing": "up", "open": "false"},
        {"id": "minecraft:barrel", "LootTable": "forgotten_hearths:chests/abandoned_homestead"},
    )
    house.set(5, 1, 8, "minecraft:cobweb")
    house.set(2, 1, 2, "minecraft:cobweb")

    if variant == 0:
        house.set(1, 1, 4, "minecraft:vine", {"east": "true", "north": "false", "south": "false", "up": "false", "west": "false"})
        house.set(4, 1, 8, "minecraft:moss_carpet")
        house.set(6, 1, 2, "minecraft:oak_leaves", {"distance": "7", "persistent": "true", "waterlogged": "false"})
    elif variant == 1:
        house.set(2, 1, 2, "minecraft:hay_block", {"axis": "y"})
        house.set(8, 1, 8, "minecraft:hay_block", {"axis": "y"})
        house.set(4, 1, 2, "minecraft:flower_pot")
    else:
        house.set(2, 1, 2, "minecraft:snow", {"layers": "3"})
        house.set(8, 1, 8, "minecraft:snow", {"layers": "2"})
        house.set(5, 1, 2, "minecraft:lantern", {"hanging": "false", "waterlogged": "false"})

    return house


def generate_structures() -> None:
    names = ("woodland_cottage", "plains_farmstead", "taiga_shelter")
    for variant, name in enumerate(names):
        build_homestead(variant).write(STRUCTURES / f"{name}.nbt")


if __name__ == "__main__":
    generate_textures()
    generate_structures()
    print("Generated original Forgotten Hearths textures and structure templates.")
