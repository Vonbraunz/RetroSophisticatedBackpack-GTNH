package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvancedFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IFilterUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IToggleable
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.handler.NetworkHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SToggleUpgradePacket
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SUpgradeSettingPacket
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class GuiBackpack(private val container: BackpackContainer) : GuiContainer(container) {

    companion object {
        private const val SLOT  = BackpackContainer.SLOT_SIZE
        private const val TOP   = BackpackContainer.TOP_PAD
        private const val LPAD  = BackpackContainer.LEFT_PAD
        private const val TPAD  = 10
        private const val BPAD  = 7
        private const val UCOL  = BackpackContainer.UPGRADE_COL_WIDTH  // 36
        private const val UGAP  = BackpackContainer.UPGRADE_GAP
        private const val USX   = BackpackContainer.UPGRADE_SLOT_X     // 9
        private const val UTOP  = BackpackContainer.UPGRADE_TOP_PAD
        private const val HGAP  = BackpackContainer.HOTBAR_GAP
        private const val IGAP  = BackpackContainer.PLAYER_INV_GAP

        // Toggle switch: 6×10 vertical, to the left of the upgrade slot
        private const val SW_X  = 2
        private const val SW_W  = 6
        private const val SW_H  = 10

        // Gear button: 7×7, to the right of the upgrade slot
        private const val GEAR_X = USX + SLOT + 1   // 28
        private const val GEAR_S = 7

        // Filter panel
        private const val FP_PAD  = 6
        private const val TITLE_H = 10
        private const val BTN_H   = 10
        private const val BTN_GAP = 3

        private const val COLOR_PANEL   = 0xFFC6C6C6.toInt()
        private const val COLOR_SHADOW  = 0xFF373737.toInt()
        private const val COLOR_HILIGHT = 0xFFFFFFFF.toInt()
        private const val COLOR_TEXT    = 0x404040
        private const val COLOR_BTN     = 0xFFAAAAAA.toInt()
        private const val COLOR_BTN_HI  = 0xFFCCCCCC.toInt()
        private val SLOTS_TEX = ResourceLocation(Tags.MOD_ID, "textures/gui/slots_background.png")
    }

    /** Upgrade slot index whose settings panel is open, or -1. */
    private var openPanelSlot: Int = -1

    init {
        xSize = UCOL + UGAP + LPAD + container.rowSize * SLOT + LPAD
        ySize = TOP + container.colSize * SLOT + IGAP + 3 * SLOT + HGAP + SLOT + BPAD
    }

    // --- Helpers ---

    private fun isAdvancedFilter(upgradeSlotIdx: Int): Boolean =
        container.filterWrapperCache[upgradeSlotIdx]?.third == true

    private fun colsForSlot(upgradeSlotIdx: Int): Int = if (isAdvancedFilter(upgradeSlotIdx)) 4 else 3

    private fun panelDimensions(upgradeSlotIdx: Int): Pair<Int, Int> {
        val entry = container.filterWrapperCache[upgradeSlotIdx] ?: return 0 to 0
        val filterable = entry.first
        val isAdv = entry.third
        val cols = colsForSlot(upgradeSlotIdx)
        val rows = (filterable.filterItems.slots + cols - 1) / cols
        val hasFilterWay = filterable is IFilterUpgrade
        val btnCount = 1 +                          // filterType
                (if (hasFilterWay) 1 else 0) +      // filterWay
                (if (isAdv) 3 else 0)               // matchType + ignoreDurability + ignoreNBT
        // Match the full main-panel content width so buttons have plenty of room
        val w = LPAD + container.rowSize * SLOT + LPAD
        // BTN_GAP separates the slot grid from the first button
        val h = FP_PAD + TITLE_H + 2 + rows * SLOT + BTN_GAP + btnCount * (BTN_H + BTN_GAP) + FP_PAD
        return w to h
    }

    // Filter panel overlays the backpack grid area (avoids NEI on the right side)
    private fun filterPanelX() = guiLeft + container.backpackOffsetX
    private fun filterPanelY() = guiTop

    /** X origin of the filter slot grid, centered within the panel. */
    private fun filterSlotOriginX(upgradeSlotIdx: Int): Int {
        val cols = colsForSlot(upgradeSlotIdx)
        val (panelW, _) = panelDimensions(upgradeSlotIdx)
        val innerW = panelW - 2 * FP_PAD
        return filterPanelX() + FP_PAD + (innerW - cols * SLOT) / 2
    }

    // --- Panel open/close ---

    private fun openPanel(upgradeSlotIdx: Int) {
        if (openPanelSlot == upgradeSlotIdx) { closePanel(); return }
        if (openPanelSlot != -1) {
            parkFilterSlots(openPanelSlot)
        } else {
            parkBackpackSlots()
        }
        openPanelSlot = upgradeSlotIdx
        updateFilterSlotPositions(upgradeSlotIdx)
    }

    private fun closePanel() {
        if (openPanelSlot == -1) return
        parkFilterSlots(openPanelSlot)
        restoreBackpackSlots()
        openPanelSlot = -1
    }

    private fun parkBackpackSlots() {
        for (i in container.backpackSlotStart until container.backpackSlotEnd) {
            val slot = container.inventorySlots[i] as? Slot ?: continue
            slot.xDisplayPosition = -1000
            slot.yDisplayPosition = -1000
        }
    }

    private fun restoreBackpackSlots() {
        for (i in 0 until container.wrapper.backpackInventorySize()) {
            val col = i % container.rowSize
            val row = i / container.rowSize
            val slot = container.inventorySlots[container.backpackSlotStart + i] as? Slot ?: continue
            slot.xDisplayPosition = container.backpackOffsetX + LPAD + col * SLOT
            slot.yDisplayPosition = TOP + row * SLOT
        }
    }

    private fun updateFilterSlotPositions(upgradeSlotIdx: Int) {
        val range = container.filterSlotRanges.firstOrNull { it.upgradeSlotIndex == upgradeSlotIdx } ?: return
        val filterable = container.filterWrapperCache[upgradeSlotIdx]?.first ?: return
        val cols = colsForSlot(upgradeSlotIdx)
        val slotsOriginX = filterSlotOriginX(upgradeSlotIdx)
        val slotsOriginY = filterPanelY() + FP_PAD + TITLE_H + 2
        for (j in 0 until filterable.filterItems.slots) {
            val idx = range.start + j
            if (idx >= container.inventorySlots.size) continue
            val slot = container.inventorySlots[idx] as? Slot ?: continue
            slot.xDisplayPosition = slotsOriginX + (j % cols) * SLOT - guiLeft
            slot.yDisplayPosition = slotsOriginY + (j / cols) * SLOT - guiTop
        }
    }

    private fun parkFilterSlots(upgradeSlotIdx: Int) {
        val range = container.filterSlotRanges.firstOrNull { it.upgradeSlotIndex == upgradeSlotIdx } ?: return
        val filterable = container.filterWrapperCache[upgradeSlotIdx]?.first ?: return
        for (j in 0 until filterable.filterItems.slots) {
            val idx = range.start + j
            if (idx >= container.inventorySlots.size) continue
            val slot = container.inventorySlots[idx] as? Slot ?: continue
            slot.xDisplayPosition = -1000
            slot.yDisplayPosition = -1000
        }
    }

    // --- Rendering ---

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        val panelX = guiLeft + UCOL + UGAP
        val panelW = LPAD + container.rowSize * SLOT + LPAD
        val N = container.colSize
        val mainH = TOP + N * SLOT

        drawPanel(panelX, guiTop, panelW, ySize)
        fontRendererObj.drawString("Backpack",  panelX + TPAD, guiTop + (TOP - 8) / 2 + 1, COLOR_TEXT)
        fontRendererObj.drawString("Inventory", panelX + TPAD, guiTop + mainH + 4,          COLOR_TEXT)

        // Only draw the backpack slot texture when the filter panel is not open
        if (openPanelSlot == -1) {
            GL11.glColor4f(1f, 1f, 1f, 1f)
            mc.textureManager.bindTexture(SLOTS_TEX)
            drawTexturedModalRect(panelX + LPAD - 1, guiTop + TOP - 1, 0, 0, container.rowSize * SLOT, N * SLOT)
        }

        val pOffX   = container.playerXOffset
        val playerY = guiTop + mainH + IGAP
        val hotbarY = playerY + 3 * SLOT + HGAP
        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SLOTS_TEX)
        drawTexturedModalRect(panelX + LPAD - 1 + pOffX, playerY - 1, 0, 0, 9 * SLOT, 3 * SLOT)
        drawTexturedModalRect(panelX + LPAD - 1 + pOffX, hotbarY - 1, 0, 0, 9 * SLOT, SLOT)

        // Auto-close panel if the upgrade was removed while the GUI was open
        if (openPanelSlot != -1 && !container.filterWrapperCache.containsKey(openPanelSlot)) closePanel()

        drawUpgradeColumn()
        if (openPanelSlot != -1) drawFilterPanel(mouseX, mouseY)
    }

    private fun drawUpgradeColumn() {
        val upgradeCount = container.wrapper.upgradeSlotsSize()
        if (upgradeCount == 0) return

        drawPanel(guiLeft, guiTop, UCOL, UTOP + upgradeCount * SLOT + 2)

        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SLOTS_TEX)
        drawTexturedModalRect(guiLeft + USX - 1, guiTop + UTOP - 1, 0, 0, SLOT, upgradeCount * SLOT)

        for (i in 0 until upgradeCount) {
            val stack = container.wrapper.upgradeItemStackHandler.inventory.getOrNull(i) ?: continue
            val upgradeItem = stack.item as? UpgradeItem ?: continue
            val wrapper = upgradeItem.getWrapper(stack) ?: continue
            if (wrapper is IToggleable) {
                drawToggle(guiLeft + SW_X, guiTop + UTOP + i * SLOT + (SLOT - SW_H) / 2, wrapper.enabled)
            }
            if (container.filterWrapperCache.containsKey(i)) {
                val gx = guiLeft + GEAR_X
                val gy = guiTop + UTOP + i * SLOT + (SLOT - GEAR_S) / 2
                drawGear(gx, gy, openPanelSlot == i)
            }
        }
    }

    private fun drawFilterPanel(mouseX: Int, mouseY: Int) {
        val idx = openPanelSlot
        val entry = container.filterWrapperCache[idx] ?: return
        val filterable = entry.first
        val isAdv = entry.third
        val filterUpgrade = filterable as? IFilterUpgrade
        val advanced = if (isAdv) filterable as? IAdvancedFilterable else null

        val cols = colsForSlot(idx)
        val rows = (filterable.filterItems.slots + cols - 1) / cols

        val px = filterPanelX()
        val py = filterPanelY()
        val (w, h) = panelDimensions(idx)
        drawPanel(px, py, w, h)

        val innerX = px + FP_PAD
        val btnW = w - 2 * FP_PAD          // full-width buttons
        var curY = py + FP_PAD

        val title = if (isAdv) "Advanced Filter" else "Basic Filter"
        fontRendererObj.drawString(title, innerX, curY + 1, COLOR_TEXT)
        curY += TITLE_H + 2

        // Center the filter slot grid within the panel
        val slotGridX = filterSlotOriginX(idx)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SLOTS_TEX)
        drawTexturedModalRect(slotGridX - 1, curY - 1, 0, 0, cols * SLOT, rows * SLOT)
        curY += rows * SLOT + BTN_GAP

        // filterType button
        val typeLabel = if (filterable.filterType == IBasicFilterable.FilterType.WHITELIST) "Whitelist" else "Blacklist"
        drawButton(innerX, curY, btnW, BTN_H, typeLabel,
            mouseX in innerX until innerX + btnW && mouseY in curY until curY + BTN_H)
        curY += BTN_H + BTN_GAP

        // filterWay button
        if (filterUpgrade != null) {
            val wayLabel = when (filterUpgrade.filterWay) {
                IFilterUpgrade.FilterWayType.IN_OUT -> "In + Out"
                IFilterUpgrade.FilterWayType.IN     -> "In Only"
                IFilterUpgrade.FilterWayType.OUT    -> "Out Only"
            }
            drawButton(innerX, curY, btnW, BTN_H, wayLabel,
                mouseX in innerX until innerX + btnW && mouseY in curY until curY + BTN_H)
            curY += BTN_H + BTN_GAP
        }

        // Advanced-only buttons
        if (advanced != null) {
            val matchLabel = "Match: " + when (advanced.matchType) {
                IAdvancedFilterable.MatchType.ITEM     -> "Item"
                IAdvancedFilterable.MatchType.MOD      -> "Mod"
                IAdvancedFilterable.MatchType.ORE_DICT -> "OreDict"
            }
            drawButton(innerX, curY, btnW, BTN_H, matchLabel,
                mouseX in innerX until innerX + btnW && mouseY in curY until curY + BTN_H)
            curY += BTN_H + BTN_GAP

            val durLabel = "Durability: " + if (advanced.ignoreDurability) "Ignore" else "Exact"
            drawButton(innerX, curY, btnW, BTN_H, durLabel,
                mouseX in innerX until innerX + btnW && mouseY in curY until curY + BTN_H)
            curY += BTN_H + BTN_GAP

            val nbtLabel = "NBT: " + if (advanced.ignoreNBT) "Ignore" else "Exact"
            drawButton(innerX, curY, btnW, BTN_H, nbtLabel,
                mouseX in innerX until innerX + btnW && mouseY in curY until curY + BTN_H)
        }
    }

    private fun drawButton(x: Int, y: Int, w: Int, h: Int, label: String, hovered: Boolean) {
        val bg = if (hovered) COLOR_BTN_HI else COLOR_BTN
        drawRect(x, y, x + w, y + h, bg)
        drawRect(x,         y,         x + w, y + 1,     COLOR_SHADOW)
        drawRect(x,         y,         x + 1, y + h,     COLOR_SHADOW)
        drawRect(x + w - 1, y,         x + w, y + h,     COLOR_HILIGHT)
        drawRect(x,         y + h - 1, x + w, y + h,     COLOR_HILIGHT)
        fontRendererObj.drawString(label,
            x + (w - fontRendererObj.getStringWidth(label)) / 2,
            y + (h - 8) / 2, COLOR_TEXT)
    }

    /** Vertical toggle switch: 6×10 track, 6×5 thumb. Green+up = enabled, gray+down = disabled. */
    private fun drawToggle(x: Int, y: Int, enabled: Boolean) {
        val trackMain  = if (enabled) 0xFF2A6B2A.toInt() else 0xFF404040.toInt()
        val trackInset = if (enabled) 0xFF1A4B1A.toInt() else 0xFF2A2A2A.toInt()
        val thumbY     = if (enabled) y else y + SW_H - 5

        drawRect(x, y, x + SW_W, y + SW_H, trackMain)
        drawRect(x,            y,          x + SW_W, y + 1,       trackInset)
        drawRect(x,            y,          x + 1,    y + SW_H,    trackInset)
        drawRect(x + SW_W - 1, y,          x + SW_W, y + SW_H,    0xFF555555.toInt())
        drawRect(x,            y + SW_H-1, x + SW_W, y + SW_H,    0xFF555555.toInt())

        drawRect(x,            thumbY,     x + SW_W, thumbY + 5,  0xFFCCCCCC.toInt())
        drawRect(x,            thumbY,     x + SW_W, thumbY + 1,  0xFFEEEEEE.toInt())
        drawRect(x,            thumbY,     x + 1,    thumbY + 5,  0xFFEEEEEE.toInt())
        drawRect(x + SW_W - 1, thumbY,     x + SW_W, thumbY + 5,  0xFF888888.toInt())
        drawRect(x,            thumbY + 4, x + SW_W, thumbY + 5,  0xFF888888.toInt())
    }

    /** 7×7 beveled gear button. Active = sunken (panel is open). */
    private fun drawGear(x: Int, y: Int, active: Boolean) {
        val bg = if (active) 0xFF888888.toInt() else COLOR_BTN
        val hi = if (active) COLOR_SHADOW  else COLOR_HILIGHT
        val sh = if (active) COLOR_HILIGHT else COLOR_SHADOW
        drawRect(x, y, x + GEAR_S, y + GEAR_S, bg)
        drawRect(x,              y,              x + GEAR_S, y + 1,          sh)
        drawRect(x,              y,              x + 1,      y + GEAR_S,     sh)
        drawRect(x + GEAR_S - 1, y,              x + GEAR_S, y + GEAR_S,     hi)
        drawRect(x,              y + GEAR_S - 1, x + GEAR_S, y + GEAR_S,     hi)
        val cx = x + GEAR_S / 2
        val cy = y + GEAR_S / 2
        drawRect(cx - 1, cy - 1, cx + 1, cy + 1, COLOR_TEXT)
        drawRect(cx - 1, y + 1,  cx + 1, y + 2,  COLOR_TEXT)
        drawRect(cx - 1, y + GEAR_S - 2, cx + 1, y + GEAR_S - 1, COLOR_TEXT)
        drawRect(x + 1,  cy - 1, x + 2,  cy + 1, COLOR_TEXT)
        drawRect(x + GEAR_S - 2, cy - 1, x + GEAR_S - 1, cy + 1, COLOR_TEXT)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            // Filter panel button clicks — panel stays open unless gear is clicked again
            if (openPanelSlot != -1) {
                val upIdx = openPanelSlot
                val cacheEntry = container.filterWrapperCache[upIdx]
                val filterable = cacheEntry?.first
                if (filterable != null) {
                    val filterUpgrade = filterable as? IFilterUpgrade
                    val advanced = if (cacheEntry!!.third) filterable as? IAdvancedFilterable else null
                    val cols = colsForSlot(upIdx)
                    val rows = (filterable.filterItems.slots + cols - 1) / cols

                    val px = filterPanelX()
                    val py = filterPanelY()
                    val (panelW, _) = panelDimensions(upIdx)
                    val innerX = px + FP_PAD
                    val btnW = panelW - 2 * FP_PAD

                    var curY = py + FP_PAD + TITLE_H + 2 + rows * SLOT + BTN_GAP

                    // filterType
                    if (mouseX in innerX until innerX + btnW && mouseY in curY until curY + BTN_H) {
                        val types = IBasicFilterable.FilterType.entries
                        filterable.filterType = types[(filterable.filterType.ordinal + 1) % types.size]
                        NetworkHandler.INSTANCE.sendToServer(C2SUpgradeSettingPacket(upIdx, 0))
                        return
                    }
                    curY += BTN_H + BTN_GAP

                    // filterWay
                    if (filterUpgrade != null) {
                        if (mouseX in innerX until innerX + btnW && mouseY in curY until curY + BTN_H) {
                            val ways = IFilterUpgrade.FilterWayType.entries
                            filterUpgrade.filterWay = ways[(filterUpgrade.filterWay.ordinal + 1) % ways.size]
                            NetworkHandler.INSTANCE.sendToServer(C2SUpgradeSettingPacket(upIdx, 1))
                            return
                        }
                        curY += BTN_H + BTN_GAP
                    }

                    // Advanced buttons
                    if (advanced != null) {
                        // matchType
                        if (mouseX in innerX until innerX + btnW && mouseY in curY until curY + BTN_H) {
                            val types = IAdvancedFilterable.MatchType.entries
                            advanced.matchType = types[(advanced.matchType.ordinal + 1) % types.size]
                            NetworkHandler.INSTANCE.sendToServer(C2SUpgradeSettingPacket(upIdx, 2))
                            return
                        }
                        curY += BTN_H + BTN_GAP
                        // ignoreDurability
                        if (mouseX in innerX until innerX + btnW && mouseY in curY until curY + BTN_H) {
                            advanced.ignoreDurability = !advanced.ignoreDurability
                            NetworkHandler.INSTANCE.sendToServer(C2SUpgradeSettingPacket(upIdx, 3))
                            return
                        }
                        curY += BTN_H + BTN_GAP
                        // ignoreNBT
                        if (mouseX in innerX until innerX + btnW && mouseY in curY until curY + BTN_H) {
                            advanced.ignoreNBT = !advanced.ignoreNBT
                            NetworkHandler.INSTANCE.sendToServer(C2SUpgradeSettingPacket(upIdx, 4))
                            return
                        }
                    }
                }
            }

            val upgradeCount = container.wrapper.upgradeSlotsSize()

            // Toggle switch clicks
            for (i in 0 until upgradeCount) {
                val stack = container.wrapper.upgradeItemStackHandler.inventory.getOrNull(i) ?: continue
                val upgradeItem = stack.item as? UpgradeItem ?: continue
                val rawWrapper = upgradeItem.getWrapper(stack) ?: continue
                val toggleable = rawWrapper as? IToggleable ?: continue
                val stripY = guiTop + UTOP + i * SLOT
                if (mouseX in (guiLeft + 1) until (guiLeft + USX - 1) &&
                    mouseY in stripY until stripY + SLOT) {
                    toggleable.toggle()
                    upgradeItem.saveWrapper(stack, rawWrapper)
                    NetworkHandler.INSTANCE.sendToServer(C2SToggleUpgradePacket(i))
                    return
                }
            }

            // Gear button clicks → open/close filter panel
            for (i in 0 until upgradeCount) {
                if (!container.filterWrapperCache.containsKey(i)) continue
                val gx = guiLeft + GEAR_X
                val gy = guiTop + UTOP + i * SLOT + (SLOT - GEAR_S) / 2
                if (mouseX in gx until gx + GEAR_S && mouseY in gy until gy + GEAR_S) {
                    openPanel(i)
                    return
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun onGuiClosed() {
        closePanel()
        super.onGuiClosed()
    }

    private fun drawPanel(x: Int, y: Int, w: Int, h: Int) {
        drawRect(x,         y,         x + w,     y + h,     COLOR_PANEL)
        drawRect(x,         y,         x + w,     y + 1,     COLOR_SHADOW)
        drawRect(x,         y,         x + 1,     y + h,     COLOR_SHADOW)
        drawRect(x + w - 1, y,         x + w,     y + h,     COLOR_HILIGHT)
        drawRect(x,         y + h - 1, x + w,     y + h,     COLOR_HILIGHT)
    }
}
