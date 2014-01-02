package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.TwilioCredentials
import scala.util.{Failure, Success}
import play.Logger
import scala.collection.mutable


object Application extends Controller with TwilioAccess {

  import PageForms._

  var callsReceived : List[String] = Nil

  def index = Action {
    implicit request =>
      Ok(views.html.index(credentialsForm)).withNewSession
  }

  def getCredentials = Action {
    implicit request =>
      credentialsForm.bindFromRequest.fold(
        errors => BadRequest(views.html.index(errors)),
        credentials => Redirect(routes.Application.testTwilio).withSession(setSessionCredentials(credentials.sid, credentials.token, credentials.appSID))
      )
  }

  def testTwilio = hasCredentials {
    (sid, token, appSID) =>
      implicit request =>
        val callToken = TwilioAPI.generateCallToken(sid, token, appSID) match {
          case Failure(ex) => Logger.info(s"Couldn't generate the call token, error: ${ex.getMessage}");""
          case Success(id) => Logger.info(s"Call token $id ready");id
        }
        Ok(views.html.twilio(sid, smsForm, callToken, callsReceived))
  }

  def sendSMS = hasCredentials {
    (sid, token, appSID) =>
      implicit request =>
        smsForm.bindFromRequest.fold(
          errors => BadRequest(views.html.twilio(sid, errors, "", callsReceived)),
          details => {
            val msg = TwilioAPI.sendSMS(sid, token, details.phone, details.msg) match {
              case Failure(ex) => Flash(Map("danger" -> s"Couldn't send sms, error: ${ex.getMessage}"))
              case Success(id) => Flash(Map("success" -> s"SMS sent with id $id"))
            }

            Redirect(routes.Application.testTwilio).flashing(msg)
          }
        )
  }

  def serveCallConfig = Action {
    implicit request =>
      Logger.info(s"Establishing a call form a browser")
      Logger.debug(s"Request: ${request.queryString.toList}")
      val to = request.queryString.get("To").getOrElse(mutable.Buffer()).mkString
      Logger.info(s"Found target number $to")
      val action = routes.Application.callDone.absoluteURL()
      Logger.info(s"Action for recording is $action")
      val xml = s"<Response><Dial callerId='+441473379566' method='GET' action='$action' record='true'><Number>$to</Number></Dial></Response>"
      Ok(xml).as("text/xml")
  }

  def callDone = Action {
    implicit request =>
      Logger.info(s"A call from a browser has ended, storing details")
      Logger.debug(s"Request: ${request.queryString.toList}")
      val status = request.queryString.get("DialCallStatus").getOrElse(mutable.Buffer()).mkString
      val sid = request.queryString.get("DialCallSid").getOrElse(mutable.Buffer()).mkString
      val duration = request.queryString.get("DialCallDuration").getOrElse(mutable.Buffer()).mkString
      val recording = request.queryString.get("RecordingUrl").getOrElse(mutable.Buffer()).mkString
      Logger.info(s"Call $sid terminated with status $status after $duration. Recording available at $recording")
      callsReceived = recording :: callsReceived
      Ok
  }

}
