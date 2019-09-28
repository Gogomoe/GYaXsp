package moe.gogo.service

import io.vertx.core.Context
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jdbc.JDBCAuth
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.ext.auth.authenticateAwait
import io.vertx.kotlin.ext.sql.*
import kotlinx.coroutines.withTimeout
import moe.gogo.CoroutineService


class AuthService(val dbClient: JDBCClient, val auth: JDBCAuth, context: Context) : CoroutineService(context) {

    private val log = LoggerFactory.getLogger(AuthService::class.java)

    override suspend fun start() {
        createTables()
    }

    override fun route(router: Router) {

        router.post("/users").coroutineHandler(::handleSinup)

        router.post("/session").coroutineHandler(::handleLogin)

    }

    private suspend fun handleSinup(context: RoutingContext) {

        val params = context.request().formAttributes()
        val username = params.get("username")
        val password = params.get("password")

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            context.fail(400, "Username or password is empty")
            return
        }

        if (username.length < 4 || password.length < 4) {
            context.fail(400, "Username or password is too short")
            return
        }

        val sqlParams = JsonArray().add(username)
        val resultSet = dbClient.queryWithParamsAwait("""SELECT * FROM user WHERE username = ?""", sqlParams)
        if (resultSet.results.isNotEmpty()) {
            context.fail(400, "Username has already existed")
            return
        }

        val salt = auth.generateSalt()
        val hash = auth.computeHash(password, salt)

        dbClient.updateWithParamsAwait("""INSERT INTO user VALUES (?, ?, ?)""", json { array(username, hash, salt) })
        context.success()

    }

    private suspend fun handleLogin(context: RoutingContext) {

        val params = context.request().formAttributes()
        val username = params.get("username")
        val password = params.get("password")

        if (username == null || password == null) {
            context.fail(400, "No username or password provided in form")
            return
        }

        val authInfo = JsonObject().put("username", username).put("password", password)

        try {
            val user = auth.authenticateAwait(authInfo)
            context.setUser(user)
            context.session()?.regenerateId()

            context.success()

        } catch (e: Throwable) {
            if (e.message != "Invalid username/password") {
                log.error("Unknown error at AuthService::handleLogin", e)
            }
            context.fail(403, e.message ?: "Unknown")
        }
    }

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


        withTimeout(5000) {
            val connection = dbClient.getConnectionAwait()

            val result = connection.querySingleAwait(
                """
                SELECT COUNT(*) FROM information_schema.TABLES WHERE
                 TABLE_NAME = 'user' or
                 TABLE_NAME = 'user_roles' or 
                 TABLE_NAME = 'roles_perms'
                """.trimIndent()
            )!!
            if (result.getInteger(0) == 3) {
                return@withTimeout
            } else {
                log.info("rebuild tables")
                connection.executeAwait("""DROP TABLE IF EXISTS user,user_roles,roles_perms""")
                sql.forEach { connection.executeAwait(it) }
            }
        }

    }


}