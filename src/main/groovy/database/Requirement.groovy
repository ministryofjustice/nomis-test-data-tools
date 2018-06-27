package database

import groovy.transform.Immutable

import java.time.LocalDate

import static database.OffenderCurfews.ApprovalStatus.*

@Immutable(knownImmutableClasses = [LocalDate])

class Requirement {

    private static final ALIAS_SPLIT_PATTERN = ~/\s+/

    String nomisId
    String firstName
    String lastName
    LocalDate dateOfBirth
    String aliasName
    String agency
    String cell
    
    LocalDate sentenceStart
    
    LocalDate initialHdced
    LocalDate initialArd
    LocalDate initialCrd
    LocalDate initialLed
    LocalDate initialSed
    LocalDate initialTused

    LocalDate overridedHdced
    LocalDate overridedArd
    LocalDate overridedCrd
    LocalDate overridedLed
    LocalDate overridedSed
    LocalDate overridedTused

    String decisionInNomis

    DeliusLink deliusLink

    String getAliasFirstName() {
        if (!aliasName) return null
        def parts = ALIAS_SPLIT_PATTERN.split(aliasName)
        parts.length > 1 ? parts[0] : null // Only supply first name if there is a last name
    }
    
    String getAliasLastName() {
        if (!aliasName) return null
        def parts = ALIAS_SPLIT_PATTERN.split(aliasName)
        parts.length > 0 ? parts[-1] : null // return the last part
    }

    OffenderCurfews.ApprovalStatus getApprovalStatus() {
        decisionInNomis?.trim()?.toUpperCase() == 'YES' ? APPROVED : null
    }
}
