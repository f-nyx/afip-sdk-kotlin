package be.rlab.afip.config

import be.rlab.afip.ServiceProvider
import be.rlab.afip.auth.AuthServiceConfig
import be.rlab.afip.auth.AuthenticationService
import be.rlab.afip.auth.CredentialsCache
import be.rlab.afip.auth.SecretsProvider
import be.rlab.afip.ticket.TicketService
import be.rlab.afip.ticket.TicketServiceConfig

class ServiceProviderConfig {
    private var environment: Environment = Environment.TEST
    private lateinit var lazyStoreConfig: () -> StoreConfig
    private lateinit var lazySecretsProviderConfig: () -> SecretsProviderConfig

    fun environment(callback: () -> Environment) {
        environment = callback()
    }

    fun store(callback: StoreConfig.() -> Unit) {
        lazyStoreConfig = { StoreConfig().apply(callback) }
    }

    fun secretsProvider(callback: SecretsProviderConfig.() -> Unit) {
        lazySecretsProviderConfig = {
            SecretsProviderConfig(lazyStoreConfig()).apply(callback)
        }
    }

    fun build(): ServiceProvider {
        val store = lazyStoreConfig().build()
        val secretsProviderConfig = lazySecretsProviderConfig()
        val secretsProvider: SecretsProvider = secretsProviderConfig.build()
        val authenticationService = AuthenticationService(
            config = AuthServiceConfig.new(environment, secretsProviderConfig.cuit),
            credentialsCache = CredentialsCache(store),
            secretsProvider = secretsProvider
        )

        return ServiceProvider(
            authenticationService,
            ticketService = TicketService(
                localConfig = TicketServiceConfig.local(environment),
                exportConfig = TicketServiceConfig.export(environment),
                authenticationService
            )
        )
    }
}
