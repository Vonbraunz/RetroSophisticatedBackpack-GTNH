package com.cleanroommc.retrosophisticatedbackpacks.util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList

object BackpackItemStackHelper {

    fun saveAllSlotsExtended(nbt: NBTTagCompound, inventory: ArrayList<ItemStack?>): NBTTagCompound {
        val list = NBTTagList()
        for ((i, stack) in inventory.withIndex()) {
            if (stack == null || stack.stackSize <= 0) continue
            val tag = NBTTagCompound()
            tag.setByte("Slot", i.toByte())
            stack.writeToNBTExtended(tag)
            list.appendTag(tag)
        }
        nbt.setTag("Items", list)
        return nbt
    }

    fun ItemStack.writeToNBTExtended(nbt: NBTTagCompound): NBTTagCompound {
        writeToNBT(nbt)
        nbt.setInteger("Count", stackSize)
        return nbt
    }

    fun loadAllItemsExtended(nbt: NBTTagCompound, inventory: ArrayList<ItemStack?>) {
        val list: NBTTagList = nbt.getTagList("Items", 10)
        for (i in 0 until list.tagCount()) {
            val tag = list.getCompoundTagAt(i)
            val j = tag.getByte("Slot").toInt() and 255
            if (j < inventory.size) inventory[j] = loadItemStackExtended(tag)
        }
    }

    fun loadItemStackExtended(nbt: NBTTagCompound): ItemStack? {
        val stack = ItemStack.loadItemStackFromNBT(nbt) ?: return null
        stack.stackSize = nbt.getInteger("Count")
        return stack
    }
}
