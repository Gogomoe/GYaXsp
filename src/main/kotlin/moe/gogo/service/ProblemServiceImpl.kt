package moe.gogo.service

import io.vertx.ext.auth.User
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.ext.auth.isAuthorizedAwait
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import moe.gogo.ServiceException
import moe.gogo.ServiceRegistry
import moe.gogo.entity.Problem
import moe.gogo.toLocalDateTime

class ProblemServiceImpl() : ProblemService {

    private lateinit var database: JDBCClient
    private lateinit var auth: AuthService

    override suspend fun start(registry: ServiceRegistry) {
        database = registry[DatabaseService::class.java].client()
        auth = registry[AuthService::class.java]
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
        auth.giveRole(user, role)
        auth.givePermission(role, permission)
        auth.givePermission("admin", permission)
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

}