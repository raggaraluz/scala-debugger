TODO
====

Last Release:     None
Upcoming Release: 1.0

The following are items of importance to tackle before the next milestone. The
projects are placed in order of lower-level to higher-level with the idea being
that lower-level projects need to be finished first before it is possible to
complete higher-level projects.

Debugger API
------------

- Add tests for the debugger API low-level implementation similar to
  [Ensime Debug Tests](https://github.com/ensime/ensime-server/blob/master/server/src/it/scala/org/ensime/intg/DebugTest.scala)

    - Will help me figure out if I need to offer a higher level abstraction
      for Scala-based debugging

    - Will expose bugs from not checking for the state of the JVM, thread, etc.

- Add step functionality

    - Stepping over

    - Stepping into

    - Stepping out of

Debugger Akka Extension
-----------------------

- Investigate what is necessary to create an Akka extension similar to ZeroMQ

- Determine what I want it to offer

    - Akka actor that receives messages when new events occur

         - How do I access the events? Do I need to worry about serialization?

         - How do I access the associated virtual machine?

    - Akka actor that receives messages for new connections (from other JVMs)

Debugger Play Application
-------------------------

- Create initial template using Facebook React with MaterializeCSS for UI

- Display information regarding connected JVMs

- Render a diagram of incoming events

- Add interaction towards adding/removing breakpoints and stepping through code

    - Needs to include a source viewer (for current code)

    - Needs to show the variables relative to the breakpoint location
