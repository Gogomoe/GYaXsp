package moe.gogo.controller

import io.vertx.core.Context
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import moe.gogo.CoroutineController
import moe.gogo.ServiceException
import moe.gogo.ServiceRegistry
import moe.gogo.service.ProblemService
import moe.gogo.toInstant

class ProblemController(registry: ServiceRegistry, context: Context) : CoroutineController(context) {

    val service = registry[ProblemService::class.java]

    override fun route(router: Router) {
        router.post("/problem").coroutineHandler(::handleCreateProblem)
        router.get("/problem/:problem_name").coroutineHandler(::handleGetProblem)
    }

    private suspend fun handleCreateProblem(context: RoutingContext) {

        val request = context.request()
        val params = request.formAttributes()

        try {
            val user = context.user() ?: throw ServiceException("User is empty")
            val problemName = params.get("problem").takeIf { it.isNotEmpty() }
                ?: throw ServiceException("Problem name is empty")

            service.createProblem(user, problemName)

            context.success()

        } catch (e: ServiceException) {
            context.fail(400, e.message ?: "Unknown")
        }
    }

    private suspend fun handleGetProblem(context: RoutingContext) {

        val request = context.request()

        try {
            val problemName = request.getParam("problem_name")
                ?: throw ServiceException("Problem name is empty")

            val problem = service.getProblem(problemName)
            context.success(jsonObject = json {
                obj(
                    "problem" to obj(
                        "name" to problem.name,
                        "create_time" to problem.createTime.toInstant(),
                        "edit_time" to problem.editTime.toInstant()
                    )
                )
            })
        } catch (e: ServiceException) {
            context.fail(400, e.message ?: "Unknown")
        }
    }

}