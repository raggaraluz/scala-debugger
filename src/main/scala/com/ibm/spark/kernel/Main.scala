package com.ibm.spark.kernel

import java.io.File

import com.ibm.spark.kernel.debugger.{BreakpointBundle, ScalaVirtualMachine, Debugger}
import com.sun.jdi._
import collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

object Main extends App {
  val debugger = new Debugger("127.0.0.1", 9877)
  val testMainClass = "com.ibm.spark.dummy.DummyMain"

  debugger.start()

  println("Options to give other JVMs: " + debugger.RemoteJvmOptions)

  while (true) {
    println("Total connected JVMs: " + debugger.getVirtualMachines.size)

    debugger.getVirtualMachines.foreach { case (virtualMachine, scalaVirtualMachine) =>
      println("Virtual Machine: " + virtualMachine.name())

      println("Classes: " +
        scalaVirtualMachine.classManager.allClassNames().mkString(","))

      // NOTE: Periodic call to get command line arguments! Does not get the
      // name of the class, though...
      Debugger.printCommandLineArguments(virtualMachine)

      println("Lines: " +
        scalaVirtualMachine.availableLinesForClass(testMainClass))

      scalaVirtualMachine.breakpointManager
        .setLineBreakpoint(testMainClass, 13)

      val bps = scalaVirtualMachine.breakpointManager
        .getLineBreakpoint(testMainClass, 13)

      bps.foreach { bp =>
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
      }

//      scalaVirtualMachine.breakpointManager
//        .removeLineBreakpoint(testMainClass, 13)
    }

    Thread.sleep(5000)
  }
}

