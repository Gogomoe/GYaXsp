package moe.gogo.service

import moe.gogo.Service
import moe.gogo.entity.Problem
import moe.gogo.entity.User

interface ProblemService : Service {

    suspend fun createProblem(user: User, problemName: String)

    suspend fun getProblem(problemName: String): Problem

    suspend fun removeProblem(user: User, problemName: String)

    suspend fun getAllProblems(): List<Problem>

    suspend fun getProblems(offset: Int = 1, limit: Int = 50): List<Problem>

}