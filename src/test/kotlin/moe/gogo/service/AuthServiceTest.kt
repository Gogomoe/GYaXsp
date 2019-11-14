package moe.gogo.service

import io.kotlintest.TestCaseOrder
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.runBlocking
import moe.gogo.ServiceException
import moe.gogo.nextString
import kotlin.random.Random

internal class AuthServiceTest : StringSpec() {

    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential

    lateinit var service: AuthService

    companion object {
        val USERNAME = "test_user_${Random.nextString()}"
        val PASSWORD = Random.nextString()
        val ROLE = "test_role/${Random.nextString()}/admin"
        val PERMISSION = "test_permission/${Random.nextString()}/manage"
    }

    init {
        println("USERNAME $USERNAME")
        println("USERNAME $PASSWORD")
        println("USERNAME $PERMISSION")
        println("USERNAME $ROLE")

        runBlocking {
            service = Services.createServices()[AuthService::class.java]
        }

        "create user"{
            service.addUser(USERNAME, PASSWORD)
        }
        "create user repeated"{
            shouldThrow<ServiceException> {
                service.addUser(USERNAME, PASSWORD)
            }
        }
        "auth user"{
            service.authUser(USERNAME, PASSWORD)
        }
        "auth user with error"{
            val error = shouldThrow<ServiceException> {
                service.authUser(USERNAME, "wrong password")
            }
            error.message shouldBe "Invalid username/password"
        }
        "get user"{
            val user = service.getUser(USERNAME)
            user.username shouldBe USERNAME
        }
        "get unknown user"{
            shouldThrow<ServiceException> {
                service.getUser("UNKNOWN USER")
            }
        }
        "give role"{
            val user = service.getUser(USERNAME)
            service.giveRole(user, ROLE)
            service.getUser(USERNAME).roles shouldContain ROLE
        }
        "give permission"{
            service.givePermission(ROLE, PERMISSION)
            service.getUser(USERNAME).permissions shouldContain PERMISSION
        }
        "remove permission"{
            service.removePermission(ROLE, PERMISSION)
            service.getUser(USERNAME).permissions shouldNotContain PERMISSION
        }
        "remove role"{
            val user = service.getUser(USERNAME)
            service.removeRole(user, ROLE)
            service.getUser(USERNAME).roles shouldNotContain ROLE
        }
        "remove permission with module"{
            val role = "test_role/${Random.nextString()}/role"
            val permModule = "test_permission/${Random.nextString()}/"
            val perm1 = "${permModule}perm1"
            val perm2 = "${permModule}perm2"

            var user = service.getUser(USERNAME)
            service.giveRole(user, role)
            service.givePermission(role, perm1)
            service.givePermission(role, perm2)

            user = service.getUser(USERNAME)
            user.permissions shouldContain perm1
            user.permissions shouldContain perm2

            service.removePermission(role, permModule)

            user = service.getUser(USERNAME)
            user.permissions shouldNotContain perm1
            user.permissions shouldNotContain perm2

            service.removeRole(user, role)
        }

        "remove role with module"{
            val roleModule = "test_role/${Random.nextString()}/"
            val role1 = "${roleModule}role1"
            val role2 = "${roleModule}role2"

            var user = service.getUser(USERNAME)
            service.giveRole(user, role1)
            service.giveRole(user, role2)

            user = service.getUser(USERNAME)
            user.roles shouldContain role1
            user.roles shouldContain role2

            service.removeRole(user, roleModule)

            user = service.getUser(USERNAME)
            user.roles shouldNotContain role1
            user.roles shouldNotContain role2
        }

        "remove permission for all"{
            val roleModule = "test_role/${Random.nextString()}/"
            val role1 = "${roleModule}role1"
            val role2 = "${roleModule}role2"
            val permModule = "test_permission/${Random.nextString()}/"
            val perm1 = "${permModule}perm1"
            val perm2 = "${permModule}perm2"

            var user = service.getUser(USERNAME)
            service.giveRole(user, role1)
            service.giveRole(user, role2)

            service.givePermission(role1, perm1)
            service.givePermission(role2, perm2)

            user = service.getUser(USERNAME)
            user.permissions shouldContain perm1
            user.permissions shouldContain perm2

            service.removePermissionForAll(permModule)

            user = service.getUser(USERNAME)
            user.permissions shouldNotContain perm1
            user.permissions shouldNotContain perm2

            service.removeRole(user, role1)
            service.removeRole(user, role2)
        }

        "remove role for all"{
            val roleModule = "test_role/${Random.nextString()}/"
            val role1 = "${roleModule}role1"
            val role2 = "${roleModule}role2"

            var user = service.getUser(USERNAME)
            var admin = service.getUser("admin")
            service.giveRole(user, role1)
            service.giveRole(user, role2)
            service.giveRole(admin, role1)
            service.giveRole(admin, role2)

            user = service.getUser(USERNAME)
            user.roles shouldContain role1
            user.roles shouldContain role2
            admin = service.getUser("admin")
            admin.roles shouldContain role1
            admin.roles shouldContain role2

            service.removeRoleForAll(roleModule)

            user = service.getUser(USERNAME)
            user.roles shouldNotContain role1
            user.roles shouldNotContain role2
            admin = service.getUser("admin")
            admin.roles shouldNotContain role1
            admin.roles shouldNotContain role2
        }

    }

}