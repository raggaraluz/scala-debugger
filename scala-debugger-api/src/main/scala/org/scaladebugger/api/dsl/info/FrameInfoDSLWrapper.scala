package org.scaladebugger.api.dsl.info

import org.scaladebugger.api.profiles.traits.info.{ObjectInfoProfile, ThreadInfoProfile, VariableInfoProfile, FrameInfoProfile}

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param frameInfoProfile The profile to wrap
 */
class FrameInfoDSLWrapper private[dsl] (
  private val frameInfoProfile: FrameInfoProfile
) {
  /** @see FrameInfoProfile#tryThisObject() */
  def withThisObject: Try[ObjectInfoProfile] = frameInfoProfile.tryThisObject

  /** @see FrameInfoProfile#thisObject() */
  def withUnsafeThisObject: ObjectInfoProfile = frameInfoProfile.thisObject

  /** @see FrameInfoProfile#tryCurrentThread() */
  def withCurrentThread: Try[ThreadInfoProfile] =
    frameInfoProfile.tryCurrentThread

  /** @see FrameInfoProfile#currentThread() */
  def withUnsafeCurrentThread: ThreadInfoProfile =
    frameInfoProfile.currentThread

  /** @see FrameInfoProfile#tryVariable(String) */
  def forVariable(name: String): Try[VariableInfoProfile] =
    frameInfoProfile.tryVariable(name)

  /** @see FrameInfoProfile#variable(String) */
  def forUnsafeVariable(name: String): VariableInfoProfile =
    frameInfoProfile.variable(name)

  /** @see FrameInfoProfile#tryAllVariables() */
  def forAllVariables: Try[Seq[VariableInfoProfile]] =
    frameInfoProfile.tryAllVariables

  /** @see FrameInfoProfile#tryArgumentLocalVariables() */
  def forArguments: Try[Seq[VariableInfoProfile]] =
    frameInfoProfile.tryArgumentLocalVariables

  /** @see FrameInfoProfile#tryNonArgumentLocalVariables() */
  def forNonArguments: Try[Seq[VariableInfoProfile]] =
    frameInfoProfile.tryNonArgumentLocalVariables

  /** @see FrameInfoProfile#tryLocalVariables() */
  def forLocalVariables: Try[Seq[VariableInfoProfile]] =
    frameInfoProfile.tryLocalVariables

  /** @see FrameInfoProfile#tryFieldVariables() */
  def forFieldVariables: Try[Seq[VariableInfoProfile]] =
    frameInfoProfile.tryFieldVariables

  /** @see FrameInfoProfile#allVariables() */
  def forUnsafeAllVariables: Seq[VariableInfoProfile] =
    frameInfoProfile.allVariables

  /** @see FrameInfoProfile#argumentLocalVariables() */
  def forUnsafeArguments: Seq[VariableInfoProfile] =
    frameInfoProfile.argumentLocalVariables

  /** @see FrameInfoProfile#nonArgumentLocalVariables() */
  def forUnsafeNonArguments: Seq[VariableInfoProfile] =
    frameInfoProfile.nonArgumentLocalVariables

  /** @see FrameInfoProfile#localVariables() */
  def forUnsafeLocalVariables: Seq[VariableInfoProfile] =
    frameInfoProfile.localVariables

  /** @see FrameInfoProfile#fieldVariables() */
  def forUnsafeFieldVariables: Seq[VariableInfoProfile] =
    frameInfoProfile.fieldVariables
}
