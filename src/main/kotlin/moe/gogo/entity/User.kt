package moe.gogo.entity

import io.vertx.kotlin.ext.auth.isAuthorizedAwait

typealias UserAuth = io.vertx.ext.auth.User

data class User(
    val username: String,
    val avatar: String?,
    val auth: UserAuth,
    val roles: List<String>,
    val permissions: List<String>
) {
    suspend fun isAuthorizedAwait(authority: String): Boolean = auth.isAuthorizedAwait(authority)
}
