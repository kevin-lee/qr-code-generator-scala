package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import models.Message
import models.MessageType
import org.apache.commons.codec.binary.Base64
import net.glxn.qrgen.QRCode
import net.glxn.qrgen.image.ImageType

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in {
      running(FakeApplication()) {
        route(FakeRequest(GET, "/boum")) must beNone
      }
    }

    "render the index page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/")).get

        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain("QR Code Generator")
      }
    }

    "generate QR code" in {
      running(FakeApplication()) {
        val url = "http://test.com"
        val width = 512
        val height = 512
        val result = route(FakeRequest(POST, "/")
          .withJsonBody(Json.obj(
            "url" -> url,
            "width" -> width,
            "height" -> height))).get

        val message = Message(MessageType.Success, "Success: QR code generation", "QR code has been successfully generated.")

        val encodedByte = Base64.encodeBase64(QRCode.from(url)
          .to(ImageType.GIF)
          .withSize(width, height)
          .withCharset("UTF-8")
          .stream().toByteArray())

        val qrUrl = "data:image/gif;base64," + new String(encodedByte)

        status(result) must equalTo(OK)
        contentType(result) must beSome.which(_ == "application/json")
        Json.parse(contentAsString(result)) must equalTo(Json.parse(s"""{
  "success": true,
  "message": {
    "messageType": "${message.messageType.value}",
    "heading": "${message.heading}",
    "body": "${message.body}"
  },
  "url":"${url}",
  "qrUrl":"${qrUrl}",
  "width": ${width},
  "height": ${height}
}
"""))
      }
    }
  }
}