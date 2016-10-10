package com.example.contacts.service

import com.example.contacts.model.{Contact, EmailAddress}
import org.scalactic.{Every, Or}

import scala.concurrent.{ExecutionContext, Future}

trait ContactService {
  def apply(req: CreateContact)(
      implicit ec: ExecutionContext): Future[Contact Or CreateContactError]

  def apply(req: GetContact)(implicit ec: ExecutionContext): Future[Option[Contact]]
}

case class CreateContact(name: CreateContact.ContactName, emailAddress: String)
object CreateContact {
  case class ContactName(first: String, last: String)
}

// TODO: Enumerate possible errors when contact is created
sealed trait CreateContactError extends Product with Serializable
case class InvalidEmailAddress(errors: Every[EmailAddress.EmailAddressValidationError])
    extends CreateContactError
case object PersistenceError extends CreateContactError

case class GetContact(id: String)
