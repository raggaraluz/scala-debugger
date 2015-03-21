package com.ibm.spark.kernel.debugger

import com.ibm.spark.kernel.utils.LogLike
import com.sun.jdi.{VirtualMachine, Value, Field}
import collection.JavaConverters._

import scala.util.Try

class FieldManager(
  protected val _virtualMachine: VirtualMachine,
  private val _classManager: ClassManager
) extends JDIHelperMethods with LogLike {
  /**
   * Retrieves the static fields and their values for the specified class.
   *
   * @param className The name of the class whose static fields to retrieve
   *
   * @return The list of static fields and their respective values
   */
  def staticFieldsForClass(className: String): Seq[(Field, Value)] = {
    _classManager.underlyingReferencesFor(className)
      .map { ref =>
      (ref, Try(ref.allFields()).map(_.asScala).getOrElse(Nil))
    } map { case (ref, fields) =>
      val staticFields =
        fields.filter(field => Try(ref.getValue(field)).isSuccess)

      (ref, staticFields)
    } map { case (ref, staticFields) =>
      staticFields.map(field => (field, ref.getValue(field)))
    } reduce { _ ++ _ }
  }
}
