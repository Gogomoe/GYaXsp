package moe.gogo.service

import io.vertx.core.Context
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jdbc.JDBCAuth
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.ext.auth.authenticateAwait
import io.vertx.kotlin.ext.sql.*
import kotlinx.coroutines.withTimeoutOrNull
import moe.gogo.CoroutineService
import moe.gogo.ServiceException
import moe.gogo.ServiceRegistry

class AuthServiceImpl(context: Context) : CoroutineService(context), AuthService {

    private val log = LoggerFactory.getLogger(AuthServiceImpl::class.java)

    lateinit var dbClient: JDBCClient
    lateinit var auth: JDBCAuth

    override suspend fun start(registry: ServiceRegistry) {
        dbClient = registry[DatabaseService::class.java].client()
        auth = JDBCAuth.create(registry.vertx(), dbClient)
        createTables()
    }

    override fun route(router: Router) {
        router.post("/session").coroutineHandler(::handleLogin)
        router.post("/users").coroutineHandler(::handleSinup)
    }

    private suspend fun handleLogin(context: RoutingContext) {

        val params = context.request().formAttributes()
        val username = params.get("username")
        val password = params.get("password")

        try {
            val user = getUser(username, password)
            context.setUser(user)
            context.session()?.regenerateId()

            context.success()

        } catch (e: ServiceException) {
            context.fail(400, e.message ?: "Unknown")
        }
    }

    private suspend fun handleSinup(context: RoutingContext) {

        val params = context.request().formAttributes()
        val username = params.get("username")
        val password = params.get("password")

        try {
            addUser(username, password)
            context.success()
        } catch (e: ServiceException) {
            context.fail(400, e.message ?: "Unknown")
        }
    }

    override suspend fun getUser(username: String?, password: String?): User {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw ServiceException("Username or password is empty")
        }

        val authInfo = JsonObject().put("username", username).put("password", password)

        try {
            return auth.authenticateAwait(authInfo)
        } catch (e: Throwable) {
            if (e.message == "Invalid username/password") {
                throw ServiceException("Invalid username/password", e)
            }
            throw e
        }
    }

    override suspend fun addUser(username: String?, password: String?) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw ServiceException("Username or password is empty")
        }

        if (username.length < 4 || password.length < 4) {
            throw ServiceException("Username or password is too short")
        }

        val sqlParams = JsonArray().add(username)
        val resultSet = dbClient.queryWithParamsAwait("""SELECT * FROM user WHERE username = ?""", sqlParams)
        if (resultSet.results.isNotEmpty()) {
            throw ServiceException("Username has already existed")
        }

        val salt = auth.generateSalt()
        val hash = auth.computeHash(password, salt)

        dbClient.updateWithParamsAwait("""INSERT INTO user VALUES (?, ?, ?)""", json { array(username, hash, salt) })

    }

    override suspend fun givePermission(roleName: String, permission: String) {
        dbClient.updateWithParamsAwait(
            """INSERT INTO roles_perms VALUES (?, ?)""",
            json { array(roleName, permission) })
    }

    override suspend fun removePermission(roleName: String, permission: String) {
        dbClient.updateWithParamsAwait(
            """DELETE FROM roles_perms WHERE role = '?' AND perm = '?'""",
            json { array(roleName, permission) })
    }

    override suspend fun giveRole(user: User, roleName: String) {
        dbClient.updateWithParamsAwait(
            """INSERT INTO user_roles VALUES (?, ?)""",
            json { array(user.username, roleName) })
    }

    override suspend fun removeRole(user: User, roleName: String) {
        dbClient.updateWithParamsAwait(
            """DELETE FROM user_roles HERE username = '?' AND role = '?'""",
            json { array(user.username, roleName) })
    }

    private val User.username: Any?
        get() = this.principal().getString("username")

    private suspend fun createTables() {

        val sql = listOf(
            """
            CREATE TABLE IF NOT EXISTS user (
             username VARCHAR(255) NOT NULL,
             password VARCHAR(255) NOT NULL,
             password_salt VARCHAR(255) NOT NULL
            );
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS user_roles (
             username VARCHAR(255) NOT NULL,
             role VARCHAR(255) NOT NULL
            );
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS roles_perms (
             role VARCHAR(255) NOT NULL,
             perm VARCHAR(255) NOT NULL
            );
            """.trimIndent(),
            """ALTER TABLE user ADD CONSTRAINT pk_username PRIMARY KEY (username);""",
            """ALTER TABLE user_roles ADD CONSTRAINT pk_user_roles PRIMARY KEY (username, role);""",
            """ALTER TABLE roles_perms ADD CONSTRAINT pk_roles_perms PRIMARY KEY (role);""",
            """ALTER TABLE user_roles ADD CONSTRAINT fk_username FOREIGN KEY (username) REFERENCES user(username);""",
            """ALTER TABLE user_roles ADD CONSTRAINT fk_roles FOREIGN KEY (role) REFERENCES roles_perms(role);"""
        )

        withTimeoutOrNull(5000) {
            dbClient.getConnectionAwait().use { connection ->
                val result = connection.querySingleAwait(
                    """
                    SELECT COUNT(*) FROM information_schema.TABLES WHERE
                     TABLE_NAME = 'user' or
                     TABLE_NAME = 'user_roles' or 
                     TABLE_NAME = 'roles_perms'
                    """.trimIndent()
                )!!
                if (result.getInteger(0) == 3) {
                    return@withTimeoutOrNull
                } else {
                    log.info("rebuild tables")
                    connection.executeAwait("""DROP TABLE IF EXISTS user,user_roles,roles_perms""")
                    sql.forEach { connection.executeAwait(it) }
                }
            }
        } ?: throw RuntimeException("AuthService setup time out")

    }

}