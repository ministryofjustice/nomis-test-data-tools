package licences

import database.Database
import database.InternalLocations
import database.OffenderBookings
import database.OffenderCurfews
import database.OffenderSentCalculations
import database.Offenders
import database.Prisons
import database.SentenceTerms
import groovy.sql.Sql

abstract class DatabaseActions implements Runnable {

    protected final Sql sql
    protected final Prisons prisons
    protected final InternalLocations internalLocations
    protected final Offenders offenders
    protected final OffenderBookings offenderBookings
    protected final SentenceTerms sentenceTerms
    protected final OffenderSentCalculations offenderSentCalculations
    protected final OffenderCurfews offenderCurfews

    DatabaseActions() {
        this.sql = Database.instance().sql()
        this.prisons = new Prisons(sql)
        this.internalLocations = new InternalLocations(sql)
        this.offenders = new Offenders(sql)
        this.offenderBookings = new OffenderBookings(sql, offenders, internalLocations)
        this.sentenceTerms = new SentenceTerms(sql)
        this.offenderSentCalculations = new OffenderSentCalculations(sql)
        this.offenderCurfews = new OffenderCurfews(sql)
    }
}
