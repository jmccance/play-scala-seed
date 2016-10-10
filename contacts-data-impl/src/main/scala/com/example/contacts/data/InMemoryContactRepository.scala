package com.example.contacts.data
import com.example.contacts.model.{Contact, ContactId, NewContact}
import org.scalactic.{Good, Or}

import scala.concurrent.{ExecutionContext, Future}

class InMemoryContactRepository extends ContactRepository {
  @volatile private[this] var contacts: Map[ContactId, Contact] = Map.empty

  override def get(id: ContactId)(implicit ec: ExecutionContext): Future[Option[Contact]] = {
    Future.successful(contacts.get(id))
  }

  override def create(newContact: NewContact)(
      implicit ec: ExecutionContext): Future[Or[Contact, CreateContactError]] = {
    Future.successful {
      val id      = ContactId(java.util.UUID.randomUUID().toString)
      val contact = newContact.withId(id)

      contacts = contacts + (id -> contact)
      Good(contact)
    }
  }
}
