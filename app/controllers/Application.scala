package controllers

import play.api._
import play.api.mvc._
import com.lckymn.kevin.JavascriptRoutable

/**
 * @author Lee, SeongHyun (Kevin)
 * @version 0.0.1 (2013-06-12)
 */
object Application extends Controller with JavascriptRoutable {
  val javascriptReverseRoute = List(controllers.routes.javascript.QrCodeController.generateQr)

}