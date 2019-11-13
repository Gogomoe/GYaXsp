package moe.gogo.service

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.jdbc.JDBCAuth
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.auth.authenticateAwait
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import moe.gogo.ServiceException
import moe.gogo.ServiceRegistry
import moe.gogo.entity.User
import moe.gogo.entity.UserAuth

class AuthServiceImpl : AuthService {

    lateinit var dbClient: JDBCClient
    lateinit var auth: JDBCAuth

    override suspend fun start(registry: ServiceRegistry) {
        dbClient = registry[DatabaseService::class.java].client()
        auth = JDBCAuth.create(registry.vertx(), dbClient)
    }

    override fun auth(): AuthProvider = auth

    override suspend fun authUser(username: String, password: String): User {

        val authInfo = JsonObject().put("username", username).put("password", password)

        try {

            val userAuth = auth.authenticateAwait(authInfo)
            return getUser(username, userAuth)

        } catch (e: Throwable) {
            if (e.message == "Invalid username/password") {
                throw ServiceException("Invalid username/password", e)
            }
            throw e
        }
    }

    override suspend fun getUser(username: String, auth: UserAuth?): User {

        val userInfo = dbClient.querySingleWithParamsAwait(
            """SELECT * FROM user_info WHERE username = ?""",
            jsonArrayOf(username)
        ) ?: throw ServiceException("User does not exist")

        val rolePerms = dbClient.queryWithParamsAwait(
            """SELECT UR.role, RP.perm FROM roles_perms RP RIGHT JOIN user_roles UR ON UR.role = RP.role WHERE UR.username = ?""",
            jsonArrayOf(username)
        )
        val roles = rolePerms.results.map { it.getString(0) }.distinct().filterNotNull()
        val perms = rolePerms.results.map { it.getString(1) }.distinct().filterNotNull()

        return User(
            username,
            userInfo.getString(1),
            auth,
            roles,
            perms
        )
    }

    override suspend fun addUser(username: String, password: String) {

        val sqlParams = JsonArray().add(username)
        val resultSet = dbClient.queryWithParamsAwait("""SELECT * FROM user WHERE username = ?""", sqlParams)
        if (resultSet.results.isNotEmpty()) {
            throw ServiceException("Username has already existed")
        }

        val salt = auth.generateSalt()
        val hash = auth.computeHash(password, salt)

        dbClient.updateWithParamsAwait("""INSERT INTO user VALUES (?, ?, ?)""", json { array(username, hash, salt) })
        dbClient.updateWithParamsAwait("""INSERT INTO user_info VALUES (?, NULL)""", jsonArrayOf(username))

    }

    override suspend fun givePermission(roleName: String, permission: String) {
        dbClient.updateWithParamsAwait(
            """INSERT INTO roles_perms VALUES (?, ?)""",
            json { array(roleName, permission) })
    }

    override suspend fun removePermission(roleName: String, permission: String) {
        dbClient.updateWithParamsAwait(
            """DELETE FROM roles_perms WHERE role = ? AND perm = ?""",
            json { array(roleName, permission) })
    }

    override suspend fun giveRole(user: User, roleName: String) {
        dbClient.updateWithParamsAwait(
            """INSERT INTO user_roles VALUES (?, ?)""",
            json { array(user.username, roleName) })
    }

    override suspend fun removeRole(user: User, roleName: String) {
        dbClient.updateWithParamsAwait(
            """DELETE FROM user_roles WHERE username = ? AND role = ?""",
            json { array(user.username, roleName) })
    }

}