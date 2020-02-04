package moe.gogo.entity

import java.time.LocalDateTime

data class ProblemProfile(val problem: Problem, val exampleCount: Int) {
    val name: String = problem.name
    val createTime: LocalDateTime = problem.createTime
    val editTime: LocalDateTime = problem.editTime
}