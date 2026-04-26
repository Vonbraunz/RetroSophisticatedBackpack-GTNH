package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class GuiBackpack(private val container: BackpackContainer) : GuiContainer(container) {

    companion object {
        private const val SLOT = BackpackContainer.SLOT_SIZE
        private const val TOP  = BackpackContainer.TOP_PAD       // 11
        private const val LPAD = BackpackContainer.LEFT_PAD      // 7
        private const val TPAD = 10                              // text left margin (LPAD + 3)
        private const val BPAD = 7                               // bottom padding inside panel
        private const val UCOL = BackpackContainer.UPGRADE_COL_WIDTH
        private const val UGAP = BackpackContainer.UPGRADE_GAP
        private const val USX  = BackpackContainer.UPGRADE_SLOT_X
        private const val UTOP = BackpackContainer.UPGRADE_TOP_PAD // 3 — tight top for upgrade column
        private const val HGAP = BackpackContainer.HOTBAR_GAP
        private const val IGAP = BackpackContainer.PLAYER_INV_GAP // 13 — space for "Inventory" label

        private const val COLOR_PANEL   = 0xFFC6C6C6.toInt()
        private const val COLOR_SHADOW  = 0xFF373737.toInt()
        private const val COLOR_HILIGHT = 0xFFFFFFFF.toInt()
        private const val COLOR_TEXT    = 0x404040

        private val SLOTS_TEX = ResourceLocation(Tags.MOD_ID, "textures/gui/slots_background.png")
    }

    init {
        xSize = UCOL + UGAP + LPAD + container.rowSize * SLOT + LPAD
        ySize = TOP + container.colSize * SLOT + IGAP + 3 * SLOT + HGAP + SLOT + BPAD
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        val panelX = guiLeft + UCOL + UGAP
        val panelW = LPAD + container.rowSize * SLOT + LPAD
        val N = container.colSize
        val mainH = TOP + N * SLOT

        // Single bordered panel covering the full GUI area
        drawPanel(panelX, guiTop, panelW, ySize)

        // "Backpack" label — top-left with comfortable margins (vertically centered in header)
        fontRendererObj.drawString("Backpack", panelX + TPAD, guiTop + (TOP - 8) / 2 + 1, COLOR_TEXT)

        // "Inventory" label — in the gap above player inventory with padding on both sides
        fontRendererObj.drawString("Inventory", panelX + TPAD, guiTop + mainH + 4, COLOR_TEXT)


        // Backpack slot borders
        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SLOTS_TEX)
        drawTexturedModalRect(panelX + LPAD - 1, guiTop + TOP - 1, 0, 0, container.rowSize * SLOT, N * SLOT)

        // Player inventory slot borders (9×3 main + hotbar)
        val pOffX = container.playerXOffset
        val playerY = guiTop + mainH + IGAP
        val hotbarY = playerY + 3 * SLOT + HGAP
        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SLOTS_TEX)
        drawTexturedModalRect(panelX + LPAD - 1 + pOffX, playerY - 1, 0, 0, 9 * SLOT, 3 * SLOT)
        drawTexturedModalRect(panelX + LPAD - 1 + pOffX, hotbarY - 1, 0, 0, 9 * SLOT, SLOT)

        // Upgrade column (separate panel to the left)
        drawUpgradeColumn(panelX)
    }

    private fun drawPanel(x: Int, y: Int, w: Int, h: Int) {
        drawRect(x,         y,         x + w,     y + h,     COLOR_PANEL)
        drawRect(x,         y,         x + w,     y + 1,     COLOR_SHADOW)
        drawRect(x,         y,         x + 1,     y + h,     COLOR_SHADOW)
        drawRect(x + w - 1, y,         x + w,     y + h,     COLOR_HILIGHT)
        drawRect(x,         y + h - 1, x + w,     y + h,     COLOR_HILIGHT)
    }

    private fun drawUpgradeColumn(panelX: Int) {
        if (container.wrapper.upgradeSlotsSize() == 0) return
        val upgradeH = UTOP + container.wrapper.upgradeSlotsSize() * SLOT + 2
        drawPanel(guiLeft, guiTop, UCOL, upgradeH)

        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SLOTS_TEX)
        drawTexturedModalRect(guiLeft + USX - 1, guiTop + UTOP - 1, 0, 0, SLOT, container.wrapper.upgradeSlotsSize() * SLOT)
    }
}
