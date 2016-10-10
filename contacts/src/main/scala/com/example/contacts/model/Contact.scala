package com.example.contacts.model

import org.scalactic.Accumulation._
import org.scalactic._

case class ContactId(value: String)

case class FirstName(value: String)
case class LastName(value: String)
case class ContactName(first: FirstName, last: LastName)

case class EmailAddress private (value: String)

object EmailAddress {
  def from(s: String): EmailAddress Or Every[EmailAddressValidationError] = {
    // Note: This code is meant to illustrate the idea of using validation functions to accumulate
    // errors. It is not meant to be a serious attempt to validate an email address.

    val hasAtSign = { s: String =>
      if (s.contains('@')) Pass
      else Fail(MissingAtSign(s))
    }

    val hasUserName = { s: String =>
      if (s.matches("^[^@]+@.*$")) Pass
      else Fail(MissingUserName(s))
    }

    val hasHostName = { s: String =>
      if (s.matches("^.*@[^@]+$")) Pass
      else Fail(MissingUserName(s))
    }

    Good(s)
      .when(
        hasAtSign,
        hasUserName,
        hasHostName
      )
      .map(EmailAddress.apply)
  }

  /** Utility method for creating an EmailAddress without validating its contents. Useful as an
    * escape hatch if you just need an EmailAddress but can either guarantee a priori that it's
    * valid or where the code you're exercising doesn't need the wrapped value to be valid.
    *
    * Using this in production code should be considered a red flag.
    */
  def unsafeFrom(s: String): EmailAddress = EmailAddress(s)

  sealed trait EmailAddressValidationError extends Product with Serializable

  case class MissingAtSign(invalidEmail: String)   extends EmailAddressValidationError
  case class MissingUserName(invalidEmail: String) extends EmailAddressValidationError
  case class MissingHostName(invalidEmail: String) extends EmailAddressValidationError
}

case class Contact(id: ContactId, name: ContactName, emailAddress: EmailAddress)

case class NewContact(name: ContactName, emailAddress: EmailAddress) {
  def withId(id: ContactId) = Contact(id, name, emailAddress)
}
