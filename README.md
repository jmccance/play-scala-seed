play-scala-seed
===============

This project captures some patterns, tips, and opinions I've acquired for Scala projects. This README will attempt to explain some of the specific choices I'm making and why.

This is a work in progress. Any section with a "Notes" subsection is even more WIP than others, and I'm just using that to capture my thoughts prior to a clearer write-up. Caveat programmator.

## Importing into IntelliJ

When any sbt project into IntelliJ, I recommend these settings:

* Uncheck "Use auto-import". This setting determines whether IntelliJ will automatically import the project whenever any changes are made to the build files. Reimporting an sbt project takes a few seconds, so this ends up being more annoying than helpful if we're making larger changes to our build. We can always reimport by either opening the sbt pane in IntelliJ and clicking the refresh button, or by opening the "Open anything" box and choose "Refresh all projects".
* Under "Downloads", check "Sources for SBT and plugins". This will make it much easier to figure out sbt problems, since we'll have ready access to the code through IntelliJ.

## Build organization

### Dependencies

In single-project, single-developer builds, managing dependencies isn't particularly complicated. Declaring them all in a single block in build.sbt, maybe with some extract variables for things that need to be on the same version, is usually fine. Larger projects have their own challenges, however, and it's nice to have all dependencies defined in a single place. The pattern below describes a way of doing this that has worked well for me in the past. With this system, it's easy to keep versions and dependencies consistent across sub-projects, and developers can add new dependencies without stepping on each other's toes.

Dependencies are defined in `project/Dependencies.scala`. Remember that anything defined in a Scala file in `project` will be available in our build.sbt and any other top-level sbt files.

Dependencies are organized into objects and fields. The idea is that each organization gets an object with a reasonable, human-selected name, like "ScalaTest" or "Typesafe". When one organization has significant sub-organizations, nested objects can be used. An example from this project is the "Play" object inside of the Typesafe object. The dependencies themselves are simply the artifact name converted to camelCase. (E.g., "play-json" becomes "playJson".)

This system is meant to help ensure that when multiple developers are working on the same project they can add dependencies in a consistent manner without naming collisions.

The upshot of this is that when we go to add dependencies to a project, it looks like this:

```scala
libraryDependencies ++= Seq(
  Dependencies.Typesafe.Play.playJson,
  Dependencies.ScalaTest.scalatest % Test
)
```

### Build definitions

All build definitions are located in a single, top-level `build.sbt` file. While sbt supports placing a `build.sbt` with each individual project, in my experience it rapidly becomes hard to keep track of which settings are applied to which projects. Consolidating everything into a top-level file makes things much easier to keep track of.

Project definitions come first, since people are generally initially interested in either which projects the build contains or in some specific project. The vals are named the same as the directory, which is also the artifact name. This is done both so its easy to identify which folder corresponds to which artifact, and so that IntelliJ doesn't need to show two names in the project view. (If we had a project directory `foo-core` and a the project val was `fooCore`, IntelliJ would display it as something like `fooCore [foo-core]`.)

Settings are defined as Seqs of settings further down in the file. Defining them as individual Seqs helps keep the build organized. An unpleasant pattern I've seen is to declare one `commonSettings` Seq that contains all sorts of unrelated settings. It can become challenging to determine which settings are related to each other in a collection like that, and stray settings can easily be left behind when people remove functionality that is no longer needed.

Instead, logical groups of settings are organized into named Seqs, and a `commonSettings` Seq is built up from that. A quick skim of `commonSettings` will tell us what is added to each build, and if we realize later that something we thought was common is actually specific to a subset of projects, it can easily be taken out and applied only to the projects that specifically need it.

### Plugins

This project includes several plugins that I think are worthwhile for any project:

* [sbt-scalafmt](): Code formatter for Scala. Avoid format wars and nitpicking in PRs by having a computer do it for us. The formatting isn't *quite* perfect, but it's good enough.
* [sbt-release](): Provides support for running automated releases. By default, this consists of setting the version to non-SNAPSHOT, tagging the commit, publishing to a repository, bumping to the next SNAPSHOT version, and committing that. These steps support a large amount of customization, however.
* [sbt-scoverage](): Provides code coverage analysis, which can be a useful metric for identifying under-exercised sections of our code base.

Additionally, the sbt plugin for Play is included since this project includes a Play-based REST API.

### Custom sbt plugins

While not needed for this project, they're worth mentioning. Writing an sbt plugin is [surprisingly trivial][writing-sbt-plugins]. If we find ourselves managing several repositories that all share common settings (e.g., artifact repositories, release settings, etc.), it's definitely worth implementing our own plugin to help keep these settings consistent and reduce the time it takes to start up a new repository.

[writing-sbt-plugins]: http://www.scala-sbt.org/0.13/docs/Plugins.html

## Configuration

### Notes

Use HOCON and Typesafe Config. Use HOCON well, taking advantage of nesting and duration types. Use `spine-case` to be consistent with other majors consumers of HOCON, like Akka and Play. Separate parsing the config from modeling your config. Use applicative validation to parse config in such a way that you get all the errors in it at the same time. Model your configuration as case classes; individual objects should not know where it came from. (Makes it easier to write tests too; you don’t need to create a Config object in order to initialize your SUT.)  Make sure your config parser is ignorant of where in the config object it is; it should expect the root of its hierarchy. This helps decouple your config parser from the structure of your config file.

Read [this blog post](http://www.janvsmachine.net/2016/07/effective-typesafe-config.html).

## Data Modeling

## Error Handling

### Notes

tl;dr: If your team isn't ready for category theory typeclasses, then use [Scalactic](https://scalactic.org)'s Or type. If you and your team are ready for Applicatives, use Cats or Scalaz. Regardless of which one you want to use, model your errors as an algebraic data type. See Cats's [documentation on Xor](http://typelevel.org/cats/tut/xor.html#xor-in-the-small-xor-in-the-large) for a good discussion of how to do this.

You could use lihaoyi's [sourcecode](https://github.com/lihaoyi/sourcecode) to add more information to the output if desired.

Use Try for creating thin Scala layers over exception-happy Java libraries. E.g., if you want a thin layer on top of the AWS SDK, creating wrappers that return Try instead of exceptions can be a good way to protect yourself from the exceptions without too much investment. (Similarly, use Option to protect yourself from libraries that want to make you deal with `null`.)

## Play

### Notes

I've disabled the PlayLayoutPlugin. As far as I can tell, the Play layout is a holdover from the original Play v1, which was trying to be more Rails-like. Disabling the layout plugin restores the traditional Maven-style layout that Java and Scala developers are familiar with, so everything should be where you expect it to be. The only downside I can see to this is that some tutorials and reference applications for Play will not match up to this layout. But if you keep in mind that `app` means `src/main/scala` and ``

## Dependency Injection

### Notes

At a minimum: We want to be able to pass in a component's dependencies, either through the constructor or through a factory method. Any of these seem like fine ways to pass around dependencies to me:

```scala
class ConstructorParams(d1: Dep1, d2: Dep2)

class FactoryMethods private (d1: Dep1, d2: Dep2)

object FactoryMethods {
  // Making the constructor private might make the API clearer. I don't have strong feelings
  // either way.
  def apply(d1: Dep1, d2: Dep2): FactoryMethods = ???
}

object O {
  // Pure functions can have dependencies too.
  def functionsThatNeedDependencies(d1: Dep1, d2: Dep2)(a1: Arg1, a2: Arg2): Result = ???

  // I'm not sold on using implicits for this, though.
  def usingImplicitsToGetDependencies(a1: Arg1, a2: Arg2)(implicit d1: Dep2, d2: Dep2): Result = ???
}
```

I think the first option is the most straightforward and plays most nicely with using libraries to help with the wiring.

Where possible, I'd like to avoid runtime injection in favor of figuring out the wiring at compile time. Would also like to avoid slathering my code with annotations that don't add anything to the meaning of the individual services. Would like the wiring to be kept separate from the components as much as possible.

* [MacWire](https://github.com/adamw/macwire): Macro-based DI with Scala. Haven't used it "for real", but seems extremely promising.
* [Scaldi](http://scaldi.org/): Nice run-time DI. Feels natural to work with.
* [Guice](https://github.com/google/guice): Main advantage is that Play supports it out of the box.

---

I don't like building component hierarchies through trait composition (i.e., "the cake pattern").

* Experience suggests it is difficult for a team to settle on a consistent way of doing it, leading to inconsistent implementations and/or time wasted debating which implementation to use.
* Scala newbies get confused by it.
* Expecting dependencies to have specific names creates a coupling between the component and the wiring.
* Declaring components with stubs in tests seems wordy to me.
* Aesthetically, it feels less functional because you're using inheritance. Even a constructor feels more like a function since you're still passing in arguments and receiving a value (the initialized instance).
