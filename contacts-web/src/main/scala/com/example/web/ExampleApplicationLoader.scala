package com.example.web

import com.example.contacts.data.{ContactRepository, InMemoryContactRepository}
import com.example.contacts.service.{ContactService, ContactServiceImpl}
import com.softwaremill.macwire._
import com.typesafe.config.ConfigFactory
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
  lazy val config = ConfigFactory.load()
  lazy val executionContext =
    play.api.libs.concurrent.Execution.Implicits.defaultContext

  lazy val contactRepository: ContactRepository =
    wire[InMemoryContactRepository]

  lazy val contactService: ContactService = wire[ContactServiceImpl]

  lazy val contactController = wire[ContactController]
}
