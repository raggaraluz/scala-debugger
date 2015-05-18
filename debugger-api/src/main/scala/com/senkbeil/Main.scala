package com.senkbeil

import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

import com.senkbeil.debugger.ListeningDebugger
import com.senkbeil.debugger.events.LoopingTaskRunner
import com.senkbeil.debugger.virtualmachines.ScalaVirtualMachine
import scala.collection.JavaConverters._

object Main extends App {
  val debugger = ListeningDebugger("127.0.0.1", 9877)
  val testMainFile = "com/senkbeil/test/Main.scala"
  val virtualMachineQueue = new ConcurrentLinkedQueue[ScalaVirtualMachine]()
  val loopingTaskRunner = new LoopingTaskRunner()

  // Start our listening debugger
  debugger.start((virtualMachine) => {
    println("Connected!")
    virtualMachineQueue.add(new ScalaVirtualMachine(
      virtualMachine, loopingTaskRunner
    ))
  })

  println("Options to give other JVMs: " + debugger.remoteJvmOptions)

  while (true) {
    println("Total connected JVMs: " + debugger.connectedVirtualMachines.size)

    virtualMachineQueue.asScala.foreach { case scalaVirtualMachine =>
      val virtualMachine = scalaVirtualMachine.underlyingVirtualMachine
      println("Virtual Machine: " + virtualMachine.name())

      println("Files: " +
        scalaVirtualMachine.classManager.allScalaFileNames.mkString("\n"))

      // NOTE: Periodic call to get command line arguments! Does not get the
      // name of the class, though...
      println("-" * 10)
      println("Main class name: " + scalaVirtualMachine.mainClassName)
      println("-" * 10)
      println("Asking for args...")
      scalaVirtualMachine.commandLineArguments.foreach(arg =>
        println("ARG: " + arg)
      )
      println("-" * 10)

      println("Lines: " + scalaVirtualMachine
        .availableLinesForFile(testMainFile).mkString(", "))

      // Add the breakpoint if it does not already exist
      if (!scalaVirtualMachine.breakpointManager.hasLineBreakpoint(testMainFile, 42))
        scalaVirtualMachine.breakpointManager
          .setLineBreakpoint(testMainFile, 42)

      val breakpointBundle = scalaVirtualMachine.breakpointManager
        .getLineBreakpoint(testMainFile, 42)

      breakpointBundle.foreach { _.foreach { bp =>
        println("CLASS: " + bp.location().declaringType().name())
        println("SOURCE NAME: " + bp.location().sourceName())
        println("SOURCE PATH: " + bp.location().sourcePath())

        val fullSourcePlusExtension =
          bp.location().sourcePath().replace(File.separatorChar, '.')
        val fullSource = fullSourcePlusExtension.substring(
          0, fullSourcePlusExtension.lastIndexOf(".scala")
        )
        println("FULL SOURCE: " + fullSource)

        val classObject = bp.location().declaringType().classObject()
      }}

      //      scalaVirtualMachine.breakpointManager
      //        .removeLineBreakpoint(testMainClass, 13)
    }

    Thread.sleep(5000)
  }
}

