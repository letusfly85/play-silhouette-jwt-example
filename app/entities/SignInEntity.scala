package entities

import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

case class SignInEntity(email: String, password: String, rememberMe: Boolean)

object SignInEntity {

  implicit val signInEntityReads: Reads[SignInEntity] = (
    (JsPath \ "email").read[String] and
    (JsPath \ "password").read[String] and
    (JsPath \ "rememberMe").read[Boolean]
    )(SignInEntity.apply _)

}

