package com.senkbeil.debugger.events

import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}

class LoopingTaskRunnerSpec extends FunSpec with Matchers
  with OneInstancePerTest
{
  private val loopingTaskRunner = new LoopingTaskRunner()

  describe("LoopingTaskRunner") {
    describe("#start") {
      it("should throw an exception if already started") {
        loopingTaskRunner.start()

        intercept[IllegalArgumentException] {
          loopingTaskRunner.start()
        }
      }

      it("should start a pool of threads based on max workers that runs tasks") {
        fail()
      }
    }

    describe("#stop") {
      it("should throw an exception if not started") {
        intercept[IllegalArgumentException] {
          loopingTaskRunner.stop()
        }
      }

      it("should stop future execution of tasks") {
        fail()
      }
    }

    describe("#addTask") {
      it("should queue up a new task") {
        fail()
      }
    }

    describe("#removeTask") {
      it("should remove a task such that it is no longer executed") {
        fail()
      }
    }
  }
}
