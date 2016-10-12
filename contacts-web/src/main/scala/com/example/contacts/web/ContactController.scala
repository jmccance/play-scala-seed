package com.example.contacts.web

import com.example.contacts.service.{ContactService, CreateContact, GetContact}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json._
import play.api.mvc._

class ContactController(contactService: ContactService) extends Controller {
  def get(id: String) = Action.async { req =>
    contactService(GetContact(id)).map {
      case Some(contact) => Ok(toJson(contact))
      case None => NotFound
    }
  }

  def create = Action.async(BodyParsers.parse.json[CreateContact]) { req =>
    contactService(req.body).map {
      _.fold(
        contact => Ok(toJson(contact)),
        errors => BadRequest(errors.toString)
      )
    }
  }
}
