package moe.gogo.controller

import io.vertx.core.Context
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import moe.gogo.CoroutineController
import moe.gogo.ServiceException
import moe.gogo.ServiceRegistry
import moe.gogo.service.ExampleService
import moe.gogo.toInstant

class ExampleController(registry: ServiceRegistry, context: Context) : CoroutineController(context) {

    private val service = registry[ExampleService::class.java]

    override fun route(router: Router) {
        router.post("/problem/:problem_name/example").coroutineHandler(::handleCreateExample)
        router.get("/problem/:problem_name/example/:example_id").coroutineHandler(::handleGetExample)
        router.put("/problem/:problem_name/example/:example_id").coroutineHandler(::handleUpdateExample)
        router.delete("/problem/:problem_name/example/:example_id").coroutineHandler(::handleRemoveExample)
    }

    private suspend fun handleCreateExample(context: RoutingContext) {
        val request = context.request()
        val params = request.formAttributes()

        try {
            val user = context.user() ?: throw ServiceException("No user found. Please login.")
            val problemName = request.getParam("problem_name") ?: throw ServiceException("Problem name is empty")

            val input = params.get("input") ?: throw ServiceException("Input is empty")
            val answer = params.get("answer") ?: throw ServiceException("Answer name is empty")

            val exampleId = service.createExample(user, problemName, input, answer)

            context.success(
                jsonObject = jsonObjectOf(
                    "example_id" to exampleId
                )
            )
        } catch (e: ServiceException) {
            context.fail(400, e.message ?: "Unknown")
        }

    }

    private suspend fun handleGetExample(context: RoutingContext) {
        val request = context.request()

        try {
            val exampleId = request.getParam("example_id")?.toInt() ?: throw ServiceException("Problem name is empty")
            val example = service.getExample(exampleId)

            context.success(
                jsonObject = jsonObjectOf(
                    "example" to jsonObjectOf(
                        "example_id" to example.id,
                        "problem_name" to example.problem,
                        "username" to example.username,
                        "input" to example.input,
                        "answer" to example.answer,
                        "create_time" to example.createTime.toInstant(),
                        "edit_time" to example.editTime.toInstant()
                    )
                )
            )
        } catch (e: ServiceException) {
            context.fail(400, e.message ?: "Unknown")
        }
    }

    private suspend fun handleUpdateExample(context: RoutingContext) {
        val request = context.request()
        val params = request.formAttributes()

        try {
            val user = context.user() ?: throw ServiceException("No user found. Please login.")
            val exampleId = request.getParam("example_id")?.toInt() ?: throw ServiceException("Problem name is empty")

            val input = params.get("input") ?: throw ServiceException("Input is empty")
            val answer = params.get("answer") ?: throw ServiceException("Answer name is empty")

            service.updateExample(user, exampleId, input, answer)

            context.success()
        } catch (e: ServiceException) {
            context.fail(400, e.message ?: "Unknown")
        }
    }

    private suspend fun handleRemoveExample(context: RoutingContext) {
        val request = context.request()

        try {
            val user = context.user() ?: throw ServiceException("No user found. Please login.")
            val exampleId = request.getParam("example_id")?.toInt() ?: throw ServiceException("Problem name is empty")

            service.removeExample(user, exampleId)

            context.success()
        } catch (e: ServiceException) {
            context.fail(400, e.message ?: "Unknown")
        }
    }

}