package moe.gogo

import io.vertx.core.Vertx

fun main() {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(MainVerticle())
}