package database

import groovy.transform.Immutable

@Immutable
class WebUserRequirement {
    String username
    String caseload
    String roleCode
}
