package database

class Roles {

    private Database database
    Roles(Database database) {
        this.database = database
    }

    void insertRole(String roleName, int roleSeq, String roleCode, String parentRoleCode, String roleType, String roleFunction, String systemDataFlag) {
        database.sql().executeInsert("""
        INSERT INTO OMS_ROLES (ROLE_ID, ROLE_NAME, ROLE_SEQ, ROLE_CODE, PARENT_ROLE_CODE, ROLE_TYPE, ROLE_FUNCTION, SYSTEM_DATA_FLAG)
        VALUES (ROLE_ID.NEXTVAL, ${roleName}, ${roleSeq}, ${roleCode}, ${parentRoleCode}, ${roleType}, ${roleFunction}, ${systemDataFlag})""".stripIndent())
    }

    void deleteRole(String roleCode) {
        database.sql().execute "DELETE FROM OMS_ROLES WHERE ROLE_CODE = ${roleCode}"
    }
}
