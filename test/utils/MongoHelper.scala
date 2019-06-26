package utils

import config.AppConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.modules.reactivemongo.ReactiveMongoApi
import org.scalatest.time.{Millis, Seconds, Span}
import repository.UserRepository

trait MongoHelper extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(
    "mongodb.uri" -> "mongodb://localhost:27017/ob-backend-test"
  ).build()

  lazy val reactiveMongoApi = app.injector.instanceOf[ReactiveMongoApi]

  def injector = app.injector

  def config: AppConfig = injector.instanceOf[AppConfig]

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(1, Seconds), interval = Span(500, Millis))

  val userRepository = new UserRepository(reactiveMongoApi)

}
