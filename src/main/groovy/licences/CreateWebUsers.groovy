package licences

class CreateWebUsers extends WebUserActions {

    private static final String PASSWORD = 'password123456'
    private static final String EMAIL_DOMAIN = 'digital.justice.gov.uk'

    static void main(String[] args) {
        new CreateWebUsers().run()
    }

    @Override
    void run() {
        sql.withTransaction {
            webUserRequirements.forEach { req ->
                webUser.ensureWebUser(req.username, PASSWORD, req.username, req.username, "${req.username}@${EMAIL_DOMAIN}", [req.caseload], req.roleCode)
            }
        }
    }
}
