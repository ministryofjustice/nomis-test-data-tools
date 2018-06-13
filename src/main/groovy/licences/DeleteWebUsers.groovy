package licences

import groovy.util.logging.Slf4j

@Slf4j
class DeleteWebUsers extends WebUserActions {

    static void main(String[] args) {
        new DeleteWebUsers().run()
    }

    @Override
    void run() {
        roles.forEach {
            def username = usernameForRole(it)
            if (webUser.userExists(username)) {
                webUser.deleteUser(username)
            } else {
                log.info("User ${username} not found. Skipping.")
            }
        }
    }
}
