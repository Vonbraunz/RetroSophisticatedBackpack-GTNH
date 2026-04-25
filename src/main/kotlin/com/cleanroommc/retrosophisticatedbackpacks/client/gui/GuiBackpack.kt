package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import net.minecraft.client.gui.inventory.GuiContainer

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

        private const val COLOR_PANEL   = 0xFFC6C6C6.toInt()
        private const val COLOR_SLOT_BG = 0xFF8B8B8B.toInt()
        private const val COLOR_SHADOW  = 0xFF373737.toInt()
        private const val COLOR_HILIGHT = 0xFFFFFFFF.toInt()
    }

    init {
        xSize = UCOL + UGAP + LPAD + container.rowSize * SLOT + LPAD
        ySize = TOP + container.colSize * SLOT + 96
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        val panelX = guiLeft + UCOL + UGAP
        val panelW = LPAD + container.rowSize * SLOT + LPAD
        val chestH = TOP + container.colSize * SLOT

        // Main panel background
        drawRect(panelX, guiTop, panelX + panelW, guiTop + ySize, COLOR_PANEL)
        drawRect(panelX,               guiTop,             panelX + panelW,     guiTop + 1,        COLOR_SHADOW)
        drawRect(panelX,               guiTop,             panelX + 1,          guiTop + ySize,    COLOR_SHADOW)
        drawRect(panelX + panelW - 1,  guiTop,             panelX + panelW,     guiTop + ySize,    COLOR_HILIGHT)
        drawRect(panelX,               guiTop + ySize - 1, panelX + panelW,     guiTop + ySize,    COLOR_HILIGHT)

        // Backpack inventory slot holes
        for (row in 0 until container.colSize) {
            for (col in 0 until container.rowSize) {
                slotHole(panelX + LPAD + col * SLOT, guiTop + TOP + row * SLOT)
            }
        }

        // Player main inventory slot holes (3 × 9)
        val playerY = guiTop + chestH + PGAP
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                slotHole(panelX + LPAD + col * SLOT, playerY + row * SLOT)
            }
        }

        // Hotbar slot holes (1 × 9)
        val hotbarY = playerY + 3 * SLOT + HGAP
        for (col in 0 until 9) {
            slotHole(panelX + LPAD + col * SLOT, hotbarY)
        }

        // Upgrade column — flush against main panel
        drawRect(guiLeft, guiTop, panelX, guiTop + ySize, COLOR_PANEL)
        drawRect(guiLeft,           guiTop,              guiLeft + 1,  guiTop + ySize,    COLOR_SHADOW)
        drawRect(guiLeft,           guiTop,              panelX,       guiTop + 1,        COLOR_SHADOW)
        drawRect(guiLeft,           guiTop + ySize - 1,  panelX,       guiTop + ySize,    COLOR_HILIGHT)

        // Upgrade slot holes
        for (i in 0 until container.wrapper.upgradeSlotsSize()) {
            slotHole(guiLeft + USX, guiTop + TOP + i * SLOT)
        }
    }

    // Draws a single slot hole: dark 1px outer ring + darker inner fill.
    // sx/sy are the slot's top-left corner (as placed in the container).
    private fun slotHole(sx: Int, sy: Int) {
        drawRect(sx - 1, sy - 1, sx + SLOT - 1, sy + SLOT - 1, COLOR_SHADOW)
        drawRect(sx,     sy,     sx + SLOT - 2, sy + SLOT - 2, COLOR_SLOT_BG)
    }
}
