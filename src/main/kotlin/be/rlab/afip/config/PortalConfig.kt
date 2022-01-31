package be.rlab.afip.config

import be.rlab.afip.portal.PortalService

class PortalConfig(
    private val objectStoreConfig: StoreConfig
) {
    private var portalService: PortalService? = null

    var cuit: Long? = null
    var password: String? = null

    fun build(): PortalService {
        requireNotNull(cuit) { "The portal cuit cannot be null." }
        requireNotNull(password) { "The portal password cannot be null." }

        if (portalService == null) {
            portalService = PortalService(objectStoreConfig.build(), cuit!!, password!!)
        }

        return portalService!!
    }
}
