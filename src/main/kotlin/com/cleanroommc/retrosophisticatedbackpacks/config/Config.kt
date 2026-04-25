package com.cleanroommc.retrosophisticatedbackpacks.config

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import net.minecraftforge.common.config.Configuration
import java.io.File

object Config {
    private lateinit var cfg: Configuration

    @JvmField var blacklistedItems: Array<String> = emptyArray()

    @JvmField val leatherBackpack = LeatherBackpackConfig()
    @JvmField val ironBackpack = IronBackpackConfig()
    @JvmField val goldBackpack = GoldBackpackConfig()
    @JvmField val diamondBackpack = DiamondBackpackConfig()
    @JvmField val obsidianBackpack = ObsidianBackpackConfig()
    @JvmField val stackUpgrade = StackUpgradeConfig()

    fun init(configDir: File) {
        cfg = Configuration(File(configDir, "${Tags.MOD_ID}_general.cfg"))
        syncConfig()
        if (cfg.hasChanged()) cfg.save()
    }

    fun syncConfig() {
        blacklistedItems = cfg.getStringList(
            "blacklistedItems", "general",
            emptyArray(), "Items that cannot be stored in backpack (requires restart)"
        )
        leatherBackpack.slots = cfg.getInt("slots", "leatherBackpack", 27, 1, 1000, "")
        leatherBackpack.upgradeSlots = cfg.getInt("upgradeSlots", "leatherBackpack", 1, 1, 20, "")
        ironBackpack.slots = cfg.getInt("slots", "ironBackpack", 54, 1, 1000, "")
        ironBackpack.upgradeSlots = cfg.getInt("upgradeSlots", "ironBackpack", 2, 1, 20, "")
        goldBackpack.slots = cfg.getInt("slots", "goldBackpack", 81, 1, 1000, "")
        goldBackpack.upgradeSlots = cfg.getInt("upgradeSlots", "goldBackpack", 3, 1, 20, "")
        diamondBackpack.slots = cfg.getInt("slots", "diamondBackpack", 108, 1, 1000, "")
        diamondBackpack.upgradeSlots = cfg.getInt("upgradeSlots", "diamondBackpack", 5, 1, 20, "")
        obsidianBackpack.slots = cfg.getInt("slots", "obsidianBackpack", 120, 1, 1000, "")
        obsidianBackpack.upgradeSlots = cfg.getInt("upgradeSlots", "obsidianBackpack", 7, 1, 20, "")
        stackUpgrade.leatherMultiplier = cfg.getInt("leatherMultiplier", "stackUpgrade", 2, 1, 64, "")
        stackUpgrade.ironMultiplier = cfg.getInt("ironMultiplier", "stackUpgrade", 4, 1, 64, "")
        stackUpgrade.goldMultiplier = cfg.getInt("goldMultiplier", "stackUpgrade", 8, 1, 64, "")
        stackUpgrade.diamondMultiplier = cfg.getInt("diamondMultiplier", "stackUpgrade", 16, 1, 64, "")
        stackUpgrade.obsidianMultiplier = cfg.getInt("obsidianMultiplier", "stackUpgrade", 32, 1, 64, "")
    }

    fun getSlotsForTier(tier: BackpackTier): Int = when (tier) {
        BackpackTier.LEATHER -> leatherBackpack.slots
        BackpackTier.IRON -> ironBackpack.slots
        BackpackTier.GOLD -> goldBackpack.slots
        BackpackTier.DIAMOND -> diamondBackpack.slots
        BackpackTier.OBSIDIAN -> obsidianBackpack.slots
    }

    fun getUpgradeSlotsForTier(tier: BackpackTier): Int = when (tier) {
        BackpackTier.LEATHER -> leatherBackpack.upgradeSlots
        BackpackTier.IRON -> ironBackpack.upgradeSlots
        BackpackTier.GOLD -> goldBackpack.upgradeSlots
        BackpackTier.DIAMOND -> diamondBackpack.upgradeSlots
        BackpackTier.OBSIDIAN -> obsidianBackpack.upgradeSlots
    }

    class LeatherBackpackConfig { var slots = 27; var upgradeSlots = 1 }
    class IronBackpackConfig { var slots = 54; var upgradeSlots = 2 }
    class GoldBackpackConfig { var slots = 81; var upgradeSlots = 3 }
    class DiamondBackpackConfig { var slots = 108; var upgradeSlots = 5 }
    class ObsidianBackpackConfig { var slots = 120; var upgradeSlots = 7 }

    class StackUpgradeConfig {
        var leatherMultiplier = 2
        var ironMultiplier = 4
        var goldMultiplier = 8
        var diamondMultiplier = 16
        var obsidianMultiplier = 32
    }
}
