package org.senkbeil.debugger.api.pipelines

import scala.collection.GenTraversableOnce

/**
 * Represents a pipeline of instructions used to perform a series of operations
 * over an arbitrary collection of data.
 *
 * @tparam A The incoming data type
 * @tparam B The outgoing data type
 * @param operation The operation to apply to incoming data
 */
class Pipeline[A, B](val operation: Operation[A, B]) {
  /** Any child pipelines whose input is the output of this pipeline. */
  @volatile private var _children: Seq[Pipeline[B, _]] = Nil

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
   * Processes the provided data through this specific pipeline instance and
   * all subsequent children of this pipeline instance. No parent pipeline
   * instance will be used during the processing of the data.
   *
   * @param data The data to process
   *
   * @return The transformed collection of data
   */
  def process(data: A*): Unit = {
    val results = operation.process(data)
    children.foreach(_.process(results: _*))
  }

  /**
   * Transforms the output of this pipeline using the provided operation.
   *
   * @param operation The operation to use to transform the output of this
   *                  pipeline instance
   * @tparam C The resulting type of the output from the operation
   *
   * @return The resulting pipeline instance from applying the operation
   */
  def transform[C](operation: Operation[B, C]): Pipeline[B, C] = {
    val childPipeline = new Pipeline(operation)
    addChildPipeline(childPipeline)
  }

  /**
   * Maps the output of this pipeline instance to new values.
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
   * @param other The other pipeline to union together
   * @tparam C The output of the other pipeline
   *
   * @return The unioned pipeline
   */
  def union[C](other: Pipeline[A, C]): Pipeline[A, A] = {
    val parentPipeline = new Pipeline(new NoOperation[A])

    parentPipeline.addChildPipeline(this)
    parentPipeline.addChildPipeline(other)

    parentPipeline
  }
}

/**
 * Contains helper utilities for pipeline creation.
 */
object Pipeline {
  /**
   * Creates an empty pipeline expecting data of the specified type.
   *
   * @tparam A The type of incoming data
   * @param klass The class representing the input of the new pipeline
   *
   * @return The new pipeline
   */
  def newPipeline[A](klass: Class[A]): Pipeline[A, A] = {
    new Pipeline(new NoOperation[A])
  }
}
