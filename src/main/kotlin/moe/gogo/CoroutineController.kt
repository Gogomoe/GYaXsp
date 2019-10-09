package moe.gogo

import io.vertx.core.Context
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class CoroutineController(val context: Context) : Controller {

    companion object {
        private val log = LoggerFactory.getLogger(CoroutineController::class.java)
    }

    val coroutineScope: CoroutineScope = CoroutineScope(context.dispatcher())

    open fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx ->
            coroutineScope.launch(context.dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    log.error("Error in ${this::class.java.name} coroutineHandler", e)
                    ctx.fail(500, e.message ?: "Unknown service error")
                }
            }
        }
    }

    open fun RoutingContext.success(statusCode: Int = 200, jsonObject: JsonObject = JsonObject()) {
        response().statusCode = statusCode
        jsonObject.put("success", true)
        end(jsonObject.encode())
    }

    open fun RoutingContext.fail(statusCode: Int = 400, message: String) {
        response().statusCode = statusCode
        val obj = jsonObjectOf(
            "success" to false,
            "error" to jsonObjectOf("message" to message)
        )
        end(obj.encode())
    }

}