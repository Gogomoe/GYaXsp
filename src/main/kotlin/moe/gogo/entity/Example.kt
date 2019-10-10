package moe.gogo.entity

import java.time.LocalDateTime

data class Example(
    val id: Int,
    val problem: String,
    val username: String,
    val input: String,
    val answer: String,
    val createTime: LocalDateTime,
    val editTime: LocalDateTime
)