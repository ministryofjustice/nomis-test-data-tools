package database

import groovy.sql.Sql
import groovy.util.logging.Slf4j

import java.time.LocalDate

@Slf4j
class OffenderBookings {

    private final Sql sql
    private final Offenders offenders
    private final InternalLocations internalLocations
    private final SqlHelper helper

    OffenderBookings(Sql sql, Offenders offenders, InternalLocations internalLocations) {
        this.sql = sql
        this.offenders = offenders
        this.internalLocations = internalLocations
        this.helper = new SqlHelper(sql)
    }

    long create(String nomisId, String agencyId, String cell, LocalDate sentenceStart) {

        long offenderId = offenders.offenderIdFromNomisId(nomisId)
        long cellId = internalLocations.find(agencyId, cell).id
        long bookingId = nextId()

        log.info "Creating OFFENDER_BOOKING for offender ${nomisId}(${offenderId}) with bookingId ${bookingId}, in agency ${agencyId}, cell ${cell}(${cellId})"

        sql.executeInsert """\
            INSERT INTO OFFENDER_BOOKINGS (
                OFFENDER_BOOK_ID, 
                BOOKING_BEGIN_DATE, 
                BOOKING_NO, 
                OFFENDER_ID, 
                DISCLOSURE_FLAG, 
                IN_OUT_STATUS, 
                ACTIVE_FLAG, 
                YOUTH_ADULT_CODE, 
                AGY_LOC_ID, 
                BOOKING_SEQ, 
                ROOT_OFFENDER_ID, 
                LIVING_UNIT_ID)
            VALUES (
                ${bookingId},
                ${SqlHelper.toSqlDate(sentenceStart)}, 
                ${'L'+bookingId}, 
                ${offenderId}, 
                'N', 
                'IN', 
                'Y', 
                'N', 
                ${agencyId}, 
                2, 
                ${offenderId}, 
                ${cellId})
            """

        bookingId
    }

    boolean exists(long bookingId) {
        helper.exists "SELECT COUNT(*) as rowCount from OFFENDER_BOOKINGS where OFFENDER_BOOK_ID = ${bookingId}"
    }

    void delete(long bookingId) {
        sql.execute"delete from OFFENDER_BOOKINGS where OFFENDER_BOOK_ID = ${bookingId}"
    }

    void tearDown(String agencyId) {

        sql.execute """\
            DELETE FROM OFFENDER_EXTERNAL_MOVEMENTS
                WHERE OFFENDER_BOOK_ID IN (
                          SELECT OFFENDER_BOOK_ID
                            FROM OFFENDER_BOOKINGS OB 
                           WHERE OB.AGY_LOC_ID = ${agencyId}
                )"""

        log.info "Deleted all OFFENDER_EXTERNAL_MOVEMENTS for agencyId ${agencyId} (${sql.updateCount})"

        sql.execute """\
            DELETE FROM OFFENDER_CONTACT_PERSONS
                WHERE OFFENDER_BOOK_ID IN (
                          SELECT OFFENDER_BOOK_ID
                            FROM OFFENDER_BOOKINGS OB 
                           WHERE OB.AGY_LOC_ID = ${agencyId}
                )"""

        log.info "Deletied all OFFENDER_CONTACT_PERSONS for agencyId ${agencyId} (${sql.updateCount})"

        sql.execute """\
            DELETE FROM BED_ASSIGNMENT_HISTORIES
                WHERE OFFENDER_BOOK_ID IN (
                          SELECT OFFENDER_BOOK_ID
                            FROM OFFENDER_BOOKINGS OB 
                           WHERE OB.AGY_LOC_ID = ${agencyId}
                )"""

        log.info "Deleted all BED_ASSIGNMENT_HISTORIES for agencyId ${agencyId} (${sql.updateCount})"

        /* OFFENDER_BOOKINGS has an 'after insert' trigger that creates an OFFENDER_BOOKING_DETIALS record.
          * This must be removed before the corresponding OFFENDER_BOOKINGS record
          */
        sql.execute """\
          DELETE FROM OFFENDER_BOOKING_DETAILS
                WHERE OFFENDER_BOOK_ID IN (
                          SELECT OFFENDER_BOOK_ID
                            FROM OFFENDER_BOOKINGS OB 
                           WHERE OB.AGY_LOC_ID = ${agencyId}
                )"""

        log.info "Deleted all OFFENDER_BOOKING_DETAILS for agencyId ${agencyId} (${sql.updateCount})"

        sql.execute """\
          DELETE FROM OFFENDER_RELEASE_DETAILS
                WHERE OFFENDER_BOOK_ID IN (
                          SELECT OFFENDER_BOOK_ID
                            FROM OFFENDER_BOOKINGS OB 
                           WHERE OB.AGY_LOC_ID = ${agencyId})"""

        log.info "Deleted all OFFENDER_RELEASE_DETAILS related to agencyId ${agencyId}  (${sql.updateCount})"


        sql.execute "DELETE from OFFENDER_BOOKINGS WHERE AGY_LOC_ID = ${agencyId}"

        log.info "Deleted all OFFENDER_BOOKINGS for agencyId ${agencyId} (${sql.updateCount})"
    }

    private long nextId() {
        sql.firstRow("SELECT OFFENDER_BOOK_ID.nextval as nextId from dual").nextId
    }

}
