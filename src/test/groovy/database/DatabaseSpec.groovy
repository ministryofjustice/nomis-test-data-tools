package database

import spock.lang.Specification

class DatabaseSpec extends Specification {

    def 'I can ping the database'() {
        when:
        def db = Database.instance()

        then:
        db.ping()
    }

}
