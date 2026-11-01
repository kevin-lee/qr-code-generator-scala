package test

import hedgehog.runner._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
object IntegrationSpec extends Properties {

  override def tests: List[Test] = List(
//    example("Application should work from within a browser", {
//      running(TestServer(3333), HTMLUNIT) { browser =>
//
//        browser.goTo("http://localhost:3333/")
//
//        Result.assert(browser.pageSource.contains("QR Code Generator"))
//      }
//    }),
  )

}
