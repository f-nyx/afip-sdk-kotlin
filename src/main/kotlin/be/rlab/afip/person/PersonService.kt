package be.rlab.afip.person

import be.rlab.afip.person.model.*
import be.rlab.afip.person.request.GetPersonRequest
import be.rlab.afip.support.*
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

/** This service gets people personal information.
 * @link https://www.afip.gob.ar/ws/ws_sr_padron_a4/manual_ws_sr_padron_a4_v1.3.pdf
 */
class PersonService(private val client: SoapClient) {

    private val formatter: DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZZ")

    fun getPersonA4(id: String): PersonA4 {
        return client.call(GetPersonRequest(id), failOnError = false) {
            val addresses = select("domicilio").map { element ->
                Address(
                    type = AddressType.of(element.string("tipoDomicilio")),
                    addressLine = element.optString("direccion"),
                    locality = element.optString("localidad"),
                    zipCode = element.string("codPostal"),
                    province = element.optString("descripcionProvincia"),
                    additionalData = element.optString("datoAdicional"),
                    additionalDataType = element.optString("tipoDatoAdicional")
                )
            }
            val taxes = select("impuesto").map { element ->
                Tax(
                    id = element.number("IdImpuesto").toLong(),
                    description = element.optString("descripcionImpuesto"),
                    status = TaxPersonStatus.of(element.string("estado")),
                    period = element.string("periodo"),
                    registrationDate = element.dateTime("ffInscripcion", formatter),
                    registrationDay = element.number("diaPeriodo").toInt()
                )
            }
            val categories = select("categoria").map { element ->
                Category(
                    id = element.number("idCategoria").toLong(),
                    description = element.optString("descripcionCategoria"),
                    taxId = element.number("IdImpuesto").toLong(),
                    status = TaxPersonStatus.of(element.string("estado")),
                    period = element.string("periodo")
                )
            }
            val phones = select("telefono").map { element ->
                Phone(
                    number = element.number("numero").toLong(),
                    lineType = PhoneLineType.of(element.string("tipoLinea")),
                    phoneType = PhoneType.of(element.string("tipoTelefono"))
                )
            }
            val emails = select("email").map { element ->
                Email(
                    address = element.string("Direccion"),
                    type = element.string("tipoEmail"),
                    status = EmailStatus.of(element.string("estado"))
                )
            }
            val activities = select("actividad").map { element ->
                Activity(
                    id = element.number("idActividad").toLong(),
                    description = element.optString("descripcionActividad"),
                    period = element.string("periodo"),
                    nameCatalog = element.number("nomenclador").toLong()
                )
            }
            val legalFrameworks = select("regimen").map { element ->
                LegalFramework(
                    id = element.number("idRegimen").toLong(),
                    type = element.optString("tipoRegimen"),
                    status = TaxPersonStatus.of(element.string("estado")),
                    description = element.optString("descripcionRegimen"),
                    period = element.string("periodo"),
                    taxId = element.number("IdImpuesto").toLong()
                )
            }
            val relationships = select("relacion").map { element ->
                Relationship(
                    type = element.optString("tipoRelacion"),
                    subtype = element.optString("subtipoRelacion"),
                    cuit = element.number("idPersona").toLong(),
                    associatedCuit = element.number("idPersonaAsociada").toLong(),
                    startDate = element.dateTime("ffRelacion", formatter),
                    expirationDate = element.optDateTime("ffVencimiento", formatter),
                )
            }
            val dependency = select("dependency").firstOrNull()?.let { element ->
                Dependency(
                    id = element.number("idDependencia").toLong(),
                    description = element.optString("descripcionDependencia")
                )
            }

            PersonA4(
                cuit = optNumber("idPersona")?.toLong(),
                personType = optString("tipoPersona")?.let(PersonType::of),
                taxIdType = optString("tipoClave")?.let(TaxIdType::valueOf),
                taxIdStatus = optString("estadoClave")?.let(TaxIdStatus::of),
                firstName = optString("nombre"),
                lastName = optString("apellido"),
                companyName = optString("razonSocial"),
                documentType = optString("tipoDocumento"),
                documentNumber = optString("numeroDocumento"),
                closeMonth = optNumber("mesCierre")?.toInt(),
                registrationDate = optDateTime("fechaInscripcion", formatter),
                socialContractDate = optDateTime("fechaContratoSocial", formatter),
                birthDate = optDateTime("fechaNacimiento", formatter),
                retirementDate = optDateTime("fechaJubilado", formatter),
                deathDate = optDateTime("fechaFallecimiento", formatter),
                gender = optString("sexo")?.let(Gender::of),
                legalNature = optString("formaJuridica"),
                residenceType = optString("tipoResidencia"),
                addresses = addresses,
                taxes = taxes,
                categories = categories,
                phones = phones,
                emails = emails,
                activities = activities,
                legalFrameworks = legalFrameworks,
                relationships = relationships,
                dependency = dependency,
                registryNumber = optString("numeroInscripcion"),
                registryOrganization = optString("organismoInscripcion"),
                registryTown = optString("localidadInscripcion"),
                registryProvince = optString("provinciaInscripcion"),
                migrationExpirationDate = optDateTime("fechaVencimientoMigracion", formatter),
                numberOfPartners = optNumber("cantidadSociosEmpresaMono")?.toInt(),
                sourceRegistryType = optString("tipoOrganismoOriginante"),
                sourceRegistry = optString("organismoOriginante"),
                retirementLaw = optNumber("leyJubilacion")?.toInt()
            )
        }
    }
}
