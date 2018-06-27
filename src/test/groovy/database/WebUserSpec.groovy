package database

import groovy.sql.Sql
import spock.lang.Specification

class WebUserSpec extends Specification {

    private WebUser ws
    private Prisons p

    def setup() {
        Sql sql = Database.instance().sql()
        ws = new WebUser(sql)
        p = new Prisons(sql)
    }

//    @spock.lang.Ignore
    def 'create and delete web user'() {
        given:
        final agency = 'TST1'
        final username = agency+'_LICENCE_RO'

        p.ensureAgencyAndCaseload(agency, "Testing")

        when:

        ws.ensureWebUser(
            username,
            'password123456',
            'RO',
            username,
            'test.user@digital.justice.gov.uk',
            [agency],
            'LICENCE_RO')

        then:
        ws.userExists username
        ws.usernameHasAccount username
        ws.userAccessibleCaseloadExists username, agency
        ws.userCaseloadRoleExists username, 'NWEB', ws.findRoleId('LICENCE_RO')
        ws.userCaseloadRoleExists username, agency, 100
        ws.userCaseloadRoleExists username, agency, 202
        ws.userCaseloadRoleExists username, agency, 962

        when:
        ws.deleteWebUser username

        then:
        ! ws.userExists(username)
        ! ws.usernameHasAccount (username)
        ! ws.userAccessibleCaseloadExists (username, agency)
        ! ws.userCaseloadRoleExists (username, 'NWEB', ws.findRoleId('LICENCE_RO'))
        ! ws.userCaseloadRoleExists (username, agency, 100)
        ! ws.userCaseloadRoleExists (username, agency, 202)
        ! ws.userCaseloadRoleExists (username, agency, 962)


        cleanup:
        p.deleteAgencyAndCaseload agency
    }
}


