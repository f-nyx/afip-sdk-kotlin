package be.rlab.afip.apps

interface PortalAppBuilder {
    fun new(
        appsService: AppsService,
        config: AppConfig
    ): PortalApp
}
