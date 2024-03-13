package be.rlab.afip.config

import be.rlab.afip.ServiceProvider
import be.rlab.afip.apps.AppsService
import be.rlab.afip.auth.AuthServiceConfig
import be.rlab.afip.auth.AuthenticationService
import be.rlab.afip.auth.CredentialsCache
import be.rlab.afip.auth.SecretsProvider
import be.rlab.afip.person.PersonService
import be.rlab.afip.person.PersonServiceConfig
import be.rlab.afip.support.SoapClient
import be.rlab.afip.support.store.InMemoryObjectStore
import be.rlab.afip.ticket.TicketService
import be.rlab.afip.ticket.TicketServiceConfig

class ServiceProviderConfig {
    private var environment: Environment = Environment.TEST
    private var lazyStoreConfig: () -> StoreConfig = { StoreConfig().apply { memory() } }
    private lateinit var lazySecretsProviderConfig: () -> SecretsProviderConfig
    private var lazyPortalConfig: (() -> PortalConfig)? = null

    fun environment(callback: () -> Environment): ServiceProviderConfig = apply {
        environment = callback()
    }

    fun store(callback: StoreConfig.() -> Unit): ServiceProviderConfig = apply {
        lazyStoreConfig = { StoreConfig().apply(callback) }
    }

    fun secretsProvider(callback: SecretsProviderConfig.() -> Unit): ServiceProviderConfig = apply {
        lazySecretsProviderConfig = {
            SecretsProviderConfig(lazyStoreConfig()).apply(callback)
        }
    }

    fun portal(callback: PortalConfig.() -> Unit): ServiceProviderConfig = apply {
        lazyPortalConfig = {
            PortalConfig(lazyStoreConfig()).apply(callback)
        }
    }

    fun build(): ServiceProvider {
        val store = lazyStoreConfig().build()
        val secretsProviderConfig = lazySecretsProviderConfig()
        val secretsProvider: SecretsProvider = secretsProviderConfig.build()
        val authConfig = AuthServiceConfig.new(environment, secretsProviderConfig.cuit)
        val authenticationService = AuthenticationService(
            credentialsCache = CredentialsCache(store),
            secretsProvider = secretsProvider,
            config = authConfig,
            client = SoapClient.notAuthenticated().apply {
                registerService(authConfig.serviceName, authConfig.endpoint)
            }
        )
        val portalServices: List<Any> = lazyPortalConfig?.let { builder ->
            val portalConfig: PortalConfig = builder()
            listOf(
                portalConfig.build(),
                AppsService(portalConfig.build())
            )
        } ?: emptyList()

        val services: List<Any> = portalServices + listOf(
            authenticationService,
            TicketService(
                client = SoapClient.authenticated(authenticationService).apply {
                    /** Configuration for the wsfe service. */
                    val localConfig = TicketServiceConfig.local(environment)
                    /** Configuration for the wsfex service. */
                    val exportConfig = TicketServiceConfig.export(environment)
                    registerService(localConfig.serviceName, localConfig.endpoint, localConfig.soapActionBase)
                    registerService(exportConfig.serviceName, exportConfig.endpoint, exportConfig.soapActionBase)
                }
            ),
            PersonService(
                client = SoapClient.authenticated(authenticationService).apply {
                    val config = PersonServiceConfig.create(environment)
                    registerService(config.serviceName, config.endpoint, config.soapActionBase)
                }
            )
        )

        return ServiceProvider(services)
    }
}
