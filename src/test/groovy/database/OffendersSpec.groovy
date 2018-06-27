package database

import spock.lang.Specification

import java.time.LocalDate

class OffendersSpec extends Specification {

    private Offenders o

    def setup() {
        def sql = Database.instance().sql()
        o = new Offenders(sql)
    }

    def 'Create and delete an Offender'() {
        given:
        final nomisId = 'A9999AE'
        !o.exists(nomisId)

        when:
        long offenderId = o.create(nomisId, "First", 'Last', LocalDate.of(1960, 1,1 ))

        then:
        o.exists(nomisId)
        o.exists(offenderId)

        when:
        o.delete(nomisId)

        then:
        !o.exists(nomisId)
        !o.exists(offenderId)
    }

    def 'offenderIdFromNomisId'() {
        given:
        final nomisId = 'A9998AE'
        !o.exists(nomisId)

        when:
        long expectedOffenderId = o.create(nomisId, "First", 'Last', LocalDate.of(1960, 1,1 ))
        long offenderId = o.offenderIdFromNomisId(nomisId)

        then:
        expectedOffenderId == offenderId

        when:
        o.delete(offenderId)

        then:
        !o.exists(offenderId)
        !o.exists(nomisId)
    }

    def 'offender with an alias'() {

        given:
        final nomisId = 'A9997AE'
        !o.exists(nomisId)

        when:
        long rootOffenderId = o.create(nomisId, "First", 'Last', LocalDate.of(1960, 1,1 ))
        long aliasOffenderId = o.create(nomisId, "First Alias", 'Last Alias', LocalDate.of(1960, 1,1 ), rootOffenderId)

        then:
        o.exists(nomisId)
        o.exists(rootOffenderId)
        o.exists(aliasOffenderId)

        when:
        o.delete(nomisId)

        then:
        !o.exists(nomisId)
        !o.exists(rootOffenderId)
        !o.exists(aliasOffenderId)
    }

    def 'fields'() {
        given:
        final nomisId = 'A9996AE'
        !o.exists(nomisId)

        when:
        long rootOffenderId = o.create(nomisId, "First", 'Last', LocalDate.of(1960, 1,1 ))
        o.createAlias(nomisId, "First Alias", 'Last Alias')

        Offender offender = o.findRoot(nomisId)
        List<Offender> offenders = o.find(nomisId)

        then:
        with(offender) {
            id == rootOffenderId
            rootId == rootOffenderId
            firstName == 'FIRST'
            lastName == 'LAST'
            dateOfBirth == LocalDate.of(1960, 1, 1)
        }

        offenders.size() == 2

        offenders.any { it.id == rootOffenderId }
        Offender alias = offenders.find { it.id != rootOffenderId }

        with(alias) {
            id != rootOffenderId
            rootId == rootOffenderId
            firstName == 'FIRST ALIAS'
            lastName == 'LAST ALIAS'
        }

        cleanup:
        o.delete(nomisId)
    }

    def 'ensure'() {
        given:
        final nomisId = 'A9996AE'
        !o.exists(nomisId)
        long id1 = o.create(nomisId, "First", 'Last', LocalDate.of(1960, 1,1 ))

        when:
        long id2 = o.ensure(nomisId, "NotFirst", "NotLast", LocalDate.of(1,1,1))

        then:
        id1 == id2

        cleanup:
        o.delete(nomisId)
    }
}
