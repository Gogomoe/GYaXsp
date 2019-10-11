package moe.gogo.service

import io.vertx.ext.auth.User
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.auth.isAuthorizedAwait
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import moe.gogo.ServiceException
import moe.gogo.ServiceRegistry
import moe.gogo.entity.Problem
import moe.gogo.toLocalDateTime

class ProblemServiceImpl() : ProblemService {

    private lateinit var database: JDBCClient
    private lateinit var auth: AuthService
    private lateinit var example: ExampleService

    override suspend fun start(registry: ServiceRegistry) {
        database = registry[DatabaseService::class.java].client()
        auth = registry[AuthService::class.java]
        example = registry[ExampleService::class.java]
    }

    override suspend fun createProblem(user: User, problemName: String) {

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

        val role = "problem/${problemName}/admin"
        val permission = "problem/${problemName}/admin"
        auth.givePermission(role, permission)
        auth.giveRole(user, role)
    }

    override suspend fun getProblem(problemName: String): Problem {

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

    override suspend fun removeProblem(user: User, problemName: String) {

        if (!isAuthorized(user, problemName)) {
            throw ServiceException("Permission denied")
        }

        example.getAllExamples(problemName).forEach {
            example.removeExample(user, it.id)
        }

        database.updateWithParamsAwait(
            """DELETE FROM problem WHERE problem_name = ?""",
            jsonArrayOf(problemName)
        )

    }

    override suspend fun getAllProblems(): List<Problem> {
        return database.queryAwait("""SELECT * FROM problem""").results.map {
            Problem(
                it.getString(0),
                it.getInstant(1).toLocalDateTime(),
                it.getInstant(2).toLocalDateTime()
            )
        }
    }

    private suspend fun isAuthorized(user: User, problemName: String) =
        user.isAuthorizedAwait("admin") || user.isAuthorizedAwait("problem/${problemName}/admin")

}