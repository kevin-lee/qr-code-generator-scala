package test

import hedgehog._
import hedgehog.runner._
import models.Message
import models.MessageType
import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode
import org.apache.commons.codec.binary.Base64
import play.api.Application
import play.api.http.Writeable
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{CookieHeaderEncoding, Request, SessionCookieBaker}
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
object ApplicationSpec extends Properties {

  override def tests: List[Test] = List(
    example("send 404 on a bad request", testBadRequest),
    example("render the index page", testIndexPage),
    example("generate QR code", testGenerateQrCode),
    example("post with an invalid width", testInvalidWidth),
    example("post with a session cookie but no CSRF token", testPostWithoutCsrfToken),
    example("post with the session cookie and the CSRF token from the index page", testPostWithCsrfToken),
  )

  private def withApp(f: Application => Result): Result = {
    val app = new GuiceApplicationBuilder().build()
    running(app)(f(app))
  }

  private def withRoute[A: Writeable](app: Application, request: Request[A])(
    f: Future[play.api.mvc.Result] => Result
  ): Result =
    route(app, request).fold(Result.failure.log(s"No route for ${request.method} ${request.uri}"))(f)

  def testBadRequest: Result = withApp { app =>
    val result = route(app, FakeRequest(GET, "/boum")).map(status(_))
    result ==== Some(NOT_FOUND)
  }

  def testIndexPage: Result = withApp { app =>
    withRoute(app, FakeRequest(GET, "/")) { home =>
      val content = contentAsString(home)
      Result.all(
        List(
          status(home) ==== OK,
          contentType(home) ==== Some("text/html"),
          Result.assert(content.contains("QR Code Generator")).log(content),
          Result.assert(!content.contains("<!-- Could not")).log(content),
        )
      )
    }
  }

  def testGenerateQrCode: Result = withApp { app =>
    val url    = "http://test.com"
    val width  = 512
    val height = 512

    withRoute(
      app,
      FakeRequest(POST, "/")
        .withJsonBody(Json.obj("url" -> url, "width" -> width, "height" -> height))
    ) { result =>
      val message = Message(MessageType.Success, "Success: QR code generation", "QR code has been successfully generated.")

      val encodedByte = Base64.encodeBase64(
        QRCode
          .from(url)
          .to(ImageType.GIF)
          .withSize(width, height)
          .withCharset("UTF-8")
          .stream()
          .toByteArray()
      )

      val qrUrl = "data:image/gif;base64," + new String(encodedByte)

      Result.all(
        List(
          status(result) ==== OK,
          contentType(result) ==== Some("application/json"),
          Json.parse(contentAsString(result)) ==== Json.obj(
            "success" -> true,
            "message" -> Json.obj(
              "messageType" -> message.messageType.value,
              "heading"     -> message.heading,
              "body"        -> message.body
            ),
            "url"     -> url,
            "qrUrl"   -> qrUrl,
            "width"   -> width,
            "height"  -> height
          ),
        )
      )
    }
  }

  def testInvalidWidth: Result = withApp { app =>
    withRoute(
      app,
      FakeRequest(POST, "/")
        .withJsonBody(Json.obj("url" -> "http://test.com", "width" -> "abc", "height" -> 512))
    ) { result =>
      Result.all(
        List(
          status(result) ==== BAD_REQUEST,
          (Json.parse(contentAsString(result)) \ "success").asOpt[Boolean] ==== Some(false),
        )
      )
    }
  }

  private val csrfTokenPattern = """<meta name="csrf-token" content="([^"]+)">""".r

  /** Makes a POST request that carries the session of the given result, as a browser does.
    * FakeRequest does not read the session from the Cookie header, so the session is set with withSession.
    * The Cookie header is still necessary because the CSRF filter checks only requests that have it.
    */
  private def postWithSessionOf(app: Application, home: Future[play.api.mvc.Result]) = {
    val sessionCookie = app.injector.instanceOf[SessionCookieBaker].encodeAsCookie(session(home))
    val cookieHeader  = app.injector.instanceOf[CookieHeaderEncoding].encodeCookieHeader(Seq(sessionCookie))
    FakeRequest(POST, "/")
      .withSession(session(home).data.toSeq: _*)
      .withHeaders(COOKIE -> cookieHeader)
      .withJsonBody(Json.obj("url" -> "http://test.com", "width" -> "512", "height" -> "512"))
  }

  def testPostWithoutCsrfToken: Result = withApp { app =>
    withRoute(app, FakeRequest(GET, "/")) { home =>
      withRoute(app, postWithSessionOf(app, home)) { result =>
        status(result) ==== FORBIDDEN
      }
    }
  }

  def testPostWithCsrfToken: Result = withApp { app =>
    withRoute(app, FakeRequest(GET, "/")) { home =>
      csrfTokenPattern.findFirstMatchIn(contentAsString(home)).map(_.group(1)) match {
        case Some(token) =>
          withRoute(app, postWithSessionOf(app, home).withHeaders("Csrf-Token" -> token)) { result =>
            Result.all(
              List(
                status(result) ==== OK,
                (Json.parse(contentAsString(result)) \ "url").asOpt[String] ==== Some("http://test.com"),
              )
            )
          }

        case None =>
          Result.failure.log("The index page has no CSRF token.")
      }
    }
  }
}
