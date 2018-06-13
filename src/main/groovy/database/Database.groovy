package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import javax.sql.DataSource

@Slf4j
class Database {

    private static Map DB_PBELL = [
            url: 'jdbc:oracle:thin:@syscon-dev.cuo8tj8nysnb.eu-west-2.rds.amazonaws.com:1521:TESTDB',
            username: 'PBELL_NOMIS_OWNER',
            password: 'PBELL_NOMIS_OWNER',
            schema: 'PBELL_NOMIS_OWNER']

    private static Map DB_DEV = [
            url: 'jdbc:oracle:thin:@10.200.97.40:1521:pvb_dev',
            username: 'OMS_OWNER',
            password: 'OMS_OWNER',
            schema: 'OMS_OWNER']

    private static instance = new Database()

    private DataSource ds

    static Database instance() {
        instance
    }

    private Database() {
//        configure(DB_PBELL)
        configure(DB_DEV)
    }

    private void configure(Map args) {
        try {
            HikariConfig config = new HikariConfig()
            config.setJdbcUrl(args.url)
            config.setUsername(args.username)
            config.setPassword(args.password)
            config.setSchema(args.schema)
            config.setMaximumPoolSize(1)
            config.setAutoCommit(true)
            ds = new HikariDataSource(config)
        } catch (Throwable t) {
            log.error("Failed to configure datasource", t)
        }
    }

    Sql sql() {
        new Sql(ds)
    }

    static boolean ping() {
        try {
            def row = instance.sql().firstRow('select 1 as good from DUAL')
            return row.good == 1
        } catch (Throwable t) {
            log.error("Bad ping", t)
            false;
        }
    }
}
