package de.rki.coronawarnapp.nearby.modules.detectiontracker

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

@Keep
data class TrackedExposureDetection(
    @JsonProperty("identifier") val identifier: String,
    @JsonProperty("startedAt") val startedAt: Instant,
    @JsonProperty("result") val result: Result? = null,
    @JsonProperty("finishedAt") val finishedAt: Instant? = null,
    @JsonProperty("enfVersion") val enfVersion: EnfVersion? = null
) {

    val isCalculating: Boolean
        get() = finishedAt == null
    val isSuccessful: Boolean
        get() = (result == Result.NO_MATCHES || result == Result.UPDATED_STATE)

    @Keep
    enum class Result {
        @JsonProperty("NO_MATCHES")
        NO_MATCHES,

        @JsonProperty("UPDATED_STATE")
        UPDATED_STATE,

        @JsonProperty("TIMEOUT")
        TIMEOUT
    }

    enum class EnfVersion {
        @JsonProperty("V1_LEGACY_MODE")
        V1_LEGACY_MODE,
        @JsonProperty("V2_WINDOW_MODE")
        V2_WINDOW_MODE
    }
}
