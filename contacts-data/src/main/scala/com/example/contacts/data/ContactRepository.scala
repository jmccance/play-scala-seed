package com.example.contacts.data

import com.example.contacts.model.{Contact, ContactId, NewContact}
import org.scalactic.Or

import scala.concurrent.{ExecutionContext, Future}

trait ContactRepository {
  def get(id: ContactId)(implicit ec: ExecutionContext): Future[Option[Contact]]

  def create(contact: NewContact)(
      implicit ec: ExecutionContext): Future[Contact Or CreateContactError]
}

// TODO Enumerate save contact errors
case class CreateContactError(cause: Throwable)
