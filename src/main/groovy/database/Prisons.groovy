package database

import groovy.sql.Sql
import groovy.util.logging.Slf4j

import static database.SqlHelper.flag

@Slf4j
class Prisons {
    private final Sql sql
    private final SqlHelper helper

    Prisons(Sql sql) {
        this.sql = sql
        this.helper = new SqlHelper(sql)
    }

    void createAgencyLocation(String id, String description, String type, Boolean active) {
        sql.executeInsert """
                INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
                                      VALUES (${id}, ${description}, ${type}, ${flag(active)})"""
    }

    void createCaseload(String caseloadId, String description, String type, int listSeq, boolean trustAccounts, boolean accessProperty, boolean active, String function) {
        sql.executeInsert """
            INSERT INTO CASELOADS (CASELOAD_ID, DESCRIPTION, CASELOAD_TYPE, LIST_SEQ, TRUST_ACCOUNTS_FLAG, 
                                   ACCESS_PROPERTY_FLAG, ACTIVE_FLAG, CASELOAD_FUNCTION) 
                           VALUES (${caseloadId}, ${description}, ${type}, ${listSeq}, ${flag(trustAccounts)}, ${flag(accessProperty)}, ${flag(active)}, ${function})"""
    }

    void createCaseloadAgencyLocation(String caseloadId, String agencyLocationId) {
        sql.executeInsert"INSERT INTO CASELOAD_AGENCY_LOCATIONS (CASELOAD_ID, AGY_LOC_ID) VALUES (${caseloadId}, ${agencyLocationId})"
    }

    void deleteAgencyLocation(String agencyLocationId){
        log.info "Deleting AGENCY_LOCATIONS ${agencyLocationId}"
        sql.execute"DELETE FROM AGENCY_LOCATIONS WHERE AGY_LOC_ID = ${agencyLocationId}"
    }

    void deleteCaseload(String caseloadId){
        log.info "Deleting CASELOADS ${caseloadId}"
        sql.execute "DELETE FROM CASELOADS WHERE CASELOAD_ID = ${caseloadId}"
    }
    void deleteCaseloadAgencyLocation(String caseloadId, String agencyLocationId) {
        log.info "Deleting CASELOAD_AGENCY_LOCATIONS (${caseloadId}, ${agencyLocationId})"
        sql.execute "DELETE FROM CASELOAD_AGENCY_LOCATIONS WHERE CASELOAD_ID = ${caseloadId} AND AGY_LOC_ID = ${agencyLocationId}"
    }

    void createAgencyAndCaseload(String id, String description) {
        createAgencyLocation(id, description, 'INST', true)
        createCaseload(id, description, 'INST', 500, true, true, true, 'GENERAL')
        createCaseloadAgencyLocation(id, id)
    }

    void deleteAgencyAndCaseload(String id) {
        deleteCaseloadAgencyLocation(id, id)
        deleteCaseload id
        deleteAgencyLocation id
    }

    boolean agencyExists(String agencyId) {
        helper.exists"SELECT COUNT(*) FROM AGENCY_LOCATIONS WHERE AGY_LOC_ID = ${agencyId}"
    }

    boolean caseloadExists(String caseloadId) {
        helper.exists"SELECT COUNT(*) FROM CASELOADS WHERE CASELOAD_ID = ${caseloadId}"
    }

    boolean caseloadAgencyLocationExists(String caseloadId, String agencyId) {
        helper.exists"SELECT COUNT(*) from CASELOAD_AGENCY_LOCATIONS WHERE CASELOAD_ID = ${caseloadId} AND AGY_LOC_ID = ${agencyId}"
    }

    void ensureAgencyLocation(String id, String description, String type, Boolean active) {
        if (!agencyExists(id)) {
            log.info "Creating AGENCY_LOCATIONS for ${id}"
            createAgencyLocation(id, description, type, active)
        } else {
            log.info "AGENCY_LOCATIONS ${id} exists"
        }
    }

    void ensureCaseload(String caseloadId, String description, String type, int listSeq, boolean trustAccounts, boolean accessProperty, boolean active, String function) {
        if (!caseloadExists(caseloadId)) {
            log.info "Creating CASELOADS ${caseloadId}"
            createCaseload(caseloadId,description, type, listSeq, trustAccounts, accessProperty, active, function)
        } else {
            log.info "CASELOADS ${caseloadId} exists"
        }
    }

    void ensureCaseloadAgencyLocation(String caseloadId, String agencyId) {
        if (!caseloadAgencyLocationExists(caseloadId, agencyId)) {
            log.info "Creating CASELOAD_AGENCY_LOCATIONS (${caseloadId} , ${agencyId})"
            createCaseloadAgencyLocation(caseloadId, agencyId)
        } else {
            log.info "CASELOAD_AGENCY_LOCATIONS (${caseloadId}, ${agencyId}) exists"
        }
    }

    void ensureAgencyAndCaseload(String id, String description) {
        ensureAgencyLocation(id, description, 'INST', true)
        ensureCaseload(id, description, 'INST', 500, true, false, true, 'GENERAL')
        ensureCaseloadAgencyLocation(id, id)
    }
}
