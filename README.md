play-scala-seed
===============

This project captures some patterns, tips, and opinions I've acquired for Scala projects. This README will attempt to explain some of the specific choices I'm making and why.

This is a work in progress. Any section with a "Notes" subsection is even more WIP than others, and I'm just using that to capture my thoughts prior to a clearer write-up. Caveat programmator.

## My Biases

We all have biases about what we like in languages, libraries, and frameworks, and I'm no exception. These biases are formed by a mix of accident, experience, and personal preference. I list a few of them below, since it might help explain some of the choices I made in this project.

**If I have to choose between "magic" and more code, I'll take more code.** One trick that frameworks use to make your life "easier" is to require that you hold to a particular convention so that they can magically generate functionality for you. These conventions can only be figured out by reading documentation and looking at sample projects, and good luck if you stumble into an edge case or want to design your application slightly differently. I would rather give up the reduced boilerplate if it means that people can easily discover how the application is put together using their IDE (without any special plugins other than Scala language support).

**I prefer compile-time solutions to run-time solutions.** If I have to choose my pain, I would rather spend more time trying to get something to compile than chasing down run-time errors. For one thing, I know when something compiles; I don't know when I've found all of the run-time errors. Because of this, for example, I prefer MacWire's macro-based wiring to Guice's runtime wiring, and I prefer play-json's (admittedly imperfect) compile time specification of JSON serializers over Jackson's ability to generate serializers at runtime.

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

Both Play and Akka make use of [Typesafe Config][typesafe-config], and in my experience it's a great library. The HOCON format it uses has a rich set of primitives, including durations ("5s", "30 minutes") and byte quantities ("128MB") to help eliminate unit errors. It also has support for references and includes.

[typesafe-config]: https://typesafehub.github.io/config/

That said, parsing configuration files is only one part of the problem. It's also important to model your application's configuration. This avoids coupling your components to your configuration library, and makes it easier to initialize components in tests. It's simpler to just declare a case class with the desired configuration than to maintain a test configuration file or define the configuration file inline.

This project defines a collection of extension methods in `com.example.util.config` to easily parse a config object into a case class or a collection of errors using Scalactic.

Jan Stette's post [Effective Typesafe Config][effective-typesafe-config] summarizes a lot of my opinions about how to best use Typesafe Config, such as making proper use of `reference.conf` files and avoiding using absolute paths. The one point I would add — simply because it bugs me to see styles mixed — is to make sure to use `spine-case` and not `camelCase` for config names. Most major users HOCON (like Play and Akka) use this style, so we might as well try to be consistent.

[effective-typesafe-config]: http://www.janvsmachine.net/2016/07/effective-typesafe-config.html

Below is an example of the basic recipe I use when defining configurations.

```scala
import java.time._
import org.scalactic._
import org.scalactic.Accumulation._
import com.example.util.config._

class MyService(config: MyServiceConfig) {
  import config._ // To simplify accessing config parameters
}

case class MyServiceConfig(timeout: Duration, maxCount: Long, id: String)

object MyServiceConfig {
  def from(config: Config): MyServiceConfig Or Every[ConfigException] = {
    withGood(
      config.duration("timeout"),
      config.long("max-count"),
      config.string("id")
    )(MyServiceConfig.apply)
  }
}
```

### Alternatives

The downside of Typesafe Config is that it has a strong Java flavor, preferring to throw exceptions instead of returning error types. There are [a number of wrappers][tc-wrappers] around Typesafe Config that make it more Scala-like. In addition to more Scala-like syntax, many of these libraries also offer reduced boilerplate for parsing Configs into case classes.

If you don't want to think too much about configuration, I would stand by my main recommendation above. If you're willing to experiment, I would definitely recommend trying one of the Scala libraries.

[tc-wrappers]: https://github.com/typesafehub/config#scala-wrappers-for-the-java-library

### Notes

Use HOCON and Typesafe Config. Use HOCON well, taking advantage of nesting and duration types. Use `spine-case` to be consistent with other majors consumers of HOCON, like Akka and Play. Separate parsing the config from modeling your config. Use applicative validation to parse config in such a way that you get all the errors in it at the same time. Model your configuration as case classes; individual objects should not know where it came from. (Makes it easier to write tests too; you don’t need to create a Config object in order to initialize your SUT.)  Make sure your config parser is ignorant of where in the config object it is; it should expect the root of its hierarchy. This helps decouple your config parser from the structure of your config file.

Read [this blog post](http://www.janvsmachine.net/2016/07/effective-typesafe-config.html).

## Data Modeling

## Error Handling

### Notes

tl;dr: If your team isn't ready for category theory typeclasses, then use [Scalactic](https://scalactic.org)'s Or type. If you and your team are ready for Applicatives, use Cats or Scalaz. Regardless of which one you want to use, model your errors as an algebraic data type. See Cats's [documentation on Xor](http://typelevel.org/cats/tut/xor.html#xor-in-the-small-xor-in-the-large) for a good discussion of how to do this.

You could use lihaoyi's [sourcecode](https://github.com/lihaoyi/sourcecode) to add more information to the output if desired.

Use Try for creating thin Scala layers over exception-happy Java libraries. E.g., if you want a thin layer on top of the AWS SDK, creating wrappers that return Try instead of exceptions can be a good way to protect yourself from the exceptions without too much investment. (Similarly, use Option to protect yourself from libraries that want to make you deal with `null`.)

## JSON

I've used [play-json][] on a number of projects and been quite happy with it. It's not perfect by any means — I wish `parse` didn't throw exceptions, for example — but most of its flaws can be mitigated with some custom helper methods.

[play-json]: https://playframework.com/documentation/2.5.x/ScalaJson

### Alternatives

The most interesting alternative to play-json is [Circe][]. Circe is a performance-focused, functional JSON library. Rather than macros (which can be finicky and a headache to debug), it uses Shapeless to derive generic serializers in a typesafe way. This should significantly reduce the boilerplate around JSON serializers.

I've yet to use it for anything myself, but it's near the top of the list of Scala libraries I'd like to try.

[Circe]: https://travisbrown.github.io/circe/

## Play

The Play Framework is a safe choice for building web applications in Scala. It has the backing of Lightbend, and has been around long enough that a lot of people (comparatively) have experience with it.

I've made a couple choices that deviate from the standard Play app. The first is that I've disabled the PlayLayoutPlugin. As far as I can tell, the Play layout is a holdover from the original Play v1, which was trying to be more Rails-like. Disabling the layout plugin restores the traditional Maven-style layout that Java and Scala developers are familiar with, so everything should be where you expect it to be. To me, the value of having a familiar and consistent project layout outweighs the slight drawback of having the documentation not quite align with the project. A description of the mappings for the Maven-style layout can be found [here](https://playframework.com/documentation/2.5.x/Anatomy#default-sbt-layout).

Next, I decided to use the [SIRD router][sird] instead of the usual `routes` file. In my experience, routes files can rapidly get unwieldy. SIRD routers compose nicely, making it easier to scale from a single router to multiple, controller-specific routers. They are also ultimately less "magical" than the `routes` file, since you can easily write tests for your router, experiment with it in the REPL or a scratch file, or drill down into the code to figure out what's happening under the hood.

[sird]: https://playframework.com/documentation/2.5.x/ScalaSirdRouter

Finally, I elected to use MacWire instead of Guice to handle dependency injection. See the section "Dependency Injection" for more information on the decision."

### Alternatives

Play seems to want to be more like a traditional framework à la Spring. For more minimal, library-style HTTP, Akka HTTP and Finch seem like the most interesting candidates. Finch in particular is interesting for being very FP-oriented as well as being [one of the fastest HTTP libraries in Scala](http://vkostyukov.net/posts/how-fast-is-finch/).

* [Akka HTTP](http://doc.akka.io/docs/akka/2.4/scala/http/index.html)
* [Finch](https://github.com/finagle/finch)

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
* Expecting dependencies to have specific names creates an unnecessary and unhelpful coupling between the component and the wiring.
* Declaring stub components for tests is wordier because you need to explicitly name each field.
* Aesthetically, it feels less functional because you're using inheritance. Even a constructor feels more like a function since you're still passing in arguments and receiving a value (the initialized instance).

## Deployment

The documentation for Play describes [a number of ways][play-deployment] to deploy your application in production. One convenient method not described there, however, is deploying to Docker. This functionality is provided indirectly through the [sbt-native-packager][] plugin. Note that for our project, the web application is in contacts-web, so the commands described in the Play documentation will need to be run from that project. For example, you want `contacts-web/dist`, not just `dist`.

To publish the Docker image locally, run this command from the Activator/SBT prompt:

```
contacts-web/docker:publishLocal
```

To run that image from the command line, run:

```
docker -e 'APPLICATION_SECRET=your_secret_here' -p 9000:9000 contacts-web:0.1-SNAPSHOT
```

[play-deployment]: https://playframework.com/documentation/2.5.x/Production
[sbt-native-packager]: http://www.scala-sbt.org/sbt-native-packager/
