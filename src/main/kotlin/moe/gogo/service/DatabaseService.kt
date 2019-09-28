package moe.gogo.service

import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import moe.gogo.Service

interface DatabaseService : Service {

    fun client(): JDBCClient

    fun config(): JsonObject

}