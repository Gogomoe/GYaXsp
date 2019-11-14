package moe.gogo.service

import io.vertx.ext.auth.AuthProvider
import moe.gogo.Service
import moe.gogo.entity.User
import moe.gogo.entity.UserAuth

interface AuthService : Service {

    fun auth(): AuthProvider

    suspend fun authUser(username: String, password: String): User

    suspend fun getUser(username: String, auth: UserAuth? = null): User

    suspend fun addUser(username: String, password: String)

    suspend fun givePermission(roleName: String, permission: String)

    suspend fun removePermission(roleName: String, permission: String)

    suspend fun removePermissionForAll(permission: String)

    suspend fun giveRole(user: User, roleName: String)

    suspend fun removeRole(user: User, roleName: String)

    suspend fun removeRoleForAll(roleName: String)

}