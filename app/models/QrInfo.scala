/**
 *
 */
package models

import java.util.Arrays
import java.util.Objects

/**
 * @author Lee, SeongHyun (Kevin)
 * @version 0.0.1 (2013-10-14)
 */
class QrInfo(val url: String, w: Int = 64, h: Int = 64) {
  val width: Int = if (w < 32) 100 else w
  val height: Int = if (h < 32) 100 else h

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: QrInfo =>
        (this.url == that.url &&
          this.width == that.width &&
          this.height == that.height)
      case _ => false
    }
  }
  override val hashCode = Objects.hash(url, Integer.valueOf(width), Integer.valueOf(height))
}

object QrInfo {
  def apply(url: String) = new QrInfo(url match {
    case null => ""
    case _ => url.trim()
  })
  def apply(url: String, width: Int, height: Int) = new QrInfo(url match {
    case null => ""
    case _ => url.trim()
  }, width, height)

  def unapply(qrInfo: QrInfo): Option[(String, Int, Int)] = {
    Option((qrInfo.url, qrInfo.width, qrInfo.height))
  }
}