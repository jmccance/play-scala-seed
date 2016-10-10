package com.example.contacts.service

import com.example.contacts.data.ContactRepository
import com.example.contacts.model._
import org.scalactic.{Bad, Good, Or}

import scala.concurrent.{ExecutionContext, Future}

class ContactServiceImpl(contactRepository: ContactRepository) extends ContactService {
  override def apply(req: CreateContact)(
      implicit ec: ExecutionContext): Future[Or[Contact, CreateContactError]] = {
    val newContactOrError =
      EmailAddress
        .from(req.emailAddress)
        .map { address =>
          NewContact(
            ContactName(FirstName(req.name.first), LastName(req.name.last)),
            address
          )
        }
        .badMap(InvalidEmailAddress.apply)

    newContactOrError match {
      case Good(newContact) =>
        contactRepository.create(newContact).map { contactOrError =>
          contactOrError.badMap(_ => PersistenceError)
        }

      case Bad(err) => Future.successful(Bad(err))
    }
  }

  override def apply(req: GetContact)(implicit ec: ExecutionContext): Future[Option[Contact]] =
    contactRepository.get(ContactId(req.id))
}
