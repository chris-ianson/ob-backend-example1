package controllers

import akka.stream.Materializer
import models.User
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserServiceInterface

import scala.concurrent.Future

class UserControllerSpec extends UserControllerSpecHelper {

  "UserControllerSpec" when {

    "get is called" should {

      "render the index page from a new instance of controller" in {
        val controller = new UserController(new SuccessfulUserServiceInterface)
        val home = controller.get().apply(FakeRequest())
        assertOK(home)
      }

      "render the index page from the application" in {
        running(_.overrides(
          bind[UserServiceInterface].to[SuccessfulUserServiceInterface]
        )) { app =>
          val controller = app.injector.instanceOf[UserController]
          val home = controller.get().apply(FakeRequest())
          assertOK(home)
        }
      }

      "render the index page from the router" in {
        running(_.overrides(
          bind[UserServiceInterface].to[SuccessfulUserServiceInterface]
        )) { app =>
          val request = FakeRequest(GET, "/users").withHeaders("Host" -> "localhost")
          val home = route(app, request).get
          assertOK(home)
        }
      }

      "return 404 if no records could be found" in {
        val controller = new UserController(new FailedUserServiceInterface)
        val home = controller.get().apply(FakeRequest())
        assert404(home)
      }

      "render a single user" in {
        val controller = new UserController(new SuccessfulUserServiceInterface)
        val home = controller.get().apply(FakeRequest())
        assertOK(home)
        contentAsJson(home) mustBe Json.toJson(Seq(
          User(None, "name", "telephone", "email")))
      }

      "return multiple users" in {
        val controller = new UserController(new SuccessfulMultipleUserServiceInterface)
        val home = controller.get().apply(FakeRequest())
        assertOK(home)
        contentAsJson(home) mustBe Json.toJson(Seq(
          User(None, "name", "telephone", "email"),
          User(None, "name 2", "telephone 2", "email 2")))
      }

    }

    "post is called" should {

      "return 400 if no data is sent" in {
        running(_.overrides(
          bind[UserServiceInterface].to[FailedUserServiceInterface]
        )) { app =>
          val requestR = FakeRequest(POST, "/users")
            .withJsonBody(Json.obj())
            .withHeaders("host" -> "localhost")
          val request = route(app, requestR).get
          status(request) mustBe BAD_REQUEST
          contentAsJson(request) mustBe Json.parse(
            """
              |{
              |"code": "INVALID_SUBMISSION",
              |"message": "submission didn't pass validation"
              |}
            """.stripMargin)
        }
      }

      "return 415 if no data is sent" in {
        running(_.overrides(
          bind[UserServiceInterface].to[FailedUserServiceInterface]
        )) { app =>
          val requestR = FakeRequest(POST, "/users")
            .withHeaders("host" -> "localhost")
          val request = route(app, requestR).get
          status(request) mustBe UNSUPPORTED_MEDIA_TYPE
        }
      }

      "return 201 when record is created" in {

        running(_.overrides(
          bind[UserServiceInterface].to[SuccessfulUserServiceInterface]
        )) { app =>
          val requestR = FakeRequest(POST, "/users")
            .withJsonBody(Json.obj("name" -> "test", "telephone" -> "test", "email" -> "test"))
            .withHeaders("host" -> "localhost")
          val request = route(app, requestR).get
          status(request) mustBe CREATED
        }

      }

    }

  }

}

trait UserControllerSpecHelper extends PlaySpec with OneAppPerSuite{
  override lazy val app = new GuiceApplicationBuilder()
    .bindings(bind[UserServiceInterface]
      .to[FailedUserServiceInterface]).build()

  implicit val materializer: Materializer = app.injector.instanceOf[Materializer]

  def assertOK(result: Future[Result]) = {
    status(result) mustBe OK
    contentType(result) mustBe Some("application/json")
  }

  def assert404(result: Future[Result]) = {
    status(result) mustBe NOT_FOUND
  }
}

class SuccessfulUserServiceInterface extends UserServiceInterface{
  override def insert(x: User): Future[Boolean] = Future.successful(true)
  def retrieve: Future[Seq[User]] = Future.successful(Seq(User(None, "name", "telephone", "email")))
}

class SuccessfulMultipleUserServiceInterface extends UserServiceInterface{
  override def insert(x: User): Future[Boolean] = Future.successful(true)
  def retrieve: Future[Seq[User]] = Future.successful(Seq(
    User(None, "name", "telephone", "email"),
    User(None, "name 2", "telephone 2", "email 2")))
}

class FailedUserServiceInterface extends UserServiceInterface{
  override def insert(x: User): Future[Boolean] = Future.successful(false)
  def retrieve: Future[Seq[User]] = Future.successful(Nil)
}
