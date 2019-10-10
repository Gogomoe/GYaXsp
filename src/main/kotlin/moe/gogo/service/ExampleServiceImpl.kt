package moe.gogo.service

import io.vertx.ext.auth.User
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.auth.isAuthorizedAwait
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import moe.gogo.ServiceException
import moe.gogo.ServiceRegistry
import moe.gogo.entity.Example
import moe.gogo.toLocalDateTime
import moe.gogo.username
import java.io.File

class ExampleServiceImpl : ExampleService {

    private lateinit var database: JDBCClient
    private lateinit var auth: AuthService

    override suspend fun start(registry: ServiceRegistry) {
        database = registry[DatabaseService::class.java].client()
        auth = registry[AuthService::class.java]
    }

    override suspend fun createExample(user: User, problemName: String, input: String, answer: String): Int {
        val result = database.updateWithParamsAwait(
            """INSERT INTO example (problem_name, username) VALUES (?, ?)""",
            jsonArrayOf(user.username, problemName)
        )
        val id = result.keys.getInteger(0)

        inputFile(id).writeText(input)
        answerFile(id).writeText(answer)

        return id
    }

    override suspend fun getExample(id: Int): Example {
        val result = database.querySingleWithParamsAwait(
            """SELECT * FROM example WHERE example_id = ?""",
            jsonArrayOf(id)
        ) ?: throw ServiceException("Example $id not exists")
        return Example(
            id,
            result.getString(1),
            result.getString(2),
            inputFile(id).readText(),
            answerFile(id).readText(),
            result.getInstant(3).toLocalDateTime(),
            result.getInstant(4).toLocalDateTime()
        )
    }

    override suspend fun removeExample(user: User, id: Int) {
        val example = getExample(id)
        if (!isAuthorized(user, example)) {
            throw ServiceException("Permission denied")
        }
        database.updateWithParamsAwait(
            """DELETE FROM example WHERE example_id = ?""",
            jsonArrayOf(id)
        )
        inputFile(id).delete()
        answerFile(id).delete()
    }

    override suspend fun updateExample(user: User, id: Int, input: String, answer: String) {
        val example = getExample(id)
        if (!isAuthorized(user, example)) {
            throw ServiceException("Permission denied")
        }
        database.updateWithParamsAwait(
            """UPDATE example SET edit_time = CURRENT_TIMESTAMP WHERE example_id = ?""",
            jsonArrayOf(id)
        )
        inputFile(id).writeText(input)
        answerFile(id).writeText(answer)
    }

    private suspend fun isAuthorized(user: User, example: Example): Boolean =
        user.username == example.username ||
                user.isAuthorizedAwait("problem/${example.problem}/admin") ||
                user.isAuthorizedAwait("admin")

    private fun answerFile(id: Int) = File("store/example/${id}_answer.txt")

    private fun inputFile(id: Int) = File("store/example/${id}_input.txt")
}