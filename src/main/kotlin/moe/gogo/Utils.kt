package moe.gogo

import io.vertx.ext.web.RoutingContext
import moe.gogo.entity.User
import moe.gogo.entity.UserAuth
import moe.gogo.service.AuthService
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

fun LocalDateTime.toInstant(): Instant = this.atZone(ZoneOffset.systemDefault()).toInstant()

fun Instant.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())

val UserAuth.username: String
    get() = this.principal().getString("username")!!

fun RoutingContext.getUser(): User? = this.session().get("user")

suspend fun RoutingContext.updateUser(): User? {
    val user = this.getUser() ?: return null
    val registry: ServiceRegistry = this.get("ServiceRegistry")
    val service = registry[AuthService::class.java]

    val updated = service.getUser(user.username, user.auth)
    this.session().put("user", updated)
    return updated
}