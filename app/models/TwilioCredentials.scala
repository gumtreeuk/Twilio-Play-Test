package models


case class TwilioCredentials (sid: String, token: String, appSID: String)

case class TwSMS(phone: String, msg: String)
