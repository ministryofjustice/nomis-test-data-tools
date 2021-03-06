package database

import spock.lang.Specification

import java.time.LocalDate

class OffenderCurfewsSpec extends Specification {

    private static final String AGENCY_ID = 'TST'

    private Prisons p
    private Offenders o
    private InternalLocations il
    private OffenderBookings ob
    private OffenderCurfews oc

    private InternalLocation cellLocation

    def setup() {
        def sql = Database.instance().sql()
        p = new Prisons(sql)
        o = new Offenders(sql)
        il = new InternalLocations(sql)
        ob = new OffenderBookings(sql, o, il)
        oc = new OffenderCurfews(sql)

        p.ensureAgencyAndCaseload(AGENCY_ID, 'Test')
        def wing = il.ensureWing(AGENCY_ID, 'WX')
        def landing = il.ensureLanding(wing, 'LX')
        cellLocation = il.ensureCell(landing, 10)
    }

    def 'create and delete'() {
        given:
        def nomisId = 'A9989AE'
        LocalDate sentenceStart = LocalDate.of(2016, 3, 12)
        LocalDate crdDate = LocalDate.of(2018, 1, 12)

        o.create(nomisId, 'First', 'Last', LocalDate.of(1960,12, 20))

        long bookingId = ob.create(nomisId, AGENCY_ID, cellLocation.description, sentenceStart)

        when:
        oc.create(
            bookingId,
            LocalDate.of(1981, 1, 12),
            LocalDate.of(1982, 2, 11),
            LocalDate.of(1983, 3, 10),
            OffenderCurfews.ApprovalStatus.APPROVED
        )

        then:
        true

        cleanup:
        oc.tearDown(AGENCY_ID)
        ob.tearDown(AGENCY_ID)
        o.delete(nomisId)

    }

    def cleanup() {
        il.tearDown(AGENCY_ID)
        p.deleteAgencyAndCaseload(AGENCY_ID)
    }
}
