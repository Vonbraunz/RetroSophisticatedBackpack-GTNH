package com.cleanroommc.retrosophisticatedbackpacks.integration.nei

import codechicken.nei.PositionedStack
import codechicken.nei.recipe.TemplateRecipeHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.item.ItemStack

class BackpackUpgradeHandler : TemplateRecipeHandler() {

    inner class CachedUpgradeSlot(backpack: ItemStack, upgrade: ItemStack) : CachedRecipe() {
        val posBackpack = PositionedStack(backpack, BACKPACK_X, SLOT_Y)
        val posUpgrade  = PositionedStack(upgrade,  UPGRADE_X, SLOT_Y)
        override fun getIngredients(): List<PositionedStack> = listOf(posBackpack, posUpgrade)
        override fun getResult(): PositionedStack? = null
    }

    override fun getRecipeName(): String = "Backpack Upgrade Slot"
    override fun getGuiTexture(): String = "retro_sophisticated_backpacks:textures/gui/backpack_background_9.png"

    override fun drawBackground(recipe: Int) {
        drawSlotBox(BACKPACK_X, SLOT_Y)
        drawSlotBox(UPGRADE_X, SLOT_Y)
        val fr = Minecraft.getMinecraft().fontRenderer
        fr.drawString("→", BACKPACK_X + 20, SLOT_Y + 4, 0x555555)
        fr.drawString("Place in upgrade slot", 5, 50, 0x555555)
    }

    private fun drawSlotBox(x: Int, y: Int) {
        Gui.drawRect(x - 1, y - 1, x + 17, y + 17, 0xFF373737.toInt())
        Gui.drawRect(x,     y,     x + 16, y + 16, 0xFF8B8B8B.toInt())
    }

    override fun loadCraftingRecipes(outputId: String, vararg results: Any) {}

    override fun loadCraftingRecipes(result: ItemStack) {
        if (result.item !is BackpackItem) return
        for (upgrade in allUpgrades()) {
            arecipes.add(CachedUpgradeSlot(result.copy(), ItemStack(upgrade)))
        }
    }

    override fun loadUsageRecipes(ingredient: ItemStack) {
        if (ingredient.item !is UpgradeItem) return
        for (backpack in allBackpacks()) {
            arecipes.add(CachedUpgradeSlot(ItemStack(backpack), ingredient.copy()))
        }
    }

    private fun allBackpacks() = listOfNotNull(
        Items.backpackLeather, Items.backpackIron, Items.backpackGold,
        Items.backpackDiamond, Items.backpackObsidian
    )

    private fun allUpgrades(): List<UpgradeItem> = listOfNotNull(
        Items.stackUpgradeTierStarter, Items.stackUpgradeTier1, Items.stackUpgradeTier2,
        Items.stackUpgradeTier3, Items.stackUpgradeTier4, Items.exponentialStackUpgrade,
        Items.inceptionUpgrade, Items.pickupUpgrade, Items.advancedPickupUpgrade,
        Items.feedingUpgrade, Items.advancedFeedingUpgrade, Items.depositUpgrade,
        Items.advancedDepositUpgrade, Items.restockUpgrade, Items.advancedRestockUpgrade,
        Items.filterUpgrade, Items.advancedFilterUpgrade,
        Items.magnetUpgrade, Items.advancedMagnetUpgrade
    ).filterIsInstance<UpgradeItem>()

    companion object {
        const val BACKPACK_X = 8
        const val UPGRADE_X  = 58
        const val SLOT_Y     = 23
    }
}
