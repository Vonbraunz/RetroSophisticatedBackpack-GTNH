package com.cleanroommc.retrosophisticatedbackpacks

import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackGuiHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.EntityEventHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.NetworkHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.proxy.RSBProxy
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.SidedProxy
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerStoppedEvent
import cpw.mods.fml.common.network.NetworkRegistry
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.LogManager

@Mod(
    modid = Tags.MOD_ID,
    name = Tags.MOD_NAME,
    version = Tags.VERSION,
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
    dependencies = "required-after:forgelin_continuous"
)
object RetroSophisticatedBackpacks {
    val LOGGER = LogManager.getLogger(Tags.MOD_NAME)

    @JvmField
    @SidedProxy(
        serverSide = "com.cleanroommc.retrosophisticatedbackpacks.proxy.RSBProxy\$ServerProxy",
        clientSide = "com.cleanroommc.retrosophisticatedbackpacks.proxy.RSBProxy\$ClientProxy"
    )
    var proxy: RSBProxy? = null

    @JvmField
    @Mod.Instance(Tags.MOD_ID)
    var instance: Any? = null

    val CREATIVE_TAB: CreativeTabs = object : CreativeTabs("creative_tab".asTranslationKey()) {
        override fun getTabIconItem(): Item =
            Items.backpackLeather ?: Items.ITEMS.firstOrNull() ?: Item.getItemById(1)
    }

    @EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        proxy?.preInit(event)
    }

    @EventHandler
    fun init(event: FMLInitializationEvent) {
        RegistryHandler.registerAll()
        NetworkHandler.register()
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, BackpackGuiHandler)
        MinecraftForge.EVENT_BUS.register(EntityEventHandler)
        proxy?.init(event)
    }

    @EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        proxy?.postInit(event)
    }

    @EventHandler
    fun onShutdown(event: FMLServerStoppedEvent) {
        CapabilityHandler.BACKPACK_INVENTORY_CACHE.clear()
        LOGGER.info("Backpack UUID cache has been cleared")
    }
}
