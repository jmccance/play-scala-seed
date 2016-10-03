package com.example.web

import play.api.libs.json.Json._
import play.api.mvc.{Action, _}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ExampleRouter extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/api/hello") =>
      Action {
        // FIXME: Placeholder demo route
        Results.Ok(obj("greeting" -> "Hello!"))
      }
  }
}
