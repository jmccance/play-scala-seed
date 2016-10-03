package com.example.util

import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable.ListBuffer

class TapSpec extends WordSpec with Matchers with GeneratorDrivenPropertyChecks {
  "tap" should {
    "return the value passed in" in {
      forAll() { (a: String, f: String => Int) =>
        tap(a)(f) should equal(a)
      }
    }

    "execute the function on the value" in {
      forAll() { s: String =>
        tap(ListBuffer[String]())(_.append(s)) should equal(ListBuffer[String](s))
      }
    }
  }

  "tapWith" should {
    "return the value passed in" in {
      forAll() { (a: String, f: String => Int) =>
        tapWith(f)(a) should equal(a)
      }
    }

    "execute the function on the value" in {
      forAll() { s: String =>
        tap(ListBuffer[String]())(_.append(s)) should equal(ListBuffer[String](s))
      }
    }

    "compose easily with other functions" in {
      """val f: String => Int = _.length
        |val g: Int => List[Int] = List(_)
        |
        |val fg: String => Int = f andThen tapWith(g)""".stripMargin should compile
    }
  }
}
