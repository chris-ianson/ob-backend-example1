package repository


import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.User
import play.api.libs.json.{Json, OFormat}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserRepository @Inject()(reactiveMongoApi: ReactiveMongoApi) extends UserRepositoryInterface {

  implicit val formats: OFormat[User] = Json.format[User]

  lazy val collection: Future[JSONCollection]= reactiveMongoApi.database.map(_.collection("users"))

  def insert(x: User): Future[WriteResult] =
    collection.flatMap(_.insert(x))

  def findAll: Future[Seq[User]] =
    collection.flatMap(_.find(BSONDocument.empty).cursor[User](ReadPreference.primary).collect[List](Int.MaxValue, Cursor.FailOnError[List[User]]()))
}

@ImplementedBy(classOf[UserRepository])
trait UserRepositoryInterface {
  def insert(x: User): Future[WriteResult]
  def findAll: Future[Seq[User]]
}
