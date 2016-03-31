package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{FrameInfoProfile, ThreadInfoProfile}

import scala.util.Try

/**
 * Represents a pure implementation of a thread profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param threadReference The reference to the underlying JDI thread
 * @param virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 * @param referenceType The reference type for this thread
 */
class PureThreadInfoProfile(
  private val threadReference: ThreadReference
)(
  private val virtualMachine: VirtualMachine = threadReference.virtualMachine(),
  private val referenceType: ReferenceType = threadReference.referenceType()
) extends PureObjectInfoProfile(threadReference)(
  virtualMachine = virtualMachine,
  threadReference = threadReference,
  referenceType = referenceType
) with ThreadInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ThreadReference = threadReference

  /**
   * Represents the name of the thread.
   *
   * @return The thread name as a string
   */
  override def name: String = threadReference.name()

  /**
   * Retrieves profiles for all frames in the stack.
   *
   * @return The collection of frame profiles
   */
  override def getFrames: Seq[FrameInfoProfile] = {
    import scala.collection.JavaConverters._
    threadReference.frames().asScala.toSeq.map(newFrameProfile)
  }

  /**
   * Retrieves the profile for the specified frame in the stack.
   *
   * @param index The index (starting with 0 being top) of the frame whose
   *              profile to retrieve
   * @return The new frame profile instance
   */
  override def getFrame(index: Int): FrameInfoProfile = {
    newFrameProfile(threadReference.frame(index))
  }

  /**
   * Returns the total frames held in the current frame stack.
   *
   * @return The total number of frames
   */
  override def getTotalFrames: Int = threadReference.frameCount()

  protected def newFrameProfile(stackFrame: StackFrame): FrameInfoProfile =
    new PureFrameInfoProfile(stackFrame)
}
