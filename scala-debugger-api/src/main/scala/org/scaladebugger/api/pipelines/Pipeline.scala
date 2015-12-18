package org.scaladebugger.api.pipelines

import java.io.Closeable

import scala.collection.GenTraversableOnce
import scala.concurrent.{Promise, Future}
import scala.util.Try

/**
 * Represents a pipeline of instructions used to perform a series of operations
 * over an arbitrary collection of data.
 *
 * @tparam A The incoming data type
 * @tparam B The outgoing data type
 * @param operation The operation to apply to incoming data
 * @param closeFunc The function to invoke to close the pipeline
 */
class Pipeline[A, B] private[pipelines] (
  val operation: Operation[A, B],
  private val closeFunc: () => Unit
) extends Closeable {
  /**
   * Creates a new instance of the pipeline with the specified operation. The
   * close function is considered a no-op on this pipeline.
   *
   * @param operation The operation to apply to incoming data
   *
   * @return The new pipeline instance
   */
  private[pipelines] def this(operation: Operation[A, B]) =
    this(operation, Pipeline.DefaultCloseFunc)

  /** Any child pipelines whose input is the output of this pipeline. */
  @volatile private var _children: Seq[Pipeline[B, _]] = Nil

  /** Failure route at this stage in the pipeline. */
  private lazy val _failure = newPipeline(new NoOperation[Throwable])

  /**
   * Retrieves the failure route of the current pipeline stage.
   *
   * @return The pipeline to process the throwable from the failure
   */
  def failed: Pipeline[Throwable, Throwable] = _failure

  /**
   * Retrieves the collection of children pipelines for the current pipeline.
   *
   * @return The collection of pipelines that are children to this pipeline
   */
  def children: Seq[Pipeline[B, _]] = _children

  /**
   * Adds the specified pipeline as a child of this pipeline.
   *
   * @param childPipeline The pipeline to add as a child
   * @tparam C The outgoing type of the child pipeline
   *
   * @return The added child pipeline
   */
  protected def addChildPipeline[C](
    childPipeline: Pipeline[B, C]
  ): Pipeline[B, C] = {
    _children :+= childPipeline
    childPipeline
  }

  /**
   * Creates a new pipeline using the given operation. This is used to generate
   * pipelines within the pipeline itself and can be overridden to generate
   * a different kind of pipeline. It is recommended to override this method
   * when subclassing pipeline.
   *
   * @param operation The operation to provide to the new pipeline
   * @param closeFunc The function used to close the new pipeline
   * @tparam C The input type of the new pipeline
   * @tparam D The output type of the new pipeline
   *
   * @return The new pipeline
   */
  protected def newPipeline[C, D](
    operation: Operation[C, D],
    closeFunc: () => Unit
  ): Pipeline[C, D] = new Pipeline[C, D](operation, closeFunc)

  /**
   * Creates a new pipeline using the given operation. This is used to generate
   * pipelines within the pipeline itself and can be overridden to generate
   * a different kind of pipeline. It is recommended to override this method
   * when subclassing pipeline.
   *
   * @note This implementation passes down the current pipeline's
   *       close function.
   *
   * @param operation The operation to provide to the new pipeline
   * @tparam C The input type of the new pipeline
   * @tparam D The output type of the new pipeline
   *
   * @return The new pipeline
   */
  protected def newPipeline[C, D](operation: Operation[C, D]): Pipeline[C, D] =
    newPipeline[C, D](operation, closeFunc)

  /**
   * Processes the provided data through this specific pipeline instance and
   * all subsequent children of this pipeline instance. No parent pipeline
   * instance will be used during the processing of the data.
   *
   * @param data The data to process
   *
   * @return If successful, the transformed collection of data at this
   *         specific pipeline instance, otherwise the thrown exception
   */
  def process(data: A*): Try[Seq[B]] = {
    val results = Try(operation.process(data))

    // If successful, continue down children paths
    results.foreach(r => children.foreach(_.process(r: _*)))

    // If failed, continue down failure path
    results.failed.foreach(throwable => failed.process(throwable))

    results
  }

  /**
   * Transforms the output of this pipeline using the provided operation.
   *
   * @note Inherits the close function of the pipeline.
   *
   * @param operation The operation to use to transform the output of this
   *                  pipeline instance
   * @tparam C The resulting type of the output from the operation
   *
   * @return The resulting pipeline instance from applying the operation
   */
  def transform[C](operation: Operation[B, C]): Pipeline[B, C] = {
    val childPipeline = newPipeline(operation)
    addChildPipeline(childPipeline)
  }

  /**
   * Maps the output of this pipeline instance to new values.
   *
   * @note Inherits the close function of the pipeline.
   *
   * @param f The function to use for mapping data to new values
   * @tparam C The resulting type of the new values
   *
   * @return The resulting pipeline instance with the mapped data
   */
  def map[C](f: (B) => C): Pipeline[B, C] = {
    transform(new MapOperation(f))
  }

  /**
   * Maps the output of this pipeline instance to new values and then flattens
   * the results.
   *
   * @param f The function to use for mapping data to new values that will
   *          be flattened
   * @tparam C The resulting type of the new values
   *
   * @return The resulting pipeline instance with the mapped and flattened data
   */
  def flatMap[C](f: (B) => GenTraversableOnce[C]): Pipeline[B, C] = {
    transform(new FlatMapOperation(f))
  }

  /**
   * Filters the output of this pipeline instance.
   *
   * @param f The function to use for filtering data (only true results will
   *          remain in output)
   *
   * @return The resulting pipeline instance with the filtered data
   */
  def filter(f: (B) => Boolean): Pipeline[B, B] = {
    transform(new FilterOperation(f))
  }

  /**
   * Filters the output of this pipeline instance.
   *
   * @param f The function to use for filtering data (only false results will
   *          remain in output)
   *
   * @return The resulting pipeline instance with the filtered data
   */
  def filterNot(f: (B) => Boolean): Pipeline[B, B] = {
    transform(new FilterNotOperation(f))
  }

  /**
   * Applies the provided function to the output of this pipeline, returning
   * nothing from the function.
   *
   * @param f The function to apply
   */
  def foreach(f: (B) => Unit): Unit = {
    transform(new ForeachOperation(f))
  }

  /**
   * Unions this pipeline with another pipeline that has the same input such
   * that input from either pipeline is used for both.
   *
   * @param other The other pipeline whose input to union together
   *
   * @return The unioned pipeline
   */
  def unionInput(other: Pipeline[A, _]): Pipeline[A, A] = {
    val parentPipeline = newPipeline(
      new NoOperation[A],
      Pipeline.DefaultCloseFunc
    )

    parentPipeline.addChildPipeline(this)
    parentPipeline.addChildPipeline(other)

    parentPipeline
  }

  /**
   * Unions this pipeline with another pipeline that has the same output such
   * that output from either pipeline flows through the union.
   *
   * @param other The other pipeline to whose output to union together
   *
   * @return The unioned pipeline
   */
  def unionOutput(other: Pipeline[_, B]): Pipeline[B, B] = {
    val childPipeline = newPipeline(new NoOperation[B], () => {
      Try(this.close(now = true))
      Try(other.close(now = true))
    })

    this.addChildPipeline(childPipeline)
    other.addChildPipeline(childPipeline)

    childPipeline
  }

  /**
   * Applies a no-op on the current pipeline.
   *
   * @return The pipeline after a no-op has been applied
   */
  def noop(): Pipeline[B, B] = transform(new NoOperation[B])

  /**
   * Closes the pipeline.
   *
   * @param now If true, should perform the closing action immediately rather
   *            than on the next data fed through the pipeline
   */
  def close(now: Boolean): Unit = {
    if (now) closeFunc()
    else transform(new CloseOperation(closeFunc))
  }

  /**
   * Closes the pipeline immediately.
   */
  def close(): Unit = close(now = true)

  /**
   * Transforms the pipeline into a future that will be evaluated once and then
   * closes the underlying pipeline.
   *
   * @return The future representing this pipeline
   */
  def toFuture: Future[B] = {
    val pipelinePromise = Promise[B]()

    foreach(value => {
      pipelinePromise.success(value)
      close()
    })

    failed.foreach(throwable => {
      pipelinePromise.failure(throwable)
      close()
    })

    pipelinePromise.future
  }
}

/**
 * Contains helper utilities for pipeline creation.
 */
object Pipeline {
  /** Represents a pipeline whose input and output types are the same. */
  type IdentityPipeline[A] = Pipeline[A, A]

  /** Default close function. Does nothing. */
  val DefaultCloseFunc: () => Unit = () => {}

  /**
   * Creates an empty pipeline expecting data of the specified type. The
   * associated close function is equivalent to a no-op.
   *
   * @tparam A The type of incoming data
   * @param klass The class representing the input of the new pipeline
   *
   * @return The new pipeline
   */
  def newPipeline[A](klass: Class[A]): IdentityPipeline[A] =
    new Pipeline(new NoOperation[A])

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
  ): Pipeline[A, A] = new Pipeline(new NoOperation[A], closeFunc)
}
