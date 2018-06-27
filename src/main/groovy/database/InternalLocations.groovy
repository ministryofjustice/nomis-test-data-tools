package database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j

@Slf4j
class InternalLocations {

    public static final String WING = 'WING'
    public static final String LANDING = 'LAND'
    public static final String CELL = 'CELL'

    private final Sql sql
    private final SqlHelper helper

    InternalLocations(Sql sql) {
        this.sql = sql
        this.helper = new SqlHelper(sql)
    }

    void create(String code, String type, String agencyId, String description, Long parentId, String userDescription) {
        sql.executeInsert """
          INSERT INTO AGENCY_INTERNAL_LOCATIONS (INTERNAL_LOCATION_ID, INTERNAL_LOCATION_CODE, INTERNAL_LOCATION_TYPE, 
                                                 AGY_LOC_ID, DESCRIPTION, PARENT_INTERNAL_LOCATION_ID, USER_DESC) 
                                         VALUES (INTERNAL_LOCATION_ID.NEXTVAL, ${code}, ${type}, ${agencyId}, ${description}, ${parentId}, ${userDescription})"""
    }

    boolean exists(String agencyId, String description) {
        helper.exists "SELECT COUNT(*) FROM AGENCY_INTERNAL_LOCATIONS WHERE AGY_LOC_ID = ${agencyId} AND DESCRIPTION = ${description}"
    }

    void delete(String agencyId, String description) {
        sql.execute "DELETE FROM AGENCY_INTERNAL_LOCATIONS WHERE AGY_LOC_ID = ${agencyId} AND DESCRIPTION = ${description}"
    }

    void delete(long id) {
        sql.execute "DELETE FROM AGENCY_INTERNAL_LOCATIONS WHERE INTERNAL_LOCATION_ID = ${id}"
    }

    void ensure(String code, String type, String agencyId, String description, Long parentId, String userDescription) {
        if (!exists(agencyId, description)) {
            log.info "Creating AGENCY_INTERNAL_LOCATIONS ${agencyId} ${description}"
            create code, type, agencyId, description, parentId, userDescription
        } else {
            log.info "AGENCY_INTERNAL_LOCATIONS ${agencyId} ${description} exists. Skipping."
        }
    }

    InternalLocation ensureWing(String agencyId, String code) {
        def description = InternalLocation.buildDescription(agencyId, code)
        def userDescription = "Wing ${code}"
        ensure(code, WING, agencyId, description, null, userDescription)
        return find(agencyId, description)
    }

    InternalLocation ensureLanding(InternalLocation parent, String code) {
        def description = InternalLocation.buildDescription(parent.description, code)
        ensure(code, LANDING, parent.agencyId, description, parent.id, null)
        find(parent.agencyId, description)
    }

    InternalLocation ensureCell(InternalLocation parent, int cell) {
        def code = InternalLocation.code(cell)
        def description = InternalLocation.buildDescription(parent.description, code)
        ensure(code, CELL, parent.agencyId, description, parent.id, null)
        find(parent.agencyId, description)
    }

    InternalLocation find(String agencyId, String description) {
//        log.info("Looking for internal location '${description} in Agency ${agencyId}")
        GroovyRowResult row = sql.firstRow"SELECT INTERNAL_LOCATION_ID id, PARENT_INTERNAL_LOCATION_ID parentId FROM AGENCY_INTERNAL_LOCATIONS WHERE AGY_LOC_ID = ${agencyId} AND DESCRIPTION = ${description}"
        new InternalLocation(id: row.id, parentId: row.parentId, agencyId: agencyId, description: description)
    }

    List<InternalLocation> find(String agencyId) {
        List<GroovyRowResult> rows = sql.rows("SELECT INTERNAL_LOCATION_ID id, PARENT_INTERNAL_LOCATION_ID parentId, AGY_LOC_ID agencyId, DESCRIPTION FROM AGENCY_INTERNAL_LOCATIONS WHERE AGY_LOC_ID = ${agencyId}")
        rows.collect { r -> new InternalLocation(id: r.id, parentId: r.parentId, agencyId: r.agencyId, description: r.description) }
    }

    void tearDown(String agencyId) {
        List<InternalLocation> locations = find(agencyId)
        locations.sort{ -it.id } // assumes ascending id by creation order. Only works if an ORACLE sequence is used...
        log.info("Deleting ${locations.size()} INTERNAL_LOCATIONS for agencyId ${agencyId}")
        locations.forEach{ il ->
            log.info "Deleting INTERNAL_LOCATION ${il.description}"
            delete il.id
        }
    }
}
