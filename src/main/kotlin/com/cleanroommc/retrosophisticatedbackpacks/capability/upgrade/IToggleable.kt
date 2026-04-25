package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider

interface IToggleable : ISidelessCapabilityProvider {
    companion object {
        const val ENABLED_TAG = "Enabled"
    }

    var enabled: Boolean

    fun toggle() {
        enabled = !enabled
    }

    object Impl : IToggleable {
        override var enabled: Boolean
            get() = false
            set(_) {}
    }
}
