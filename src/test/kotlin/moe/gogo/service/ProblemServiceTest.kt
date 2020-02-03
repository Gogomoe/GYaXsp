package moe.gogo.service

import io.kotlintest.Spec
import io.kotlintest.TestCaseOrder
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.runBlocking
import moe.gogo.ServiceException
import moe.gogo.entity.User
import moe.gogo.nextString
import kotlin.random.Random

internal class ProblemServiceTest : StringSpec() {

    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential

    companion object {
        lateinit var service: ProblemService
        lateinit var auth: AuthService

        lateinit var users: Users
        lateinit var admin: User
        lateinit var noPermUser: User

        lateinit var updateAdmin: suspend () -> User
    }

    override fun beforeSpecClass(spec: Spec, tests: List<TopLevelTest>) = runBlocking {
        val registry = Services.createServices()
        service = registry[ProblemService::class.java]
        auth = registry[AuthService::class.java]

        users = Users.create(auth)
        admin = users.createUser("admin_${Random.nextString()}")
        auth.giveRole(admin, "admin")

        updateAdmin = update@{
            return@update users.updateUser(admin).also { admin = it }
        }

        noPermUser = users.createUser("no_perm_${Random.nextString()}")
    }

    init {
        val problemName = "problem_${Random.nextString()}"
        "create problem"{
            service.createProblem(admin, problemName)
            val problem = service.getProblem(problemName)
            problem.name shouldBe problemName
            updateAdmin().isAuthorizedAwait("problem/$problemName/admin") shouldBe true
        }
        "create problem repeated"{
            shouldThrow<ServiceException> {
                service.createProblem(admin, problemName)
            }
        }
        "create problem with no permission"{
            shouldThrow<ServiceException> {
                val newProblem = "problem_${Random.nextString()}"
                service.createProblem(noPermUser, newProblem)
            }
        }
        "get all problem"{
            service.getAllProblems().any { it.name == problemName } shouldBe true
        }
        "remove problem with no permission"{
            shouldThrow<ServiceException> {
                service.removeProblem(noPermUser, problemName)
            }
        }
        "remove problem"{
            service.removeProblem(admin, problemName)
            shouldThrow<ServiceException> {
                service.getProblem(problemName)
            }
            updateAdmin().isAuthorizedAwait("problem/$problemName/admin") shouldBe false
        }
        "get problems"{
            val names = List(5) { "problem_${Random.nextString()}" }
            try {
                names.forEach { service.createProblem(admin, it) }
                val first = service.getProblems(1, 2)
                first.size shouldBe 2
                first.map { it.name } shouldContainExactly listOf(names[4], names[3])
                val second = service.getProblems(3, 2)
                second.size shouldBe 2
                second.map { it.name } shouldContainExactly listOf(names[2], names[1])
                val third = service.getProblems(5, 2)
                third.size shouldBe 1
                third.first().name shouldBe names.first()
            } finally {
                names.forEach { service.removeProblem(admin, it) }
            }
        }
    }

}