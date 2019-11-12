package moe.gogo.verticle.handler

import io.vertx.core.Context
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import moe.gogo.entity.UserAuth
import moe.gogo.getUser
import moe.gogo.service.AuthService
import moe.gogo.username

class UserHandler(private val authService: AuthService, context: Context) : Handler<RoutingContext> {

    private val coroutineScope: CoroutineScope = CoroutineScope(context.dispatcher())

    override fun handle(routingContext: RoutingContext) {
        val auth: UserAuth? = routingContext.user()
        if (auth == null || routingContext.getUser() != null) {
            routingContext.next()
            return
        }
        coroutineScope.launch {
            routingContext.session().put("user", authService.getUser(auth.username, auth))
            routingContext.next()
        }
    }

}