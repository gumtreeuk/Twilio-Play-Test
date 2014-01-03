package controllers

import scala.collection.JavaConversions._
import com.twilio.sdk.TwilioRestClient
import com.twilio.sdk.resource.factory.SmsFactory
import com.twilio.sdk.resource.instance.Sms
import scala.util.Try
import play.api.Logger
import com.twilio.sdk.client.TwilioCapability

/**
 * Service to connect to Twilio
 */
object TwilioAPI {

  private val PHONE_FROM = "+441473379566"

  def sendSMS(sid: String, token: String, to: String, msg: String) = Try {
    Logger.info(s"Sending SMS to $to with text $msg")

    val client = new TwilioRestClient(sid, token)

    val params= Map(("Body", msg), ("To", to), ("From", PHONE_FROM))

    val messageFactory: SmsFactory = client.getAccount.getSmsFactory
    val message: Sms = messageFactory.create(params)

    message.getSid
  }

  def generateCallToken(sid: String, token: String, appSID: String) = Try {
    Logger.info(s"Generating call token for app $appSID")
    val capability = new TwilioCapability(sid, token)
    capability.allowClientOutgoing(appSID)
    capability.generateToken
  }

}
