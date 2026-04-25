package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class GuiBackpack(private val container: BackpackContainer) : GuiContainer(container) {

    companion object {
        // Background texture — covers the main panel area (slot grid + player inventory)
        // backpack_background_9.png encodes the 9-column layout; the 12-column version
        // would need a different texture (TODO: port backpack_background_12.png if present)
        private val BG_9  = ResourceLocation(Tags.MOD_ID, "textures/gui/backpack_background_9.png")
        // Standard chest-like background for the 12-wide layout when texture is missing
        private val BG_CHEST = ResourceLocation("minecraft", "textures/gui/container/generic_54.png")

        // Layout constants kept in sync with BackpackContainer
        private const val SLOT  = BackpackContainer.SLOT_SIZE
        private const val TOP   = BackpackContainer.TOP_PAD
        private const val LPAD  = BackpackContainer.LEFT_PAD
        private const val UGAP  = BackpackContainer.UPGRADE_GAP
        private const val UCOL  = BackpackContainer.UPGRADE_COL_WIDTH
    }

    init {
        // Total GUI width: upgrade column + gap + left pad + rowSize slots + right pad
        xSize = UCOL + UGAP + LPAD + container.rowSize * SLOT + LPAD
        // Total GUI height: top + backpack rows + gap + 3×player rows + hotbar gap + hotbar + bottom pad
        ySize = TOP + container.colSize * SLOT +
                BackpackContainer.PLAYER_INV_GAP +
                3 * SLOT +
                BackpackContainer.HOTBAR_GAP +
                SLOT +
                8
    }

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        GL11.glColor4f(1f, 1f, 1f, 1f)

        // Main panel background (covers backpack grid + player inventory)
        val bg = if (container.rowSize <= 9) BG_9 else BG_CHEST
        mc.textureManager.bindTexture(bg)

        val mainPanelX = guiLeft + UCOL + UGAP
        // Draw as many full 176×224 texture regions as needed (the texture was designed for 9-wide)
        drawTexturedModalRect(mainPanelX, guiTop, 0, 0,
            LPAD + container.rowSize * SLOT + LPAD,
            TOP + container.colSize * SLOT + BackpackContainer.PLAYER_INV_GAP + 3 * SLOT + BackpackContainer.HOTBAR_GAP + SLOT + 8)

        // Upgrade column background — draw a simple dark panel using the slots_background texture
        val slotsBg = ResourceLocation(Tags.MOD_ID, "textures/gui/slots_background.png")
        mc.textureManager.bindTexture(slotsBg)
        val upgradeH = TOP + container.wrapper.upgradeSlotsSize() * SLOT + 8
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, UCOL, upgradeH)
    }
}
