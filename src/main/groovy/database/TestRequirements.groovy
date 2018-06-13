package database

/**
 * A handy source of Requirements in Reader form
 */
class TestRequirements {

    private static final SPECIFICATION_RESOURCE_PATH = '/Test Licence Offenders.csv'

    static Reader requirementsReader() {
        def uri = getClass().getResource(SPECIFICATION_RESOURCE_PATH).toURI()
        new File(uri).newReader()
    }
}
