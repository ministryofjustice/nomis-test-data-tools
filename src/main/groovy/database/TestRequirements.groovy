package database

/**
 * A handy source of Requirements in Reader form
 */
class TestRequirements {

    private static final String SPECIFICATION_RESOURCE_PATH = '/Test Licence Offenders.csv'

    static Reader reader() {
        new TestRequirements().readerM()
    }

    Reader readerM() {
        def uri = getClass().getResource(SPECIFICATION_RESOURCE_PATH).toURI()
        new File(uri).newReader()
    }

}
