package licences

import groovy.util.logging.Slf4j

@Slf4j
class DeleteWebUsers extends WebUserActions {

    static void main(String[] args) {
        new DeleteWebUsers().run()
    }

    @Override
    void run() {
        sql.withTransaction {
            webUserRequirements.forEach { req ->
                if (webUser.userExists(req.username)) {
                    webUser.deleteWebUser req.username
                } else {
                    log.info "User ${req.username} Not Found."
                }
            }
        }
    }
}
