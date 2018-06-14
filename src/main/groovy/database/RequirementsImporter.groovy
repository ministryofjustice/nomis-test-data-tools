package database

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

import static com.xlson.groovycsv.CsvParser.parseCsv

class RequirementsImporter {

    private static final DateTimeFormatter LONG_DATE_FORMATTER = DateTimeFormatter.ofPattern('dd/MM/yyyy', Locale.UK)
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern('dd/MM/yy', Locale.UK)

    private static final String SHOWN = 'Shown in our app'
    private static final String NOMIS_ID = 'NOMIS ID'
    private static final String FIRST_NAME = 'First Name'
    private static final String LAST_NAME = 'Last Name'
    private static final String DATE_OF_BIRTH = 'DOB'
    private static final String ALIAS_NAME = 'Alias Name'
    private static final String AGENCY = 'Agency'
    private static final String CELL = 'Cell'
    private static final String RO = 'Assigned RO'
    private static final String DELIUS_USERNAME = 'Delius Username'
    private static final String DELIUS_NOMIS_STAFF_ID = 'Delius Nomis Staff ID'
    private static final String RO_FIRST_NAME = 'RO Firstname'
    private static final String RO_LAST_NAME = 'RO Last Name'
    private static final String HDC_OFFENCE_TYPE = 'HDC Offence Type'
    private static final String SENTENCE_START = 'Sentence Start'
    private static final String INITIAL_HDCED = 'Initial HDCED'
    private static final String INITIAL_CRD_ARD = 'Initial CRD/ARD'
    private static final String INITIAL_LED = 'Initial LED'
    private static final String INITIAL_SED = 'Initial SED'
    private static final String INITIAL_TUSED = 'Initial TUSED'
    private static final String SENTENCE_2_START = 'Sentence 2 Start Date'
    private static final String SENTENCE_2_END = 'Sentence 2 End Date'
    private static final String EFFECTIVE_HDCED = 'Effective HDCED'
    private static final String EFFECTIVE_ARD_CRD = 'Effective ARD/CRD'
    private static final String EFFECTIVE_LED = 'Effective LED'
    private static final String EFFECTIVE_SED = 'Effective SED'
    private static final String EFFECTIVE_TUSED = 'Effective TUSED'
    private static final String DECISION_IN_NOMIS = 'Decision In NOMIS'
    private static final String SENTENCE_DETAILS = 'Sentence Details'
    private static final String SQL = 'SQL'

    static final COLUMN_KEYS = [SHOWN, NOMIS_ID, FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, ALIAS_NAME, AGENCY, CELL, RO,
                         DELIUS_USERNAME, DELIUS_NOMIS_STAFF_ID, RO_FIRST_NAME, RO_LAST_NAME, HDC_OFFENCE_TYPE,
                         SENTENCE_START, INITIAL_HDCED, INITIAL_CRD_ARD, INITIAL_LED, INITIAL_SED, INITIAL_TUSED,
                         SENTENCE_2_START, SENTENCE_2_END, EFFECTIVE_HDCED, EFFECTIVE_ARD_CRD, EFFECTIVE_LED,
                         EFFECTIVE_LED, EFFECTIVE_SED, EFFECTIVE_TUSED, DECISION_IN_NOMIS, SENTENCE_DETAILS, SQL ]

    static List<Map<String, String>> lines(Reader reader) {
        Iterator input = parseCsv(reader)
        input.toList().collect{ it.toMap() } as List<Map<String, String>>
    }

    static List<Requirement> read(Reader reader) {
        fromLines(lines(reader))
    }

    private static List<Requirement> fromLines(List<Map<String, String>> lines) {
        lines.collect {line -> new Requirement(
                nomisId:           line[NOMIS_ID],
                firstName:         line[FIRST_NAME],
                lastName:          line[LAST_NAME],
                dateOfBirth:       fromDateString(line[DATE_OF_BIRTH]),
                aliasName:         line[ALIAS_NAME],
                agency:            line[AGENCY],
                cell:              line[CELL],
                sentenceStart:     fromDateString(line[SENTENCE_START]),

                initialHdced:      fromDateString(line[INITIAL_HDCED]),
                initialCrd:        fromDateString(line[INITIAL_CRD_ARD]),
                initialLed:        fromDateString(line[INITIAL_LED]),
                initialSed:        fromDateString(line[INITIAL_SED]),
                initialTused:      fromDateString(line[INITIAL_TUSED]),

                overridedHdced:    fromDateString(line[EFFECTIVE_HDCED]),
                overridedCrd:      fromDateString(line[EFFECTIVE_ARD_CRD]),
                overridedLed:      fromDateString(line[EFFECTIVE_LED]),
                overridedSed:      fromDateString(line[EFFECTIVE_SED]),
                overridedTused:    fromDateString(line[EFFECTIVE_TUSED]),
                decisionInNomis:   line[DECISION_IN_NOMIS],

                deliusLink: new DeliusLink(
                    firstName:      line[RO_FIRST_NAME],
                    lastName:       line[RO_LAST_NAME],
                    deliusUsername: line[DELIUS_USERNAME],
                    nomisUsername:  line[DELIUS_NOMIS_STAFF_ID]
                )
        ) }
    }

    static fromDateString(String dateString) {
        try {
            return fromLongDateString(dateString)
        } catch(DateTimeParseException e) {
            return fromShortDateString(dateString)
        }
    }

    private static LocalDate fromLongDateString(String dateString) {
        dateString ? LocalDate.parse(dateString.trim(), LONG_DATE_FORMATTER) : null
    }

    private static LocalDate fromShortDateString(String dateString) {
        dateString ? LocalDate.parse(dateString.trim(), SHORT_DATE_FORMATTER) : null
    }
}
