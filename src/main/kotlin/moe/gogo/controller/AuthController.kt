package moe.gogo.controller

import io.vertx.core.Context
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonObjectOf
import moe.gogo.CoroutineController
import moe.gogo.ServiceException
import moe.gogo.ServiceRegistry
import moe.gogo.getUser
import moe.gogo.service.AuthService

class AuthController(serviceRegistry: ServiceRegistry, context: Context) : CoroutineController(context) {

    private val service = serviceRegistry[AuthService::class.java]

    override fun route(router: Router) {
        router.get("/session").coroutineHandler(::handleGetSession)
        router.post("/session").coroutineHandler(::handleLogin)
        router.post("/user").coroutineHandler(::handleSinup)
        router.get("/user/:username").coroutineHandler(::handleGetUser)
    }

    private suspend fun handleGetSession(context: RoutingContext) {

        val user = context.getUser()
        val json = if (user == null) {
            jsonObjectOf(
                "session" to false
            )
        } else {
            jsonObjectOf(
                "session" to true,
                "user" to jsonObjectOf(
                    "username" to user.username,
                    "avatar" to (user.avatar ?: defaultAvatar()),
                    "roles" to json { array(user.roles) },
                    "perms" to json { array(user.permissions) }
                )
            )
        }

        context.success(jsonObject = json)
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

    private suspend fun handleGetUser(context: RoutingContext) {
        val request = context.request()
        val username = request.getParam("username") ?: throw ServiceException("Username not found")
        val user = service.getUser(username)
        context.success(
            jsonObject = jsonObjectOf(
                "user" to jsonObjectOf(
                    "username" to user.username,
                    "avatar" to (user.avatar ?: defaultAvatar()),
                    "roles" to json { array(user.roles) },
                    "perms" to json { array(user.permissions) }
                )
            )
        )
    }

    private fun defaultAvatar(): String = "/avatar/default.jpg"

}