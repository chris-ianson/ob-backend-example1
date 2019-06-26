package models

import play.api.libs.json.Json

trait ErrorResponse

object ErrorResponse extends ErrorResponse {

  private def error(code: String, message: String) = Json.obj("code" -> code, "message"-> message)

  val INVALID_SUBMISSION = error("INVALID_SUBMISSION", "submission didn't pass validation")
}