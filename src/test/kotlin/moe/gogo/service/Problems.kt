package moe.gogo.service

import moe.gogo.entity.Problem
import moe.gogo.entity.User
import moe.gogo.nextString
import kotlin.random.Random

class Problems private constructor(val users: Users, val service: ProblemService) {

    companion object {
        fun create(users: Users, service: ProblemService) = Problems(users, service)
    }

    lateinit var admin: User

    val problems = mutableListOf<String>()

    suspend fun start() {
        admin = users.createUser("admin_problems_${Random.nextString()}")
        users.auth.giveRole(admin, "admin")
    }

    suspend fun stop() {
        problems.forEach {
            service.removeProblem(admin, it)
        }
    }

    suspend fun createProblem(problem: String): Problem {
        service.createProblem(admin, problem)
        problems.add(problem)
        return service.getProblem(problem)
    }

}