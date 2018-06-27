package database

import groovy.sql.Sql
import groovy.util.logging.Slf4j

import java.time.LocalDate

import static database.SqlHelper.toSqlDate

@Slf4j
class OffenderCurfews {

    enum ApprovalStatus {
        APPROVED, INACTIVE, OPT_OUT, REJECTED
    }

    private final Sql sql

    OffenderCurfews(Sql sql) {
        this.sql = sql
    }

    void create(long bookingId, LocalDate eligibilityDate, LocalDate assessmentDate, LocalDate ardCrdDate, ApprovalStatus approvalStatus) {

        log.info "Creating OFFENDER_CURFEWS for bookingId ${bookingId}"

        sql.executeInsert """\
          INSERT INTO OFFENDER_CURFEWS (
            OFFENDER_BOOK_ID, 
            OFFENDER_CURFEW_ID, 
            ELIGIBILITY_DATE, 
            ASSESSMENT_DATE, 
            ARD_CRD_DATE, 
            APPROVAL_STATUS) 
          VALUES (
              ${bookingId},
              OFFENDER_CURFEW_ID.nextval,
              ${toSqlDate(eligibilityDate)},
              ${toSqlDate(assessmentDate)},
              ${toSqlDate(ardCrdDate)},
              ${approvalStatus?.toString()}
          )"""
    }

    void tearDown(String agencyId) {

        log.info "Deleting all OFFENDER_CURFEWS (and HDC_STATUS_REASONS, HDC_STATUS_TRACKINGS) for agencyId ${agencyId}"

        sql.execute """\
          DELETE FROM HDC_STATUS_REASONS
                WHERE HDC_STATUS_TRACKING_ID IN (
                    SELECT hst.HDC_STATUS_TRACKING_ID
                      FROM HDC_STATUS_TRACKINGS hst
                           JOIN OFFENDER_CURFEWS oc on hst.OFFENDER_CURFEW_ID = oc.OFFENDER_CURFEW_ID
                           JOIN OFFENDER_BOOKINGS ob on oc.OFFENDER_BOOK_ID = ob.OFFENDER_BOOK_ID
                     WHERE ob.AGY_LOC_ID = ${agencyId}
                )"""

        long hdcStatusReasonsCount = sql.updateCount

        sql.execute """\
          DELETE FROM HDC_STATUS_TRACKINGS
                WHERE OFFENDER_CURFEW_ID IN (
                  SELECT OFFENDER_CURFEW_ID
                    FROM OFFENDER_CURFEWS oc
                         JOIN OFFENDER_BOOKINGS ob on oc.OFFENDER_BOOK_ID = ob.OFFENDER_BOOK_ID
                   WHERE ob.AGY_LOC_ID = ${agencyId}
                )"""

        long hdcStatusTrackingCount = sql.updateCount

        sql.execute """\
            DELETE FROM OFFENDER_CURFEWS 
                  WHERE OFFENDER_BOOK_ID IN (
                            SELECT OB.OFFENDER_BOOK_ID 
                              FROM OFFENDER_BOOKINGS OB 
                             WHERE OB.AGY_LOC_ID = ${agencyId}
                        )"""
        log.info "Deleted: OFFENDER_CURFEWS (${sql.updateCount}), HDC_STATUS_REASONS (${hdcStatusReasonsCount}), HDC_STATUS_TRACKINGS (${hdcStatusTrackingCount})"
    }
}
