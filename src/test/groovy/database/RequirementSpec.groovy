package database

import database.OffenderCurfews.ApprovalStatus
import spock.lang.Specification
import spock.lang.Unroll

class RequirementSpec extends Specification {

    @Unroll
    def 'derived properties aliasFirstName aliasLastName'() {
        when:
        def r = new Requirement(aliasName: alias)

        then:
        r.aliasFirstName == aliasFirst
        r.aliasLastName == aliasLast


        where:
        alias          | aliasFirst | aliasLast
        'Fred  Dibnah' | 'Fred'     | 'Dibnah'
        'Fred'         |  null      | 'Fred'
        null           |  null      |  null
        '   '          |  null      |  null
    }

    @Unroll
    def 'ApprovalStatus from decisionInNomis'() {
        when:
        def r = new Requirement(decisionInNomis: decision)

        then:
        r.approvalStatus == approvalStatus

        where:
        decision | approvalStatus
        null     | null
        ''       | null
        'No'     | null
        'NO'     | null
        'Yes'    | ApprovalStatus.APPROVED
        'YES'    | ApprovalStatus.APPROVED
        ' YeS '  | ApprovalStatus.APPROVED
    }
}
