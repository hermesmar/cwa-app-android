package de.rki.coronawarnapp.covidcertificate.person.core

import android.content.Context
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings.Companion.CURRENT_PERSON_KEY
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings.Companion.PERSONS_SETTINGS_MAP
import de.rki.coronawarnapp.covidcertificate.person.model.PersonSettings
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.FakeDataStore
import java.time.Instant

@Suppress("MaxLineLength")
class PersonCertificatesStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    private lateinit var fakeDataStore: FakeDataStore
    private val personIdentifier1 = CertificatePersonIdentifier(
        dateOfBirthFormatted = "01.10.2020",
        firstNameStandardized = "fN",
        lastNameStandardized = "lN"
    )

    private val personIdentifier2 = CertificatePersonIdentifier(
        dateOfBirthFormatted = "20.10.2020",
        firstNameStandardized = "ffNN",
        lastNameStandardized = "llNN"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        fakeDataStore = FakeDataStore()
    }

    private fun createInstance() = PersonCertificatesSettings(
        dataStore = fakeDataStore,
        mapper = SerializationModule.jacksonBaseMapper
    )

    @Test
    fun `init is sideeffect free`() = runTest {
        createInstance().apply {
            currentCwaUser.first() shouldBe null
            personsSettings.first() shouldBe emptyMap()
        }
    }

    @Test
    fun `clearing deletes all data`() = runTest {
        createInstance().apply {
            setCurrentCwaUser(personIdentifier1)
            setBoosterNotifiedAt(personIdentifier1)
            setDccReissuanceNotifiedAt(personIdentifier1)
            setGStatusNotifiedAt(personIdentifier1)

            fakeDataStore[CURRENT_PERSON_KEY] shouldNotBe null
            fakeDataStore[PERSONS_SETTINGS_MAP] shouldNotBe null

            reset()

            fakeDataStore[CURRENT_PERSON_KEY] shouldBe null
            fakeDataStore[PERSONS_SETTINGS_MAP] shouldBe null
        }
    }

    @Test
    fun `store current cwa user person identifier`() = runTest {
        val testIdentifier = CertificatePersonIdentifier(
            firstNameStandardized = "firstname",
            lastNameStandardized = "lastname",
            dateOfBirthFormatted = "1999-12-24"
        )

        createInstance().apply {
            currentCwaUser.first() shouldBe null
            setCurrentCwaUser(testIdentifier)
            currentCwaUser.first() shouldBe testIdentifier

            val raw = fakeDataStore[CURRENT_PERSON_KEY] as String
            raw.toComparableJsonPretty() shouldBe """
                {
                  "dateOfBirth": "1999-12-24",
                  "familyNameStandardized": "lastname",
                  "givenNameStandardized": "firstname"
                }
            """.toComparableJsonPretty()
        }
    }

    @Test
    fun `remove current cwa user person identifier`() = runTest {
        val testIdentifier = CertificatePersonIdentifier(
            firstNameStandardized = "firstname",
            lastNameStandardized = "lastname",
            dateOfBirthFormatted = "1999-12-24"
        )

        createInstance().apply {
            currentCwaUser.first() shouldBe null
            setCurrentCwaUser(testIdentifier)
            currentCwaUser.first() shouldBe testIdentifier
            removeCurrentCwaUser()
            currentCwaUser.first() shouldBe null
        }
    }

    @Test
    fun `set G status for a person with no setting`() = runTest {
        createInstance().apply {
            setGStatusNotifiedAt(personIdentifier1, Instant.EPOCH)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": null,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": null,
                			"showAdmissionStateChangedBadge": true,
                			"lastAdmissionStateNotifiedAt": 0.0
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `set G status for a person that has settings`() = runTest {
        createInstance().apply {
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setGStatusNotifiedAt(personIdentifier1, Instant.EPOCH)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": 0.0,
                			"showDccReissuanceBadge": true,
                			"lastDccReissuanceNotifiedAt": 0.0,
                			"showAdmissionStateChangedBadge": true,
                			"lastAdmissionStateNotifiedAt": 0.0
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `dismiss G status for a person with no settings`() = runTest {
        createInstance().apply {
            dismissGStatusBadge(personIdentifier1)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": null,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": null,
                			"showAdmissionStateChangedBadge": false,
                			"lastAdmissionStateNotifiedAt": null
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `dismiss G status for a person that has settings`() = runTest {
        createInstance().apply {
            setGStatusNotifiedAt(personIdentifier1, Instant.EPOCH)
            dismissGStatusBadge(personIdentifier1)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": null,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": null,
                			"showAdmissionStateChangedBadge": false,
                			"lastAdmissionStateNotifiedAt": 0.0
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `set booster for a person has not settings`() = runTest {
        createInstance().apply {
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": 0.0,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": null,
                			"showAdmissionStateChangedBadge": false,
                			"lastAdmissionStateNotifiedAt": null
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `set booster for a person has settings`() = runTest {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": 0.0,
                			"showDccReissuanceBadge": true,
                			"lastDccReissuanceNotifiedAt": 0.0,
                			"showAdmissionStateChangedBadge": false,
                			"lastAdmissionStateNotifiedAt": null
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `set dcc reissuance for a person has not settings`() = runTest {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": null,
                			"showDccReissuanceBadge": true,
                			"lastDccReissuanceNotifiedAt": 0.0,
                			"showAdmissionStateChangedBadge": false,
                			"lastAdmissionStateNotifiedAt": null
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()

            personsSettings.first() shouldBe mapOf(
                personIdentifier1 to PersonSettings(
                    showDccReissuanceBadge = true,
                    lastDccReissuanceNotifiedAt = Instant.EPOCH
                )
            )
        }
    }

    @Test
    fun `set dcc reissuance for a person has settings`() = runTest {
        createInstance().apply {
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": 0.0,
                			"showDccReissuanceBadge": true,
                			"lastDccReissuanceNotifiedAt": 0.0,
                			"showAdmissionStateChangedBadge": false
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `dismiss dcc reissuance for a person has not settings`() = runTest {
        createInstance().apply {
            dismissReissuanceBadge(personIdentifier1)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": null,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": null,
                			"showAdmissionStateChangedBadge": false,
                			"lastAdmissionStateNotifiedAt": null
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `dismiss dcc reissuance for a person has settings`() = runTest {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier1)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": 0.0,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": 0.0,
                			"showAdmissionStateChangedBadge": false
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `acknowledge booster for a person has not settings`() = runTest {
        createInstance().apply {
            acknowledgeBoosterRule(personIdentifier1, "BRN-123")
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": "BRN-123",
                			"lastBoosterNotifiedAt": null,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": null,
                			"showAdmissionStateChangedBadge": false,
                			"lastAdmissionStateNotifiedAt": null
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `acknowledge booster for a person has settings`() = runTest {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier1)
            acknowledgeBoosterRule(personIdentifier1, "BRN-123")
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": "BRN-123",
                			"lastBoosterNotifiedAt": 0.0,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": 0.0,
                			"showAdmissionStateChangedBadge": false
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `clear booster for a person has settings`() = runTest {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier1)
            acknowledgeBoosterRule(personIdentifier1, "BRN-123")
            clearBoosterRuleInfo(personIdentifier1)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": 0.0,
                			"showAdmissionStateChangedBadge": false
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `clear booster for a person has not settings`() = runTest {
        createInstance().apply {
            clearBoosterRuleInfo(personIdentifier1)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"showDccReissuanceBadge": false,
                			"showAdmissionStateChangedBadge": false
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `save settings for many persons`() = runTest {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            setGStatusNotifiedAt(personIdentifier1, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier1)
            dismissGStatusBadge(personIdentifier1)
            acknowledgeBoosterRule(personIdentifier1, "BRN-123")

            setBoosterNotifiedAt(personIdentifier2, Instant.EPOCH)
            setDccReissuanceNotifiedAt(personIdentifier2, Instant.EPOCH)
            setGStatusNotifiedAt(personIdentifier2, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier2)
            dismissGStatusBadge(personIdentifier2)
            acknowledgeBoosterRule(personIdentifier2, "BRN-456")

            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                        "{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                          "lastSeenBoosterRuleIdentifier": "BRN-123",
                          "lastBoosterNotifiedAt": 0.0,
                          "showDccReissuanceBadge": false,
                          "lastDccReissuanceNotifiedAt": 0.0,
                          "showAdmissionStateChangedBadge": false,
                          "lastAdmissionStateNotifiedAt": 0.0
                        },
                        "{\"dateOfBirth\":\"20.10.2020\",\"familyNameStandardized\":\"llNN\",\"givenNameStandardized\":\"ffNN\"}": {
                          "lastSeenBoosterRuleIdentifier": "BRN-456",
                          "lastBoosterNotifiedAt": 0.0,
                          "showDccReissuanceBadge": false,
                          "lastDccReissuanceNotifiedAt": 0.0,
                          "showAdmissionStateChangedBadge": false,
                          "lastAdmissionStateNotifiedAt": 0.0
                        }
                    }
                }
            """.trimIndent()
                .toComparableJsonPretty()

            personsSettings.first() shouldBe mapOf(
                personIdentifier1 to PersonSettings(
                    lastBoosterNotifiedAt = Instant.EPOCH,
                    lastSeenBoosterRuleIdentifier = "BRN-123",
                    showDccReissuanceBadge = false,
                    lastDccReissuanceNotifiedAt = Instant.EPOCH,
                    lastAdmissionStateNotifiedAt = Instant.EPOCH
                ),
                personIdentifier2 to PersonSettings(
                    lastBoosterNotifiedAt = Instant.EPOCH,
                    lastSeenBoosterRuleIdentifier = "BRN-456",
                    showDccReissuanceBadge = false,
                    lastDccReissuanceNotifiedAt = Instant.EPOCH,
                    lastAdmissionStateNotifiedAt = Instant.EPOCH
                )
            )
        }
    }

    @Test
    fun `clean up settings for outdated persons`() = runTest {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            setGStatusNotifiedAt(personIdentifier1, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier1)
            dismissGStatusBadge(personIdentifier1)
            acknowledgeBoosterRule(personIdentifier1, "BRN-123")

            setDccReissuanceNotifiedAt(personIdentifier2, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier2, Instant.EPOCH)
            setGStatusNotifiedAt(personIdentifier2, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier2)
            dismissGStatusBadge(personIdentifier2)
            acknowledgeBoosterRule(personIdentifier2, "BRN-456")

            cleanSettingsNotIn(personIdentifiers = setOf(personIdentifier2))

            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                        "{\"dateOfBirth\":\"20.10.2020\",\"familyNameStandardized\":\"llNN\",\"givenNameStandardized\":\"ffNN\"}": {
                          "lastSeenBoosterRuleIdentifier": "BRN-456",
                          "lastBoosterNotifiedAt": 0.0,
                          "showDccReissuanceBadge": false,
                          "lastDccReissuanceNotifiedAt": 0.0,
                          "showAdmissionStateChangedBadge": false,
                          "lastAdmissionStateNotifiedAt": 0.0
                        }
                    }
                }
            """.trimIndent()
                .toComparableJsonPretty()

            personsSettings.first() shouldBe mapOf(
                personIdentifier2 to PersonSettings(
                    lastBoosterNotifiedAt = Instant.EPOCH,
                    lastSeenBoosterRuleIdentifier = "BRN-456",
                    showDccReissuanceBadge = false,
                    lastDccReissuanceNotifiedAt = Instant.EPOCH,
                    lastAdmissionStateNotifiedAt = Instant.EPOCH
                )
            )
        }
    }
}
