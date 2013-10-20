package controllers

import net.glxn.qrgen._
import net.glxn.qrgen.image._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import play.api.libs.json.Json

import models.Message
import models.QrInfo
import models.QrInfo
import models.MessageType

import java.util.concurrent.TimeUnit
import org.apache.commons.codec.binary.Base64
import services.QrCodeGenerator
import services.Base64CodeGenerator

/**
 * @author Lee, SeongHyun (Kevin)
 * @version 0.0.1 (2013-10-14)
 */
object QrCodeController extends Controller {

  val qrCodeGenerator = Base64CodeGenerator()

  def qrIndex = Action { implicit request =>
    Ok(views.html.qr.index(message = Message.noMessage))
  }

  val qrInfoForm = Form(
    mapping(
      "url" -> text,
      "width" -> number,
      "height" -> number)(QrInfo.apply)(QrInfo.unapply))

  def generateQr = Action { implicit request =>
    val qrInfo = qrInfoForm.bindFromRequest.get
    val qrCode = qrCodeGenerator.generate(qrInfo)
    val message = Message(MessageType.Success, "Success: QR code generation", "QR code has been successfully generated.")

    val result = Json.obj(
      "success" -> true,
      "message" -> Json.obj(
        "messageType" -> message.messageType.value,
        "heading" -> message.heading,
        "body" -> message.body),
      "url" -> qrInfo.url,
      "qrUrl" -> qrCode,
      "width" -> qrInfo.width,
      "height" -> qrInfo.height)
    Ok(result)
  }
}