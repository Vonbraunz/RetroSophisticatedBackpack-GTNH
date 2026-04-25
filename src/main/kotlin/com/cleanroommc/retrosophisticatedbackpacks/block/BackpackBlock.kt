package com.cleanroommc.retrosophisticatedbackpacks.block

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackTier
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackHelper
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import com.cleanroommc.retrosophisticatedbackpacks.util.IModelRegister
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.BlockState
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.ArrayList

class BackpackBlock(
    val registryName: String,
    explosionResistance: Float,
    val tier: BackpackTier,
) : Block(Material.carpet), ITileEntityProvider, IModelRegister.Block {

    companion object {
        val LEFT_TANK: PropertyBool = PropertyBool.create("left_tank")
        val RIGHT_TANK: PropertyBool = PropertyBool.create("right_tank")
        val BATTERY: PropertyBool = PropertyBool.create("battery")
        val FACING: PropertyDirection = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL)

        private val BOOL_PROPERTIES = arrayOf(LEFT_TANK, RIGHT_TANK, BATTERY)
    }

    constructor(registryName: String, tier: BackpackTier) : this(registryName, 0.8f, tier)

    init {
        setUnlocalizedName(registryName.asTranslationKey())
        setCreativeTab(RetroSophisticatedBackpacks.CREATIVE_TAB)
        setResistance(explosionResistance)
        setHardness(0.8f)
        setStepSound(Block.soundTypeCloth)
        setLightOpacity(0)
        defaultState = blockState.baseState
            .withProperty(LEFT_TANK, false)
            .withProperty(RIGHT_TANK, false)
            .withProperty(BATTERY, false)
            .withProperty(FACING, EnumFacing.NORTH)

        Blocks.BLOCKS.add(this)
        Blocks.BACKPACK_BLOCKS.add(this)
        RegistryHandler.MODELS.add(this)
    }

    override fun isOpaqueCube(): Boolean = false

    override fun setBlockBoundsBasedOnState(worldIn: IBlockAccess, x: Int, y: Int, z: Int) {
        val state = getStateFromMeta(worldIn.getBlockMetadata(x, y, z))
        when (state.getValue(FACING)) {
            EnumFacing.NORTH, EnumFacing.SOUTH ->
                setBlockBounds(1 / 16f, 0f, 4 / 16f, 15 / 16f, 14 / 16f, 12 / 16f)
            else ->
                setBlockBounds(4 / 16f, 0f, 1 / 16f, 12 / 16f, 14 / 16f, 15 / 16f)
        }
    }

    override fun createBlockState(): BlockState =
        BlockState(this, LEFT_TANK, RIGHT_TANK, BATTERY, FACING)

    override fun getStateFromMeta(meta: Int): IBlockState {
        val leftTank = (meta and 0b10000) shr 4 == 1
        val rightTank = (meta and 0b01000) shr 3 == 1
        val battery = (meta and 0b00100) shr 2 == 1
        val facing = EnumFacing.byHorizontalIndex(meta and 0b00011)
        return defaultState
            .withProperty(LEFT_TANK, leftTank)
            .withProperty(RIGHT_TANK, rightTank)
            .withProperty(BATTERY, battery)
            .withProperty(FACING, facing)
    }

    override fun getMetaFromState(state: IBlockState): Int {
        var meta = 0
        for (boolProp in BOOL_PROPERTIES) {
            if (state.getValue(boolProp)) meta = meta or 1
            meta = meta shl 1
        }
        meta = meta or state.getValue(FACING).horizontalIndex
        return meta
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
        // Update facing metadata based on placer direction
        val facingBits = placer.horizontalFacing.opposite.horizontalIndex
        val currentMeta = world.getBlockMetadata(x, y, z)
        world.setBlockMetadataWithNotify(x, y, z, (currentMeta and 0b11100) or facingBits, 2)

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
