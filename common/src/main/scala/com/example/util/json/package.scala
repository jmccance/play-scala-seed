package com.example.util

import java.io.InputStream

import play.api.libs.json._
import play.api.libs.json.Json._
import org.scalactic._

package object json {
  /** [[play.api.libs.json.Json#parse(String)]] normally throws an exception if there's an error
    * during parsing.. This function does the same thing without throwing exceptions.
    */
  def parseSafe(s: String): JsValue Or Throwable = attempt(parse(s))

  /** [[play.api.libs.json.Json#parse(Array[Byte])]] normally throws an exception if there's an
    * error during parsing.. This function does the same thing without throwing exceptions.
    */
  def parseSafe(bytes: Array[Byte]): JsValue Or Throwable = attempt(parse(bytes))

  /** [[play.api.libs.json.Json#parse(InputStream)]] normally throws an exception if there's an
    * error during parsing.. This function does the same thing without throwing exceptions.
    */
  def parseSafe(is: InputStream): JsValue Or Throwable = attempt(parse(is))
}
