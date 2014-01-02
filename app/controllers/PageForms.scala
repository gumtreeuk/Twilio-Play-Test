package controllers

import play.api.data.Form
import models.{TwSMS, TwilioCredentials}
import play.api.data.Forms._

/**
 * All forms used by the app
 */
object PageForms {

  val credentialsForm: Form[TwilioCredentials] = Form(
    mapping(
      "sid" -> nonEmptyText,
      "token" -> nonEmptyText,
      "appSID" -> nonEmptyText
    )(TwilioCredentials.apply)(TwilioCredentials.unapply)
  )

  val smsForm : Form[TwSMS] = Form(
    mapping(
      "to" -> nonEmptyText,
      "msg" -> nonEmptyText
    )(TwSMS.apply)(TwSMS.unapply)
  )

}
