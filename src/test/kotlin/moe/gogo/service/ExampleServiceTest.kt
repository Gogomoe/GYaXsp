package moe.gogo.service

import io.kotlintest.*
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.runBlocking
import moe.gogo.ServiceException
import moe.gogo.entity.Problem
import moe.gogo.entity.User
import moe.gogo.nextString
import kotlin.random.Random

class ExampleServiceTest : StringSpec() {

    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential

    companion object {
        lateinit var service: ExampleService
        lateinit var auth: AuthService

        lateinit var users: Users
        lateinit var problems: Problems

        lateinit var problem: Problem
        lateinit var manager: User
        lateinit var user: User

    }

    override fun beforeSpecClass(spec: Spec, tests: List<TopLevelTest>) = runBlocking {
        val registry = Services.createServices()
        auth = registry[AuthService::class.java]
        service = registry[ExampleService::class.java]

        users = Users.create(auth)
        problems = Problems.create(users, registry[ProblemService::class.java])
        problems.start()

        problem = problems.createProblem("problem_${Random.nextString()}")

        manager = users.createUser("problem_manager_user_${Random.nextString()}")
        auth.giveRole(manager, "problem/${problem.name}/admin")

        user = users.createUser("problem_user_${Random.nextString()}")
    }

    override fun afterSpecClass(spec: Spec, results: Map<TestCase, TestResult>) = runBlocking {
        problems.stop()
    }

    init {
        var exampleId = -1
        var exampleManagerId = -1
        "create example"{
            val input = Random.nextString()
            val answer = Random.nextString()
            exampleId = service.createExample(user, problem.name, input, answer)
            val example = service.getExample(exampleId)
            example.username shouldBe user.username
            example.input shouldBe input
            example.answer shouldBe answer
        }
        "update example"{
            val input = Random.nextString()
            val answer = Random.nextString()
            service.updateExample(user, exampleId, input, answer)
            val example = service.getExample(exampleId)
            example.input shouldBe input
            example.answer shouldBe answer
        }
        "update example by manager"{
            val input = Random.nextString()
            val answer = Random.nextString()
            service.updateExample(manager, exampleId, input, answer)
            val example = service.getExample(exampleId)
            example.username shouldBe user.username // user is not change
            example.input shouldBe input
            example.answer shouldBe answer
        }
        "update example with on perm"{
            val input = Random.nextString()
            val answer = Random.nextString()
            exampleManagerId = service.createExample(manager, problem.name, input, answer)
            shouldThrow<ServiceException> {
                service.updateExample(user, exampleManagerId, Random.nextString(), Random.nextString())
            }
            val example = service.getExample(exampleManagerId)
            example.username shouldBe manager.username
            example.input shouldBe input
            example.answer shouldBe answer
        }
        "remove example"{
            service.removeExample(user, exampleId)
        }
        "remove example repeated"{
            shouldThrow<ServiceException> {
                service.removeExample(manager, exampleId)
            }
        }
        "remove example with on perm"{
            shouldThrow<ServiceException> {
                service.removeExample(user, exampleManagerId)
            }
            service.removeExample(manager, exampleManagerId)
        }
    }

}