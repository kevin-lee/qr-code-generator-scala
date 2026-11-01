package controllers

import models.Message
import models.MessageType
import models.QrInfo
import org.webjars.play.WebJarsUtil
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.data.Forms.text
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.mvc._
import services.Base64CodeGenerator

import javax.inject.{Inject, Singleton}

/**
 * @author Lee, SeongHyun (Kevin)
 * @version 0.0.1 (2013-10-14)
 */
@Singleton
class QrCodeController @Inject() (
  val controllerComponents: ControllerComponents
)(implicit webJarsUtil: WebJarsUtil)
    extends BaseController {

  val qrCodeGenerator = Base64CodeGenerator()

  def qrIndex: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.qr.index(message = Message.noMessage))
  }

  val qrInfoForm: Form[QrInfo] = Form(
    mapping(
      "url" -> text,
      "width" -> number,
      "height" -> number)(QrInfo.apply)(QrInfo.unapply))

  private def messageToJson(message: Message): JsObject =
    Json.obj(
      "messageType" -> message.messageType.value,
      "heading" -> message.heading,
      "body" -> message.body)

  def generateQr: Action[AnyContent] = Action { implicit request =>
    qrInfoForm.bindFromRequest().fold(
      _ =>
        BadRequest(Json.obj(
          "success" -> false,
          "message" -> messageToJson(Message(MessageType.Danger, "Error: QR code generation", "The URL, width or height is not valid.")))),
      qrInfo =>
        Ok(Json.obj(
          "success" -> true,
          "message" -> messageToJson(Message(MessageType.Success, "Success: QR code generation", "QR code has been successfully generated.")),
          "url" -> qrInfo.url,
          "qrUrl" -> qrCodeGenerator.generate(qrInfo),
          "width" -> qrInfo.width,
          "height" -> qrInfo.height))
    )
  }
}
