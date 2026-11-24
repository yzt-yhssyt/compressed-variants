# 压缩变体 · Compressed Variants

> 13 种原版敌对生物各获得一重、二重、三重压缩变体：护甲 8×/80×、生命上万、会穿甲，附带 39 个刷怪蛋。
>
> Tier I–III compressed variants of 13 hostile mobs: 8×/80× armor, five-digit health, mobs that wear gear, and 39 spawn eggs.

**Minecraft 1.21.1 · Fabric · 作者 yzt**

## 简介

夜晚不再一样。每一只僵尸、骷髅、蜘蛛、末影人和苦力怕都可能以压缩形态出现，越往下越硬；三重个体是真正需要备战的目标。

## 特性

- 13 种生物 × 3 级 = 39 个变体：僵尸、尸壳、溺尸、僵尸村民、僵尸猪灵、骷髅、流浪者、凋灵骷髅、沼骸、蜘蛛、洞穴蜘蛛、末影人、苦力怕
- 自然生成 100% 被替换：50% 一重 / 25% 二重 / 25% 三重，不再出现普通个体
- 护甲 ×8（一重）/ ×80（二重、三重）；生命 150–250 / 1000–1500 / 9000–13500 随机
- 攻击 15–25 / 25–50 / 50–75；人形怪会穿甲（一重铁或一级压缩甲，二重三重金属或二级压缩甲），装备 5% 掉落
- 超过 20 点的护甲按 (护甲 − 20) / 2 转换为额外生命
- 等级写入 DataTracker 与 NBT，区块重载不会回滚，也不会重新随机
- 39 个刷怪蛋 + /compressed_variants 命令，方便测试与整合包作者布置
- 若同时安装「终极压缩装备」，怪物有概率直接穿上该模组的压缩护甲

## 安装

1. 安装 Minecraft 1.21.1 与 Fabric Loader（≥ 0.16.14）
2. 安装依赖：Fabric API（必需）；终极压缩装备（可选联动）
3. 把 `compressed-variants-1.0.0.jar` 放进 `mods` 文件夹

## 依赖

Fabric API（必需）；终极压缩装备（可选联动）

## English

Night is no longer the same. Every zombie, skeleton, spider, enderman and creeper can show up compressed, and the deeper the tier the harder it hits. Tier III is a fight you prepare for.

- 13 mobs × 3 tiers = 39 variants: zombie, husk, drowned, zombie villager, zombified piglin, skeleton, stray, wither skeleton, bogged, spider, cave spider, enderman, creeper
- Natural spawns are replaced 100% of the time: 50% tier I / 25% tier II / 25% tier III, no plain versions left
- Armor ×8 (tier I) and ×80 (tier II–III); health rolls 150–250 / 1000–1500 / 9000–13500
- Attack 15–25 / 25–50 / 50–75. Humanoids wear armor (iron or tier I compressed gear at tier I, diamond or tier II compressed gear above), with a 5% drop chance
- Armor above 20 is converted to extra health at (armor − 20) / 2
- Tier is stored in a DataTracker and in NBT, so chunk reloads never reroll or reset a mob
- 39 spawn eggs plus the /compressed_variants command for testing and pack authors
- If Ultimate Compressed Gear is installed, mobs can spawn wearing that mod's compressed armor

### Installation

1. Minecraft 1.21.1 with Fabric Loader (≥ 0.16.14)
2. Dependencies: Fabric API (required); Ultimate Compressed Gear (optional integration)
3. Drop `compressed-variants-1.0.0.jar` into your `mods` folder

## 链接

- 源码 / Source: https://github.com/yzt-yhssyt/compressed-variants
- 问题反馈 / Issues: https://github.com/yzt-yhssyt/compressed-variants/issues

## 许可

MIT License © 2026 yzt. 你可以自由使用、修改、再分发，包括放进整合包。
