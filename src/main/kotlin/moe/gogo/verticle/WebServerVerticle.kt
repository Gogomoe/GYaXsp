package moe.gogo.verticle

import io.vertx.core.DeploymentOptions
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class WebServerVerticle : CoroutineVerticle() {

    override suspend fun start() = coroutineScope {

        val server = vertx.createHttpServer()
        val router = Router.router(vertx)

        launch {
            val api = APIServerVerticle()
            vertx.deployVerticleAwait(api, DeploymentOptions().setConfig(config))
            router.mountSubRouter("/api", api.router())
        }

        server.requestHandler(router).listenAwait(8080)

        Unit

    }

}