package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.oredict.ShapedOreRecipe

class BackpackUpgradeRecipe(output: ItemStack, vararg args: Any) : IRecipe {

    private val inner = ShapedOreRecipe(output, *args)

    override fun matches(inv: InventoryCrafting, world: World): Boolean = inner.matches(inv, world)

    override fun getCraftingResult(inv: InventoryCrafting): ItemStack {
        val result = inner.recipeOutput.copy()
        for (i in 0 until inv.sizeInventory) {
            val stack = inv.getStackInSlot(i) ?: continue
            if (stack.item is BackpackItem) {
                stack.tagCompound?.let { result.tagCompound = it.copy() as NBTTagCompound }
                break
            }
        }
        return result
    }

    override fun getRecipeSize(): Int = inner.recipeSize
    override fun getRecipeOutput(): ItemStack = inner.recipeOutput
}
