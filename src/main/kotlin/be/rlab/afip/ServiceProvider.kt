package be.rlab.afip

import be.rlab.afip.auth.AuthenticationService
import be.rlab.afip.config.ServiceProviderConfig
import be.rlab.afip.ticket.TicketService

class ServiceProvider(
    val authenticationService: AuthenticationService,
    val ticketService: TicketService
) {
    companion object {
        fun new(callback: ServiceProviderConfig.() -> Unit): ServiceProvider {
            return ServiceProviderConfig().apply(callback).build()
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun<reified T : Any> getService(): T {
        return when (T::class) {
            AuthenticationService::class -> authenticationService as T
            TicketService::class -> ticketService as T
            else -> throw RuntimeException("service class not supported: ${T::class}")
        }
    }
}
