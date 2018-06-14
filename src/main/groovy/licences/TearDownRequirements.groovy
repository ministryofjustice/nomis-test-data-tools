package licences

import database.DeliusLink
import database.Requirement
import database.TestRequirements
import groovy.util.logging.Slf4j

import static database.RequirementsImporter.read

@Slf4j
class TearDownRequirements extends DatabaseActions {

    private static String AGENCY = 'LT3'

    static void main(String[] args) {
        new TearDownRequirements().run()
    }

    void run() {
        sql.withTransaction {

            tearDownAgency AGENCY

            List<Requirement> requirements = read requirementsReader()

            Set<DeliusLink> linkSet = requirements.collect { it.deliusLink } as Set
            linkSet.forEach {
                offenderContactPersons.deletePerson it.deliusUsername
                // Next line commented out because we're re-using existing nomisUsernames (DELIUS_ID, DELIUS_USER2 etc)
                // Not sure this is a good idea.
                //  webUser.deleteWebUser it.nomisUsername
            }

            requirements.forEach {
                offenders.delete it.nomisId
            }
//            throw new Exception() // Don't tear down.  Roll back the transaction instead.
        }
    }

    void tearDownAgency(String agencyId) {
        offenderContactPersons.tearDownContactPersons agencyId
        offenderCurfews.tearDown agencyId
        sentenceTerms.tearDown agencyId
        offenderSentCalculations.tearDown agencyId
        offenderBookings.tearDown agencyId
        internalLocations.tearDown agencyId
    }

    static Reader requirementsReader() {
        return TestRequirements.reader()
    }
}
