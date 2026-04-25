package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.block.Blocks
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import com.cleanroommc.retrosophisticatedbackpacks.util.IModelRegister
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.item.Item

object RegistryHandler {
    @JvmField
    val MODELS = mutableListOf<IModelRegister>()

    fun registerAll() {
        registerItems()
        registerBlocks()
        registerTileEntities()
        RecipeHandler.registerRecipes()
    }

    private fun registerItems() {
        // Trigger Items object initialization — upgrade items add themselves to Items.ITEMS in their init blocks
        val items = Items.ITEMS
        for (item in items) {
            if (item is UpgradeItem) {
                GameRegistry.registerItem(item, item.registryNameStr)
            }
        }
    }

    private fun registerBlocks() {
        for (block in Blocks.BACKPACK_BLOCKS) {
            // GameRegistry creates BackpackItem(block) via reflection; BackpackItem.init adds it to Items lists
            GameRegistry.registerBlock(block, BackpackItem::class.java, block.registryName)
        }
        // Populate named item references after registration
        Items.backpackLeather = Item.getItemFromBlock(Blocks.leatherBackpack) as? BackpackItem
        Items.backpackIron = Item.getItemFromBlock(Blocks.ironBackpack) as? BackpackItem
        Items.backpackGold = Item.getItemFromBlock(Blocks.goldBackpack) as? BackpackItem
        Items.backpackDiamond = Item.getItemFromBlock(Blocks.diamondBackpack) as? BackpackItem
        Items.backpackObsidian = Item.getItemFromBlock(Blocks.obsidianBackpack) as? BackpackItem
    }

    private fun registerTileEntities() {
        GameRegistry.registerTileEntity(BackpackTileEntity::class.java, "${Tags.MOD_ID}:backpack")
    }
}
