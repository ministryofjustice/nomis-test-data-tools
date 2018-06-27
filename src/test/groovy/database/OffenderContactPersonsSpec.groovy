package database

import spock.lang.Specification

import java.time.LocalDate

class OffenderContactPersonsSpec extends Specification {

    private static final String AGENCY_ID = 'TST'

    private Prisons p
    private Offenders o
    private InternalLocations il
    private OffenderBookings ob
    private OffenderContactPersons ocp

    private InternalLocation cellLocation

    def setup() {
        def sql = Database.instance().sql()
        p = new Prisons(sql)
        o = new Offenders(sql)
        il = new InternalLocations(sql)
        ob = new OffenderBookings(sql, o, il)
        ocp = new OffenderContactPersons(sql)

        p.ensureAgencyAndCaseload(AGENCY_ID, 'Test')
        def wing = il.ensureWing(AGENCY_ID, 'WX')
        def landing = il.ensureLanding(wing, 'LX')
        cellLocation = il.ensureCell(landing, 10)
    }

    def 'create and delete Person'() {
        given:
        def deliusUsername = 'TEST_DELIUS_USERNAME'
        ! ocp.personExists(deliusUsername)

        when:
        ocp.createPerson'First', 'Last', deliusUsername

        then:
        ocp.personExists deliusUsername

        when:
        ocp.deletePerson deliusUsername

        then:
        ! ocp.personExists(deliusUsername)
    }

    def 'create and delete Offender Contact'() {
        given:
        def nomisId = 'A9989AE'
        def deliusUsername = 'TEST_DELIUS_USERNAME'

        def sentenceStart = LocalDate.of(2016, 3, 12)

        ocp.createPerson('First', 'Last', 'TEST_DELIUS_USERNAME')

        o.create(nomisId, 'First', 'Last', LocalDate.of(1960,12, 20))

        long bookingId = ob.create(nomisId, AGENCY_ID, cellLocation.description, sentenceStart)

        when:
        ocp.createOffenderContact bookingId, deliusUsername

        then:
        ocp.personExists deliusUsername
        ocp.offenderContactExists bookingId, deliusUsername

        when:
        ocp.tearDownContactPersons AGENCY_ID

        then:
        ! ocp.offenderContactExists(bookingId, deliusUsername)


        cleanup:
        ocp.deletePerson deliusUsername
        ob.tearDown AGENCY_ID
        o.delete nomisId
    }

    def cleanup() {
        il.tearDown AGENCY_ID
        p.deleteAgencyAndCaseload AGENCY_ID
    }
}
