package moe.gogo.controller

import io.vertx.core.Context
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.jsonObjectOf
import moe.gogo.CoroutineController
import moe.gogo.ServiceException
import moe.gogo.ServiceRegistry
import moe.gogo.entity.Example
import moe.gogo.service.ExampleService
import moe.gogo.toInstant

class ExampleController(registry: ServiceRegistry, context: Context) : CoroutineController(context) {

    private val service = registry[ExampleService::class.java]

    override fun route(router: Router) {
        router.post("/problem/:problem_name/example").coroutineHandler(::handleCreateExample)
        router.get("/problem/:problem_name/example/:example_id").coroutineHandler(::handleGetExample)
        router.put("/problem/:problem_name/example/:example_id").coroutineHandler(::handleUpdateExample)
        router.delete("/problem/:problem_name/example/:example_id").coroutineHandler(::handleRemoveExample)
        router.get("/problem/:problem_name/examples").coroutineHandler(::handleGetAllExamples)
    }

    private suspend fun handleCreateExample(context: RoutingContext) {
        val request = context.request()
        val params = request.formAttributes()

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

    }

    private suspend fun handleGetExample(context: RoutingContext) {
        val request = context.request()

        val exampleId = request.getParam("example_id")?.toInt() ?: throw ServiceException("Problem name is empty")
        val example = service.getExample(exampleId)

        context.success(
            jsonObject = jsonObjectOf(
                "example" to example.toJson()
            )
        )
    }

    private suspend fun handleUpdateExample(context: RoutingContext) {
        val request = context.request()
        val params = request.formAttributes()

        val user = context.user() ?: throw ServiceException("No user found. Please login.")
        val exampleId = request.getParam("example_id")?.toInt() ?: throw ServiceException("Problem name is empty")

        val input = params.get("input") ?: throw ServiceException("Input is empty")
        val answer = params.get("answer") ?: throw ServiceException("Answer name is empty")

        service.updateExample(user, exampleId, input, answer)

        context.success()
    }

    private suspend fun handleRemoveExample(context: RoutingContext) {
        val request = context.request()

        val user = context.user() ?: throw ServiceException("No user found. Please login.")
        val exampleId = request.getParam("example_id")?.toInt() ?: throw ServiceException("Problem name is empty")

        service.removeExample(user, exampleId)

        context.success()
    }

    private suspend fun handleGetAllExamples(context: RoutingContext) {
        val request = context.request()

        val problemName = request.getParam("problem_name") ?: throw ServiceException("Problem name is empty")

        val jsons = service.getAllExamples(problemName).map {
            it.toJson()
        }

        context.success(
            jsonObject = jsonObjectOf(
                "examples" to jsonArrayOf(*jsons.toTypedArray())
            )
        )
    }

    private fun Example.toJson(): JsonObject {
        return jsonObjectOf(
            "example_id" to this.id,
            "problem_name" to this.problem,
            "username" to this.username,
            "input" to this.input,
            "answer" to this.answer,
            "create_time" to this.createTime.toInstant(),
            "edit_time" to this.editTime.toInstant()
        )
    }

}