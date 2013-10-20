/**
 *
 */
package com.lckymn.kevin

import play.api._
import play.api.mvc._
import play.core.Router.JavascriptReverseRoute
import play.core.Router._

/**
 * @author Lee, SeongHyun (Kevin)
 * @version 0.0.1 (2013-10-14)
 */
trait JavascriptRoutable extends Results {
  val javascriptReverseRoute: List[JavascriptReverseRoute]

  def javascriptRoutes = Action { implicit request =>
    import controllers.routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        javascriptReverseRoute:_*
      )
    ).as("text/javascript") 
  }
}