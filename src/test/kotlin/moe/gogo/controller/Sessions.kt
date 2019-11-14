package moe.gogo.controller

import io.kotlintest.fail
import io.kotlintest.shouldBe
import io.vertx.core.MultiMap
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientSession
import io.vertx.ext.web.client.spi.CookieStore
import io.vertx.kotlin.ext.web.client.sendFormAwait
import moe.gogo.TestWebServer

object Sessions {

    fun get(): WebClientSession {
        val client = WebClient.create(TestWebServer.vertx)
        val cookieStore = CookieStore.build()
        return WebClientSession.create(client, cookieStore)
    }

    suspend fun login(username: String, password: String): Pair<WebClientSession, HttpResponse<Buffer>> {
        val session = get()
        val form = multiMap(
            "username" to username,
            "password" to password
        )

        val response = session.postAbs("http://localhost:8080/api/session").sendFormAwait(form)
        return session to response
    }

}

fun HttpResponse<Buffer>.shouldSuccess() {
    shouldBeJson()
    println(this.bodyAsJsonObject().encodePrettily())
    this.bodyAsJsonObject().getBoolean("success") shouldBe true
}

fun HttpResponse<Buffer>.shouldFail() {
    shouldBeJson()
    println(this.bodyAsJsonObject().encodePrettily())
    this.bodyAsJsonObject().getBoolean("success") shouldBe false
}

private fun HttpResponse<Buffer>.shouldBeJson() {
    val contentType = this.headers()["Content-Type"]
    if (contentType != "application/json") {
        fail("${this.statusCode()} ${this.statusMessage()} $contentType")
    }
}

fun multiMap(vararg pairs: Pair<String, String>): MultiMap {
    val form = MultiMap.caseInsensitiveMultiMap()
    form.addAll(mapOf(*pairs))
    return form
}