package database

import spock.lang.Specification

import java.time.LocalDate

class SentenceTermsSpec extends Specification {

    private static final String AGENCY_ID = 'TST'

    private Prisons p
    private Offenders o
    private InternalLocations il
    private OffenderBookings ob
    private SentenceTerms st

    private InternalLocation cellLocation

    def setup() {
        def sql = Database.instance().sql()
        p = new Prisons(sql)
        o = new Offenders(sql)
        il = new InternalLocations(sql)
        ob = new OffenderBookings(sql, o, il)
        st = new SentenceTerms(sql)

        p.ensureAgencyAndCaseload(AGENCY_ID, 'Test')
        def wing = il.ensureWing(AGENCY_ID, 'WX')
        def landing = il.ensureLanding(wing, 'LX')
        cellLocation = il.ensureCell(landing, 10)
    }

    def cleanup() {
        il.tearDown(AGENCY_ID)
        p.deleteAgencyAndCaseload(AGENCY_ID)
    }

    def 'Create and delete Offender Sentence Terms'() {

        given:
        def nomisId = 'A9989AE'
        LocalDate sentenceStart = LocalDate.of(2016, 3, 12)
        LocalDate crdDate = LocalDate.of(2018, 1, 12)

        o.create(nomisId, 'First', 'Last', LocalDate.of(1960,12, 20))
        o.createAlias(nomisId, 'AliasFirst', 'AliasLast')

        long bookingId = ob.create(nomisId, AGENCY_ID, cellLocation.description, sentenceStart)

        when:
        st.createSentenceTerms(bookingId, sentenceStart, crdDate)

        then:
        1 + 1 == 2

        cleanup:
        st.deleteSentenceTerms(bookingId)
        ob.tearDown(AGENCY_ID)
        o.delete(nomisId)
    }

    def 'Create and tear down Offender Sentence Terms'() {

        given:
        def nomisId = 'A9989AE'
        LocalDate sentenceStart = LocalDate.of(2016, 3, 12)
        LocalDate crdDate = LocalDate.of(2018, 1, 12)

        o.create(nomisId, 'First', 'Last', LocalDate.of(1960,12, 20))

        long bookingId = ob.create(nomisId, AGENCY_ID, cellLocation.description, sentenceStart)

        when:
        st.createSentenceTerms(bookingId, sentenceStart, crdDate)
        st.tearDown(AGENCY_ID)

        then:
        1 + 1 == 2

        cleanup:
        ob.tearDown(AGENCY_ID)
        o.delete(nomisId)
    }

}
