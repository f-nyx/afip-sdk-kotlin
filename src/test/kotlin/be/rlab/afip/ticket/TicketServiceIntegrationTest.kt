package be.rlab.afip.ticket

import be.rlab.afip.ServiceProvider
import be.rlab.afip.ticket.model.*
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

@EnabledIfEnvironmentVariable(named = "AFIP_RUN_INTEGRATION", matches = "yes")
class TicketServiceIntegrationTest {
    data class ParameterTypeSource(
        val values: List<*>,
        val exec: TicketService.() -> List<*>
    )

    companion object {
        val STORE_DIR: String by lazy { System.getenv("AFIP_TEST_STORE_DIR") }
        val KEY_STORE_FILE: String by lazy { System.getenv("AFIP_TEST_KEY_STORE_FILE") }
        val CUIT: Long by lazy { System.getenv("AFIP_TEST_CUIT").toLong() }
        val ALIAS: String by lazy { System.getenv("AFIP_TEST_ALIAS") }
        val PASSWORD: String by lazy { System.getenv("AFIP_TEST_PASSWORD") }

        @JvmStatic
        fun parameterTypesSource(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(ParameterTypeSource(TransactionType.all()) { getTransactionTypes() }),
                Arguments.of(ParameterTypeSource(TicketType.all()) { getTicketTypes() }),
                Arguments.of(ParameterTypeSource(TaxType.all()) { getTaxes() }),
                Arguments.of(ParameterTypeSource(IvaType.all()) { getIvaTypes() }),
                Arguments.of(ParameterTypeSource(DocumentType.all()) { getDocumentTypes() }),
                Arguments.of(ParameterTypeSource(CurrencyType.all()) { getCurrencyTypes() }),
                Arguments.of(ParameterTypeSource(UnitType.all()) { getUnitTypes() }),
                Arguments.of(ParameterTypeSource(Language.all()) { getLanguages() }),
                Arguments.of(ParameterTypeSource(IncotermType.all()) { getIncoterms() }),
                Arguments.of(ParameterTypeSource(Location.all()) { getLocations() }),
                Arguments.of(ParameterTypeSource(listOf(
                    PointOfSale.new(0, "CAE", blocked = false),
                    PointOfSale.new(1, "CAE", blocked = true)
                )) { getPointsOfSale() }),
            )
        }
    }

    private val serviceProvider: ServiceProvider by lazy {
        ServiceProvider.new {
            store {
                fileSystem { storeDir = File(STORE_DIR) }
            }
            secretsProvider {
                cuit = CUIT
                alias = ALIAS
                password = PASSWORD

                fileSystem { keyStoreFile = File(KEY_STORE_FILE) }
            }
        }
    }

    @Test
    fun getLastTicketId() {
        // prepare
        val service: TicketService = serviceProvider.getService()
        // act
        val lastTicketId = service.getLastTicketId(3, TicketType.TicketE)
        // assert
        assert(lastTicketId >= 0)
    }

    @ParameterizedTest
    @MethodSource("parameterTypesSource")
    fun getParameters(source: ParameterTypeSource) {
        val service: TicketService = serviceProvider.getService()
        // act
        val values = source.exec(service)
        // assert
        assert(source.values.all { parameterType -> values.contains(parameterType) })
    }

    @Test
    fun getMaxItemsPerTicket() {
        // prepare
        val service: TicketService = serviceProvider.getService()
        // act
        val maxTicketsPerRequest = service.getMaxItemsPerTicket()
        // assert
        assert(maxTicketsPerRequest == 250)
    }

    @Test
    fun getCurrencyValues() {
        // prepare
        val service: TicketService = serviceProvider.getService()
        // act
        val currencyValues = service.getCurrencyValues(DateTime.now().minusDays(1))
        // assert
        assert(currencyValues.isNotEmpty())
    }

    @Test
    fun getLocationEntities() {
        // prepare
        val service: TicketService = serviceProvider.getService()
        // act
        val locationEntities = service.getLocationEntities()
        // assert
        assert(locationEntities.isNotEmpty())
    }

    @Test
    fun newTicketC_noDocumentRequired() {
        // prepare
        val service: TicketService = serviceProvider.getService()
        val now = DateTime.now()

        // act
        val ticket: Ticket<TicketItem> = service.newTicketC(3) {
            addServiceItem(
                totalValue = 2000.0,
                startDate = now.minusDays(15),
                endDate = now.plusDays(15),
                paymentDueDate = now.plusDays(25)
            )
        }

        // assert
        assert(ticket.status == TicketStatus.ACCEPTED)
        assert(ticket.items[0].transactionType == TransactionType.Services)
        assert(ticket.items[0].document == Document.new(DocumentType.OTHER, 0))
        assert(ticket.items[0].netValue == 2000.0)
        assert(ticket.items[0].totalValue == 2000.0)
        assert(ticket.items[0].serviceStartDate == now.minusDays(15))
        assert(ticket.items[0].serviceEndDate == now.plusDays(15))
        assert(ticket.items[0].paymentDueDate == now.plusDays(25))
        assert(ticket.items[0].currencyType == CurrencyType.ARS)
        println(ticket)
    }

    @Test
    fun newTicketE() {
        // prepare
        val service: TicketService = serviceProvider.getService()

        // act
        val ticket: Ticket<ExportTicketItem> = service.newTicketE(3) {
            exportRecipient(
                customerFullName = "John Smith",
                customerFullAddress = "P Sherman Calle Wallaby 235",
                customerLegalId = "12345678",
                customerType = EntityType.INDIVIDUAL,
                targetLocation = Location.UnitedStates,
                language = Language.English,
                paymentMethod = "Wire Transfer"
            )
            addExportServiceItem(
                code = "ABC123",
                description = "Software Development Services",
                totalValue = 1000.0
            )
        }

        // assert
        assert(ticket.status == TicketStatus.ACCEPTED)
        assert(ticket.items[0].totalValue == 1000.0)
        println(ticket)
    }
}
