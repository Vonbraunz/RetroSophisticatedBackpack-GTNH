package com.cleanroommc.retrosophisticatedbackpacks.proxy

import com.cleanroommc.retrosophisticatedbackpacks.block.Blocks
import com.cleanroommc.retrosophisticatedbackpacks.client.RSBBlockRenderTypes
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.GuiBackpack
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.handler.ConfigHandler
import com.cleanroommc.retrosophisticatedbackpacks.integration.nei.NEIIntegration
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import cpw.mods.fml.client.registry.ClientRegistry
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.Item
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.oredict.OreDictionary
import org.lwjgl.input.Keyboard

abstract class RSBProxy {
    open fun preInit(event: FMLPreInitializationEvent) {
        Config.init(event.modConfigurationDirectory)
        MinecraftForge.EVENT_BUS.register(ConfigHandler)
    }

    open fun init(event: FMLInitializationEvent) {
        for (backpackItem in Items.BACKPACK_ITEMS) {
            OreDictionary.registerOre("backpack", backpackItem)
            OreDictionary.registerOre("sophisticatedBackpack", backpackItem)
        }
        for (backpackBlock in Blocks.BACKPACK_BLOCKS) {
            OreDictionary.registerOre("backpack", backpackBlock)
            OreDictionary.registerOre("sophisticatedBackpack", backpackBlock)
        }
    }

    open fun postInit(event: FMLPostInitializationEvent) {}

    open fun registerItemRenderer(item: Item, meta: Int, id: String) {}

    open fun createBackpackGui(container: BackpackContainer): Any? = null

    class ServerProxy : RSBProxy()

    class ClientProxy : RSBProxy() {
        companion object {
            val OPEN_BACKPACK_KEYBIND = KeyBinding(
                "key.open_backpack.desc".asTranslationKey(),
                Keyboard.KEY_B,
                "key.category".asTranslationKey()
            )
        }

        override fun preInit(event: FMLPreInitializationEvent) {
            super.preInit(event)
            RSBBlockRenderTypes.register()
        }

        override fun init(event: FMLInitializationEvent) {
            super.init(event)
            ClientRegistry.registerKeyBinding(OPEN_BACKPACK_KEYBIND)
        }

        override fun postInit(event: FMLPostInitializationEvent) {
            super.postInit(event)
            if (Loader.isModLoaded("NotEnoughItems")) {
                NEIIntegration.register()
            }
        }

        override fun createBackpackGui(container: BackpackContainer): Any? = GuiBackpack(container)
    }
}
