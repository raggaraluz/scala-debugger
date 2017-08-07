# Scala Debugger

[![Stories in Ready](https://badge.waffle.io/ensime/scala-debugger.svg?label=ready&title=Ready)](http://waffle.io/ensime/scala-debugger)
[![Build Status](https://ci.senkbeil.org/api/badges/ensime/scala-debugger/status.svg)](https://ci.senkbeil.org/ensime/scala-debugger)
[![Build status](https://ci.appveyor.com/api/projects/status/8mcnhcm1jofomg2f/branch/master?svg=true)](https://ci.appveyor.com/project/chipsenkbeil/scala-debugger/branch/master)
[![Scaladex](https://index.scala-lang.org/ensime/scala-debugger/scala-debugger-api/latest.svg?color=orange)](https://index.scala-lang.org/ensime/scala-debugger)
[![Scaladoc 2.10](https://img.shields.io/badge/Scaladoc-2.10-34B6A8.svg?style=flat)](http://www.javadoc.io/doc/org.scala-debugger/scala-debugger-api_2.10)
[![Scaladoc 2.11](https://img.shields.io/badge/Scaladoc-2.11-34B6A8.svg?style=flat)](http://www.javadoc.io/doc/org.scala-debugger/scala-debugger-api_2.11)
[![Scaladoc 2.12](https://img.shields.io/badge/Scaladoc-2.12-34B6A8.svg?style=flat)](http://www.javadoc.io/doc/org.scala-debugger/scala-debugger-api_2.12)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ensime/scala-debugger)

A simple debugger library and tooling for Scala. Tested on OpenJDK 7 for Windows and Linux.
- Visit the [main site](https://scala-debugger.org/) for more documentation.
- Check out the [wiki](https://github.com/ensime/scala-debugger/wiki) for the current roadmap.

## About

The project contains several modules including
- A wrapper library around the Java Debugger Interface
- A tool similar to `jdb`, written to allow debugging Java and Scala interactively
- A programming language used as the interactive CLI for the debugger tool
- An sbt plugin leveraging the debugger tool for interactive debugging

![Debugger Tool a.k.a. sdb](https://github.com/ensime/scala-debugger/blob/media/gifs/sdb.gif)

## Installing the debugger tool

### Homebrew

The formula for `sdb`, the interactive debugger tool for the command line, is
available via `brew install chipsenkbeil/personal/scala-debugger`.

Once installed, you can run `sdb` on the command line.

### sbt

With `sbt` installed, add the following plugin to `~/.sbt/0.13/plugins/sdb.sbt`:

```
addSbtPlugin("org.scala-debugger" %% "sbt-scala-debugger" % "1.1.0-M3")
```

You can now run the plugin via `sbt sdb:run`.

## Building from source

Make sure that you have some form of _sbt_ installed and available on your path
as it is used to compile the Scala Debugger source modules.

### Compiling all modules

From the root of the project:

```bash
make
```

or

```bash
make build
```

This will build all modules for Scala 2.10, 2.11, and 2.12.

### Assembling executable jars

From the root of the project:

```bash
make assembly
```

This will assemble relevant modules such as `sdb` for Scala 2.10, 2.11, and 2.12.

## Documentation

Make sure that you have some form of _sbt_ installed and available on your path
as it is used to compile the Scala Debugger documentation module. Furthermore,
you must have the [grus](https://github.com/chipsenkbeil/grus) plugin enabled for
your version of sbt. This can be done by adding the
following to `~/.sbt/0.13/plugins/grus.sbt`:

```
addSbtPlugin("org.senkbeil" %% "sbt-grus" % "0.1.0")
```

### Building the documentation

From the root of the project:

```bash
make docs
```

### Serving the documentation locally

From the root of the project:

```bash
make serve-docs
```

### Publishing the documentation to remote host

From the root of the project:

```bash
make push-docs
```

_This requires having contributor access to the repository._

## Running tests

Make sure that you have [sbt-extras](https://github.com/paulp/sbt-extras)
installed as it will pick up memory and compiler options from `.jvmopts`.

### Unit Tests

From the root of the project:

```bash
make unit-test-all
```

This will run unit tests for Scala 2.10, 2.11, and 2.12.

_Note: Roughly 2.5 GB of RAM is needed to compile the unit tests._

### Integration Tests

From the root of the project:

```bash
make it-test-all
```

This will run integration tests for Scala 2.10, 2.11, and 2.12.

## View project statistics

This requires you to have [cloc](https://github.com/AlDanial/cloc) installed
and available on your path.

```bash
make stats
```

This will calculate statistical information about the project and print it
out to the terminal.

## License

The Scala debugger API is licensed under [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0).

