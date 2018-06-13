package licences

import database.Requirement
import database.TestRequirements
import groovy.util.logging.Slf4j

import java.time.LocalDate

import static database.RequirementsImporter.read

@Slf4j
class SetUpRequirements extends DatabaseActions {

    private static String AGENCY = 'LT3'

    static void main(String[] args) {
        new SetUpRequirements().run()
    }

    void run() {
        ensurePrisons()

        ensureInternalLocations AGENCY

        List<Requirement> requirements = read requirementsReader()

        applyRequirements requirements
    }

    static Reader requirementsReader() {
        TestRequirements.requirementsReader()
    }

    void ensurePrisons() {
        sql.withTransaction {
            prisons.with {
                ensureAgencyAndCaseload AGENCY, 'Licence Test Prison 3'

                ensureAgencyLocation 'COURT1', 'Court 1', 'CRT', true

                ensureCaseloadAgencyLocation AGENCY, 'OUT'

                ensureCaseloadAgencyLocation AGENCY, 'TRN'
            }
        }
    }

    void ensureInternalLocations(String agencyId) {
        sql.withTransaction {

            def wingCode = 'A'

            def wing = internalLocations.ensureWing agencyId, wingCode

            (1..3).forEach { l ->
                def landing = internalLocations.ensureLanding wing, l.toString()
                (1..10).forEach { internalLocations.ensureCell landing, it }
            }
        }
    }

    void applyRequirements(List<Requirement> requirements) {
        requirements.forEach { implementRequirement it }
    }

    void implementRequirement(Requirement requirement) {
        sql.withTransaction {
            requirement.with {
                if (offenders.exists(nomisId)) {
                    log.warn("Found OFFENDER records for ${nomisId}. Skipping this requirement.")
                    return;
                }
                offenders.create(nomisId, firstName, lastName, dateOfBirth)

                if (aliasName) {
                    offenders.createAlias(nomisId, aliasFirstName, aliasLastName)
                }

                long bookingId = offenderBookings.create(nomisId, agency, cell, sentenceStart)
                sentenceTerms.createSentenceTerms(bookingId, sentenceStart, initialCrd)
                offenderSentCalculations.createSentenceCalculations(
                        bookingId,
                        sentenceStart,
                        initialHdced,
                        null,
                        initialCrd,
                        initialLed,
                        initialSed,
                        initialTused,
                        overridedHdced,
                        null,
                        overridedCrd,
                        overridedLed,
                        overridedSed,
                        overridedHdced)

                // Create an OFFENDER_CURFEWS record for the booking.  This record supersedes any OFFENDER_CURFEW record
                // that may have been created as a side-effect of createSentenceCalculations, so long as assessmentDate
                // is null.

                // Neither effectiveDate nor ardCrdDate are currently used by the elite2-api (OffenderCurfewServiceImpl)
                // when filtering offender bookings for the /offender-sentences/home-detention-curfew-candidates REST
                // resource.

                // Note that effectiveDate must be populated due to a NOT NULL constraint. In NOMIS it looks as if
                // effectiveDate is one of overridedHdced or initialHdced, so this code does the same, but uses
                // the current date if both the above are null.
                // This isn't quite right, but as we always want to insert a record we *must* have an effectiveDate.
                def effectiveDate = overridedHdced ?: initialHdced ?: LocalDate.now()
                def ardCrdDate = (overridedCrd ?: initialCrd) ?: (overridedArd ?: initialArd)

               offenderCurfews.create(bookingId, effectiveDate, null, ardCrdDate, approvalStatus)
            }
        }
    }
}
