package moe.gogo

import io.vertx.ext.web.RoutingContext
import moe.gogo.entity.User
import moe.gogo.entity.UserAuth
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

fun LocalDateTime.toInstant(): Instant = this.atZone(ZoneOffset.systemDefault()).toInstant()

fun Instant.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())

val UserAuth.username: String
    get() = this.principal().getString("username")!!

fun RoutingContext.getUser(): User? = this.session().get("user")