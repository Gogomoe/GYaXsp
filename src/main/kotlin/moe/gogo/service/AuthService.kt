package moe.gogo.service

import io.vertx.ext.auth.AuthProvider
import moe.gogo.Service
import moe.gogo.entity.User
import moe.gogo.entity.UserAuth

interface AuthService : Service {

    fun auth(): AuthProvider

    suspend fun getUser(username: String, password: String): User

    suspend fun getUser(auth: UserAuth): User

    suspend fun addUser(username: String, password: String)

    suspend fun givePermission(roleName: String, permission: String)

    suspend fun removePermission(roleName: String, permission: String)

    suspend fun giveRole(user: User, roleName: String)

    suspend fun removeRole(user: User, roleName: String)

}