package moe.gogo.service

import io.vertx.ext.auth.User
import moe.gogo.Service
import moe.gogo.entity.Problem

interface ProblemService : Service {

    suspend fun createProblem(user: User, problemName: String)

    suspend fun getProblem(problemName: String): Problem

    suspend fun removeProblem(user: User, problemName: String)

    suspend fun getAllProblems(): List<Problem>

}