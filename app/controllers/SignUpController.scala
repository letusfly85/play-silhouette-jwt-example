package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers._
import entities.SignUpEntity
import models.User
import models.services.{AuthTokenService, UserService}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, ControllerComponents, Request}
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Sign Up` controller.
 *
 * @param components             The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param authTokenService       The auth token service implementation.
 * @param avatarService          The avatar service implementation.
 * @param passwordHasherRegistry The password hasher registry.
 * @param mailerClient           The mailer client.
 * @param assets                 The Play assets finder.
 * @param ex                     The execution context.
 */
class SignUpController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  avatarService: AvatarService,
  passwordHasherRegistry: PasswordHasherRegistry,
  mailerClient: MailerClient
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
    request.body.validate[SignUpEntity].fold(
      errors => {
        println(errors.toString)
        Future.successful(forBiddenResult)
      },
      signUpEntity => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, signUpEntity.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            //todo let user to know already user exists
            Future.successful(forBiddenResult)
          case None =>
            val authInfo = passwordHasherRegistry.current.hash(signUpEntity.password)
            val user = User(
              userID = UUID.randomUUID(),
              loginInfo = loginInfo,
              firstName = Some(signUpEntity.firstName),
              lastName = Some(signUpEntity.lastName),
              fullName = Some(signUpEntity.firstName + " " + signUpEntity.lastName),
              email = Some(signUpEntity.email),
              avatarURL = None,
              activated = true
            )
            for {
              avatar <- avatarService.retrieveURL(signUpEntity.email)
              user <- userService.save(user.copy(avatarURL = avatar))
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authToken <- authTokenService.create(user.userID)
            } yield {
              //todo activate user
              /*
              val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
              mailerClient.send(Email(
                subject = Messages("email.sign.up.subject"),
                from = Messages("email.from"),
                to = Seq(data.email),
                bodyText = Some(views.txt.emails.signUp(user, url).body),
                bodyHtml = Some(views.html.emails.signUp(user, url).body)
              ))
              */

              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              println(authToken)
              Ok(JsObject.empty)
            }
        }
      }
    )
  }
}
