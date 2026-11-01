package test

import hedgehog._
import hedgehog.runner._
import models.QrInfo

object QrInfoSpec extends Properties {

  override def tests: List[Test] = List(
    property("a size below 32 becomes 100", testSizeBelowMinimum),
    property("a size of 32 or more does not change", testValidSize),
    property("the URL is trimmed", testUrlTrimmed),
  )

  def testSizeBelowMinimum: Property =
    for {
      width  <- Gen.int(Range.linear(-1024, 31)).log("width")
      height <- Gen.int(Range.linear(-1024, 31)).log("height")
    } yield {
      val qrInfo = QrInfo("http://test.com", width, height)
      Result.all(List(qrInfo.width ==== 100, qrInfo.height ==== 100))
    }

  def testValidSize: Property =
    for {
      width  <- Gen.int(Range.linear(32, 4096)).log("width")
      height <- Gen.int(Range.linear(32, 4096)).log("height")
    } yield {
      val qrInfo = QrInfo("http://test.com", width, height)
      Result.all(List(qrInfo.width ==== width, qrInfo.height ==== height))
    }

  def testUrlTrimmed: Property =
    for {
      url <- Gen.string(Gen.alphaNum, Range.linear(1, 50)).log("url")
    } yield QrInfo(s"  $url  ", 64, 64).url ==== url
}
