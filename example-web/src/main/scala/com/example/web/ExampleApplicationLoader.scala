package com.example.web

import com.softwaremill.macwire._
import play.api.ApplicationLoader.Context
import play.api._
import play.api.routing.Router

class ExampleApplicationLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    new ApplicationComponents(context).application
  }
}

class ApplicationComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
    with ExampleAppModule {
  override lazy val router: Router = wire[ExampleRouter]
}

trait ExampleAppModule {
  // TODO: Application components go here.
}
