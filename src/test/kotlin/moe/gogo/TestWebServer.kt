package moe.gogo

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.closeAwait
import io.vertx.kotlin.core.deployVerticleAwait
import moe.gogo.verticle.WebServerVerticle

object TestWebServer {

    lateinit var vertx: Vertx

    suspend fun start() {
        vertx = Vertx.vertx()

        val dbConfig = JsonObject(this::class.java.classLoader.getResource("db_config.json")?.readText())

        val config = JsonObject().put("db_config", dbConfig)
        val option = DeploymentOptions().setConfig(config)

        vertx.deployVerticleAwait(WebServerVerticle(), option)
    }

    suspend fun stop() {
        vertx.closeAwait()
    }

}