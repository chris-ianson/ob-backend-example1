package repository

import models.User
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import reactivemongo.bson.BSONObjectID
import utils.MongoHelper

import scala.concurrent.ExecutionContext.Implicits.global

class UserRepositorySpec extends PlaySpec with MongoHelper with BeforeAndAfterEach with ScalaFutures {

  val user = User(
    Some(BSONObjectID.generate()),
    "name",
    "telephone",
    "email")

  override def beforeEach() {
    await(userRepository.collection.flatMap(_.drop(failIfNotFound = false)))
  }

  "UserRepository" should {

    "insert a record into the database" in {

      userRepository.insert(user).map {
        _ =>
          val records = userRepository.findAll
          whenReady(records) {
            x => x.head mustBe User(None, "name", "telephone", "email")
          }
      }
    }

    "get a record from the database" in {

      await(userRepository.collection.flatMap(_.insert(ordered =false).one(
        User(Some(BSONObjectID.generate()), "test name", "telephone number", "email"))))

      userRepository.findAll.map ( _.head mustBe User(None, "test name", "telephone number", "email"))
    }

  }

}