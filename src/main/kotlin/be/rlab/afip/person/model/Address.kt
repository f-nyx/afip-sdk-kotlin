package be.rlab.afip.person.model

data class Address(
    val type: AddressType,
    val addressLine: String?,
    val locality: String?,
    val zipCode: String,
    val province: String?,
    val additionalData: String?,
    val additionalDataType: String?
)
