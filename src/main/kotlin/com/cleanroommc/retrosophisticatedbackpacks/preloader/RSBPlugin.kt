package com.cleanroommc.retrosophisticatedbackpacks.preloader

import cpw.mods.fml.relauncher.IFMLLoadingPlugin

@IFMLLoadingPlugin.Name("Retro Sophisticated Backpacks")
@IFMLLoadingPlugin.MCVersion("1.7.10")
class RSBPlugin : IFMLLoadingPlugin {
    override fun getASMTransformerClass(): Array<out String> = arrayOf()
    override fun getModContainerClass(): String? = null
    override fun getSetupClass(): String? = null
    override fun injectData(data: Map<String?, Any?>?) {}
    override fun getAccessTransformerClass(): String? = null
}