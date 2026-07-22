"""Generate a minimal 1x1x1 empty structure .nbt for Pantheon GameTests.

Produces src/main/resources/data/pantheon/structure/<name>.nbt matching the
1.21.1 structure format. The structure is a 1x1x1 air block, no entities,
no block entities. Size is encoded as [x, y, z] = [1, 1, 1].

NBT format (big-endian):
  TAG_Compound("") {
    "size": TAG_List(TAG_Int) [1, 1, 1]
    "entities": TAG_List(TAG_End) []
    "blocks": TAG_List(TAG_End) []
    "palette": TAG_List(TAG_End) []
    "DataVersion": TAG_Int 3955  (1.21.1 = 3955)
  }
"""
import struct
import sys
from pathlib import Path

# NBT tag type IDs
TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12

# 1.21.1 data version
DATA_VERSION = 3955


class NBTWriter:
    def __init__(self):
        self.buf = bytearray()

    def _name(self, name: str):
        # UTF-8 length-prefixed (unsigned short)
        encoded = name.encode("utf-8")
        self.buf += struct.pack(">H", len(encoded))
        self.buf += encoded

    def tag_compound(self, name: str, payload: bytes):
        self.buf += bytes([TAG_COMPOUND])
        self._name(name)
        self.buf += payload

    def tag_int(self, name: str, value: int):
        self.buf += bytes([TAG_INT])
        self._name(name)
        self.buf += struct.pack(">i", value)

    def tag_list(self, name: str, element_tag_id: int, elements: bytes):
        self.buf += bytes([TAG_LIST])
        self._name(name)
        self.buf += bytes([element_tag_id])
        self.buf += struct.pack(">i", len(elements) // 4 if element_tag_id == TAG_INT else 0)
        # length is number of elements; for empty list length=0
        # We handle the count separately below
        self.buf += elements

    def tag_list_empty(self, name: str):
        # TAG_List with element type TAG_End (0) and length 0
        self.buf += bytes([TAG_LIST])
        self._name(name)
        self.buf += bytes([TAG_END])  # element type
        self.buf += struct.pack(">i", 0)  # length

    def tag_list_int(self, name: str, values: list[int]):
        self.buf += bytes([TAG_LIST])
        self._name(name)
        self.buf += bytes([TAG_INT])  # element type
        self.buf += struct.pack(">i", len(values))
        for v in values:
            self.buf += struct.pack(">i", v)

    def end(self):
        self.buf += bytes([TAG_END])


def build_structure_nbt() -> bytes:
    w = NBTWriter()

    # Root compound payload (root has empty name, handled by tag_compound wrapper)
    payload = bytearray()

    # We build the payload inline then wrap in root compound at the end.
    p = NBTWriter()

    # "size": TAG_List(TAG_Int) [1, 1, 1]
    p.tag_list_int("size", [1, 1, 1])

    # "entities": TAG_List(TAG_End) []
    p.tag_list_empty("entities")

    # "blocks": TAG_List(TAG_End) []
    p.tag_list_empty("blocks")

    # "palette": TAG_List(TAG_End) []
    p.tag_list_empty("palette")

    # "DataVersion": TAG_Int 3955
    p.tag_int("DataVersion", DATA_VERSION)

    # End compound
    p.end()

    # Wrap in root compound (empty name)
    root = NBTWriter()
    root.buf += bytes([TAG_COMPOUND])
    root._name("")
    root.buf += p.buf

    return bytes(root.buf)


def main():
    out_dir = Path("src/main/resources/data/pantheon/structure")
    out_dir.mkdir(parents=True, exist_ok=True)

    # Generate templates for each GameTest in the spec
    # GameTest template name = lowercase method name (no class prefix since we use @PrefixGameTestTemplate(false))
    # But the spec uses @GameTestHolder(MODID) which prepends simple class name unless @PrefixGameTestTemplate(false)
    # We'll use @PrefixGameTestTemplate(false) so template name = method name lowercase
    templates = [
        "pantheon_faction_create",
        "pantheon_faction_assign",
        "pantheon_faction_info",
        "pantheon_faction_list",
        "pantheon_temple_mirror",
        "pantheon_persistence",
        "pantheon_faction_color",
        "pantheon_faction_id_inference",
    ]

    for name in templates:
        data = build_structure_nbt()
        out_path = out_dir / f"{name}.nbt"
        out_path.write_bytes(data)
        print(f"Wrote {out_path} ({len(data)} bytes)")


if __name__ == "__main__":
    main()