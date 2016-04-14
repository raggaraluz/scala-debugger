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
  /** @see FrameInfoProfile#tryGetThisObject() */
  def withThisObject: Try[ObjectInfoProfile] = frameInfoProfile.tryGetThisObject

  /** @see FrameInfoProfile#getThisObject() */
  def withUnsafeThisObject: ObjectInfoProfile = frameInfoProfile.getThisObject

  /** @see FrameInfoProfile#tryGetCurrentThread() */
  def withCurrentThread: Try[ThreadInfoProfile] =
    frameInfoProfile.tryGetCurrentThread

  /** @see FrameInfoProfile#getCurrentThread() */
  def withUnsafeCurrentThread: ThreadInfoProfile =
    frameInfoProfile.getCurrentThread

  /** @see FrameInfoProfile#tryGetVariable(String) */
  def forVariable(name: String): Try[VariableInfoProfile] =
    frameInfoProfile.tryGetVariable(name)

  /** @see FrameInfoProfile#getVariable(String) */
  def forUnsafeVariable(name: String): VariableInfoProfile =
    frameInfoProfile.getVariable(name)

  /** @see FrameInfoProfile#tryGetAllVariables() */
  def forAllVariables: Try[Seq[VariableInfoProfile]] =
    frameInfoProfile.tryGetAllVariables

  /** @see FrameInfoProfile#tryGetArgumentLocalVariables() */
  def forArguments: Try[Seq[VariableInfoProfile]] =
    frameInfoProfile.tryGetArgumentLocalVariables

  /** @see FrameInfoProfile#tryGetNonArgumentLocalVariables() */
  def forNonArguments: Try[Seq[VariableInfoProfile]] =
    frameInfoProfile.tryGetNonArgumentLocalVariables

  /** @see FrameInfoProfile#tryGetLocalVariables() */
  def forLocalVariables: Try[Seq[VariableInfoProfile]] =
    frameInfoProfile.tryGetLocalVariables

  /** @see FrameInfoProfile#tryGetFieldVariables() */
  def forFieldVariables: Try[Seq[VariableInfoProfile]] =
    frameInfoProfile.tryGetFieldVariables

  /** @see FrameInfoProfile#getAllVariables() */
  def forUnsafeAllVariables: Seq[VariableInfoProfile] =
    frameInfoProfile.getAllVariables

  /** @see FrameInfoProfile#getArgumentLocalVariables() */
  def forUnsafeArguments: Seq[VariableInfoProfile] =
    frameInfoProfile.getArgumentLocalVariables

  /** @see FrameInfoProfile#getNonArgumentLocalVariables() */
  def forUnsafeNonArguments: Seq[VariableInfoProfile] =
    frameInfoProfile.getNonArgumentLocalVariables

  /** @see FrameInfoProfile#getLocalVariables() */
  def forUnsafeLocalVariables: Seq[VariableInfoProfile] =
    frameInfoProfile.getLocalVariables

  /** @see FrameInfoProfile#getFieldVariables() */
  def forUnsafeFieldVariables: Seq[VariableInfoProfile] =
    frameInfoProfile.getFieldVariables
}
