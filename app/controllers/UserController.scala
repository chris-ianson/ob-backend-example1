package controllers

import com.google.inject.{Inject, Singleton}
import models.{ErrorResponse, User}
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{Action, BodyParser, BodyParsers, Controller}
import services.UserServiceInterface

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(userService: UserServiceInterface) extends Controller {

  def get = Action.async {
    implicit request =>
      for {
        result <- userService.retrieve
      } yield {

        if (result.isEmpty) {
          NotFound
        } else {
          Ok(Json.toJson(result)).as("application/json")
        }
      }
  }

  def post(): Action[User] = Action.async(validateJson[User]) {
    implicit request => {

      val user = request.body

      userService.insert(user).map {
        case true => Created.as("application/json")
        case false => InternalServerError
      }.recover {
        case _ => InternalServerError
      }

    }

  }

  private def validateJson[A: Reads]: BodyParser[A] = BodyParsers.parse.json.validate(
    _.validate[A].asEither.left.map(e => {
      BadRequest(ErrorResponse.INVALID_SUBMISSION).as("application/json")
    })
  )

}
