# RetroSophisticatedBackpacks — GTNH Backport

A backport of [RetroSophisticatedBackpacks](https://github.com/P3pp3rF1y/RetroSophisticatedBackpacks) for Minecraft 1.7.10 / GregTech: New Horizons.

## Disclaimer

This mod **IS NOT OFFICIAL WORK** of original author P3pp3rF1y, please do not report any issue to the original author. This is based off CleanroomMC's Retro Sophisticated Backpacks backport to 1.12

This mod is licensed under **GPLv3**. All assets are forked before Sophisticated Backpack changed its license to All Rights Reserved.

## Tiers

| Tier | Slots | Upgrade Slots |
|------|-------|---------------|
| Leather | 27 | 1 |
| Iron | 54 | 3 |
| Gold | 81 | 6 |
| Diamond | 108 | 9 |
| Obsidian | 120 | 12 |

Upgrade tiers by surrounding the previous backpack with the appropriate material. Inventory contents are preserved on upgrade.

## Upgrades

| Upgrade | Advanced | Description |
|---------|----------|-------------|
| **Pickup** | ✓ | Auto-collects nearby dropped items into the backpack |
| **Magnet** | ✓ | Actively attracts dropped items within a radius (5 / 9 blocks) |
| **Void** | ✓ | Destroys items routed into the backpack that match the filter |
| **Deposit** | ✓ | Shift-click a chest/container to deposit matching items |
| **Restock** | ✓ | Shift-click a chest/container to pull matching items |
| **Feeding** | ✓ | Automatically feeds the player from food stored in the backpack |
| **Filter** | ✓ | Controls which items can enter or leave the backpack (in / out / both) |
| **Stack** | — | Increases the stack size limit per slot (5 tiers + exponential); hover the upgrade to see the effective limit |
| **Inception** | — | Allows storing backpacks inside backpacks |

All upgrades with filtering support a whitelist/blacklist and can be configured via the gear icon in the backpack GUI. Advanced variants add ore dictionary matching, ignore-durability, and ignore-NBT options.

## NEI Integration

- Press **R** on any backpack to see all compatible upgrades
- Press **U** on any upgrade item to see which backpacks accept it
- Backpack tier upgrade recipes are visible via standard recipe lookup

## Requirements

- Minecraft 1.7.10
- [Forgelin-Continuous](https://github.com/CleanroomMC/Forgelin-Continuous)

## License

GPLv3 — see [LICENSE](LICENSE)
