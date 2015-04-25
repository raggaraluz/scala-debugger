package com.senkbeil.test

import sys.process.Process

/**
 * Represents a running JVM instance with which to query regarding state.
 *
 * @param process The process representing this JVM
 */
class TestJvm(private val process: Process) {

  /**
   * Stops this test JVM and destroys the underlying process.
   */
  def stop(): Unit = process.destroy()
}
