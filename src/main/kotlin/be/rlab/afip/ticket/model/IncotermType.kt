package be.rlab.afip.ticket.model

/** Enumeration of well-know incoterms.
 *
 * The incoterms are rules set by the ICC (International Chamber of Commerce) that define
 * the scope of commercial operations.
 */
open class IncotermType(
    id: String,
    description: String
) : ParameterType(id, description) {
    object EXW : IncotermType("EXW", "EXW")
    object FCA : IncotermType("FCA", "FCA")
    object FAS : IncotermType("FAS", "FAS")
    object FOB : IncotermType("FOB", "FOB")
    object CFR : IncotermType("CFR", "CFR")
    object CIF : IncotermType("CIF", "CIF")
    object CPT : IncotermType("CPT", "CPT")
    object CIP : IncotermType("CIP", "CIP")
    object DDP : IncotermType("DDP", "DDP")
    object DAP : IncotermType("DAP", "DAP")
    object DPU : IncotermType("DPU", "DPU")

    companion object : ParameterTypeSupport<IncotermType>() {
        override fun all(): List<IncotermType> = listOf(
            EXW, FCA, FAS, FOB, CFR, CIF, CPT, CIP, DDP, DAP, DPU
        )

        override fun new(id: String, description: String): IncotermType =
            IncotermType(id, description)
    }
}
