package com.cleanroommc.retrosophisticatedbackpacks.block

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpacks.client.RSBBlockRenderTypes
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackHelper
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import com.cleanroommc.retrosophisticatedbackpacks.util.IModelRegister
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.ArrayList

// Metadata layout (4 bits):
//   bits [1:0] = horizontal facing index (0=S,1=W,2=N,3=E)
//   bits [3:2] = unused (reserved for future tank/battery flags)
class BackpackBlock(
    val registryName: String,
    explosionResistance: Float,
    val tier: BackpackTier,
) : Block(Material.carpet), ITileEntityProvider, IModelRegister.Block {

    constructor(registryName: String, tier: BackpackTier) : this(registryName, 0.8f, tier)

    init {
        setBlockName(registryName.asTranslationKey())
        setCreativeTab(RetroSophisticatedBackpacks.CREATIVE_TAB)
        setResistance(explosionResistance)
        setHardness(0.8f)
        setStepSound(Block.soundTypeCloth)
        setLightOpacity(0)

        Blocks.BLOCKS.add(this)
        Blocks.BACKPACK_BLOCKS.add(this)
        RegistryHandler.MODELS.add(this)
    }

    private var clipsIcon: IIcon? = null

    override fun registerBlockIcons(register: IIconRegister) {
        clipsIcon = register.registerIcon("${Tags.MOD_ID}:${tier.registryName}_clips")
    }

    override fun getIcon(side: Int, meta: Int): IIcon = clipsIcon ?: super.getIcon(side, meta)

    override fun isOpaqueCube(): Boolean = false

    override fun getRenderType(): Int = RSBBlockRenderTypes.BACKPACK_RENDER_TYPE

    override fun setBlockBoundsBasedOnState(worldIn: IBlockAccess, x: Int, y: Int, z: Int) {
        val meta = worldIn.getBlockMetadata(x, y, z)
        val facingIdx = meta and 0b0011
        // facingIdx 0=S,1=W,2=N,3=E → S and N are along Z axis
        if (facingIdx == 0 || facingIdx == 2) {
            setBlockBounds(1 / 16f, 0f, 4 / 16f, 15 / 16f, 14 / 16f, 12 / 16f)
        } else {
            setBlockBounds(4 / 16f, 0f, 1 / 16f, 12 / 16f, 14 / 16f, 15 / 16f)
        }
    }

    override fun getMobilityFlag(): Int = 2  // DESTROY on piston push

    override fun hasComparatorInputOverride(): Boolean = true

    override fun getComparatorInputOverride(world: World, x: Int, y: Int, z: Int, side: Int): Int {
        val te = world.getTileEntity(x, y, z) as? BackpackTileEntity ?: return 0
        val wrapper = te.wrapper
        val slots = wrapper.getSlots()
        if (slots == 0) return 0
        var filledSlots = 0
        var fillSum = 0.0f
        for (i in 0 until slots) {
            val stack = wrapper.getStackInSlot(i) ?: continue
            if (stack.stackSize <= 0) continue
            val limit = wrapper.backpackItemStackHandler.getStackLimit(i, stack)
            fillSum += stack.stackSize.toFloat() / limit.toFloat()
            filledSlots++
        }
        if (filledSlots == 0) return 0
        return ((fillSum / slots) * 14).toInt() + 1
    }

    override fun onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, placer: EntityLivingBase, stack: ItemStack) {
        // Facing: opposite of placer's horizontal look
        val yaw = (placer.rotationYaw * 4f / 360f + 0.5f).toInt() and 3
        // yaw: 0=S,1=W,2=N,3=E (matches vanilla chest convention)
        val currentMeta = world.getBlockMetadata(x, y, z)
        world.setBlockMetadataWithNotify(x, y, z, (currentMeta and 0b1100) or (yaw and 0b0011), 2)

        // Copy backpack inventory from item stack into tile entity
        val wrapper = BackpackHelper.getWrapper(stack) ?: return
        val te = world.getTileEntity(x, y, z) as? BackpackTileEntity ?: return
        te.wrapper.deserializeNBT(wrapper.serializeNBT())
    }

    override fun onBlockActivated(
        world: World, x: Int, y: Int, z: Int,
        player: EntityPlayer, side: Int,
        hitX: Float, hitY: Float, hitZ: Float,
    ): Boolean {
        if (!world.isRemote) {
            if (player.isSneaking) {
                world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "dig.cloth", 1f, 0.5f)
                val meta = world.getBlockMetadata(x, y, z)
                for (drop in getDrops(world, x, y, z, meta, 0)) {
                    world.spawnEntityInWorld(EntityItem(world, x + 0.5, y + 0.5, z + 0.5, drop))
                }
                world.setBlockToAir(x, y, z)
                return true
            }
            val te = world.getTileEntity(x, y, z) as? BackpackTileEntity ?: return true
            te.openGui(player)
        }
        return true
    }

    override fun createNewTileEntity(world: World, meta: Int): TileEntity = BackpackTileEntity()

    override fun getDrops(world: World, x: Int, y: Int, z: Int, metadata: Int, fortune: Int): ArrayList<ItemStack> {
        val drops = ArrayList<ItemStack>()
        val stack = ItemStack(Item.getItemFromBlock(this))
        val te = world.getTileEntity(x, y, z) as? BackpackTileEntity
        if (te != null) {
            BackpackHelper.saveWrapper(stack, te.wrapper)
        }
        drops.add(stack)
        return drops
    }
}
