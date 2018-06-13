package database

import groovy.sql.Sql
import spock.lang.Specification

import java.time.LocalDate

class OffenderBookingsSpec extends Specification {

    private static final String AGENCY_ID = 'TST'

    private Prisons p
    private Offenders o
    private InternalLocations il
    private OffenderBookings ob

    private InternalLocation cellLocation

    private Sql sql

    def setup() {
        sql = Database.instance().sql()
        p = new Prisons(sql)
        o = new Offenders(sql)
        il = new InternalLocations(sql)
        ob = new OffenderBookings(sql, o, il)

        sql.withTransaction {
            p.ensureAgencyAndCaseload(AGENCY_ID, 'Test')
            def wing = il.ensureWing(AGENCY_ID, 'WX')
            def landing = il.ensureLanding(wing, 'LX')
            cellLocation = il.ensureCell(landing, 10)
        }
    }

    def cleanup() {
        sql.withTransaction {
            il.tearDown(AGENCY_ID)
            p.deleteAgencyAndCaseload(AGENCY_ID)
        }
    }

    def 'create and delete'() {
        sql.withTransaction {
            given:
            def nomisId = 'A9990AE'
            long offenderId = o.create(nomisId, 'First', 'Last', LocalDate.of(1960, 12, 20))
            o.createAlias(nomisId, 'AliasFirst', 'AliasLast')

            when:
            long bookingId = ob.create(nomisId, AGENCY_ID, cellLocation.description, LocalDate.of(1980, 5, 21))

            then:
            ob.exists(bookingId)

            when:
            ob.delete(bookingId)

            then:
            !ob.exists(bookingId)

            cleanup:
            ob.tearDown(AGENCY_ID)
            o.delete(nomisId)
        }
    }
}

