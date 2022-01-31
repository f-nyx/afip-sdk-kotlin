package be.rlab.afip

import be.rlab.afip.config.ServiceProviderConfig

class ServiceProvider(val services: List<Any>) {
    companion object {
        fun new(callback: ServiceProviderConfig.() -> Unit): ServiceProvider {
            return ServiceProviderConfig().apply(callback).build()
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun<reified T : Any> getService(): T {
        return services.find { service -> service is T }?.let { service -> service as T }
            ?: throw RuntimeException("service class not supported: ${T::class}")
    }
}
