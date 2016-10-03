package com.example.util

import java.time.Duration

import com.typesafe.config.{Config, ConfigException}
import org.scalactic._

import scala.collection.JavaConverters._

/** Utility methods for working with instances of [[com.typesafe.config.Config]]. This package
  * primarily adds extension methods to instances of Config that return errors using
  * [[org.scalactic.Or]] instead of throwing exceptions, where the Bad type is
  * [[com.typesafe.config.ConfigException]]. This allows for extracting config classes with
  * accumulating errors. For example:
  *
  * {{{
  *   import org.scalactic._
  *   import org.scalactic.Accumulation._ // for `withGood`
  *
  *   case class FooConfig(maxSize: Int, timeout: Duration)
  *
  *   object FooConfig {
  *     def from(config: Config): FooConfig Or Every[ConfigException] = {
  *       withGood(
  *         config.int("max-size"),
  *         config.duration("timeout")
  *       )(FooConfig.apply)
  *     }
  *   }
  * }}}
  *
  */
package object config {

  implicit class ConfigOps(val config: Config) extends AnyVal {

    def boolean(path: String): Boolean Or Every[ConfigException] =
      lift(config.getBoolean)(path)

    def booleanList(path: String): Seq[Boolean] Or Every[ConfigException] =
      lift(config.getBooleanList)(path).map(_.asScala.map(Boolean2boolean))

    def subconfig(path: String): Config Or Every[ConfigException] =
      lift(config.getConfig)(path)

    def memorySize(path: String): Long Or Every[ConfigException] =
      lift(config.getBytes)(path).map(_.toLong)

    def memorySizeList(path: String): Seq[Long] Or Every[ConfigException] =
      lift(config.getBytesList)(path).map(_.asScala.map(_.toLong))

    def double(path: String): Double Or Every[ConfigException] =
      lift(config.getDouble)(path)

    def doubleList(path: String): Seq[Double] Or Every[ConfigException] =
      lift(config.getDoubleList)(path).map(_.asScala.map(_.toDouble))

    def duration(path: String): Duration Or Every[ConfigException] =
      lift(config.getDuration)(path)

    def durationList(path: String): Seq[Duration] Or Every[ConfigException] =
      lift(config.getDurationList)(path).map(_.asScala)

    def int(path: String): Int Or Every[ConfigException] =
      lift(config.getInt)(path)

    def intList(path: String): Seq[Int] Or Every[ConfigException] =
      lift(config.getIntList)(path).map(_.asScala.map(_.toInt))

    def long(path: String): Long Or Every[ConfigException] =
      lift(config.getLong)(path)

    def longList(path: String): Seq[Long] Or Every[ConfigException] =
      lift(config.getLongList)(path).map(_.asScala.map(_.toLong))

    def number(path: String): Number Or Every[ConfigException] =
      lift(config.getNumber)(path)

    def numberList(path: String): Seq[Number] Or Every[ConfigException] =
      lift(config.getNumberList)(path).map(_.asScala)

    def string(path: String): String Or Every[ConfigException] =
      lift(config.getString)(path)

    def stringList(path: String): Seq[String] Or Every[ConfigException] =
      lift(config.getStringList)(path).map(_.asScala)

    /** Lift `f` into Or[A, Every[ConfigException] so that if any ConfigExceptions are thrown
      * they'll be caught and return as a Bad.
      */
    private def lift[A, B](f: A => B): A => B Or Every[ConfigException] = { a =>
      attempt(f(a))
        .badMap {
          case ce: ConfigException => One(ce)
          case t: Throwable =>
            // The Typesafe API insists that it only throws ConfigExceptions. We'll respect this
            // claim for now and treat unexpected exceptions as unrecoverable failures.
            throw t
        }
    }
  }
}
