package moe.gogo

import io.vertx.core.Context
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class CoroutineService(val context: Context) {

    val coroutineScope: CoroutineScope = CoroutineScope(context.dispatcher())

    abstract fun route(router: Router)

    abstract suspend fun start()

    open fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx ->
            coroutineScope.launch(context.dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ctx.fail(500, e.message ?: "Unknown service error")
                }
            }
        }
    }

    open fun RoutingContext.fail(statusCode: Int = 400, message: String) {
        response().statusCode = statusCode
        val obj = jsonObjectOf(
            "error" to jsonObjectOf("message" to message)
        )
        end(obj.encode())
    }
}