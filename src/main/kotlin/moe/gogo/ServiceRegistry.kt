package moe.gogo

interface ServiceRegistry {

    companion object {
        fun create(): ServiceRegistry = ServiceRegistryImpl()
    }

    fun register(clazz: Class<out Service>, service: Service)

    operator fun <T : Service> get(clazz: Class<T>): T

    operator fun set(clazz: Class<out Service>, service: Service) = register(clazz, service)


}