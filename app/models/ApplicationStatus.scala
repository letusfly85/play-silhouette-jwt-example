package models

import play.api.libs.json._

final case class ApplicationStatus(status: String, version: String)

object ApplicationStatus {
  implicit val statusWrites = new Writes[ApplicationStatus] {
    def writes(status: ApplicationStatus) = Json.obj(
      "status" -> status.status,
      "version" -> status.version
    )
  }
}
