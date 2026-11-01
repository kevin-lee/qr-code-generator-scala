package controllers

import com.lckymn.kevin.JavascriptRoutable
import play.api.mvc._
import play.api.routing.JavaScriptReverseRoute

import javax.inject.{Inject, Singleton}

/**
 * @author Lee, SeongHyun (Kevin)
 * @version 0.0.1 (2013-06-12)
 */
@Singleton
class Application @Inject() (
  val controllerComponents: ControllerComponents
) extends BaseController
    with JavascriptRoutable {

  /* This must not be a val. The routes prefix is set after this controller is created. */
  def javascriptReverseRoute: List[JavaScriptReverseRoute] =
    List(controllers.routes.javascript.QrCodeController.generateQr)

}
