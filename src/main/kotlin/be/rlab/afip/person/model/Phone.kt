package be.rlab.afip.person.model

data class Phone(
    val number: Long,
    val lineType: PhoneLineType,
    val phoneType: PhoneType
)
