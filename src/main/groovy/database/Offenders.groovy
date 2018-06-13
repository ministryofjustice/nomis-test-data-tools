package database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import java.time.LocalDate

@Slf4j
class Offenders {
    private final Sql sql
    private final SqlHelper helper

    Offenders(Sql sql) {
        this.sql = sql
        this.helper = new SqlHelper(sql)
    }

    long create(String nomisId, String firstName, String lastName, LocalDate dateOfBirth, Long rootOffenderId = null) {

        long offenderId = nextId()

        log.info("Creating OFFENDERS with OFFENDER_ID ${offenderId}, OFFENDER_ID_DISPLAY ${nomisId} and ROOT_OFFENDER_ID ${rootOffenderId}")

        def sqlDateOfBirth = dateOfBirth == null ? null : java.sql.Date.valueOf(dateOfBirth)

        sql.executeInsert """
            INSERT INTO OFFENDERS(
                OFFENDER_ID, 
                OFFENDER_ID_DISPLAY, 
                FIRST_NAME, 
                LAST_NAME, 
                BIRTH_DATE, 
                ROOT_OFFENDER_ID, 
                SEX_CODE, 
                LAST_NAME_KEY, 
                RACE_CODE,
                ID_SOURCE_CODE,
                ALIAS_NAME_TYPE,
                CREATE_DATE)
           VALUES (
                ${offenderId}, 
                ${nomisId.toUpperCase()}, 
                ${firstName?.toUpperCase()}, 
                ${lastName?.toUpperCase()}, 
                ${sqlDateOfBirth}, 
                ${rootOffenderId ?: offenderId}, 
                'M', 
                ${lastName}, 
                'W1',
                'SEQ',
                'CN',
                SYSDATE
            )"""

        offenderId
    }

    long ensure(String nomisId, String firstName, String lastName, LocalDate dateOfBirth, Long rootOffenderId = null) {
        List<Offender> offenders = find(nomisId)
        if (offenders.size() > 0) {
            Offender rootOffender = offenders.find { it.id == it.rootId }
            if (rootOffender) {
                return rootOffender.id
            }
            throw new Exception("Found OFFENDERS records for Nomis ID ${nomisId}, but no root offender record found")
        }
        create(nomisId, firstName, lastName, dateOfBirth)
    }

    long createAlias(String nomisId, String firstName, String lastName) {
        Offender rootOffender = findRoot(nomisId);
        create(nomisId, firstName, lastName, rootOffender.dateOfBirth, rootOffender.id)
    }

    private long nextId() {
        sql.firstRow("SELECT OFFENDER_ID.nextval as nextId from dual").nextId
    }

    void delete(long offenderId) {
        sql.execute("DELETE FROM OFFENDERS WHERE OFFENDER_ID = ${offenderId}")
        log.info("Deleted OFFENDERS with id ${offenderId} (${sql.updateCount})")
    }

    void delete(String nomisId) {
        if (hasChildren(nomisId)) {
            log.info "Not deleting OFFENDERS with nomisId ${nomisId}. Prevented by OFFENDER_BOOKINGS children."
            return
        }
        sql.execute("DELETE FROM OFFENDERS WHERE OFFENDER_ID_DISPLAY = ${nomisId}")
        log.info("Deleted OFFENDERS with nomisId ${nomisId} (${sql.updateCount})")

    }

    boolean hasChildren(String nomisId) {
        sql.firstRow("""\
            SELECT COUNT(*) as rowCount
              FROM OFFENDER_BOOKINGS ob
                   JOIN OFFENDERS o on ob.OFFENDER_ID = o.OFFENDER_ID
             WHERE o.OFFENDER_ID_DISPLAY = ${nomisId}""").rowCount > 0
    }

    boolean exists(long offenderId) {
        helper.exists "SELECT COUNT(*) as rowCount FROM OFFENDERS WHERE OFFENDER_ID = ${offenderId}"
    }

    boolean exists(String nomisId) {
        helper.exists "SELECT COUNT(*) as rowCount FROM OFFENDERS WHERE OFFENDER_ID_DISPLAY = ${nomisId}"
    }

    boolean exists(String nomisId, String firstName, String lastName) {
        helper.exists """\
            SELECT COUNT(*) as rowCount 
              FROM OFFENDERS 
             WHERE OFFENDER_ID_DISPLAY = ${nomisId} AND
                   FIRST_NAME = ${firstName?.toUpperCase()} AND
                   LAST_NAME = ${lastName?.toUpperCase()}
"""
    }


    long offenderIdFromNomisId(String nomisId) {
        sql.firstRow("SELECT OFFENDER_ID as nomisId FROM OFFENDERS WHERE OFFENDER_ID_DISPLAY = ${nomisId} AND OFFENDER_ID = ROOT_OFFENDER_ID").nomisId
    }

    Offender findRoot(String nomisId) {
        GroovyRowResult row = sql.firstRow"""
            SELECT OFFENDER_ID, 
                   ROOT_OFFENDER_ID,
                   FIRST_NAME, 
                   LAST_NAME,
                   BIRTH_DATE
              FROM OFFENDERS 
             WHERE OFFENDER_ID_DISPLAY = ${nomisId} AND OFFENDER_ID = ROOT_OFFENDER_ID"""

        fromRow row, nomisId
    }

    List<Offender> find(String nomisId) {
        List<GroovyRowResult> rows = sql.rows """
            SELECT OFFENDER_ID, 
                   ROOT_OFFENDER_ID,
                   FIRST_NAME, 
                   LAST_NAME,
                   BIRTH_DATE
              FROM OFFENDERS 
             WHERE OFFENDER_ID_DISPLAY = ${nomisId}"""

        rows.collect{ fromRow it, nomisId }
    }

    private static Offender fromRow(GroovyRowResult row, String nomisId) {
        LocalDate dateOfBirth = row.BIRTH_DATE == null ? null : row.BIRTH_DATE.toLocalDateTime().toLocalDate()

        new Offender(
                id: row.OFFENDER_ID,
                rootId: row.ROOT_OFFENDER_ID,
                nomisId: nomisId,
                firstName: row.FIRST_NAME,
                lastName: row.LAST_NAME,
                dateOfBirth: dateOfBirth )
    }
}
