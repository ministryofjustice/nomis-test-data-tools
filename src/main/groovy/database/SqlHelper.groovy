package database

import groovy.sql.Sql

import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

class SqlHelper {
    private final Sql sql

    SqlHelper(Sql sql) {
        this.sql = sql
    }

    static String flag(boolean flag) {
        flag ? 'Y' : 'N'
    }

    static java.sql.Date toSqlDate(LocalDate localDate) {
        localDate == null ? null : java.sql.Date.valueOf(localDate)
    }

    static Timestamp toTimestamp(LocalDateTime localDateTime) {
        localDateTime == null ? null : Timestamp.valueOf(localDateTime)
    }

    boolean exists(GString query) {
        def result = sql.firstRow(query)
        result[0] > 0
    }
}
