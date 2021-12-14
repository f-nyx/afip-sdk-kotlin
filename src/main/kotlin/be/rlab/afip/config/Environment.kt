package be.rlab.afip.config

/** Supported environments for AFIP web services.
 */
enum class Environment {
    TEST,
    PRODUCTION;

    /** Resolves a value based on this environment.
     * @param test Value if the environment is [TEST].
     * @param production Value if the environment is [PRODUCTION].
     * @return the value for this environment.
     */
    fun<T> resolveValue(
        test: T,
        production: T
    ): T = when(this) {
        TEST -> test
        PRODUCTION -> production
    }
}
