package database

import groovy.transform.Immutable

@Immutable
class InternalLocation {
    long id
    Long parentId
    String agencyId
    String description

    static buildDescription(String parent, String code) {
        "${parent}-${code}".toString()
    }

    static String code(int code) {
        String.format('%03d', code)
    }
}
