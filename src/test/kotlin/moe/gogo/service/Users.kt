package moe.gogo.service

import moe.gogo.entity.User
import moe.gogo.nextString
import kotlin.random.Random

class Users private constructor(val auth: AuthService) {

    companion object {
        fun create(auth: AuthService) = Users(auth)

        val userStore = mutableMapOf<User, Users>()
    }

    private val passwords = mutableMapOf<String, String>()

    suspend fun createUser(username: String): User {
        val password = Random.nextString()
        passwords[username] = password
        auth.addUser(username, password)
        return auth.authUser(username, password).also {
            userStore[it] = this
        }
    }

    suspend fun updateUser(user: User): User {
        val username = user.username
        val password = passwords[username]
        return auth.authUser(username, password!!)
    }

}

suspend fun User.update(): User {
    return Users.userStore[this]?.updateUser(this)!!
}