package actors

import akka.actor.Actor
import services.SlackNotifyService

class SlackNotifyActor extends Actor {

  def receive = {
    case (token: String, message: String) =>
      SlackNotifyService.notify(token, message)
  }

}
