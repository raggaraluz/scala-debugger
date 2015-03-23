package com.ibm.spark.kernel.utils

import scala.util.Try

object TryExtras {
  implicit class TryImplicits[T](val baseTry: Try[T]) extends AnyVal {
    /**
     * Converts a Try(...) to an Option(...) where null is represented as None
     * instead of Some(null).
     *
     * @return The Option representation of the Try instance
     */
    def toFilteredOption = baseTry.toOption.flatMap(Option(_))
  }
}
