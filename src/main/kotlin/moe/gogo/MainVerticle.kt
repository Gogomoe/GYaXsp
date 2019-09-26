package moe.gogo

import io.vertx.core.AbstractVerticle

class MainVerticle : AbstractVerticle() {

    override fun start() {
        println("hello world")
        vertx.close()
    }

}