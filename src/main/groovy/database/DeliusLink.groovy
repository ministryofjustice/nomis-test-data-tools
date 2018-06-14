package database

import groovy.transform.Immutable

/**
 * The relationship between a Delius user and a NOMIS user.
 */
@Immutable
class DeliusLink {
    String firstName
    String lastName
    String deliusUsername
    String nomisUsername

}
