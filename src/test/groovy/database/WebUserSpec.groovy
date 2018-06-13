package database

import groovy.sql.Sql
import spock.lang.Specification

class WebUserSpec extends Specification {

    private WebUser ws
    private Prisons p
    private SqlHelper sqlHelper

    def setup() {
        Sql sql = Database.instance().sql()
        ws = new WebUser(sql)
        p = new Prisons(sql)
        sqlHelper = new SqlHelper(sql)
    }

    def 'install package'() {

        when:
        ws.installPackage()

        then:
        true
    }

//    @spock.lang.Ignore
    def 'create and delete user'() {
        given:
        def agency = 'TST1'
        def username = 'LICENCE_RO_' + agency

        p.ensureAgencyAndCaseload(agency, "Testing")

        when:

        ws.createUser(
            username,
            'password123456',
            'RO',
            username,
            'test.user@digital.justice.gov.uk',
            agency,
            'LICENCE_RO')
        then:
        ws.userExists username

        when:
        ws.deleteUser username

        then:
        ! ws.userExists(username)

        cleanup:
        p.deleteAgencyAndCaseload(agency)
    }
}


