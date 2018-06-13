package database

import groovy.transform.Immutable

import java.time.LocalDate

@Immutable(knownImmutableClasses = [LocalDate])
class Offender {
    long id
    long rootId
    String nomisId
    String firstName
    String lastName
    LocalDate dateOfBirth
}
