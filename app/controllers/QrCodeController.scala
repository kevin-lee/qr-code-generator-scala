package controllers

import models.Message
import models.MessageType
import models.QrInfo
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.text
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
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

    Ok(Json.obj(
      "success" -> true,
      "message" -> Json.obj(
        "messageType" -> message.messageType.value,
        "heading" -> message.heading,
        "body" -> message.body),
      "url" -> qrInfo.url,
      "qrUrl" -> qrCode,
      "width" -> qrInfo.width,
      "height" -> qrInfo.height))
  }
}