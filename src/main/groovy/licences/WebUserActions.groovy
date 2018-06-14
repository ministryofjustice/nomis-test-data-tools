package licences

import database.Database
import database.WebUser
import database.WebUserRequirement
import groovy.sql.Sql

abstract class WebUserActions implements Runnable {

    protected static final String AGENCY = 'LT3'

    protected final Sql sql
    protected final WebUser webUser

    protected static final List<String> roles = [
            'LICENCE_CA',
            'LICENCE_RO',
            'LICENCE_DM',
            'CENTRAL_ADMIN'
    ]

    List<WebUserRequirement> webUserRequirements = [
            new WebUserRequirement(username: 'CA_USER', caseload:'LT3', roleCode: 'LICENCE_CA'),
            new WebUserRequirement(username: 'DM_USER', caseload:'LT3', roleCode: 'LICENCE_DM'),
            new WebUserRequirement(username: 'RO_USER', caseload:'LT3', roleCode: 'LICENCE_RO'),
    ]

    WebUserActions() {
        this.sql = Database.instance().sql()
        this.webUser = new WebUser(sql)
    }

    static String usernameForRole(String role) {
        "${AGENCY}_${role}".toString()
    }
}
