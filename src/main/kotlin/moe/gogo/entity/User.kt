package moe.gogo.entity

import io.vertx.kotlin.ext.auth.isAuthorizedAwait
import java.lang.IllegalStateException

typealias UserAuth = io.vertx.ext.auth.User

data class User(
    val username: String,
    val avatar: String?,
    val auth: UserAuth?,
    val roles: List<String>,
    val permissions: List<String>
) {
    suspend fun isAuthorizedAwait(authority: String): Boolean =
        auth?.isAuthorizedAwait(authority) ?: throw IllegalStateException("Cannot use no auth user to authorize")
}
