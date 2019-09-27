package moe.gogo.verticle

import io.vertx.ext.auth.jdbc.JDBCAuth
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import moe.gogo.service.AuthService

class APIServerVerticle : CoroutineVerticle() {

    lateinit var restAPI: Router

    override suspend fun start() = coroutineScope {

        val dbConfig = config.getJsonObject("db_config")
        val dbClient = JDBCClient.createShared(vertx, dbConfig)

        val auth = JDBCAuth.create(vertx, dbClient)

        restAPI = Router.router(vertx)

        restAPI.route().handler(BodyHandler.create())
        restAPI.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)))

        restAPI.route().handler { routingContext ->
            routingContext.response().putHeader("Content-Type", "application/json")
            routingContext.next()
        }

        val services = listOf(
            AuthService(dbClient, auth, context)
        )

        services.forEach { service ->
            launch {
                service.start()
                service.route(restAPI)
            }
        }

        Unit

    }

    fun router(): Router = restAPI

}