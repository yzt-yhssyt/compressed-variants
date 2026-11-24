from __future__ import annotations

import io
import json
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
WS = ROOT.parent
CLIENT_JAR = WS / ".gradle-home" / "caches" / "fabric-loom" / "1.21.1" / "minecraft-client.jar"
TEXTURE_OUT = ROOT / "src/main/resources/assets/compressed_variants/textures/entity/compressed"
MODEL_OUT = ROOT / "src/main/resources/assets/compressed_variants/models/item"
ICON_OUT = ROOT / "src/main/resources/assets/compressed_variants/icon.png"
LANG_OUT = ROOT / "src/main/resources/assets/compressed_variants/lang"

# id -> (vanilla path, family, humanoid)
VARIANTS = {
    "zombie": ("zombie/zombie.png", "zombie", True),
    "husk": ("zombie/husk.png", "zombie", True),
    "drowned": ("zombie/drowned.png", "zombie", True),
    "zombie_villager": ("zombie_villager/zombie_villager.png", "zombie", True),
    "zombified_piglin": ("piglin/zombified_piglin.png", "zombie", True),
    "skeleton": ("skeleton/skeleton.png", "skeleton", True),
    "stray": ("skeleton/stray.png", "skeleton", True),
    "wither_skeleton": ("skeleton/wither_skeleton.png", "skeleton", True),
    "bogged": ("skeleton/bogged.png", "skeleton", True),
    "spider": ("spider/spider.png", "spider", False),
    "cave_spider": ("spider/cave_spider.png", "spider", False),
    "enderman": ("enderman/enderman.png", "enderman", False),
    "creeper": ("creeper/creeper.png", "creeper", False),
}

NAMES = {
    "zombie": ("僵尸", "Zombie"),
    "husk": ("尸壳", "Husk"),
    "drowned": ("溺尸", "Drowned"),
    "zombie_villager": ("僵尸村民", "Zombie Villager"),
    "zombified_piglin": ("僵尸猪灵", "Zombified Piglin"),
    "skeleton": ("骷髅", "Skeleton"),
    "stray": ("流浪者", "Stray"),
    "wither_skeleton": ("凋灵骷髅", "Wither Skeleton"),
    "bogged": ("沼骸", "Bogged"),
    "spider": ("蜘蛛", "Spider"),
    "cave_spider": ("洞穴蜘蛛", "Cave Spider"),
    "enderman": ("末影人", "Enderman"),
    "creeper": ("苦力怕", "Creeper"),
}

TIER_ZH = {1: "一重压缩", 2: "二重压缩", 3: "三重压缩"}
TIER_EN = {1: "Tier I", 2: "Tier II", 3: "Tier III"}

BLUE = [(8, 16, 40), (16, 36, 88), (28, 64, 150), (52, 108, 205), (110, 170, 255)]
PURPLE = [(24, 8, 46), (48, 18, 84), (82, 36, 140), (126, 68, 200), (178, 122, 255)]
BLACK = [(6, 6, 6), (16, 16, 16), (30, 30, 30), (48, 48, 48), (74, 74, 74)]
GREEN = [(4, 24, 8), (12, 52, 18), (22, 86, 30), (40, 130, 48), (78, 190, 86)]


def palette_for(family: str, tier: int):
    if tier == 3:
        return GREEN
    if family == "zombie" and tier == 1:
        return BLACK
    return PURPLE if tier == 2 else BLUE


def is_eye(r: int, g: int, b: int) -> bool:
    if r > 65 and r > g * 1.45 and r > b * 1.10:
        return True
    # Enderman's magenta/purple eyes.
    if r > 45 and b > 55 and r > g * 1.30 and b > g * 1.15:
        return True
    return False


def recolor(img: Image.Image, family: str, tier: int) -> Image.Image:
    palette = palette_for(family, tier)
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    src = img.load()
    dst = out.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = src[x, y]
            if a == 0:
                continue
            if tier == 3:
                # Tier 3 is a uniform green recolor, eyes included.
                lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                idx = min(len(palette) - 1, max(0, int(lum * len(palette))))
                tr, tg, tb = palette[idx]
                dst[x, y] = (tr, tg, tb, a)
                continue
            if is_eye(r, g, b):
                dst[x, y] = (r, g, b, a)
                continue
            # Zombie skin is green; keep it so the mob still looks like a zombie.
            if family == "zombie" and g > r * 1.12 and g > b * 1.12 and g > 28:
                dst[x, y] = (r, g, b, a)
                continue
            lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            idx = min(len(palette) - 1, max(0, int(lum * len(palette))))
            tr, tg, tb = palette[idx]
            # Keep a little of the vanilla noise so the texture is not flat.
            # Zombie tier 1 should read as black clothes, not navy.
            blend = 0.04 if family == "zombie" and tier == 1 else 0.18
            dst[x, y] = (
                int(tr * (1.0 - blend) + r * blend),
                int(tg * (1.0 - blend) + g * blend),
                int(tb * (1.0 - blend) + b * blend),
                a,
            )
    return out


def main() -> None:
    TEXTURE_OUT.mkdir(parents=True, exist_ok=True)
    MODEL_OUT.mkdir(parents=True, exist_ok=True)
    LANG_OUT.mkdir(parents=True, exist_ok=True)

    zf = zipfile.ZipFile(CLIENT_JAR)
    for variant_id, (path, family, _humanoid) in VARIANTS.items():
        name = f"assets/minecraft/textures/entity/{path}"
        base = Image.open(io.BytesIO(zf.read(name))).convert("RGBA")
        for tier in (1, 2, 3):
            recolor(base, family, tier).save(TEXTURE_OUT / f"{variant_id}_t{tier}.png")

    # 128x128 mod icon: a tier-3 green zombie head on a dark rounded tile.
    zombie = Image.open(TEXTURE_OUT / "zombie_t3.png").convert("RGBA")
    head = zombie.crop((8, 8, 16, 16)).resize((96, 96), Image.Resampling.NEAREST)
    icon = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    draw = ImageDraw.Draw(icon)
    draw.rounded_rectangle((2, 2, 125, 125), radius=18, fill=(10, 30, 14, 255),
                           outline=(63, 224, 90, 255), width=3)
    icon.alpha_composite(head, (16, 16))
    icon.save(ICON_OUT)

    zh = {"command.compressed_variants.set": "已将 %s 个压缩变体设为 %s 重压缩",
          "itemGroup.compressed_variants.spawn_eggs": "压缩变体刷怪蛋"}
    en = {"command.compressed_variants.set": "Set %s compressed variants to tier %s",
          "itemGroup.compressed_variants.spawn_eggs": "Compressed Variant Spawn Eggs"}

    for variant_id, (_path, _family, _humanoid) in VARIANTS.items():
        zh_name, en_name = NAMES[variant_id]
        for tier in (1, 2, 3):
            item_id = f"{variant_id}_spawn_egg_{tier}"
            model = {"parent": "minecraft:item/template_spawn_egg"}
            (MODEL_OUT / f"{item_id}.json").write_text(json.dumps(model, indent=2) + "\n",
                                                       encoding="utf-8")
            zh[f"item.compressed_variants.{item_id}"] = f"{TIER_ZH[tier]}{zh_name}刷怪蛋"
            en[f"item.compressed_variants.{item_id}"] = f"Compressed {en_name} Spawn Egg ({TIER_EN[tier]})"

    (LANG_OUT / "zh_cn.json").write_text(json.dumps(zh, ensure_ascii=False, indent=2) + "\n",
                                          encoding="utf-8")
    (LANG_OUT / "en_us.json").write_text(json.dumps(en, ensure_ascii=False, indent=2) + "\n",
                                          encoding="utf-8")

    print(f"Generated {len(VARIANTS) * 3} entity textures, "
          f"{len(VARIANTS) * 3} spawn egg models, icon and language files.")


if __name__ == "__main__":
    main()
