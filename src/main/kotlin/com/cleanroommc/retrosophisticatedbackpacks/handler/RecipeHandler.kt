package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.item.Items as RSBItems
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.init.Blocks as MCBlocks
import net.minecraft.init.Items as MCItems
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.ShapedOreRecipe

object RecipeHandler {

    fun registerRecipes() {
        backpacks()
        upgradeBase()
        stackUpgrades()
        upgrades()
        advancedUpgrades()
    }

    private fun backpacks() {
        // Leather: SLS / SCS / LLL
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.backpackLeather),
            "SLS", "SCS", "LLL",
            'S', MCItems.string,
            'L', MCItems.leather,
            'C', MCBlocks.chest
        )
        // Iron: UUU / UBU / UUU — surrounding with iron ingots upgrades the previous tier
        GameRegistry.addRecipe(BackpackUpgradeRecipe(
            ItemStack(RSBItems.backpackIron),
            "UUU", "UBU", "UUU",
            'U', MCItems.iron_ingot,
            'B', RSBItems.backpackLeather!!
        ))
        GameRegistry.addRecipe(BackpackUpgradeRecipe(
            ItemStack(RSBItems.backpackGold),
            "UUU", "UBU", "UUU",
            'U', MCItems.gold_ingot,
            'B', RSBItems.backpackIron!!
        ))
        GameRegistry.addRecipe(BackpackUpgradeRecipe(
            ItemStack(RSBItems.backpackDiamond),
            "UUU", "UBU", "UUU",
            'U', MCItems.diamond,
            'B', RSBItems.backpackGold!!
        ))
        // Obsidian: UNU / NBN / UNU
        GameRegistry.addRecipe(BackpackUpgradeRecipe(
            ItemStack(RSBItems.backpackObsidian),
            "UNU", "NBN", "UNU",
            'U', MCBlocks.obsidian,
            'N', MCItems.nether_star,
            'B', RSBItems.backpackDiamond!!
        ))
    }

    private fun upgradeBase() {
        // SIS / ILI / SIS
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.upgradeBase),
            "SIS", "ILI", "SIS",
            'S', MCItems.string,
            'I', MCItems.iron_ingot,
            'L', MCItems.leather
        )
    }

    private fun stackUpgrades() {
        // Starter: coal_block surrounds upgrade_base
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.stackUpgradeTierStarter),
            "UUU", "UBU", "UUU",
            'U', MCBlocks.coal_block,
            'B', RSBItems.upgradeBase
        )
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.stackUpgradeTier1),
            "UUU", "UBU", "UUU",
            'U', MCBlocks.iron_block,
            'B', RSBItems.stackUpgradeTierStarter
        )
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.stackUpgradeTier2),
            "UUU", "UBU", "UUU",
            'U', MCBlocks.gold_block,
            'B', RSBItems.stackUpgradeTier1
        )
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.stackUpgradeTier3),
            "UUU", "UBU", "UUU",
            'U', MCBlocks.diamond_block,
            'B', RSBItems.stackUpgradeTier2
        )
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.stackUpgradeTier4),
            "UUU", "UBU", "UUU",
            'U', MCItems.nether_star,
            'B', RSBItems.stackUpgradeTier3
        )
        // Exponential: CNC / UBU / EUE
        // end_crystal doesn't exist in 1.7.10 — substituted with ender_eye
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.exponentialStackUpgrade),
            "CNC", "UBU", "EUE",
            'C', MCItems.ender_eye,
            'N', MCItems.nether_star,
            'U', RSBItems.stackUpgradeTier4,
            'B', RSBItems.upgradeBase,
            'E', MCBlocks.emerald_block
        )
    }

    private fun upgrades() {
        // Deposit:  P  / IBI / RCR
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.depositUpgrade),
            " P ", "IBI", "RCR",
            'P', MCBlocks.piston,
            'I', MCItems.iron_ingot,
            'B', RSBItems.upgradeBase,
            'R', MCItems.redstone,
            'C', MCBlocks.chest
        )
        // Pickup:  P  / SBS / RRR
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.pickupUpgrade),
            " P ", "SBS", "RRR",
            'P', MCBlocks.sticky_piston,
            'S', MCItems.string,
            'B', RSBItems.upgradeBase,
            'R', MCItems.redstone
        )
        // Feeding:  C  / PBW /  E
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.feedingUpgrade),
            " C ", "PBW", " E ",
            'C', MCItems.golden_carrot,
            'P', MCItems.golden_apple,
            'W', MCItems.speckled_melon,
            'E', MCItems.ender_pearl,
            'B', RSBItems.upgradeBase
        )
        // Filter: RSR / SBS / RSR
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.filterUpgrade),
            "RSR", "SBS", "RSR",
            'R', MCItems.redstone,
            'S', MCItems.string,
            'B', RSBItems.upgradeBase
        )
        // Restock:  P  / IBI / RCR
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.restockUpgrade),
            " P ", "IBI", "RCR",
            'P', MCBlocks.sticky_piston,
            'I', MCItems.iron_ingot,
            'B', RSBItems.upgradeBase,
            'R', MCItems.redstone,
            'C', MCBlocks.chest
        )
        // Inception: ENE / DBD / EDE
        GameRegistry.addShapedRecipe(
            ItemStack(RSBItems.inceptionUpgrade),
            "ENE", "DBD", "EDE",
            'E', MCItems.ender_eye,
            'N', MCItems.nether_star,
            'D', MCItems.diamond,
            'B', RSBItems.upgradeBase
        )
    }

    private fun advancedUpgrades() {
        // All advanced upgrades share the pattern:  D  / GBG / RRR
        // where B is the base (non-advanced) upgrade
        val adv = listOf(
            RSBItems.advancedDepositUpgrade  to RSBItems.depositUpgrade,
            RSBItems.advancedPickupUpgrade   to RSBItems.pickupUpgrade,
            RSBItems.advancedFeedingUpgrade  to RSBItems.feedingUpgrade,
            RSBItems.advancedFilterUpgrade   to RSBItems.filterUpgrade,
            RSBItems.advancedRestockUpgrade  to RSBItems.restockUpgrade,
        )
        for ((output, base) in adv) {
            GameRegistry.addShapedRecipe(
                ItemStack(output),
                " D ", "GBG", "RRR",
                'D', MCItems.diamond,
                'G', MCItems.gold_ingot,
                'B', base,
                'R', MCItems.redstone
            )
        }
    }
}
