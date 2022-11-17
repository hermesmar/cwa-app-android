package de.rki.coronawarnapp.srs.core.model

enum class SrsSubmissionType(val type: String) {
    SRS_SELF_TEST("SRS_SELF_TEST"),
    SRS_REGISTERED_RAT("SRS_REGISTERED_RAT"),
    SRS_UNREGISTERED_RAT("SRS_UNREGISTERED_RAT"),
    SRS_REGISTERED_PCR("SRS_REGISTERED_PCR"),
    SRS_UNREGISTERED_PCR("SRS_UNREGISTERED_PCR"),
    SRS_RAPID_PCR("SRS_RAPID_PCR"),
    SRS_OTHER("SRS_OTHER")
}
