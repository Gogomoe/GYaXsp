package moe.gogo

import io.vertx.ext.web.Router

interface Controller {

    fun route(router: Router)

}