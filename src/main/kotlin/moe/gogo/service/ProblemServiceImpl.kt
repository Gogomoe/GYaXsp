package moe.gogo.service

import io.vertx.core.Context
import io.vertx.ext.auth.User
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.ext.auth.isAuthorizedAwait
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import moe.gogo.*
import moe.gogo.entity.Problem

class ProblemServiceImpl(context: Context) : CoroutineService(context), ProblemService {

    private lateinit var database: JDBCClient

    override suspend fun start(registry: ServiceRegistry) {
        database = registry[DatabaseService::class.java].client()
    }

    override fun route(router: Router) {
        router.post("/problem").coroutineHandler(::handleCreateProblem)
        router.get("/problem/:problem_name").coroutineHandler(::handleGetProblem)
    }

    private suspend fun handleCreateProblem(context: RoutingContext) {

        val params = context.request().formAttributes()
        val problemName = params.get("problem")

        try {
            val user = context.user()
            createProblem(user, problemName)

            context.success()

        } catch (e: ServiceException) {
            context.fail(400, e.message ?: "Unknown")
        }
    }

    private suspend fun handleGetProblem(context: RoutingContext) {
        val problemName = context.request().getParam("problem_name")
        try {
            val problem = getProblem(problemName)
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

    override suspend fun createProblem(user: User?, problemName: String?) {
        if (user == null) {
            throw ServiceException("User is empty")
        }
        if (problemName == null) {
            throw ServiceException("Problem name is empty")
        }

        if (!user.isAuthorizedAwait("role:admin")) {
            throw ServiceException("Permission denied")
        }

        val resultSet = database.queryWithParamsAwait(
            """SELECT * FROM problem WHERE problem_name = ?""",
            json { array(problemName) })
        if (resultSet.results.isNotEmpty()) {
            throw ServiceException("Problem has already existed")
        }

        database.updateWithParamsAwait(
            """INSERT INTO problem (problem_name) VALUES (?)""",
            json { array(problemName) })
    }

    override suspend fun getProblem(problemName: String?): Problem {
        if (problemName == null) {
            throw ServiceException("Problem name is empty")
        }
        val result = database.querySingleWithParamsAwait(
            """SELECT * FROM problem WHERE problem_name = ?""",
            json { array(problemName) })
            ?: throw ServiceException("Problem does not exist")

        return Problem(
            result.getString(0),
            result.getInstant(1).toLocalDateTime(),
            result.getInstant(2).toLocalDateTime()
        )
    }

}