package moe.gogo.verticle

import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.coroutineScope
import moe.gogo.Controller
import moe.gogo.ServiceRegistry
import moe.gogo.controller.AuthController
import moe.gogo.controller.ExampleController
import moe.gogo.controller.ProblemController
import moe.gogo.service.*

class APIServerVerticle : CoroutineVerticle() {

    lateinit var restAPI: Router

    override suspend fun start() = coroutineScope {

        val registry = ServiceRegistry.create(vertx, context)

        val services = listOf(
            DatabaseService::class.java to DatabaseServiceImpl(),
            AuthService::class.java to AuthServiceImpl(),
            ProblemService::class.java to ProblemServiceImpl(),
            ExampleService::class.java to ExampleServiceImpl()
        )

        services.forEach { (clazz, service) ->
            registry[clazz] = service
        }
        services.forEach { (_, service) ->
            service.start(registry)
        }

        restAPI = Router.router(vertx)

        restAPI.route().handler(BodyHandler.create())
        restAPI.route().handler(
            SessionHandler.create(LocalSessionStore.create(vertx))
                .setAuthProvider(registry[AuthService::class.java].auth())
        )

        restAPI.route().handler { routingContext ->
            routingContext.response().putHeader("Content-Type", "application/json")
            routingContext.next()
        }

        val controllers: List<Controller> = listOf(
            AuthController(registry, context),
            ProblemController(registry, context),
            ExampleController(registry, context)
        )

        controllers.forEach {
            it.route(restAPI)
        }

        Unit

    }

    fun router(): Router = restAPI

}