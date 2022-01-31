package be.rlab.afip.apps

data class AppConfig(
    val name: String,
    val type: String,
    val description: String,
    val url: String,
    val level: Int,
    val organization: String,
    val authVersion: String,
    val publicKey: String?,
    val canRemove: Boolean,
    val canDelegate: Boolean,
    val canRequest: Boolean,
    val isVisible: Boolean,
    val isDefault: Boolean,
    val isOnline: Boolean
)
