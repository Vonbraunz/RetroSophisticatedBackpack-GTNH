package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IToggleable
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.handler.NetworkHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SToggleUpgradePacket
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class GuiBackpack(private val container: BackpackContainer) : GuiContainer(container) {

    companion object {
        private const val SLOT  = BackpackContainer.SLOT_SIZE
        private const val TOP   = BackpackContainer.TOP_PAD       // 11
        private const val LPAD  = BackpackContainer.LEFT_PAD      // 7
        private const val TPAD  = 10                              // text left margin
        private const val BPAD  = 7                               // bottom padding inside main panel
        private const val UCOL  = BackpackContainer.UPGRADE_COL_WIDTH  // 34
        private const val UGAP  = BackpackContainer.UPGRADE_GAP
        private const val USX   = BackpackContainer.UPGRADE_SLOT_X    // 3
        private const val UTOP  = BackpackContainer.UPGRADE_TOP_PAD   // 3
        private const val HGAP  = BackpackContainer.HOTBAR_GAP
        private const val IGAP  = BackpackContainer.PLAYER_INV_GAP    // 16

        // Toggle switch geometry (6×10 track, 6×5 thumb), positioned LEFT of each upgrade slot
        private const val SW_X  = 2               // left edge of switch (after 1px border + 1px gap)
        private const val SW_W  = 6               // track width  (vertical switch is narrow)
        private const val SW_H  = 10              // track height (vertical switch is tall)

        private const val COLOR_PANEL    = 0xFFC6C6C6.toInt()
        private const val COLOR_SHADOW   = 0xFF373737.toInt()
        private const val COLOR_HILIGHT  = 0xFFFFFFFF.toInt()
        private const val COLOR_TEXT     = 0x404040

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

        // Main panel
        drawPanel(panelX, guiTop, panelW, ySize)

        // Labels
        fontRendererObj.drawString("Backpack",  panelX + TPAD, guiTop + (TOP - 8) / 2 + 1, COLOR_TEXT)
        fontRendererObj.drawString("Inventory", panelX + TPAD, guiTop + mainH + 4,          COLOR_TEXT)

        // Backpack slot borders
        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SLOTS_TEX)
        drawTexturedModalRect(panelX + LPAD - 1, guiTop + TOP - 1, 0, 0, container.rowSize * SLOT, N * SLOT)

        // Player inventory slot borders
        val pOffX   = container.playerXOffset
        val playerY = guiTop + mainH + IGAP
        val hotbarY = playerY + 3 * SLOT + HGAP
        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SLOTS_TEX)
        drawTexturedModalRect(panelX + LPAD - 1 + pOffX, playerY - 1, 0, 0, 9 * SLOT, 3 * SLOT)
        drawTexturedModalRect(panelX + LPAD - 1 + pOffX, hotbarY - 1, 0, 0, 9 * SLOT, SLOT)

        // Upgrade column + slot borders + toggle switches
        drawUpgradeColumn()
    }

    private fun drawUpgradeColumn() {
        val upgradeCount = container.wrapper.upgradeSlotsSize()
        if (upgradeCount == 0) return

        val upgradeH = UTOP + upgradeCount * SLOT + 2
        drawPanel(guiLeft, guiTop, UCOL, upgradeH)

        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SLOTS_TEX)
        drawTexturedModalRect(guiLeft + USX - 1, guiTop + UTOP - 1, 0, 0, SLOT, upgradeCount * SLOT)

        for (i in 0 until upgradeCount) {
            val stack = container.wrapper.upgradeItemStackHandler.inventory.getOrNull(i) ?: continue
            val wrapper = (stack.item as? UpgradeItem)?.getWrapper(stack) as? IToggleable ?: continue
            val switchX = guiLeft + SW_X
            val switchY = guiTop + UTOP + i * SLOT + (SLOT - SW_H) / 2  // centered in slot height
            drawToggle(switchX, switchY, wrapper.enabled)
        }
    }

    /**
     * Vertical toggle switch: 6×10 track with a 6×5 sliding thumb.
     * Thumb at the top = enabled (green track), thumb at the bottom = disabled (gray track).
     */
    private fun drawToggle(x: Int, y: Int, enabled: Boolean) {
        val trackMain  = if (enabled) 0xFF2A6B2A.toInt() else 0xFF404040.toInt()
        val trackInset = if (enabled) 0xFF1A4B1A.toInt() else 0xFF2A2A2A.toInt()
        val thumbY     = if (enabled) y else y + SW_H - 5

        // Track background
        drawRect(x, y, x + SW_W, y + SW_H, trackMain)
        // Track inset shadow — top and left edges
        drawRect(x,        y,          x + SW_W, y + 1,     trackInset)
        drawRect(x,        y,          x + 1,    y + SW_H,  trackInset)
        // Track highlight — bottom and right edges
        drawRect(x + SW_W - 1, y,          x + SW_W, y + SW_H,  0xFF555555.toInt())
        drawRect(x,            y + SW_H - 1, x + SW_W, y + SW_H, 0xFF555555.toInt())

        // Thumb body (full width, half height)
        drawRect(x, thumbY, x + SW_W, thumbY + 5, 0xFFCCCCCC.toInt())
        // Thumb top highlight
        drawRect(x,        thumbY,     x + SW_W,  thumbY + 1,  0xFFEEEEEE.toInt())
        // Thumb left highlight
        drawRect(x,        thumbY,     x + 1,     thumbY + 5,  0xFFEEEEEE.toInt())
        // Thumb right shadow
        drawRect(x + SW_W - 1, thumbY,     x + SW_W, thumbY + 5, 0xFF888888.toInt())
        // Thumb bottom shadow
        drawRect(x,            thumbY + 4, x + SW_W, thumbY + 5, 0xFF888888.toInt())
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            val upgradeCount = container.wrapper.upgradeSlotsSize()
            for (i in 0 until upgradeCount) {
                val stack = container.wrapper.upgradeItemStackHandler.inventory.getOrNull(i) ?: continue
                val upgradeItem = stack.item as? UpgradeItem ?: continue
                val rawWrapper = upgradeItem.getWrapper(stack) ?: continue
                val toggleable = rawWrapper as? IToggleable ?: continue

                // Click zone covers the left strip of the column (where the switch sits)
                val stripX = guiLeft + 1
                val stripY = guiTop + UTOP + i * SLOT
                if (mouseX in stripX until (guiLeft + USX - 1) &&
                    mouseY in stripY until (stripY + SLOT)) {
                    // Optimistic client-side toggle for immediate visual feedback
                    toggleable.toggle()
                    upgradeItem.saveWrapper(stack, rawWrapper)
                    NetworkHandler.INSTANCE.sendToServer(C2SToggleUpgradePacket(i))
                    return
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun drawPanel(x: Int, y: Int, w: Int, h: Int) {
        drawRect(x,         y,         x + w,     y + h,     COLOR_PANEL)
        drawRect(x,         y,         x + w,     y + 1,     COLOR_SHADOW)
        drawRect(x,         y,         x + 1,     y + h,     COLOR_SHADOW)
        drawRect(x + w - 1, y,         x + w,     y + h,     COLOR_HILIGHT)
        drawRect(x,         y + h - 1, x + w,     y + h,     COLOR_HILIGHT)
    }
}
