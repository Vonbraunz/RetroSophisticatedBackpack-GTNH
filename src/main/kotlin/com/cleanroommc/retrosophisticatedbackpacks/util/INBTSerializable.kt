package com.cleanroommc.retrosophisticatedbackpacks.util

import net.minecraft.nbt.NBTBase

interface INBTSerializable<T : NBTBase> {
    fun serializeNBT(): T
    fun deserializeNBT(nbt: T)
}
