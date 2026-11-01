package filters

import play.api.http.HttpConfiguration
import play.api.http.HttpVerbs.{GET, HEAD}
import play.api.libs.streams.Accumulator
import play.api.mvc.{EssentialAction, EssentialFilter, RequestHeader, Results}

import javax.inject.{Inject, Singleton}

/**
 * The routes have no match for the context path with a trailing slash, so redirect it to the context path.
 * This handles only the context path. A redirect for any path with a trailing slash would be an open redirect,
 * because "//example.com/" would redirect to "//example.com".
 */
@Singleton
class TrailingSlashFilter @Inject() (httpConfiguration: HttpConfiguration) extends EssentialFilter {

  private val context = httpConfiguration.context

  private def isContextWithSlash(request: RequestHeader): Boolean =
    (request.method == GET || request.method == HEAD) && context != "/" && request.path == s"$context/"

  def apply(next: EssentialAction): EssentialAction = EssentialAction { request =>
    if (isContextWithSlash(request)) {
      val query = if (request.rawQueryString.isEmpty) "" else s"?${request.rawQueryString}"
      Accumulator.done(Results.MovedPermanently(s"$context$query"))
    } else {
      next(request)
    }
  }
}
