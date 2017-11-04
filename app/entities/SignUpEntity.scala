package entities

import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

case class SignUpEntity(email: String, firstName: String, lastName: String, password: String)

object SignUpEntity {

  implicit val signUpEntityReads: Reads[SignUpEntity] = (
    (JsPath \ "email").read[String] and
    (JsPath \ "firstName").read[String] and
    (JsPath \ "lastName").read[String] and
    (JsPath \ "password").read[String]
  )(SignUpEntity.apply _)

}
