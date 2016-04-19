package org.scaladebugger.test.info

import org.scaladebugger.test.helpers.Stubs._

/**
 * Provides test of examining frames.
 *
 * @note Should have a class name of org.scaladebugger.test.info.Frames
 */
object Frames {
  def main(args: Array[String]) {
    noop(None) // Breakpointable

    val ic = new InnerClass
    ic.method() // Breakpointable

    noop(None) // Breakpointable
  }

  class InnerClass {

    class InnerInnerClass {
      def method(): Unit = {
        (() => {
          noop(None) // Breakpointable
        })()

        noop(None) // Breakpointable
      }
    }

    def method(): Unit = {
      noop(None) // Breakpointable

      (() => {
        noop(None) // Breakpointable
      })()

      val iic = new InnerInnerClass
      iic.method() // Breakpointable
    }
  }
}

