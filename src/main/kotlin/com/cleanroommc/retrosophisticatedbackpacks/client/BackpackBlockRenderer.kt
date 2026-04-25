package com.cleanroommc.retrosophisticatedbackpacks.client

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.world.IBlockAccess

@SideOnly(Side.CLIENT)
object BackpackBlockRenderer : ISimpleBlockRenderingHandler {

    override fun renderWorldBlock(
        world: IBlockAccess, x: Int, y: Int, z: Int,
        block: Block, modelId: Int, renderer: RenderBlocks
    ): Boolean = renderer.renderStandardBlock(block, x, y, z)

    override fun renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks) {}

    // false → inventory uses the 2D icon path (getIconFromDamage) instead of 3D block render
    override fun shouldRender3DInInventory(modelId: Int): Boolean = false

    override fun getRenderId(): Int = RSBBlockRenderTypes.BACKPACK_RENDER_TYPE
}
