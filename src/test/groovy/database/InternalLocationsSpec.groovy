package database

import spock.lang.Specification

import static database.InternalLocations.*

class InternalLocationsSpec extends Specification {

    private static final AGENCY_ID = 'T1'

    InternalLocations il
    Prisons prisons

    def setup() {
        def sql = Database.instance().sql()
        il = new InternalLocations(sql)
        prisons = new Prisons(sql)
        prisons.ensureAgencyAndCaseload AGENCY_ID, 'TEST'
    }

    def cleanup() {
        prisons.deleteAgencyAndCaseload AGENCY_ID
    }

    def 'Not exists, create, exists cycle'() {
        given:

        def description = "${AGENCY_ID}-A"
        !il.exists(AGENCY_ID, description)

        when:
        il.ensure 'A', WING, AGENCY_ID, description, null, 'House Block A'

        then:
        il.exists AGENCY_ID, description

        cleanup:
        il.delete AGENCY_ID, description
    }

    def 'Exists, find, delete by id'() {
        given:

        def description = "${AGENCY_ID}-A"
        !il.exists(AGENCY_ID, description)
        il.ensure 'A', WING, AGENCY_ID, description, null, 'House Block A'
        il.exists AGENCY_ID, description

        when:
        InternalLocation location = il.find AGENCY_ID, description

        then:
        location.agencyId == AGENCY_ID
        location.description == description

        when:
        il.delete location.id

        then:
        !il.exists(AGENCY_ID, description)
    }

    def 'buildDescription'() {
        expect:
        InternalLocation.buildDescription(AGENCY_ID, 'A') == AGENCY_ID + '-A'
    }

    def 'code'() {
        expect:
        InternalLocation.code(3) == '003'
    }

    def 'Wing, landing and cell'() {
        given:
        def wingCode = 'A'
        !il.exists(AGENCY_ID, InternalLocation.buildDescription(AGENCY_ID, wingCode))

        when:
        def wing = il.ensureWing(AGENCY_ID, wingCode)
        def landing = il.ensureLanding(wing, '1')
        def cell = il.ensureCell(landing, 1)

        then:
        wing.description == AGENCY_ID + '-A'
        landing.description == AGENCY_ID + '-A-1'
        cell.description == AGENCY_ID + '-A-1-001'

        il.exists(AGENCY_ID, wing.description)
        il.exists(AGENCY_ID, landing.description)
        il.exists(AGENCY_ID, cell.description)

        cleanup:
        il.delete cell.id
        il.delete landing.id
        il.delete wing.id
    }

    def 'tear down locations for agency'() {
        when:
        il.tearDown AGENCY_ID

        then:
        il.find(AGENCY_ID).size() == 0

    }
}
