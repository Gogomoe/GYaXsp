package moe.gogo

class ServiceRegistryImpl : ServiceRegistry {

    private val services: MutableMap<Class<*>, Service> = mutableMapOf()

    override fun register(clazz: Class<out Service>, service: Service) {
        services[clazz] = service
    }

    override fun <T : Service> get(clazz: Class<T>): T {
        return services[clazz]!! as T
    }

}