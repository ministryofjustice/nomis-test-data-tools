package database

import groovy.sql.Sql
import groovy.util.logging.Slf4j

import java.time.LocalDate

import static database.SqlHelper.toSqlDate

@Slf4j
class OffenderSentCalculations {

    private final Sql sql
    private final OffenderCurfews offenderCurfews

    OffenderSentCalculations(Sql sql) {
        this.sql = sql
        this.offenderCurfews = new OffenderCurfews(sql)
    }

    def createSentenceCalculations(
            long bookingId,
            LocalDate calculationDate,

            LocalDate hdcedCalculatedDate,
            LocalDate ardCalculatedDate,
            LocalDate crdCalculatedDate,
            LocalDate ledCalculatedDate,
            LocalDate sedCalculatedDate,
            LocalDate tusedCalculatedDate,

            LocalDate hdccedOverridedDate,
            LocalDate ardOverridedDate,
            LocalDate crdOverridedDate,
            LocalDate ledOverridedDate,
            LocalDate sedOverridedDate,
            LocalDate tusedOverridedDate

            ) {

        log.info "Creating OFFENDER_SENT_CALCULATIONS for bookingId ${bookingId} calculationDate ${calculationDate}"

        sql.executeInsert """\
            INSERT INTO OFFENDER_SENT_CALCULATIONS (
                OFFENDER_SENT_CALCULATION_ID, 
                OFFENDER_BOOK_ID, 
                CALCULATION_DATE, 

                HDCED_CALCULATED_DATE, 
                ARD_CALCULATED_DATE,
                CRD_CALCULATED_DATE,
                LED_CALCULATED_DATE,
                SED_CALCULATED_DATE,
                TUSED_CALCULATED_DATE,
                
                HDCED_OVERRIDED_DATE, 
                ARD_OVERRIDED_DATE,
                CRD_OVERRIDED_DATE,
                LED_OVERRIDED_DATE,
                SED_OVERRIDED_DATE,
                TUSED_OVERRIDED_DATE,

                CALC_REASON_CODE)
            VALUES (
                OFFENDER_SENT_CALCULATION_ID.NEXTVAL, 
                ${bookingId}, 

                ${toSqlDate(calculationDate)},

                ${toSqlDate(hdcedCalculatedDate)},
                ${toSqlDate(ardCalculatedDate)},
                ${toSqlDate(crdCalculatedDate)},
                ${toSqlDate(ledCalculatedDate)},
                ${toSqlDate(sedCalculatedDate)},
                ${toSqlDate(tusedCalculatedDate)},

                ${toSqlDate(hdccedOverridedDate)},
                ${toSqlDate(ardOverridedDate)},
                ${toSqlDate(crdOverridedDate)},
                ${toSqlDate(ledOverridedDate)},
                ${toSqlDate(sedOverridedDate)},
                ${toSqlDate(tusedOverridedDate)},
                
                'NEW')"""
    }

    void tearDown(String agencyId) {
        // Creating an OFFENDER_SENT_CACLUATIONS record fires a trigger that creates an OFFENDER_CURFEWS record (and further children)
        offenderCurfews.tearDown agencyId

        log.info "Deleting all HDC_CALC_EXCLUSION_REASONS for agencyId ${agencyId}"

        sql.execute """\
            DELETE FROM HDC_CALC_EXCLUSION_REASONS
                  WHERE OFFENDER_BOOK_ID IN (
                      SELECT OFFENDER_BOOK_ID 
                        FROM OFFENDER_BOOKINGS
                       WHERE AGY_LOC_ID = ${agencyId}
                  )"""

        log.info "Deleting all OFFENDER_SENT_CALCULATIONS for agencyId ${agencyId}"

        sql.execute """\
            DELETE FROM OFFENDER_SENT_CALCULATIONS
                  WHERE OFFENDER_BOOK_ID IN (
                      SELECT OFFENDER_BOOK_ID 
                        FROM OFFENDER_BOOKINGS
                       WHERE AGY_LOC_ID = ${agencyId}
                  )"""
    }
}
