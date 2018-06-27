package database

import spock.lang.Specification

class PrisonsSpec extends Specification {

    private Prisons prisons

    def setup() {
        prisons = new Prisons(Database.instance().sql())
    }

    def 'should find agency LEI'() {
        expect:
        prisons.agencyExists 'LEI'
    }

    def 'should not find agency XXX'() {
        expect:
        !prisons.agencyExists('XXX')
    }

    def 'Should find caseload LEI'() {
        expect:
        prisons.caseloadExists 'LEI'
    }

    def 'Should not find caseload XXX'() {
        expect:
        !prisons.caseloadExists('XXX')
    }

    def 'Should find caseloadAgencyLocation LEI - LEI'() {
        expect:
        prisons.caseloadAgencyLocationExists 'LEI', 'LEI'
    }

    def 'Should not find caseloadAgencyLocation LEI - XXX'() {
        expect:
        !prisons.caseloadAgencyLocationExists('LEI', 'XXX')
    }

    def 'Should not find caseloadAgencyLocation XXX - LEI'() {
        expect:
        !prisons.caseloadAgencyLocationExists('XXX', 'LEI')
    }

    def 'Should create and delete agency and caseload'() {
        given:
        def id = 'QQQ'

        prisons.deleteAgencyAndCaseload id

        !prisons.agencyExists(id)
        !prisons.caseloadExists(id)
        when:
        prisons.createAgencyAndCaseload id, "Prison ${id}"

        then:
        prisons.caseloadAgencyLocationExists id, id

        and:

        prisons.deleteAgencyAndCaseload id
        !prisons.agencyExists(id)
        !prisons.caseloadExists(id)
    }

    def 'CaselooadAgencyLocations OUT and TRN should exist'() {
        expect:
        prisons.agencyExists 'OUT'
        prisons.agencyExists 'TRN'
    }

    def 'ensure Agency and Caseload QQQ'() {
        given:
        def id = 'QQQ'
        !prisons.agencyExists(id)
        !prisons.caseloadExists(id)

        when:
        prisons.ensureAgencyAndCaseload id, 'Test'
        prisons.ensureAgencyAndCaseload id, 'Test 2'

        then:
        prisons.agencyExists id
        prisons.caseloadExists id

        prisons.deleteAgencyAndCaseload id
    }
}
