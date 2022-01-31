package be.rlab.afip.portal

data class PortalConfig(
    val accessToken: String,
    val apiConfig: ApiConfig
) {
    fun resolveEndpoint(
        endpointName: String,
        vararg params: Pair<String, String>
    ): String {
        require(endpointName in apiConfig.endpoints) { "endpoint $endpointName not found" }
        val endpoint = params.fold(apiConfig.endpoints.getValue(endpointName)) { url, (name, value) ->
            url.replace("{$name}", value)
        }
        return "${ApiConfig.API_URL}/${apiConfig.baseUrl.removeSurrounding("/")}/${endpoint.removePrefix("/")}"
    }
}
