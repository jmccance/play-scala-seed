package com.example.contacts.web

import com.example.contacts.model._
import play.api.libs.functional.syntax._
import play.api.libs.json._

/** play-json serializers for the Contact model. */
trait ContactFormats {
  implicit val contactIdWrites: Writes[ContactId] =
    Writes.of[String].contramap(_.value)

  implicit val firstNameWrites: Writes[FirstName] =
    Writes.of[String].contramap(_.value)
  implicit val lastNameWrites: Writes[LastName] =
    Writes.of[String].contramap(_.value)
  implicit val contactNameWrites: Writes[ContactName] =
    Json.writes[ContactName]

  implicit val emailAddressWrites: Writes[EmailAddress] =
    Writes.of[String].contramap(_.value)

  implicit val contactWrites: Writes[Contact] = Json.writes[Contact]
}
