package com.ibm.spark.kernel.debugger

import com.sun.jdi.{VirtualMachine, ThreadReference, ObjectReference}
import com.sun.jdi.request.BreakpointRequest

class BreakpointBundle(
  private var breakpointRequests: Seq[BreakpointRequest] = Nil
) extends Seq[BreakpointRequest] {
  /**
   *
   * @param idx
   * @return
   */
  override def apply(idx: Int): BreakpointRequest = breakpointRequests(idx)

  /**
   *
   * @return
   */
  override def length: Int = breakpointRequests.length

  /**
   *
   * @return
   */
  override def iterator: Iterator[BreakpointRequest] =
    breakpointRequests.iterator

  /**
   *
   * @param objectReference
   */
  def addInstanceFilter(objectReference: ObjectReference): Unit =
    breakpointRequests.foreach(_.addInstanceFilter(objectReference))

  /**
   *
   * @param threadReference
   */
  def addThreadFilter(threadReference: ThreadReference): Unit =
    breakpointRequests.foreach(_.addThreadFilter(threadReference))

  /**
   *
   * @param i
   */
  def addCountFilter(i: Int): Unit =
    breakpointRequests.foreach(_.addCountFilter(i))

  /**
   *
   */
  def disable(): Unit = breakpointRequests.foreach(_.disable())

  /**
   *
   */
  def enable(): Unit = breakpointRequests.foreach(_.enable())

  /**
   *
   * @param o
   * @return
   */
  def getProperty(o: scala.Any): AnyRef = {
    val properties = breakpointRequests.map(_.getProperty(o))
    val globalProperty = properties.head

    require(properties.forall(globalProperty == _),
      "Underlying properties are not in sync!")

    globalProperty
  }

  /**
   *
   * @return
   */
  def isEnabled: Boolean =
    breakpointRequests.map(_.isEnabled).reduce(_ && _)

  /**
   *
   * @param o
   * @param o1
   */
  def putProperty(o: scala.Any, o1: scala.Any): Unit =
    breakpointRequests.foreach(_.putProperty(o, o1))

  /**
   *
   * @param b
   */
  def setEnabled(b: Boolean): Unit =
    breakpointRequests.foreach(_.setEnabled(b))

  /**
   *
   * @param i
   */
  def setSuspendPolicy(i: Int): Unit =
    breakpointRequests.foreach(_.setSuspendPolicy(i))

  /**
   *
   * @return
   */
  def suspendPolicy: Int = {
    val suspendPolicies = breakpointRequests.map(_.suspendPolicy())
    val globalSuspendPolicy = suspendPolicies.head

    require(suspendPolicies.forall(globalSuspendPolicy == _),
      "Underlying suspend policies are not in sync!")

    globalSuspendPolicy
  }

  /**
   *
   * @return
   */
  def virtualMachine: VirtualMachine = {
    val virtualMachines = breakpointRequests.map(_.virtualMachine())
    val globalVirtualMachine = virtualMachines.head

    require(virtualMachines.forall(globalVirtualMachine eq _),
      "Underlying virtual machines are not in sync!")

    globalVirtualMachine
  }

}
