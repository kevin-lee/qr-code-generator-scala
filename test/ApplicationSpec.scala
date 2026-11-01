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
    example("the session cookie has the Secure flag", testSessionCookieSecure),
    example("redirect the context path with a trailing slash", testContextWithSlash),
    example("keep the query in the redirect of the context path", testContextWithSlashAndQuery),
    example("serve the context path with no trailing slash", testContextWithoutSlash),
    example("do not redirect a POST to the context path with a trailing slash", testPostContextWithSlash),
    example("do not redirect other paths with a trailing slash", testOtherPathsWithSlash),
    example("the JavaScript route for generating QR code has the context path", testJavascriptRouteWithContext),
  )

  private def withApp(f: Application => Result): Result = {
    val app = new GuiceApplicationBuilder().build()
    running(app)(f(app))
  }

  private val context = "/qr-code-generator-scala"

  private def withContextApp(f: Application => Result): Result = {
    val app = new GuiceApplicationBuilder().configure("play.http.context" -> context).build()
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

  def testSessionCookieSecure: Result = withApp { app =>
    withRoute(app, FakeRequest(GET, "/")) { home =>
      app.injector.instanceOf[SessionCookieBaker].encodeAsCookie(session(home)).secure ==== true
    }
  }

  def testContextWithSlash: Result = withContextApp { app =>
    withRoute(app, FakeRequest(GET, s"$context/")) { result =>
      Result.all(List(status(result) ==== MOVED_PERMANENTLY, redirectLocation(result) ==== Some(context)))
    }
  }

  def testContextWithSlashAndQuery: Result = withContextApp { app =>
    withRoute(app, FakeRequest(GET, s"$context/?a=1")) { result =>
      redirectLocation(result) ==== Some(s"$context?a=1")
    }
  }

  def testContextWithoutSlash: Result = withContextApp { app =>
    withRoute(app, FakeRequest(GET, context)) { result =>
      status(result) ==== OK
    }
  }

  def testPostContextWithSlash: Result = withContextApp { app =>
    withRoute(
      app,
      FakeRequest(POST, s"$context/")
        .withJsonBody(Json.obj("url" -> "http://test.com", "width" -> 512, "height" -> 512))
    ) { result =>
      status(result) ==== NOT_FOUND
    }
  }

  def testOtherPathsWithSlash: Result = withContextApp { app =>
    Result.all(
      List(s"$context/boum/", "//example.com/", "/boum/").map { path =>
        withRoute(app, FakeRequest(GET, path)) { result =>
          redirectLocation(result) ==== None
        }
      }
    )
  }

  private val javascriptRouteUrlPattern = """method:"POST", url:"([^"]+)"""".r

  /** The routes prefix is global. The app with no context runs first, so that the context app starts with the default prefix. */
  def testJavascriptRouteWithContext: Result = {
    val defaultPrefixSet = withApp { app =>
      withRoute(app, FakeRequest(GET, "/"))(home => status(home) ==== OK)
    }
    val routeInContext = withContextApp { app =>
      withRoute(app, FakeRequest(GET, s"$context/assets/javascripts/routes")) { routes =>
        javascriptRouteUrlPattern.findFirstMatchIn(contentAsString(routes)).map(_.group(1)) match {
          case Some(url) =>
            withRoute(
              app,
              FakeRequest(POST, url)
                .withJsonBody(Json.obj("url" -> "http://test.com", "width" -> 512, "height" -> 512))
            ) { result =>
              Result.all(List(url ==== context, status(result) ==== OK))
            }

          case None =>
            Result.failure.log("The JavaScript routes have no POST route.")
        }
      }
    }
    Result.all(List(defaultPrefixSet, routeInContext))
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
