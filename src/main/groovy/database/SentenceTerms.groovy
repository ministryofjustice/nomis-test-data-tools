package database

import groovy.sql.Sql
import groovy.util.logging.Slf4j

import java.time.LocalDate

import static database.SqlHelper.toSqlDate

@Slf4j
class SentenceTerms {
    private final Sql sql

    SentenceTerms(Sql sql) {
        this.sql = sql
    }

    private long createOffenderCase(long bookingId, LocalDate sentenceStart) {
        long caseId = nextCaseId()

        log.info("Creating OFFENDER_CASES for bookingId ${bookingId}, sentenceStart ${sentenceStart}")

        sql.executeInsert """\
          INSERT INTO OFFENDER_CASES (
            CASE_ID, 
            OFFENDER_BOOK_ID, 
            CASE_TYPE, 
            CASE_STATUS, 
            BEGIN_DATE, 
            AGY_LOC_ID,  
            STATUS_UPDATE_REASON, 
            LIDS_CASE_NUMBER, 
            CASE_SEQ)
          VALUES (
            ${caseId}, 
            ${bookingId}, 
            'A', 
            'A', 
            ${toSqlDate(sentenceStart.minusDays(10))}, 
            'COURT1', 
            'A', 
            ${caseId}, 
            1)"""

        caseId
    }

    private long createOrder(long bookingId, long caseId, LocalDate sentenceStart) {
        long orderId = nextOrderId()

        log.info("Creating ORDERS for bookingId ${bookingId}, caseId ${caseId}, sentenceStart ${sentenceStart}")

        sql.executeInsert """\
          INSERT INTO ORDERS (
            ORDER_ID, 
            OFFENDER_BOOK_ID, 
            CASE_ID, 
            COURT_DATE, 
            ORDER_TYPE, 
            ISSUING_AGY_LOC_ID, 
            ORDER_STATUS, 
            NON_REPORT_FLAG)
          VALUES (
            ${orderId}, 
            ${bookingId}, 
            ${caseId}, 
            ${toSqlDate(sentenceStart.minusDays(10))},
            'AUTO', 
            'COURT1', 
            'A', 
            'N')"""

        orderId
    }

    private def createOffenderSentence(long bookingId, long orderId, long caseId, LocalDate sentenceStart, LocalDate crdDate) {

        LocalDate endDate = sentenceStart.plusDays(200)

        log.info("Creating OFFENDER_SENTENCES for bookingId ${bookingId}, orderId ${orderId}, caseId ${caseId}, sentenceStart ${sentenceStart}, crdDate ${crdDate}")

        sql.executeInsert """\
          INSERT INTO OFFENDER_SENTENCES (
            OFFENDER_BOOK_ID, 
            SENTENCE_SEQ, 
            ORDER_ID, 
            SENTENCE_CALC_TYPE, 
            SENTENCE_STATUS, 
            START_DATE, 
            END_DATE, 
            CASE_ID,
            ARD_CALCULATED_DATE, 
            SED_CALCULATED_DATE, 
            SENTENCE_CATEGORY, 
            FINE_AMOUNT, 
            SENTENCE_LEVEL,
            DISCHARGE_DATE, 
            STATUS_UPDATE_REASON, 
            LINE_SEQ)
          VALUES (
            ${bookingId}, 
            1, 
            ${orderId}, 
            'R', 
            'A', 
            ${toSqlDate(sentenceStart)}, 
            ${toSqlDate(endDate)}, 
            ${caseId},
            ${toSqlDate(crdDate)}, 
            ${toSqlDate(crdDate)}, 
            'LICENCE', 
            120.00, 
            'IND',
            ${toSqlDate(endDate)}, 
            'A', 
            1)"""
    }

    private def createOffenderCharge(long bookingId, long caseId) {

        log.info("Creating OFFENDER_CHARGES for bookingId ${bookingId}, caseId ${caseId}")

        sql.executeInsert """\
          INSERT INTO OFFENDER_CHARGES (
            OFFENDER_CHARGE_ID, 
            OFFENDER_BOOK_ID, 
            STATUTE_CODE, 
            OFFENCE_CODE, 
            CHARGE_STATUS, 
            MOST_SERIOUS_FLAG, 
            CASE_ID)
          VALUES (
            OFFENDER_CHARGE_ID.NEXTVAL, 
            ${bookingId}, 
            'RV98', 
            'RV98011', 
            'A', 
            'Y', 
            ${caseId})"""
    }

    private def createOffenderSentenceTerms(long bookingId, LocalDate sentenceStart){

        log.info("Creating OFFENDER_SENTENCE_TERMS for bookingId ${bookingId}, sentenceStart ${sentenceStart}")

        sql.executeInsert """\
          INSERT INTO OFFENDER_SENTENCE_TERMS (
            OFFENDER_BOOK_ID, 
            SENTENCE_SEQ, 
            TERM_SEQ, 
            SENTENCE_TERM_CODE, 
            YEARS, 
            MONTHS, 
            START_DATE, 
            END_DATE, 
            LIFE_SENTENCE_FLAG)
          VALUES (
            ${bookingId}, 
            NVL((SELECT MAX(SENTENCE_SEQ) FROM OFFENDER_SENTENCE_TERMS WHERE OFFENDER_BOOK_ID = ${bookingId}),0) + 1,
            1, 
            'IMP', 
            1, 
            0, 
            ${toSqlDate(sentenceStart)},
            ${toSqlDate(sentenceStart.plusDays(200))}, 
            'N')"""
    }

    void createSentenceTerms(long bookingId, LocalDate sentenceStart, LocalDate crdDate) {
        long caseId = createOffenderCase(bookingId, sentenceStart)
        long orderId = createOrder(bookingId, caseId, sentenceStart)
        createOffenderSentence(bookingId, orderId, caseId, sentenceStart, crdDate)
        createOffenderCharge(bookingId, caseId)
        createOffenderSentenceTerms(bookingId, sentenceStart)
    }

    void deleteSentenceTerms(long bookingId) {
        log.info("Deleting from OFFENDER_SENTENCE_TERMS, OFFENDER_CHARGES, OFFENDER_SENTENCES, ORDERS and OFFENDER_CASES where OFFENDER_BOOK_ID = ${bookingId}")

        sql.execute "delete from OFFENDER_SENTENCE_TERMS where OFFENDER_BOOK_ID = ${bookingId}"
        sql.execute "delete from OFFENDER_CHARGES where OFFENDER_BOOK_ID = ${bookingId}"
        sql.execute "delete from OFFENDER_SENTENCES where OFFENDER_BOOK_ID = ${bookingId}"
        sql.execute "delete from ORDERS where OFFENDER_BOOK_ID = ${bookingId}"
        sql.execute "delete from OFFENDER_CASES where OFFENDER_BOOK_ID = ${bookingId}"
    }

    void tearDown(String agencyId) {

        log.info("Deleting all OFFENDER_SENTENCE_TERMS for agencyId ${agencyId}")

        sql.execute """\
          delete from OFFENDER_SENTENCE_TERMS 
                where OFFENDER_BOOK_ID IN (
                          select ob.OFFENDER_BOOK_ID
                            from OFFENDER_BOOKINGS ob 
                           where ob.AGY_LOC_ID = ${agencyId}
                )"""

        log.info("Deleting all OFFENDER_SENTENCE_ADJUSTS for agencyId ${agencyId}")

        sql.execute """\
          delete from OFFENDER_SENTENCE_ADJUSTS 
                where OFFENDER_BOOK_ID IN (
                          select ob.OFFENDER_BOOK_ID
                            from OFFENDER_BOOKINGS ob 
                           where ob.AGY_LOC_ID = ${agencyId}
                )"""

        log.info("Deleting all OFFENDER_SENTENCES for agencyId ${agencyId}")

        sql.execute """\
          delete from OFFENDER_SENTENCES 
                where OFFENDER_BOOK_ID IN (
                          select ob.OFFENDER_BOOK_ID
                            from OFFENDER_BOOKINGS ob 
                           where ob.AGY_LOC_ID = ${agencyId}
                )"""

        log.info("Deleting all COURT_EVENT_CHARGES for agencyId ${agencyId}")

        sql.execute """\
          delete from COURT_EVENT_CHARGES  
                where OFFENDER_CHARGE_ID IN (
                          select oc.OFFENDER_CHARGE_ID
                            from OFFENDER_CHARGES oc
                                 join OFFENDER_BOOKINGS ob on oc.OFFENDER_BOOK_ID = ob.OFFENDER_BOOK_ID
                           where ob.AGY_LOC_ID = ${agencyId}
                )"""

        log.info("Deleting all OFFENDER_SENTENCE_CHARGES for agencyId ${agencyId}")

        sql.execute """\
          delete from OFFENDER_SENTENCE_CHARGES 
                where OFFENDER_BOOK_ID IN (
                          select ob.OFFENDER_BOOK_ID
                            from OFFENDER_BOOKINGS ob 
                           where ob.AGY_LOC_ID = ${agencyId}
                )"""

        log.info("Deleting all OFFENDER_CHARGES for agencyId ${agencyId}")

        sql.execute """\
          delete from OFFENDER_CHARGES 
                where OFFENDER_BOOK_ID IN (
                          select ob.OFFENDER_BOOK_ID
                            from OFFENDER_BOOKINGS ob 
                           where ob.AGY_LOC_ID = ${agencyId}
                )"""

        log.info("Deleting all ORDERS for agencyId ${agencyId}")

        sql.execute """\
            DELETE from ORDERS 
                  WHERE OFFENDER_BOOK_ID IN(
                          SELECT OFFENDER_BOOK_ID
                            FROM OFFENDER_BOOKINGS ob
                           WHERE ob.AGY_LOC_ID = ${agencyId}
                      )"""

        log.info("Deleting all OFFENDER_CASES for agencyId ${agencyId}")

        sql.execute """\
            delete from OFFENDER_CASES
                  where OFFENDER_BOOK_ID IN (
                          select ob.OFFENDER_BOOK_ID
                            from OFFENDER_BOOKINGS ob 
                           where ob.AGY_LOC_ID = ${agencyId}
                  )"""
    }

    private long nextCaseId() {
        sql.firstRow("SELECT CASE_ID.nextval as caseId from DUAL").caseId
    }

    private long nextOrderId() {
        sql.firstRow("SELECT ORDER_ID.nextval as orderId from DUAL").orderId
    }
}
