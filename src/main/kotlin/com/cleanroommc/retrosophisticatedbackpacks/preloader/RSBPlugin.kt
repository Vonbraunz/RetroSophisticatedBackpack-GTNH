package com.cleanroommc.retrosophisticatedbackpacks.preloader

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import cpw.mods.fml.relauncher.IFMLLoadingPlugin
import zone.rong.mixinbooter.IEarlyMixinLoader

@IFMLLoadingPlugin.Name("Retro Sophisticated Backpacks")
@IFMLLoadingPlugin.MCVersion("1.7.10")
class RSBPlugin : IFMLLoadingPlugin, IEarlyMixinLoader {
    override fun getASMTransformerClass(): Array<out String> =
        arrayOf()

    override fun getModContainerClass(): String? =
        null

    override fun getSetupClass(): String? =
        null

    override fun injectData(data: Map<String?, Any?>?) {
    }

    override fun getAccessTransformerClass(): String? =
        null

    override fun getMixinConfigs(): List<String> =
        listOf("mixin.${Tags.MOD_ID}.json")
}