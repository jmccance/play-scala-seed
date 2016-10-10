package com.example.web

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ExampleRouter(contactController: ContactController) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/api/contact/$id") => contactController.get(id)
    case POST(p"/api/contact")    => contactController.create
  }
}
