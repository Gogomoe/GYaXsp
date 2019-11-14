package moe.gogo.service

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import moe.gogo.ServiceRegistry
import moe.gogo.ServiceRegistryImpl

object Services {

    suspend fun createServices(): ServiceRegistry {
        val vertx = Vertx.vertx()
        val context = vertx.orCreateContext
        val dbConfig = JsonObject(this::class.java.classLoader.getResource("db_config.json")?.readText())
        context.config().put("db_config", dbConfig)

        val registry = ServiceRegistryImpl(vertx, context)

        val services = listOf(
            DatabaseService::class.java to DatabaseServiceImpl(),
            AuthService::class.java to AuthServiceImpl(),
            ProblemService::class.java to ProblemServiceImpl(),
            ExampleService::class.java to ExampleServiceImpl()
        )

        services.forEach { (clazz, service) ->
            registry[clazz] = service
        }
        services.forEach { (_, service) ->
            service.start(registry)
        }

        return registry
    }

}

