package services

import models.User
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import reactivemongo.api.commands._
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.errors.ConnectionException
import repository.UserRepositoryInterface
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class UserServiceSpec extends PlaySpec {

  import UserServiceSpec._

  "UserServiceSpec" when {

    "insert is called" should {

      "handle successful insert" in {

        val service = new UserService(new TestSuccessUserRepository)

        service.insert(user).map(_ mustBe true)
      }

      "handle failed insert" in {

        val service = new UserService(new TestFailureUserRepository)

        service.insert(user).map(_ mustBe false)
      }

    }

    "findAll is called" should {

      "return zero records" in {

        val service = new UserService(new TestFailureUserRepository)

        service.retrieve.map(_ mustBe Seq.empty)
      }

      "return multiple records" in {

        val service = new UserService(new TestSuccessUserRepository)

        service.retrieve.map {
          _ mustBe Seq(
            User(
              None,
              "name",
              "telephone",
              "email"),
            User(
              None,
              "name",
              "telephone",
              "email")
          )
        }
      }

    }

  }

}

object UserServiceSpec {

  class TestSuccessUserRepository extends UserRepositoryInterface {
    override def insert(x: User): Future[WriteResult] = Future.successful(DefaultWriteResult(true, 1, Seq.empty, None, None, None))

    override def findAll: Future[Seq[User]] = Future.successful(Seq(
      User(
        None,
        "name",
        "telephone",
        "email"),
      User(
        None,
        "name",
        "telephone",
        "email")
    ))
  }

  class TestFailureUserRepository extends UserRepositoryInterface {
    override def insert(x: User): Future[WriteResult] = Future.failed(ConnectionException(""))

    override def findAll: Future[Seq[User]] = Future.successful(Seq.empty)
  }

  val user = User(
    Some(BSONObjectID.generate()),
    "name",
    "telephone",
    "email")
}