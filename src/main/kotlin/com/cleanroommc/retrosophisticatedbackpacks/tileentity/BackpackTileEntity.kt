package com.cleanroommc.retrosophisticatedbackpacks.tileentity

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity

class BackpackTileEntity : TileEntity() {
    companion object {
        private const val BACKPACK_INVENTORY_TAG = "backpackInventory"
        const val GUI_ID = 0
    }

    val wrapper = BackpackWrapper()

    fun openGui(player: EntityPlayer) {
        player.openGui(RetroSophisticatedBackpacks.instance, GUI_ID, worldObj, xCoord, yCoord, zCoord)
    }

    override fun getDescriptionPacket(): Packet {
        val nbt = NBTTagCompound()
        writeToNBT(nbt)
        return S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 3, nbt)
    }

    override fun writeToNBT(compound: NBTTagCompound) {
        compound.setTag(BACKPACK_INVENTORY_TAG, wrapper.serializeNBT())
        super.writeToNBT(compound)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)
        if (compound.hasKey(BACKPACK_INVENTORY_TAG)) {
            wrapper.deserializeNBT(compound.getCompoundTag(BACKPACK_INVENTORY_TAG))
        } else {
            RetroSophisticatedBackpacks.LOGGER.warn("Backpack tile entity has no wrapper NBT")
        }
    }

    fun getDisplayName(): String = "container.backpack".asTranslationKey()
}
