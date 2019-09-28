package moe.gogo

import io.vertx.core.Context
import io.vertx.core.Vertx

interface ServiceRegistry {

    fun vertx(): Vertx

    fun context(): Context

    companion object {
        fun create(vertx: Vertx, context: Context): ServiceRegistry = ServiceRegistryImpl(vertx, context)
    }

    fun register(clazz: Class<out Service>, service: Service)

    operator fun <T : Service> get(clazz: Class<T>): T

    operator fun set(clazz: Class<out Service>, service: Service) = register(clazz, service)

}