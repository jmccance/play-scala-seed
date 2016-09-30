scala-seed
==========

This project captures some patterns and opinions I've acquired for Scala projects.

## Importing into IntelliJ

When any sbt project into IntelliJ, I recommend these settings:

* Uncheck "Use auto-import". This setting determines whether IntelliJ will automatically import the project whenever any changes are made to the build files. Reimporting an sbt project takes a few seconds, so this ends up being more annoying than helpful if you're making larger changes to your build. You can always reimport by either opening the sbt pane in IntelliJ and clicking the refresh button, or by opening the "Open anything" box and choose "Refresh all projects".
* Under "Downloads", check "Sources for SBT and plugins". This will make it much easier to figure out sbt problems, since you'll have ready access to the code through IntelliJ.

## Build organization

### Dependencies

Dependencies are defined in `project/Dependencies.scala`. While this would be excessive for a single project build, in a multi-project build it helps keep libraries consistent by defining them all in one place. (Remember that anything defined in a Scala file in `project` will be available in your build.sbt and any other top-level sbt files.)

Dependencies.scala uses objects to organize dependencies. The idea is that each organization gets an object with a reasonable, human-selected name, like "ScalaTest" or "Typesafe". When one organization has significant sub-organizations, nested objects can be used. An example from this project is the "Play" object inside of the Typesafe object. The dependencies themselves are simply the artifact name converted to camelCase. (E.g., "play-json" becomes "playJson".)

This system is meant to help ensure that when multiple developers are working on the same project they can add dependencies in a consistent manner without naming collisions.

The upshot of this is that when we go to add dependencies to a project, it looks like this:

```scala
libraryDependencies ++= Seq(
  Dependencies.Typesafe.Play.playJson,
  Dependencies.ScalaTest.scalatest % Test
)
```

Since all your dependencies are defined in one place, it's trivial to update versions for all usages in sub-projects. Since they're defined as Scala objects and vals, your IDE can help you discover which dependencies are available even if you don't quite remember the name.

### Build definitions

All build definitions are located in a single, top-level `build.sbt` file. While sbt supports placing a `build.sbt` with each individual project, in my experience it rapidly becomes hard to keep track of which settings are applied to which projects. Consolidating everything into a top-level file makes things much easier to keep track of.

Project definitions come first, since people are generally initially interested in either which projects the build contains or in some specific project. The vals are named the same as the directory, which is also the artifact name. This is done both so its easy to identify which folder corresponds to which artifact, and so that IntelliJ doesn't need to show two names in the project view. (If you had a project directory `foo-core` and a the project val was `fooCore`, IntelliJ would display it as something like `fooCore [foo-core]`.)

Settings are defined as Seqs of settings further down in the file. Defining them as individual Seqs helps keep the build organized. Common ones are then aggregated together into `commonSettings` so they can be easily added to each project.

### Plugins

This project includes several plugins that I think are worthwhile for any project:

* [sbt-scalafmt](): Code formatter for Scala. Avoid format wars and nitpicking in PRs by having a computer do it for you. The formatting isn't *quite* perfect, but it's good enough.
* [sbt-release](): Provides support for running automated releases. By default, this consists of setting the version to non-SNAPSHOT, tagging the commit, publishing to a repository, bumping to the next SNAPSHOT version, and committing that. These steps support a large amount of customization, however.
* [sbt-scoverage](): Provides code coverage analysis, which can be a useful metric for identifying under-exercised sections of your code base.

Additionally, the sbt plugin for Play is included since this project includes a Play-based REST API.

### Custom sbt plugins

While not needed for this project, they're worth mentioning. Writing an sbt plugin is [surprisingly trivial][writing-sbt-plugins]. If you find yourself managing several repositories that all share common settings (e.g., artifact repositories, release settings, etc.), it's definitely worth implementing your own plugin to help keep these settings consistent and reduce the time it takes to start up a new repository.

[writing-sbt-plugins]: http://www.scala-sbt.org/0.13/docs/Plugins.html
