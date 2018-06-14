package database

import groovy.sql.Sql
import groovy.util.logging.Slf4j

import java.time.LocalDate

@Slf4j
class WebUser {
    private static final List<String> STANDARD_ROLES = ['100', '202', '962']

    private static final String WEB_USER_CASELOAD = 'NWEB'

    private Sql sql
    private SqlHelper sqlHelper

    WebUser(Sql sql) {
        this.sql = sql
        this.sqlHelper = new SqlHelper(sql)
    }

    /**
     *
     * @param username the Oracle and Nomis username
     * @param password
     * @param firstName
     * @param lastName
     * @param emailAddress
     * @param caseloadIds List of caseLoad Ids, First entry is the working caseload
     * @param roleCode The role to be assigned to the user.
     */
    void ensureWebUser(String username, String password, String firstName, String lastName, String emailAddress, List<String> caseloadIds, String roleCode) {
        ensureOracleUser(username, password)
        ensureStaffAccount(username, firstName, lastName, emailAddress, caseloadIds[0])
        ensureUserCaseloadRoles(username, caseloadIds)
        ensureUserAccessibleCaseload(username, WEB_USER_CASELOAD)
        ensureUserCaseloadRole(username, WEB_USER_CASELOAD, roleCode)
    }


    void ensureOracleUser(String username, String password) {
        if (!userExists(username)) {
            log.info "Create User ${username}"
            sql.execute "CREATE USER ${username} IDENTIFIED BY ${password}".toString()
        }

        log.info "Configuring GRANTs for ${username}"

        sql.execute "ALTER USER ${username} GRANT CONNECT THROUGH API_PROXY_USER".toString()
        sql.execute "GRANT CREATE SESSION TO ${username}".toString()
        sql.execute "GRANT TAG_USER TO  ${username}".toString()
        sql.execute "ALTER USER ${username} DEFAULT ROLE TAG_USER".toString()
        sql.execute "ALTER USER ${username} PROFILE TAG_GENERAL".toString()
        sql.execute "GRANT CREATE SESSION TO  ${username}".toString()
        sql.execute "GRANT TAG_USER TO  ${username}".toString()
        sql.execute "GRANT TAG_RO TO  ${username}".toString()
        sql.execute "GRANT CONNECT TO  ${username}".toString()
        sql.execute "ALTER USER ${username} DEFAULT ROLE TAG_RO".toString()
    }

    boolean userExists(String username) {
        sqlHelper.exists "SELECT count(*) FROM DBA_USERS where USERNAME = ${username}"
    }

    boolean usernameHasAccount(String username) {
        sqlHelper.exists "select count(*) from STAFF_USER_ACCOUNTS WHERE USERNAME = ${username}"
    }

    void ensureStaffAccount(String username, String firstName, String lastName, String emailAddress, String workingCaseloadId) {

        if (usernameHasAccount(username)) {
            log.info "Found STAFF_USER_ACCOUNTS for ${username}. Reassigning workingCaseload to ${workingCaseloadId}."
            sql.execute "UPDATE STAFF_USER_ACCOUNTS SET WORKING_CASELOAD_ID = ${workingCaseloadId} WHERE USERNAME = ${username}"
            return
        }

        long staffId = sql.firstRow("SELECT STAFF_ID.NEXTVAL as staffId FROM DUAL").staffId

        log.info "Creating STAFF_MEMBER for staffId ${staffId}"

        sql.execute """\
            INSERT INTO STAFF_MEMBERS (
                STAFF_ID, 
                LAST_NAME, 
                FIRST_NAME, 
                BIRTHDATE,
                UPDATE_ALLOWED_FLAG, 
                SUSPENDED_FLAG, 
                AS_OF_DATE,
                ROLE, 
                SEX_CODE, 
                STATUS, 
                SUSPENSION_DATE, 
                SUSPENSION_REASON, 
                FORCE_PASSWORD_CHANGE_FLAG,
                LAST_PASSWORD_CHANGE_DATE, 
                LICENSE_CODE, 
                LICENSE_EXPIRY_DATE, 
                CREATE_DATETIME,
                CREATE_USER_ID, 
                MODIFY_DATETIME, 
                MODIFY_USER_ID, 
                TITLE, 
                NAME_SEQUENCE, 
                QUEUE_CLUSTER_ID,
                AUDIT_TIMESTAMP, 
                AUDIT_USER_ID, 
                AUDIT_MODULE_NAME, 
                AUDIT_CLIENT_USER_ID,
                AUDIT_CLIENT_IP_ADDRESS, 
                AUDIT_CLIENT_WORKSTATION_NAME, 
                AUDIT_ADDITIONAL_INFO,
                FIRST_LOGON_FLAG, 
                SIGNIFICANT_DATE, 
                SIGNIFICANT_NAME, 
                NATIONAL_INSURANCE_NUMBER)
            VALUES (
                ${staffId}, 
                ${lastName.toUpperCase()}, 
                ${firstName.toUpperCase()},
                ${SqlHelper.toSqlDate(LocalDate.of(1980, 1, 1))},
                'Y', 
                'N',
                sysdate, 
                NULL, 
                'M', 
                'ACTIVE', 
                NULL, 
                'CA', 
                'N', 
                NULL, 
                NULL, 
                NULL,
                sysdate, 
                'SYSCON_ADM', 
                sysdate, 
                'OMS_OWNER', 
                NULL, 
                NULL, 
                2,
                sysdate, 
                'OMS_OWNER', 
                'JDBC Thin Client', 
                'mick', 
                '10.200.3.14', 
                'unknown', 
                NULL, 
                'N', 
                NULL,
                NULL, 
                NULL)"""

        long internetAddressId = sql.firstRow("SELECT INTERNET_ADDRESS_ID.NEXTVAL as internetAddressId FROM DUAL").internetAddressId

        log.info "Creating INTERNET_ADDRESSES for staffId ${staffId}"

        sql.execute """\
          INSERT INTO INTERNET_ADDRESSES (
              INTERNET_ADDRESS_ID, 
              OWNER_ID, 
              OWNER_CLASS, 
              INTERNET_ADDRESS_CLASS, 
              INTERNET_ADDRESS)
        VALUES (
            ${internetAddressId}, 
            ${staffId}, 
            'STF', 
            'EMAIL', 
            ${emailAddress})"""

        log.info "Creating STAFF_USER_ACCOUNTS for ${username}, staffId ${staffId}, workingCaseloadId ${workingCaseloadId}"
        sql.execute """\
            INSERT INTO STAFF_USER_ACCOUNTS (
            USERNAME, 
            STAFF_ID, 
            STAFF_USER_TYPE, 
            ID_SOURCE,
            WORKING_CASELOAD_ID, 
            CREATE_DATETIME,
            CREATE_USER_ID, 
            MODIFY_DATETIME, 
            MODIFY_USER_ID, 
            AUDIT_TIMESTAMP,
            AUDIT_USER_ID, 
            AUDIT_MODULE_NAME, 
            AUDIT_CLIENT_USER_ID, 
            AUDIT_CLIENT_IP_ADDRESS,
            AUDIT_CLIENT_WORKSTATION_NAME, 
            AUDIT_ADDITIONAL_INFO)
        VALUES (
            ${username},
            ${staffId},
            'GENERAL', 
            'USER',
            ${workingCaseloadId}, 
            sysdate, 
            'SYSCON_ADM', 
            sysdate, 
            ${username},
            sysdate, 
            ${username}, 
            'frmweb@weblg01.syscon.ca (TNS V1-V3)', 
            'skadubur', 
            '10.200.2.11', 
            'SVAGGA-E4310', 
            NULL)"""
    }

    void ensureUserCaseloadRoles(String username, List<String> caseloadIds) {

        caseloadIds.forEach { caseloadId ->
            ensureUserAccessibleCaseload(username, caseloadId)

            STANDARD_ROLES.forEach { roleCode -> ensureUserCaseloadRole(username, caseloadId, roleCode)
            }
        }
    }

    void ensureUserAccessibleCaseload(String username, String caseloadId) {
        if (!userAccessibleCaseloadExists(username, caseloadId)) {
            log.info "Creating USER_ACCESSIBLE_CASELOADS for ${username}, caseloadId ${caseloadId}"

            sql.execute """\
                    INSERT INTO USER_ACCESSIBLE_CASELOADS (
                        CASELOAD_ID, 
                        USERNAME, 
                        START_DATE, 
                        CREATE_DATETIME, 
                        CREATE_USER_ID, 
                        MODIFY_DATETIME, 
                        MODIFY_USER_ID, 
                        AUDIT_TIMESTAMP, 
                        AUDIT_USER_ID, 
                        AUDIT_MODULE_NAME,
                        AUDIT_CLIENT_USER_ID, 
                        AUDIT_CLIENT_IP_ADDRESS, 
                        AUDIT_CLIENT_WORKSTATION_NAME)
                    VALUES (
                        ${caseloadId}, 
                        ${username}, 
                        sysdate, 
                        sysdate, 
                        'SYSCON_ADM', 
                        NULL, 
                        NULL, 
                        sysdate, 
                        'SYSCON_ADM',
                        'OUUUSERS',
                        'JHickinbotham',
                        '10.200.1.42',
                        'Sheffield')"""
        }
    }

    boolean userAccessibleCaseloadExists(String username, String caseloadId) {
        sqlHelper.exists "select count(*) from USER_ACCESSIBLE_CASELOADS where CASELOAD_ID = ${caseloadId} AND USERNAME = ${username}"
    }

    void ensureUserCaseloadRole(String username, String caseloadId, String roleCode) {
        long roleId = findRoleId(roleCode)
        if (!userCaseloadRoleExists(username, caseloadId, roleId)) {
            log.info "Creating USER_CASELOAD_ROLE for ${username}, ${caseloadId}, ${roleCode} (${roleId})"
            sql.execute """\
                    INSERT INTO USER_CASELOAD_ROLES (
                        ROLE_ID, 
                        USERNAME, 
                        CASELOAD_ID, 
                        CREATE_DATETIME, 
                        CREATE_USER_ID,
                        MODIFY_DATETIME, 
                        MODIFY_USER_ID, AUDIT_TIMESTAMP, 
                        AUDIT_USER_ID, 
                        AUDIT_MODULE_NAME,
                        AUDIT_CLIENT_USER_ID, 
                        AUDIT_CLIENT_IP_ADDRESS, 
                        AUDIT_CLIENT_WORKSTATION_NAME )
                    VALUES (
                        ${roleId}, 
                        ${username}, 
                        ${caseloadId}, 
                        sysdate, 
                        'SYSCON_ADM', 
                        NULL, 
                        NULL,  
                        sysdate, 
                        'SYSCON_ADM', 
                        'OUUUSERS', 
                        'TRichardson', 
                        '10.200.3.3', 
                        'trevlt')"""
        }
    }

    boolean userCaseloadRoleExists(String username, String caseloadId, long roleId) {
        sqlHelper.exists "select count(*) from USER_CASELOAD_ROLES WHERE ROLE_ID = ${roleId} AND USERNAME = ${username} AND CASELOAD_ID = ${caseloadId}"
    }

    long findRoleId(String roleCode) {
        sql.firstRow("select ROLE_ID as roleId from OMS_ROLES WHERE ROLE_CODE = ${roleCode}").roleId
    }

    void deleteWebUser(String username) {
        log.info("Deleting user ${username}")
        sql.execute "DELETE FROM USER_CASELOAD_ROLES where USERNAME = ${username}"
        sql.execute "DELETE FROM USER_ACCESSIBLE_CASELOADS where USERNAME = ${username}"
        def staffIds = sql.rows("SELECT STAFF_ID FROM STAFF_USER_ACCOUNTS WHERE USERNAME = ${username}").collect {
            it.STAFF_ID
        }
        sql.execute "DELETE FROM STAFF_USER_ACCOUNTS where USERNAME = ${username}"
        staffIds.forEach { staffId ->
            log.info "Deleting from STAFF_LOCATION_ROLES for staffId ${staffId}"
            sql.execute "DELETE FROM STAFF_LOCATION_ROLES where SAC_STAFF_ID = ${staffId}"
            log.info "Deleting INTERNET_ADDRESSES and STAFF_MEMBERS for staffId ${staffId}"
            sql.execute "DELETE FROM INTERNET_ADDRESSES where OWNER_ID = ${staffId}"
            sql.execute "DELETE FROM STAFF_MEMBERS where STAFF_ID = ${staffId}"
        }
        if (sqlHelper.exists("SELECT COUNT(*) from DBA_USERS where USERNAME = ${username}")) {
            sql.execute "DROP USER ${username} CASCADE".toString()
        } else {
            log.info "The Oracle user ${username} wasn't found."
        }
    }
}
