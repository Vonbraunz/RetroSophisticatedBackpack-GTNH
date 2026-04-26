package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.oredict.ShapedOreRecipe

class BackpackUpgradeRecipe(output: ItemStack, vararg args: Any) : ShapedOreRecipe(output, *args) {

    override fun getCraftingResult(inv: InventoryCrafting): ItemStack {
        val result = recipeOutput.copy()
        for (i in 0 until inv.sizeInventory) {
            val stack = inv.getStackInSlot(i) ?: continue
            if (stack.item is BackpackItem) {
                stack.tagCompound?.let { result.tagCompound = it.copy() as NBTTagCompound }
                break
            }
        }
        return result
    }
}
