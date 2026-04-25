package com.cleanroommc.retrosophisticatedbackpacks.client

import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.IItemRenderer.ItemRenderType
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
object BackpackItemRenderer : IItemRenderer {

    override fun handleRenderType(item: ItemStack, type: ItemRenderType): Boolean =
        type == ItemRenderType.INVENTORY

    override fun shouldUseRenderHelper(type: ItemRenderType, item: ItemStack, helper: ItemRendererHelper): Boolean = false

    override fun renderItem(type: ItemRenderType, item: ItemStack, vararg data: Any) {
        val icon = (item.item as? BackpackItem)?.getIconFromDamage(item.itemDamage) ?: return

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        Minecraft.getMinecraft().textureManager.bindTexture(TextureMap.locationItemsTexture)

        val t = Tessellator.instance
        t.startDrawingQuads()
        t.addVertexWithUV(0.0, 16.0, 0.0, icon.minU.toDouble(), icon.maxV.toDouble())
        t.addVertexWithUV(16.0, 16.0, 0.0, icon.maxU.toDouble(), icon.maxV.toDouble())
        t.addVertexWithUV(16.0, 0.0, 0.0, icon.maxU.toDouble(), icon.minV.toDouble())
        t.addVertexWithUV(0.0, 0.0, 0.0, icon.minU.toDouble(), icon.minV.toDouble())
        t.draw()

        GL11.glDisable(GL11.GL_BLEND)
    }
}
