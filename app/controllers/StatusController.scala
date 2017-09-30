package controllers

import javax.inject._

import actors.SlackNotifyActor
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, Materializer}
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import models._

import scala.concurrent.ExecutionContextExecutor


@Singleton
class StatusController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  implicit val system: ActorSystem  = ActorSystem("showcase")
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = ActorMaterializer()

  val showcaseActor: ActorRef = system.actorOf(Props(classOf[SlackNotifyActor]), "slack-notify-actor")

  def status() = Action { implicit request: Request[AnyContent] =>
    val applicationStatus = new ApplicationStatus(status = "alive", version = "1.0.0")
    val result = Json.toJson(applicationStatus)

    val message: (String, String) = ("dummyToken", "dummyMessage")

    //async pass message
    showcaseActor ! message

    Ok(result)
  }

}
