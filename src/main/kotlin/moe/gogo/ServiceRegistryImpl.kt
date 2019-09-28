package moe.gogo

import io.vertx.core.Context
import io.vertx.core.Vertx

class ServiceRegistryImpl(private val vertx: Vertx, private val context: Context) : ServiceRegistry {

    override fun vertx(): Vertx = vertx

    override fun context(): Context = context

    private val services: MutableMap<Class<*>, Service> = mutableMapOf()

    override fun register(clazz: Class<out Service>, service: Service) {
        services[clazz] = service
    }

    override fun <T : Service> get(clazz: Class<T>): T {
        return services[clazz]!! as T
    }

}