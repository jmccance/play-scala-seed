package com.example.contacts

/** The web package contains classes specific to the REST API: controllers, routers,
  * JSON formats, and the application loader.
  *
  * Formats are implemented as mix-ins in order to keep the files manageable.
  */
package object web extends ContactFormats with ContactServiceRequestFormats
