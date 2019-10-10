package moe.gogo

import io.vertx.ext.auth.User
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

fun LocalDateTime.toInstant(): Instant = this.atZone(ZoneOffset.systemDefault()).toInstant()

fun Instant.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())

val User.username: String
    get() = this.principal().getString("username")!!