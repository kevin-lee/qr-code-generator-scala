/**
 *
 */
package services

import models.QrInfo
import org.apache.commons.codec.binary.Base64
import net.glxn.qrgen.QRCode
import net.glxn.qrgen.image.ImageType

/**
 * @author Lee, SeongHyun (Kevin)
 * @version 0.0.1 (2013-10-20)
 */
trait QrCodeGenerator[T] {
  def generate(qrInfo: QrInfo): T
}

class Base64CodeGenerator extends QrCodeGenerator[String] {
  def generate(qrInfo: QrInfo): String = {
    val prefix = "data:image/gif;base64,"
    val encodedByte = Base64.encodeBase64(QRCode.from(qrInfo.url)
      .to(ImageType.GIF)
      .withSize(qrInfo.width, qrInfo.height)
      .withCharset("UTF-8")
      .stream().toByteArray())

    prefix + new String(encodedByte)
  }
}

object Base64CodeGenerator {
  def apply() = new Base64CodeGenerator
}