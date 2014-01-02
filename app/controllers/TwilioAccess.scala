package controllers

import play.api.mvc._
import play.api.libs.iteratee.{Input, Done}

/**
  * Checks the user has the Twilio sid/auth token in the session before allowing requests
  */
trait TwilioAccess {

  private val TOKEN: String = "token"
  private val SID: String = "sid"
  private val APPSID: String = "appSID"

  private def sid(request: RequestHeader) = request.session.get(Application.SID)

  private def auth(request: RequestHeader) = request.session.get(Application.TOKEN)

  private def appSID(request: RequestHeader) = request.session.get(Application.APPSID)

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.index).flashing("warning" -> "SID, App SID or Auth token missing in session")

  def setSessionCredentials(sid: String, token: String, appSID: String) = Session(Map((SID, sid), (TOKEN, token), (APPSID, appSID)))

  def hasCredentials(f: => (String, String, String) => Request[AnyContent] => Result) = credentialsPresent(onUnauthorized) {
    (sid, token, appSID) =>
      Action(request => f(sid, token, appSID)(request))
  }

  private def credentialsPresent[A](onUnauthorized: RequestHeader => SimpleResult)(action: (String, String, String) => EssentialAction): EssentialAction = {
    EssentialAction {
      request =>
        val sid = request.session.get(Application.SID)
        val token = request.session.get(Application.TOKEN)
        val appSID = request.session.get(Application.APPSID)

        if (sid.isEmpty || token.isEmpty || appSID.isEmpty)
          Done(onUnauthorized(request), Input.Empty)
        else
          action(sid.get, token.get, appSID.get)(request)
    }
  }
}