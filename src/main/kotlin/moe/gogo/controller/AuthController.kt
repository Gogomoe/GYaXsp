package moe.gogo.controller

import io.vertx.core.Context
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import moe.gogo.CoroutineController
import moe.gogo.ServiceException
import moe.gogo.ServiceRegistry
import moe.gogo.service.AuthService

class AuthController(serviceRegistry: ServiceRegistry, context: Context) : CoroutineController(context) {

    private val service = serviceRegistry[AuthService::class.java]

    override fun route(router: Router) {
        router.post("/session").coroutineHandler(::handleLogin)
        router.post("/user").coroutineHandler(::handleSinup)
    }

    private suspend fun handleLogin(context: RoutingContext) {

        val params = context.request().formAttributes()
        val username = params.get("username")
        val password = params.get("password")

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw ServiceException("Username or password is empty")
        }

        val user = service.authUser(username, password)
        context.setUser(user.auth)
        context.put("user", user)
        context.session()?.regenerateId()

        context.success()

    }

    private suspend fun handleSinup(context: RoutingContext) {

        val params = context.request().formAttributes()
        val username = params.get("username")
        val password = params.get("password")

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw ServiceException("Username or password is empty")
        }
        if (username.length < 4 || password.length < 4) {
            throw ServiceException("Username or password is too short")
        }

        service.addUser(username, password)
        context.success()

    }
}