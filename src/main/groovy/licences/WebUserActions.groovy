package licences

import database.Database
import database.WebUser
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

    WebUserActions() {
        this.sql = Database.instance().sql()
        this.webUser = new WebUser(sql)
    }

    static String usernameForRole(String role) {
        "${AGENCY}_${role}".toString()
    }
}
