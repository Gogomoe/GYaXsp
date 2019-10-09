package moe.gogo.verticle

import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import moe.gogo.CoroutineService
import moe.gogo.Service
import moe.gogo.ServiceRegistry
import moe.gogo.service.AuthService
import moe.gogo.service.AuthServiceImpl
import moe.gogo.service.DatabaseService
import moe.gogo.service.DatabaseServiceImpl

class APIServerVerticle : CoroutineVerticle() {

    lateinit var restAPI: Router

    override suspend fun start() = coroutineScope {

        val registry = ServiceRegistry.create(vertx, context)

        restAPI = Router.router(vertx)

        val databaseService = DatabaseServiceImpl()
        databaseService.start(registry)
        registry[DatabaseService::class.java] = databaseService

        val authService = AuthServiceImpl(context)
        authService.start(registry)
        registry[AuthService::class.java] = authService

        restAPI.route().handler(BodyHandler.create())
        restAPI.route().handler(
            SessionHandler.create(LocalSessionStore.create(vertx)).setAuthProvider(authService.auth)
        )

        restAPI.route().handler { routingContext ->
            routingContext.response().putHeader("Content-Type", "application/json")
            routingContext.next()
        }

        authService.route(restAPI)

        val services: List<Pair<Class<out Service>, CoroutineService>> = emptyList()

        services.forEach { (clazz, service) ->
            registry[clazz] = service
            launch {
                service.start(registry)
                service.route(restAPI)
            }
        }

        Unit

    }

    fun router(): Router = restAPI

}