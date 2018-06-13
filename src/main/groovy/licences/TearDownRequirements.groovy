package licences

import database.Requirement
import database.TestRequirements
import groovy.util.logging.Slf4j

import static database.RequirementsImporter.read

@Slf4j
class TearDownRequirements extends  DatabaseActions {

    private static String AGENCY = 'LT3'

    static void main(String[] args) {
        new TearDownRequirements().run()
    }

    static Reader requirementsReader() {
        return TestRequirements.requirementsReader()
    }

    void tearDownAgency(String agencyId) {
        offenderCurfews.tearDown agencyId
        sentenceTerms.tearDown agencyId
        offenderSentCalculations.tearDown agencyId
        offenderBookings.tearDown agencyId
        internalLocations.tearDown agencyId
    }

    void run() {
        sql.withTransaction {
            tearDownAgency AGENCY

            List<Requirement> requirements = read requirementsReader()

            requirements.forEach {
                offenders.delete it.nomisId
            }
//            throw new Exception() // Don't tear down.  Roll back the transaction instead.
        }
    }
}
