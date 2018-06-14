package database

import groovy.sql.Sql
import groovy.util.logging.Slf4j

@Slf4j
class OffenderContactPersons {
    private final Sql sql
    private final SqlHelper sqlHelper

    OffenderContactPersons(Sql sql) {
        this.sql = sql
        this.sqlHelper = new SqlHelper(sql)
    }

    void createPerson(String firstName, String lastName, String personIdentifier) {
        if (personExists(personIdentifier)) {
            log.info "There is already a PERSONS having identifier ${personIdentifier}. Skipping."
            return
        }
        long personId = nextPersonId()

        log.info "Creating PERSONS with person_id ${personId}. firsName ${firstName}, lastName ${lastName}"

        sql.execute """\
            INSERT INTO PERSONS (
                PERSON_ID, 
                LAST_NAME, 
                FIRST_NAME, 
                MIDDLE_NAME) 
            VALUES (
                ${personId}, 
                ${firstName.toUpperCase()}, 
                ${lastName.toUpperCase()}, 
                NULL)"""

        log.info "Creating PERSON_IDENTIFIERS for person_id ${personId}, personIdentifier ${personIdentifier}. firsName ${firstName}, lastName ${lastName}"

        sql.execute """\
            INSERT INTO PERSON_IDENTIFIERS (
                PERSON_ID, 
                ID_SEQ, 
                IDENTIFIER_TYPE,
                IDENTIFIER)
            VALUES (
                ${personId},  
                NVL((SELECT MAX(ID_SEQ) FROM PERSON_IDENTIFIERS WHERE PERSON_ID = ${personId}),0) + 1,
                'EXTERNAL_REF', 
                ${personIdentifier})"""
    }

    void createOffenderContact(long bookingId, String personIdentifier) {
        if (offenderContactExists(bookingId, personIdentifier)) {
            log.info "Offender Contact (bookingId ${bookingId}, ${personIdentifier}) already exists. Skipping"
            return
        }
        long personId = personIdFromPersonIdentifier(personIdentifier)

        log.info "Creating OFFENDER_CONTACT_PERSONS for personIdentifier ${personIdentifier} (${personId}) and bookingId ${bookingId}"

        sql.execute """\
            INSERT INTO OFFENDER_CONTACT_PERSONS (
                OFFENDER_CONTACT_PERSON_ID, 
                OFFENDER_BOOK_ID, 
                PERSON_ID, 
                CONTACT_TYPE, 
                RELATIONSHIP_TYPE, 
                EMERGENCY_CONTACT_FLAG, 
                NEXT_OF_KIN_FLAG, 
                ACTIVE_FLAG)
            VALUES (
            OFFENDER_CONTACT_PERSON_ID.NEXTVAL, 
            ${bookingId}, 
            ${personId}, 
            'O', 'COM', 
            'N', 
            'N', 
            'Y')"""
    }

    boolean offenderContactExists(long bookingId, String personIdentifier) {
        if (!personExists(personIdentifier)) return false
        long personId = personIdFromPersonIdentifier personIdentifier
        sqlHelper.exists """\
            select count(*)
              from OFFENDER_CONTACT_PERSONS
             where PERSON_ID = ${personId} AND
                   OFFENDER_BOOK_ID = ${bookingId}"""
    }

    boolean personExists(String personIdentifier) {
        sqlHelper.exists """\
            SELECT count(*) 
              FROM PERSON_IDENTIFIERS 
             WHERE IDENTIFIER = ${personIdentifier} AND
                   IDENTIFIER_TYPE = 'EXTERNAL_REF' """
    }

    long personIdFromPersonIdentifier(String personIdentifier) {
        sql.firstRow ("""\
              SELECT PERSON_ID as personId
                FROM PERSON_IDENTIFIERS 
               WHERE IDENTIFIER = ${personIdentifier} AND
                     IDENTIFIER_TYPE = 'EXTERNAL_REF'
            ORDER BY ID_SEQ DESC
            """).personId
    }

    long nextPersonId() {
        sql.firstRow ("SELECT PERSON_ID.NEXTVAL as personId FROM DUAL").personId
    }

    void deletePerson(String personIdentifier) {
        if (!personExists(personIdentifier)) {
            log.info "Person ${personIdentifier} not found."
            return
        }

        long personId = personIdFromPersonIdentifier(personIdentifier)

        log.info "Deleting Person with personIdentifier ${personIdentifier}(${personId})"

        sql.execute "DELETE FROM PERSON_IDENTIFIERS WHERE PERSON_ID = ${personId}"
        sql.execute "DELETE FROM PERSONS WHERE PERSON_ID = ${personId}"
    }

    void tearDownContactPersons(String agencyId) {
        sql.execute """\
            DELETE FROM OFFENDER_CONTACT_PERSONS
                  WHERE OFFENDER_BOOK_ID IN (
                            SELECT ob.OFFENDER_BOOK_ID
                            FROM OFFENDER_BOOKINGS ob
                            WHERE ob.AGY_LOC_ID = ${agencyId}
                        )"""

    }
}
