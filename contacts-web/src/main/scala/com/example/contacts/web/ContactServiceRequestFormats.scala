package com.example.contacts.web

import com.example.contacts.service.{CreateContact, GetContact}
import play.api.libs.json._

trait ContactServiceRequestFormats {
  implicit val createContactContactNameFormat = Json.format[CreateContact.ContactName]
  implicit val createContactFormat            = Json.format[CreateContact]

  implicit val getContactFormat = Json.format[GetContact]
}
