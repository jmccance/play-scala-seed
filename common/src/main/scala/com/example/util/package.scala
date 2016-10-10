package com.example

package object util {

  /** Allow Eithers to be treated as right-biased, so that methods like `map` and `flatMap` can be
    * invoked directly without needing to use `right`.
    *
    * This conversion will not be needed in Scala 2.12:
    *   http://www.scala-lang.org/news/2.12.0-RC1
    */
  implicit class RightBiasedEitherSyntax[A, B](val underlying: Either[A, B])
      extends AnyVal {

    def flatMap[AA >: A, Y](f: B => Either[AA, Y]): Either[AA, Y] =
      underlying.right.flatMap(f)

    def foreach[U](f: B => U): Unit = {
      underlying.right.foreach(f)
      ()
    }

    def getOrElse[BB >: B](or: => BB): BB = underlying.right.getOrElse(or)

    def map[Y](f: B => Y): Either[A, Y] = underlying.right.map(f)

    // Note: At this time recover does not have an analogue, even in 2.12.
    def recover[BB >: B](f: A => BB): Either[A, BB] = underlying match {
      case Left(a) => Right(f(a))
      case _ => underlying
    }
  }

  /** Run a function on a value and then return that value. Useful when you want to run a function
    * on a value but still return the same value. The most common use case is for initializing
    * mutable things from Java libraries.
    *
    * {{{
    *   val w: Widget = tap(new Widget) { f =>
    *     f.setHeight(6)
    *     f.setWidth(7)
    *     f.setPowerLevel(9000.01)
    *   }
    * }}}
    */
  def tap[A](a: A): (A => Any) => A = { f =>
    f(a)
    a
  }

  /** Create a function that, when run on a value, will return that value.
    * Useful for the same reasons as [[com.example.util.tap]], but
    * specialized for function composition. In this context, the usual use
    * case is to run a side-effecting function inside a chain of function calls.
    *
    * {{{
    *   val transformer =
    *     (buildFoo
    *       andThen twiddleFoo
    *       andThen tapWith { f =>
    *         logger.debug(s"Current state of foo: $f")
    *       }
    *       andThen convertFooToBar
    *       andThen convertBarToBaz)
    * }}}
    */
  def tapWith[A](f: A => Any): A => A = tap(_)(f)
}
