package licences

class CreateWebUsers extends WebUserActions {

    private static final String PASSWORD = 'password123456'
    private static final String EMAIL_DOMAIN = 'digital.justice.gov.uk'

    static void main(String[] args) {
        new CreateWebUsers().run()
    }

    @Override
    void run() {
        roles.forEach{
            webUser.ensureUser(usernameForRole(it), PASSWORD, it, it, "${it}@${EMAIL_DOMAIN}", AGENCY, it)
        }
    }
}
