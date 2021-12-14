package be.rlab.afip.support

object SoapTestUtils {
    fun loadResponse(
        serviceName: String,
        operationName: String,
        responseName: String
    ): String {
        val path = "responses/$serviceName/$operationName.$responseName.xml"
        return Thread.currentThread().contextClassLoader.getResourceAsStream(path)
            ?.bufferedReader()?.readText()
            ?: throw RuntimeException("Cannot load response: $path")
    }
}
