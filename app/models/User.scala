package models

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers._

case class User(
                 _id: Option[BSONObjectID] = Some(BSONObjectID.generate()),
                 name: String,
                 telephone: String,
                 email: String)

object User {
  implicit val formats: OFormat[User] = Json.format[User]
}