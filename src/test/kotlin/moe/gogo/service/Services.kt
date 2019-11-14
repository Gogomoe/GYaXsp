package moe.gogo.service

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import moe.gogo.ServiceRegistry
import moe.gogo.ServiceRegistryImpl
import moe.gogo.entity.User
import moe.gogo.nextString
import kotlin.random.Random

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

class Users private constructor(val auth: AuthService) {

    companion object {
        fun create(auth: AuthService) = Users(auth)
    }

    private val passwords = mutableMapOf<String, String>()

    suspend fun createUser(username: String): User {
        val password = Random.nextString()
        passwords[username] = password
        auth.addUser(username, password)
        return auth.authUser(username, password)
    }

    suspend fun updateUser(user: User): User {
        val username = user.username
        val password = passwords[username]
        return auth.authUser(username, password!!)
    }

}


