package moe.gogo.controller

import io.kotlintest.*
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.specs.StringSpec
import io.vertx.ext.web.client.WebClientSession
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendFormAwait
import kotlinx.coroutines.runBlocking
import moe.gogo.TestWebServer
import moe.gogo.nextString
import kotlin.random.Random

internal class AuthControllerTest : StringSpec() {

    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential

    override fun beforeSpecClass(spec: Spec, tests: List<TopLevelTest>) = runBlocking {
        TestWebServer.start()
    }

    override fun afterSpecClass(spec: Spec, results: Map<TestCase, TestResult>) = runBlocking {
        TestWebServer.stop()
    }

    companion object {
        val USERNAME = "test_user_${Random.nextString()}"
        val PASSWORD = Random.nextString()

        init {
            println("USERNAME $USERNAME")
            println("USERNAME $PASSWORD")
        }
    }

    init {
        "create user"{
            val session = Sessions.get()
            val form = multiMap(
                "username" to USERNAME,
                "password" to PASSWORD
            )

            val response = session.postAbs("http://localhost:8080/api/user").sendFormAwait(form)
            response.shouldSuccess()
        }
        "login"{
            loginSession() shouldNotBe null
        }
        "login with error"{
            val (_, response) = Sessions.login(USERNAME, "WRONG PASSWORD")
            response.shouldFail()

            val body = response.bodyAsJsonObject()
            body.getJsonObject("error").getString("message") shouldBe "Invalid username/password"
        }
        "get session"{
            val session = loginSession()

            val response = session.getAbs("http://localhost:8080/api/session").sendAwait()
            response.shouldSuccess()

            val body = response.bodyAsJsonObject()
            body.getJsonObject("user").getString("username") shouldBe USERNAME
        }

    }

    suspend fun loginSession(): WebClientSession {
        val (session, response) = Sessions.login(USERNAME, PASSWORD)
        val body = response.bodyAsJsonObject()
        if (body.getBoolean("success")) {
            return session
        } else {
            throw RuntimeException(body.getString("error"))
        }
    }

}