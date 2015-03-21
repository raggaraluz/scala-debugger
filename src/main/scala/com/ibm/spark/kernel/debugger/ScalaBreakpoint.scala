package com.ibm.spark.kernel.debugger

import com.sun.jdi._
import com.sun.jdi.request.BreakpointRequest

class ScalaBreakpoint(
  private val className: String,
  private val lineNumber: Int,
  private val underlyingBreakpointRequests: Seq[BreakpointRequest]
) extends BreakpointRequest { self =>
  private val fakeLocation = new Location {
    private val locations = self.underlyingBreakpointRequests.map(_.location())

    override def sourceName(): String =
      this.sourceName(this.virtualMachine().getDefaultStratum)

    override def sourceName(s: String): String = {
      val sourceNames = locations.map(_.sourceName(s))
      val globalSourceName = sourceNames.head

      require(sourceNames.forall(globalSourceName == _),
        "Underlying location source names are not in sync!")

      globalSourceName
    }

    // TODO: Figure out how to determine this by reconstructing the source
    override def codeIndex(): Long = ???

    // TODO: Figure out how to get method representation
    override def method(): Method = ???

    override def sourcePath(): String =
      this.sourcePath(this.virtualMachine().getDefaultStratum)

    override def sourcePath(s: String): String = {
      val sourcePaths = locations.map(_.sourcePath(s))
      val globalSourcePath = sourcePaths.head

      require(sourcePaths.forall(globalSourcePath == _),
        "Underlying location source paths are not in sync!")

      globalSourcePath
    }

    override def lineNumber(): Int =
      this.lineNumber(this.virtualMachine().getDefaultStratum)

    override def lineNumber(s: String): Int = {
      val lineNumbers = locations.map(_.lineNumber(s))
      val globalLineNumber = lineNumbers.head

      require(lineNumbers.forall(globalLineNumber == _),
        "Underlying location line numbers are not in sync!")

      globalLineNumber
    }

    override def declaringType(): ReferenceType = {
      val declaringTypes = locations.map(_.declaringType())
      val globalDeclaringType = declaringTypes.head

      require(declaringTypes.forall(globalDeclaringType eq _),
        "Underlying location declaring types are not in sync!")

      globalDeclaringType
    }

    override def virtualMachine(): VirtualMachine = self.virtualMachine()

    // TODO: Need codeIndex and method to compare line position
    override def compareTo(o: Location): Int = ???
  }

  override def addInstanceFilter(objectReference: ObjectReference): Unit =
    underlyingBreakpointRequests.foreach(_.addInstanceFilter(objectReference))

  override def addThreadFilter(threadReference: ThreadReference): Unit =
    underlyingBreakpointRequests.foreach(_.addThreadFilter(threadReference))

  override def location(): Location = fakeLocation

  override def addCountFilter(i: Int): Unit =
    underlyingBreakpointRequests.foreach(_.addCountFilter(i))

  override def disable(): Unit =
    underlyingBreakpointRequests.foreach(_.disable())

  override def enable(): Unit =
    underlyingBreakpointRequests.foreach(_.enable())

  override def getProperty(o: scala.Any): AnyRef = {
    val properties = underlyingBreakpointRequests.map(_.getProperty(o))
    val globalProperty = properties.head

    require(properties.forall(globalProperty == _),
      "Underlying properties are not in sync!")

    globalProperty
  }

  override def isEnabled: Boolean =
    underlyingBreakpointRequests.map(_.isEnabled).reduce(_ && _)

  override def putProperty(o: scala.Any, o1: scala.Any): Unit =
    underlyingBreakpointRequests.foreach(_.putProperty(o, o1))

  override def setEnabled(b: Boolean): Unit =
    underlyingBreakpointRequests.foreach(_.setEnabled(b))

  override def setSuspendPolicy(i: Int): Unit =
    underlyingBreakpointRequests.foreach(_.setSuspendPolicy(i))

  override def suspendPolicy(): Int = {
    val suspendPolicies = underlyingBreakpointRequests.map(_.suspendPolicy())
    val globalSuspendPolicy = suspendPolicies.head

    require(suspendPolicies.forall(globalSuspendPolicy == _),
      "Underlying suspend policies are not in sync!")

    globalSuspendPolicy
  }

  override def virtualMachine(): VirtualMachine = {
    val virtualMachines = underlyingBreakpointRequests.map(_.virtualMachine())
    val globalVirtualMachine = virtualMachines.head

    require(virtualMachines.forall(globalVirtualMachine eq _),
      "Underlying virtual machines are not in sync!")

    globalVirtualMachine
  }
}
