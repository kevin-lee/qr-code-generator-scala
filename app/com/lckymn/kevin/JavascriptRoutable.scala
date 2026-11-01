/**
 *
 */
package com.lckymn.kevin

import play.api.mvc._
import play.api.routing.{JavaScriptReverseRoute, JavaScriptReverseRouter}

/**
 * @author Lee, SeongHyun (Kevin)
 * @version 0.0.1 (2013-10-14)
 */
trait JavascriptRoutable { self: BaseController =>

  def javascriptReverseRoute: List[JavaScriptReverseRoute]

  def javascriptRoutes: Action[AnyContent] = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        javascriptReverseRoute: _*
      )
    ).as("text/javascript")
  }
}
