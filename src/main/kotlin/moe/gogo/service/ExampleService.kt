package moe.gogo.service

import io.vertx.ext.auth.User
import moe.gogo.Service
import moe.gogo.entity.Example

interface ExampleService : Service {

    suspend fun createExample(user: User, problemName: String, input: String, answer: String)

    suspend fun getExample(id: Int): Example

    suspend fun removeExample(user: User, id: Int)

    suspend fun updateExample(user: User, id: Int, input: String, answer: String)

}