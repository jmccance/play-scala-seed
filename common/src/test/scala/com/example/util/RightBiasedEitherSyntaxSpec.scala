package com.example.util

import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable.ListBuffer

class RightBiasedEitherSyntaxSpec
  extends WordSpec
    with Matchers
    with GeneratorDrivenPropertyChecks {

  "RightBiasedEitherSyntax" should {
    "enable flatMap on Either[A, B]" in {
      forAll() { (e: Either[String, Int], f: Int => Either[String, Int]) =>
        e.flatMap(f) should equal(e.right.flatMap(f))
      }
    }

    "enable foreach on Either[A, B]" in {
      forAll() { (e: Either[String, Int]) =>
        val lb1 = ListBuffer[Int]()
        val lb2 = ListBuffer[Int]()

        e.foreach(lb1.append(_)) should equal(())
        e.right.foreach(lb2.append(_)) should equal(())

        lb1 should equal(lb2)
      }
    }

    "enable getOrElse on Either[A, B]" in {
      forAll() { (e: Either[String, Int], n: Int) =>
        e.getOrElse(n) should equal(e.right.getOrElse(n))
      }
    }

    "enable map on Either[A, B]" in {
      forAll() { (e: Either[String, Int], f: Int => Double) =>
        e.map(f) should equal(e.right.map(f))
      }
    }

    "add recover to Either[A, B]" which {
      "preserves the value if it is Right" in {
        forAll() { (e: Either[String, Int], f: String => Int) =>
          whenever(e.isRight) {
            e.recover(f) should equal(e)
          }
        }
      }

      "maps a Left to a Right using f if it is a left" in {
        forAll() { (e: Either[String, Int], f: String => Int) =>
          whenever(e.isLeft) {
            val expected = Right(f(e.left.get))
            e.recover(f) should equal(expected)
          }
        }
      }
    }
  }
}
