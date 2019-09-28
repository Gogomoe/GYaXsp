package moe.gogo.service

import io.vertx.ext.auth.User
import moe.gogo.Service

interface AuthService : Service {

    suspend fun getUser(username: String?, password: String?): User

    suspend fun addUser(username: String?, password: String?)

}