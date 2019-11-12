package moe.gogo.controller

import io.kotlintest.fail
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.AnnotationSpec
import io.vertx.core.MultiMap
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientSession
import io.vertx.ext.web.client.spi.CookieStore
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendFormAwait
import kotlinx.coroutines.runBlocking
import moe.gogo.TestWebServer
import moe.gogo.nextString
import kotlin.random.Random

internal class AuthControllerTest : AnnotationSpec() {

    @BeforeAll
    fun init() = runBlocking {
        TestWebServer.start()
    }

    @AfterAll
    fun stop() = runBlocking {
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

    @Test
    fun create_user() = runBlocking {
        val session = getSession()
        val form = MultiMap.caseInsensitiveMultiMap()
        form.addAll(
            mapOf(
                "username" to USERNAME,
                "password" to PASSWORD
            )
        )

        val response = session.postAbs("http://localhost:8080/api/user").sendFormAwait(form)
        response.shouldSuccess()
    }

    @Test
    fun login() = runBlocking {
        loginSession() shouldNotBe null
    }

    @Test
    fun get_session() = runBlocking {
        val session = loginSession()

        val response = session.getAbs("http://localhost:8080/api/session").sendAwait()
        response.shouldSuccess()

        val body = response.bodyAsJsonObject()
        body.getJsonObject("user").getString("username") shouldBe USERNAME
    }

    fun getSession(): WebClientSession {
        val client = WebClient.create(TestWebServer.vertx)
        val cookieStore = CookieStore.build()
        return WebClientSession.create(client, cookieStore)
    }

    suspend fun loginSession(): WebClientSession {
        val session = getSession()
        val form = MultiMap.caseInsensitiveMultiMap()
        form.addAll(
            mapOf(
                "username" to USERNAME,
                "password" to PASSWORD
            )
        )

        val response = session.postAbs("http://localhost:8080/api/session").sendFormAwait(form)
        val body = response.bodyAsJsonObject()
        if (body.getBoolean("success")) {
            return session
        } else {
            throw RuntimeException(body.getString("error"))
        }
    }

    fun HttpResponse<*>.shouldSuccess() {
        val contentType = this.headers()["Content-Type"]
        if (contentType != "application/json") {
            fail("${this.statusCode()} ${this.statusMessage()} $contentType")
        }
        println(this.bodyAsJsonObject().encodePrettily())
        this.bodyAsJsonObject().getBoolean("success") shouldBe true
    }

}