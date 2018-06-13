package database

import spock.lang.Specification

class MapSpecification extends Specification {

    def 'mapKeys'() {
        given:
        def FN = 'First Name'
        def LN = 'Last Name'
        def m = [(FN): 'Eric', (LN):'Cantona']

        expect:
        m[FN] == 'Eric'
        m[LN] == 'Cantona'
    }
}
