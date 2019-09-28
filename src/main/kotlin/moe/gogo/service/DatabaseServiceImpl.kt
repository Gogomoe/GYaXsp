package moe.gogo.service

import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import moe.gogo.ServiceRegistry

class DatabaseServiceImpl : DatabaseService {

    private lateinit var config: JsonObject
    private lateinit var client: JDBCClient

    override suspend fun start(registry: ServiceRegistry) {
        config = registry.context().config().getJsonObject("db_config")
        client = JDBCClient.createShared(registry.vertx(), config)
    }

    override fun client(): JDBCClient = client

    override fun config(): JsonObject = config

}