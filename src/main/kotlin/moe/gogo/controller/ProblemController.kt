package moe.gogo.controller

import io.vertx.core.Context
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.core.json.obj
import moe.gogo.*
import moe.gogo.entity.Problem
import moe.gogo.service.ProblemService

class ProblemController(registry: ServiceRegistry, context: Context) : CoroutineController(context) {

    val service = registry[ProblemService::class.java]

    override fun route(router: Router) {
        router.post("/problem").coroutineHandler(::handleCreateProblem)
        router.get("/problem/:problem_name").coroutineHandler(::handleGetProblem)
        router.delete("/problem/:problem_name").coroutineHandler(::handleRemoveProblem)
        router.get("/problems").coroutineHandler(::handleGetAllProblem)
    }

    private suspend fun handleCreateProblem(context: RoutingContext) {

        val request = context.request()
        val params = request.formAttributes()

        val user = context.getUser() ?: throw ServiceException("User is empty")
        val problemName = params.get("problem").takeIf { it.isNotEmpty() }
            ?: throw ServiceException("Problem name is empty")

        service.createProblem(user, problemName)

        context.success()

    }

    private suspend fun handleGetProblem(context: RoutingContext) {

        val request = context.request()

        val problemName = request.getParam("problem_name")
            ?: throw ServiceException("Problem name is empty")

        val problem = service.getProblem(problemName)
        context.success(jsonObject = json {
            obj(
                "problem" to problem.toJson()
            )
        })
    }

    private suspend fun handleRemoveProblem(context: RoutingContext) {

        val request = context.request()

        val user = context.getUser() ?: throw ServiceException("User is empty")
        val problemName = request.getParam("problem_name")
            ?: throw ServiceException("Problem name is empty")

        service.removeProblem(user, problemName)

        context.success()
    }

    private suspend fun handleGetAllProblem(context: RoutingContext) {

        val problems = service.getAllProblems().map { it.toJson() }

        context.success(
            jsonObject = jsonObjectOf(
                "problems" to jsonArrayOf(*problems.toTypedArray())
            )
        )
    }

    private fun Problem.toJson(): JsonObject = jsonObjectOf(
        "name" to this.name,
        "create_time" to this.createTime.toInstant(),
        "edit_time" to this.editTime.toInstant()
    )

}