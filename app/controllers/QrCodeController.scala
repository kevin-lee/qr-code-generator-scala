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

/**
 * @author Lee, SeongHyun (Kevin)
 * @version 0.0.1 (2013-10-14)
 */
object QrCodeController extends Controller {

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

    val message = Message(MessageType.Success, "Success: QR code generation", "QR code has been successfully generated.")

    Ok(Json.parse(s"""{
  "success": true,
  "message": {
    "messageType": "${message.messageType.value}",
    "heading": "${message.heading}",
    "body": "${message.body}"
  },
  "url":"${qrInfo.url}",
  "qrUrl":"${generateQrWithBase64Image(qrInfo)}",
  "width": ${qrInfo.width},
  "height": ${qrInfo.height}
}
"""))
  }

  private def generateQrWithBase64Image(qrInfo: QrInfo): String = {
    val prefix = "data:image/gif;base64,"
    val encodedByte = Base64.encodeBase64(QRCode.from(qrInfo.url)
      .to(ImageType.GIF)
      .withSize(qrInfo.width, qrInfo.height)
      .withCharset("UTF-8")
      .stream().toByteArray())

    prefix + new String(encodedByte)
  }
}