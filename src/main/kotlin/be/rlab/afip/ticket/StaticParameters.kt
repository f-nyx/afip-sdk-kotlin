package be.rlab.afip.ticket

/** Static parameters that can be configured for all the operations.
 * Some of these parameters can be overridden by request-specific parameters.
 */
object StaticParameters {
    /** Max value to create tickets of type C without identifying the target person.
     * If the ticket total value exceeds this value, it requires a valid Document.
     */
    var maxValueWithoutIdentityC: Double = 10_000.0
}
