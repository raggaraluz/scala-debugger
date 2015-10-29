package org.senkbeil.debugger.api.pipelines

import java.io.Closeable

/**
 * Represents a pipeline of instructions used to perform a series of operations
 * over an arbitrary collection of data. This pipeline is closeable, meaning
 * that the close operation performs an action.
 *
 * @tparam A The incoming data type
 * @tparam B The outgoing data type
 * @param operation The operation to apply to incoming data
 * @param closeFunc The function to invoke to close the pipeline
 */
class CloseablePipeline[A, B] private[pipelines] (
  override val operation: Operation[A, B],
  private val closeFunc: () => Unit
) extends Pipeline[A, B](operation) with Closeable {

  /** Generates a closeable pipeline that passes down the close function. */
  override protected def newPipeline[C, D](
    operation: Operation[C, D]
  ): Pipeline[C, D] = new CloseablePipeline(operation, closeFunc)

  /**
   * Closes the pipeline.
   *
   * @param now If true, should perform the closing action immediately rather
   *            than on the next data fed through the pipeline
   */
  override def close(now: Boolean): Unit = {
    if (now) closeFunc()
    else transform(new CloseOperation(closeFunc))
  }

  /**
   * Closes the pipeline immediately.
   */
  def close(): Unit = close(now = true)
}

/**
 * Contains helper utilities for closeable pipeline creation.
 */
object CloseablePipeline {
  /**
   * Creates an empty, closeable pipeline expecting data of the specified type.
   *
   * @tparam A The type of incoming data
   * @param klass The class representing the input of the new pipeline
   * @param closeFunc The function to invoke when closing the pipeline
   *
   * @return The new pipeline
   */
  def newPipeline[A](
    klass: Class[A],
    closeFunc: () => Unit
  ): CloseablePipeline[A, A] = {
    new CloseablePipeline(new NoOperation[A], closeFunc)
  }
}
