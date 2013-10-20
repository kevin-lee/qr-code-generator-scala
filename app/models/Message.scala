/**
 *
 */
package models

import scala.None

/**
 * @author Lee, SeongHyun (Kevin)
 * @version 0.0.1 (2013-09-24)
 */
trait Message {
  def messageType: MessageType
  def heading: String
  def body: String
}

case class DefaultMessage(val messageType: MessageType,
  val heading: String,
  val body: String) extends Message {
}

private object NoMessage extends Message {
  val messageType: MessageType = MessageType.noMessageType
  val heading: String = ""
  val body: String = ""
}

object Message {
  val noMessage: Message = NoMessage
  def apply(messageType: MessageType, heading: String, body: String): Message = {
    if ((null == messageType || MessageType.noMessageType == messageType) &&
      (null == heading || heading.isEmpty) &&
      (null == body || body.isEmpty))
      noMessage
    else
      new DefaultMessage(messageType, heading, body)
  }
}

trait MessageType {
  def name: String
  def prefix: String
  def value: String
  def withPrefix: String
}

object NilMessageType extends MessageType {
  val name: String = "None"
  val prefix: String = ""
  val value: String = ""
  val withPrefix: String = ""
}

private case class DefaultMessageType(val name: String,
  val prefix: String = "-",
  val value: String) extends MessageType {
  val withPrefix = prefix + value
}

object MessageType {
  def noMessageType = NilMessageType
  def Info: MessageType = DefaultMessageType(name = "Info", value = "info")
  def Success: MessageType = DefaultMessageType(name = "Success", value = "success")
  def Warning: MessageType = DefaultMessageType(name = "Warning", value = "warning")
  def Danger: MessageType = DefaultMessageType(name = "Danger", value = "danger")
}
