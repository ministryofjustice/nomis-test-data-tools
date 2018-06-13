package database

import spock.lang.Specification

import java.time.LocalDate

import static RequirementsImporter.*

class RequirementsImporterSpec extends Specification {

    def 'convert String to LocalDate'() {
        expect:
        fromLongDateString('01/01/1995') == LocalDate.of(1995, 1, 1)
    }

    def 'The importer reads the spec'() {

        when: 'I read the requirements'
        def lines = lines reader()

        then: 'I have some imported lines'
        lines.size() > 0
    }


    def 'The importer extracts the expected columns'() {

        when:
        def lines = lines reader()

        then:
        lines.forEach { assertAllColumnsPresent it }
    }

    def 'importer builds requirements'() {

        when:
        List<Requirement> requirements = read reader()

        then:
        requirements.size() == 20

        with(requirements[0]) {
            firstName       == 'Nathan'
            lastName        == 'Pullman-Smith'
            dateOfBirth     == LocalDate.of(1995, 1, 1)
            aliasName       == 'Steam'
            agency          == 'LT3'
            cell            == 'LT3-A-1-003'
            sentenceStart   == LocalDate.of(2018,1, 31)
            initialHdced    == LocalDate.of(2018,10,30)
            initialCrd      == LocalDate.of(2019, 1, 30)
            initialLed      == LocalDate.of(2020, 1, 25)
            initialSed      == LocalDate.of(2020, 1, 25)
            initialTused    == null
            decisionInNomis == 'No'
        }

        with(requirements[1]) {
            overridedHdced == LocalDate.of(2019, 8, 29)
            overridedCrd   == LocalDate.of(2020, 1, 10)
            overridedLed   == LocalDate.of(2021, 12, 25)
            overridedSed   == LocalDate.of(2021, 12, 25)
        }

        with(requirements[2]) {
            initialTused == LocalDate.of(2021, 10, 25)
        }

        with(requirements[6]) {
            decisionInNomis == 'Yes'
        }
    }

    private static void assertAllColumnsPresent(Map<String, String> row) {
        assert row.keySet() == (COLUMN_KEYS as Set)
    }

    private static Reader reader() {
        TestRequirements.requirementsReader()
    }
}
