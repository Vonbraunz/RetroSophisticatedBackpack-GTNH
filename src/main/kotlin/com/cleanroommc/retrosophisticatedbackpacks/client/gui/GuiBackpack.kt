package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class GuiBackpack(private val container: BackpackContainer) : GuiContainer(container) {

    companion object {
        private const val SLOT = BackpackContainer.SLOT_SIZE
        private const val TOP  = BackpackContainer.TOP_PAD
        private const val LPAD = BackpackContainer.LEFT_PAD
        private const val UCOL = BackpackContainer.UPGRADE_COL_WIDTH
        private const val UGAP = BackpackContainer.UPGRADE_GAP
        private const val USX  = BackpackContainer.UPGRADE_SLOT_X
        private const val PGAP = BackpackContainer.PLAYER_INV_GAP
        private const val HGAP = BackpackContainer.HOTBAR_GAP

        // PGAP = 26 = main-panel-bottom-pad(5) + panel-gap(4) + inv-label-area(17)
        private const val MAIN_BPAD = 5     // bottom padding inside main panel
        private const val PANEL_GAP = 4     // visible gap between the two panels
        private const val INV_TPAD  = 17    // top padding inside inventory panel (room for label)

        private const val COLOR_PANEL   = 0xFFC6C6C6.toInt()
        private const val COLOR_SHADOW  = 0xFF373737.toInt()
        private const val COLOR_HILIGHT = 0xFFFFFFFF.toInt()
        private const val COLOR_TEXT    = 0x404040

        private val SLOTS_TEX = ResourceLocation(Tags.MOD_ID, "textures/gui/slots_background.png")
    }

    // Inventory panel matches main panel width; player slots are centered within it
    private val invPanelW get() = LPAD + container.rowSize * SLOT + LPAD

    init {
        xSize = UCOL + UGAP + LPAD + container.rowSize * SLOT + LPAD
        // ySize = chest area + main bottom pad + gap + inv label + 3 rows + hotbar gap + hotbar + inv bottom pad
        ySize = TOP + container.colSize * SLOT + MAIN_BPAD + PANEL_GAP + INV_TPAD + 3 * SLOT + HGAP + SLOT + MAIN_BPAD
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        val panelX    = guiLeft + UCOL + UGAP
        val panelW    = LPAD + container.rowSize * SLOT + LPAD
        val chestH    = TOP + container.colSize * SLOT

        // y-coordinates of the two panels
        val mainTop   = guiTop
        val mainBot   = guiTop + chestH + MAIN_BPAD
        val invTop    = mainBot + PANEL_GAP
        val invBot    = guiTop + ySize

        // ── Upgrade column panel (left, height matches main panel) ──────────────
        val upgradeH = TOP + container.wrapper.upgradeSlotsSize() * SLOT + MAIN_BPAD
        drawRect(guiLeft, mainTop, panelX, mainTop + upgradeH, COLOR_PANEL)
        drawRect(guiLeft,            mainTop,                guiLeft + 1, mainTop + upgradeH, COLOR_SHADOW)
        drawRect(guiLeft,            mainTop,                panelX,      mainTop + 1,        COLOR_SHADOW)
        drawRect(panelX - 1,         mainTop,                panelX,      mainTop + upgradeH, COLOR_HILIGHT)
        drawRect(guiLeft,            mainTop + upgradeH - 1, panelX,      mainTop + upgradeH, COLOR_HILIGHT)

        if (container.wrapper.upgradeSlotsSize() > 0) {
            GL11.glColor4f(1f, 1f, 1f, 1f)
            mc.textureManager.bindTexture(SLOTS_TEX)
            drawTexturedModalRect(guiLeft + USX - 1, mainTop + TOP - 1, 0, 0, SLOT, container.wrapper.upgradeSlotsSize() * SLOT)
        }

        // ── Main backpack panel ─────────────────────────────────────────────────
        drawRect(panelX, mainTop, panelX + panelW, mainBot, COLOR_PANEL)
        drawRect(panelX,              mainTop,     panelX + panelW, mainTop + 1,  COLOR_SHADOW)
        drawRect(panelX,              mainTop,     panelX + 1,      mainBot,      COLOR_SHADOW)
        drawRect(panelX + panelW - 1, mainTop,     panelX + panelW, mainBot,      COLOR_HILIGHT)
        drawRect(panelX,              mainBot - 1, panelX + panelW, mainBot,      COLOR_HILIGHT)

        fontRendererObj.drawString("Backpack", panelX + LPAD, mainTop + 6, COLOR_TEXT)

        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SLOTS_TEX)
        drawTexturedModalRect(panelX + LPAD - 1, mainTop + TOP - 1, 0, 0, container.rowSize * SLOT, container.colSize * SLOT)

        // ── Player inventory panel ──────────────────────────────────────────────
        drawRect(panelX, invTop, panelX + invPanelW, invBot, COLOR_PANEL)
        drawRect(panelX,                 invTop,     panelX + invPanelW, invTop + 1,  COLOR_SHADOW)
        drawRect(panelX,                 invTop,     panelX + 1,         invBot,      COLOR_SHADOW)
        drawRect(panelX + invPanelW - 1, invTop,     panelX + invPanelW, invBot,      COLOR_HILIGHT)
        drawRect(panelX,                 invBot - 1, panelX + invPanelW, invBot,      COLOR_HILIGHT)

        fontRendererObj.drawString("Inventory", panelX + LPAD, invTop + 5, COLOR_TEXT)

        val playerY  = guiTop + chestH + PGAP
        val hotbarY  = playerY + 3 * SLOT + HGAP
        val pSlotX   = panelX + LPAD - 1 + container.playerXOffset
        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SLOTS_TEX)
        drawTexturedModalRect(pSlotX, playerY - 1, 0, 0, 9 * SLOT, 3 * SLOT)
        drawTexturedModalRect(pSlotX, hotbarY - 1, 0, 0, 9 * SLOT, SLOT)
    }
}
