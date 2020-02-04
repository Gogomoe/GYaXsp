package moe.gogo.service

import moe.gogo.Service
import moe.gogo.entity.Problem
import moe.gogo.entity.ProblemProfile
import moe.gogo.entity.User

interface ProblemService : Service {

    suspend fun createProblem(user: User, problemName: String)

    suspend fun getProblem(problemName: String): Problem

    suspend fun removeProblem(user: User, problemName: String)

    suspend fun getAllProblems(): List<Problem>

    suspend fun getProblems(offset: Int = 0, limit: Int = 50): List<ProblemProfile>

    suspend fun getProblemsCount(): Int

}