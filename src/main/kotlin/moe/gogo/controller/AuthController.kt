package moe.gogo.controller

import io.vertx.core.Context
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import moe.gogo.CoroutineController
import moe.gogo.ServiceException
import moe.gogo.ServiceRegistry
import moe.gogo.service.AuthService

class AuthController(serviceRegistry: ServiceRegistry, context: Context) : CoroutineController(context) {

    private val log = LoggerFactory.getLogger(AuthController::class.java)
    private val service = serviceRegistry[AuthService::class.java]

    override fun route(router: Router) {
        router.post("/session").coroutineHandler(::handleLogin)
        router.post("/user").coroutineHandler(::handleSinup)
    }

    private suspend fun handleLogin(context: RoutingContext) {

        val params = context.request().formAttributes()
        val username = params.get("username")
        val password = params.get("password")

        try {
            val user = service.getUser(username, password)
            context.setUser(user)
            context.session()?.regenerateId()

            context.success()

        } catch (e: ServiceException) {
            context.fail(400, e.message ?: "Unknown")
        }
    }

    private suspend fun handleSinup(context: RoutingContext) {

        val params = context.request().formAttributes()
        val username = params.get("username")
        val password = params.get("password")

        try {
            service.addUser(username, password)
            context.success()
        } catch (e: ServiceException) {
            context.fail(400, e.message ?: "Unknown")
        }
    }
}