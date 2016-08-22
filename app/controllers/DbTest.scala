package controllers

import javax.inject.Inject

import play.api.mvc._
import util.DBService

class DbTest @Inject()(db: DBService) extends Controller {

  def index = Action { implicit result =>
    Ok(views.html.db("DBTest"))
  }

  def dbTest = Action(parse.multipartFormData) { implicit request =>
    val path = request.body.dataParts("path").head

    if (db.addByFilepath(1, null, null, path)) {
      Redirect(routes.DbTest.index()).flashing("message" -> "Success")
    } else {
      Redirect(routes.DbTest.index()).flashing("message" -> "Failure")
    }
  }
}