package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.block.BackpackBlock
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackHelper
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackGuiHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.util.IModelRegister
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.util.StatCollector
import net.minecraft.world.World

class BackpackItem(block: net.minecraft.block.Block) : ItemBlock(block), IModelRegister {
    val backpackBlock: BackpackBlock = block as BackpackBlock
    val tier get() = backpackBlock.tier
    val numberOfSlots get() = com.cleanroommc.retrosophisticatedbackpacks.config.Config.getSlotsForTier(tier)
    val numberOfUpgradeSlots get() = com.cleanroommc.retrosophisticatedbackpacks.config.Config.getUpgradeSlotsForTier(tier)

    init {
        setMaxStackSize(1)
        setCreativeTab(RetroSophisticatedBackpacks.CREATIVE_TAB)
        setUnlocalizedName(backpackBlock.registryName.asTranslationKey())
        Items.ITEMS.add(this)
        Items.BACKPACK_ITEMS.add(this)
        RegistryHandler.MODELS.add(this)
    }

    // Shift-click on a block → deposit/restock with that block's inventory
    override fun onItemUseFirst(
        stack: ItemStack, player: EntityPlayer, world: World,
        x: Int, y: Int, z: Int, side: Int,
        hitX: Float, hitY: Float, hitZ: Float
    ): Boolean {
        if (player.isSneaking) {
            val te = world.getTileEntity(x, y, z) as? IInventory ?: return false
            val wrapper = getOrCreateWrapper(stack) ?: return false
            var transferred = BackpackInventoryHelper.attemptDepositOnInventory(wrapper, te)
            transferred = BackpackInventoryHelper.attemptRestockFromInventory(wrapper, te) || transferred
            if (transferred) {
                BackpackHelper.saveWrapper(stack, wrapper)
                world.playSoundAtEntity(player, "mob.armor.equip_iron", 0.5f, 0.5f)
                return true
            }
        }
        return false
    }

    // Normal right-click on block: if sneaking, place block; otherwise open GUI
    override fun onItemUse(
        stack: ItemStack, player: EntityPlayer, world: World,
        x: Int, y: Int, z: Int, side: Int,
        hitX: Float, hitY: Float, hitZ: Float
    ): Boolean {
        val te = world.getTileEntity(x, y, z)
        if (player.isSneaking && te != null) {
            return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ)
        }
        if (!world.isRemote) {
            ensureWrapper(stack)
            player.openGui(RetroSophisticatedBackpacks.instance, BackpackGuiHandler.BACKPACK_ITEM_GUI_ID, world, player.inventory.currentItem, 0, 0)
        }
        return true
    }

    // Right-click on air → open GUI
    override fun onItemRightClick(stack: ItemStack, world: World, player: EntityPlayer): ItemStack {
        if (!world.isRemote) {
            ensureWrapper(stack)
            player.openGui(RetroSophisticatedBackpacks.instance, BackpackGuiHandler.BACKPACK_ITEM_GUI_ID, world, player.inventory.currentItem, 0, 0)
        }
        return stack
    }

    // Shift-click on entity → deposit/restock with entity inventory
    override fun itemInteractionForEntity(stack: ItemStack, player: EntityPlayer, entity: EntityLivingBase): Boolean {
        if (player.isSneaking) {
            val wrapper = getOrCreateWrapper(stack) ?: return false
            var transferred = BackpackInventoryHelper.attemptDepositOnEntity(wrapper, entity)
            transferred = BackpackInventoryHelper.attemptRestockFromEntity(wrapper, entity) || transferred
            if (transferred) {
                BackpackHelper.saveWrapper(stack, wrapper)
                player.worldObj.playSoundAtEntity(player, "mob.armor.equip_iron", 0.5f, 0.5f)
                return true
            }
        }
        return false
    }

    // Feed upgrade tick, UUID cache
    override fun onUpdate(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (!world.isRemote && entity is EntityPlayerMP) {
            val wrapper = BackpackHelper.getWrapper(stack) ?: return
            if (entity.ticksExisted % 20 == 0) wrapper.feed(entity, wrapper.backpackItemStackHandler)
            if (!wrapper.isCached) CapabilityHandler.cacheBackpackInventory(wrapper)
        }
    }

    @Suppress("UNCHECKED_CAST", "OVERRIDE_DEPRECATION")
    override fun addInformation(stack: ItemStack, player: EntityPlayer?, tooltip: MutableList<*>, advanced: Boolean) {
        (tooltip as MutableList<String>).add(
            StatCollector.translateToLocalFormatted(
                "tooltip.backpack.inventory_size".asTranslationKey(), numberOfSlots
            )
        )
        (tooltip as MutableList<String>).add(
            StatCollector.translateToLocalFormatted(
                "tooltip.backpack.upgrade_slots_size".asTranslationKey(), numberOfUpgradeSlots
            )
        )
    }

    private var itemIcon: IIcon? = null

    override fun registerIcons(register: IIconRegister) {
        itemIcon = register.registerIcon("${Tags.MOD_ID}:backpack_${tier.registryName}")
    }

    override fun getIconFromDamage(meta: Int): IIcon = itemIcon ?: super.getIconFromDamage(meta)

    override fun getSpriteNumber(): Int = 1

    override fun registerModels() {}

    private fun getOrCreateWrapper(stack: ItemStack): BackpackWrapper? {
        val existing = BackpackHelper.getWrapper(stack)
        if (existing != null) return existing
        val wrapper = BackpackWrapper({ numberOfSlots }, { numberOfUpgradeSlots })
        BackpackHelper.saveWrapper(stack, wrapper)
        return wrapper
    }

    private fun ensureWrapper(stack: ItemStack) {
        if (BackpackHelper.getWrapper(stack) == null) {
            BackpackHelper.saveWrapper(stack, BackpackWrapper({ numberOfSlots }, { numberOfUpgradeSlots }))
        }
    }
}
