# Scala Debugger API {: .logo }

Scala abstractions and tooling around the Java Debugger Interface.
{: .logo-subtext }

```scala
val fileName = "file.scala"
val lineNumber = 37

scalaVirtualMachine.onUnsafeBreakpoint(fileName, lineNumber).foreach(breakpointEvent => {
    println(s"Reached breakpoint $fileName:$lineNumber")
})
```

---

## <i class="fa fa-laptop large-icon vertical-center"></i> Installation {: .logo }

When using sbt:

```scala
libraryDependencies += "org.scala-debugger" %% "scala-debugger-api" % "1.0.0"
```

See the [installation page][installation] for more information.

---

## <i class="fa fa-gears large-icon vertical-center"></i> Documentation {: .logo }

<div class="row flex-container flex-container-center">
    <div class="col-md-5 link-container v-center h-center spaced-col">
        <a href="/getting-started/what-is-a-debugger/">
            <i class="fa fa-play-circle-o large-icon vertical-center"></i>
            <h4>Getting Started</h4>
            <p class="black-text">Quick introduction to debugging and using the Scala debugger API.</p>
        </a>
    </div>

    <div class="col-md-5 col-md-offset-2 link-container v-center h-center spaced-col">
        <a href="/advanced-topics/what-is-the-profile-system/">
            <i class="fa fa-wrench large-icon vertical-center"></i>
            <h4>Advanced Topics</h4>
            <p class="black-text">Explanations of more advanced Scala debugger API features.</p>
        </a>
    </div>
</div>

<div class="row flex-container flex-container-center">
    <div class="col-md-5 link-container v-center h-center spaced-col">
        <a href="/cookbook/creating-a-launching-debugger/">
            <i class="fa fa-book large-icon vertical-center"></i>
            <h4>Cookbook</h4>
            <p class="black-text">Brief code examples for common cases when using the Scala debugger API.</p>
        </a>
    </div>

    <div class="col-md-5 col-md-offset-2 link-container v-center h-center spaced-col">
        <a href="/external/scaladoc-2.10/">
            <i class="icon-scala large-icon vertical-center" style="padding-right: 0px;"></i>
            <h4>Scaladoc</h4>
            <p class="black-text">Reference material for all programmatic APIs in the Scala debugger API.</p>
        </a>
    </div>
</div>

---

## <i class="fa fa-group large-icon vertical-center"></i> Contributing {: .logo }

All contributions are welcome! As a reminder, the project is licensed under the
[Apache 2.0 license][license].

<div class="row flex-container flex-container-center">
    <div class="col-md-3 link-container v-center h-center spaced-col">
        <a href="/about/roadmap/">
            <i class="fa fa-road large-icon vertical-center"></i>
            <h4>Roadmap</h4>
            <p class="black-text">View the planned features here.</p>
        </a>
    </div>

    <div class="col-md-3 col-md-offset-1 link-container v-center h-center spaced-col">
        <a href="https://github.com/ensime/scala-debugger/issues">
            <i class="fa fa-thumbs-o-up large-icon vertical-center"></i>
            <i class="fa fa-thumbs-o-down large-icon vertical-center"></i>
            <h4>Issues</h4>
            <p class="black-text">Report bugs and request features here.</p>
        </a>
    </div>

    <div class="col-md-3 col-md-offset-1 link-container v-center h-center spaced-col">
        <a href="https://github.com/ensime/scala-debugger/pulls">
            <i class="fa fa-github-alt large-icon vertical-center"></i>
            <h4>Pull Requests</h4>
            <p class="black-text">Submit your own fixes and features here.</p>
        </a>
    </div>
</div>

<br />

See the [contributing][contributing] page for more information.

---

<div class="h-center">
    <p>Site main page inspired by content from <a href="http://scalamock.org">ScalaMock</a>.</p>
    <p>Project proudly a member of <a href="https://github.com/ensime">ENSIME</a>.</p>
</div>

[installation]: /getting-started/installation/
[license]: https://www.apache.org/licenses/LICENSE-2.0
[contributing]: /about/contributing/


