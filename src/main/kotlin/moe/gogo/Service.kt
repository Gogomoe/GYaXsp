package moe.gogo

interface Service {

    suspend fun start(registry: ServiceRegistry)

}