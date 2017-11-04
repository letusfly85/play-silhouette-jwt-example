package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import entities.SignInEntity
import models.services.UserService
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.{AbstractController, ControllerComponents, Request}
import utils.auth.DefaultEnv

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Sign In` controller.
 *
 * @param components             The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param credentialsProvider    The credentials provider.
 * @param socialProviderRegistry The social provider registry.
 * @param configuration          The Play configuration.
 * @param clock                  The clock instance.
 * @param assets                 The Play assets finder.
 */
class SignInController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  configuration: Configuration,
  clock: Clock
)(
  implicit
  assets: AssetsFinder,
  ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {


  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async(parse.json) { implicit request: Request[JsValue] =>
    val forBiddenResult = Forbidden(JsObject.empty)
    request.body.validate[SignInEntity].fold(
      errors => {
        Future.successful(forBiddenResult)
      },
      signInEntity => {
        val credentials = Credentials(signInEntity.email, signInEntity.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) if !user.activated =>
              Future.successful(Ok(JsObject.empty))
            case Some(user) =>
              silhouette.env.authenticatorService.create(loginInfo).map {
                case authenticator => authenticator
              }.flatMap { authenticator =>
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                  silhouette.env.authenticatorService.embed(v, forBiddenResult)
                }
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException =>
            forBiddenResult
        }
      }
    )
  }
}
