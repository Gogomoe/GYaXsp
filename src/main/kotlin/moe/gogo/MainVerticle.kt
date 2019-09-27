package moe.gogo

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject
import moe.gogo.verticle.WebServerVerticle

class MainVerticle : AbstractVerticle() {

    override fun start() {

        val dbConfig = JsonObject(this::class.java.classLoader.getResource("db_config.json")?.readText())

        val config = JsonObject().put("db_config", dbConfig)
        val option = DeploymentOptions().setConfig(config)

        vertx.deployVerticle(WebServerVerticle(), option).setHandler {
            if (it.failed()) {
                it.cause().printStackTrace()
            }
        }
    }

}