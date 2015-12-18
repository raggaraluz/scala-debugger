package org.scaladebugger.api.lowlevel.events.filters

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class MethodNameFilterSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val testName = "some name"
  private val methodNameFilter = MethodNameFilter(name = testName)

  describe("MethodNameFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the method name filter") {
        methodNameFilter.toProcessor.argument should be (methodNameFilter)
      }
    }
  }
}
