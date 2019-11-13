package moe.gogo.verticle.handler

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import moe.gogo.ServiceRegistry

class ServiceRegistryHandler(val registry: ServiceRegistry) : Handler<RoutingContext> {

    override fun handle(context: RoutingContext) {
        context.put("ServiceRegistry", registry)
        context.next()
    }

}
