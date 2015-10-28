package org.senkbeil.debugger.api.pipelines

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

import scala.collection.GenTraversableOnce
import scala.reflect.ClassTag

class CloseablePipelineSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("CloseablePipeline") {
    describe("#newPipeline") {
      it("should generate new closeable pipelines using the current pipeline's close function") {
        val mockCloseFunc = mockFunction[Unit]
        val closeablePipeline = new CloseablePipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc
        )

        mockCloseFunc.expects().once()

        val childCloseablePipeline = closeablePipeline.noop()
        childCloseablePipeline.close(now = true)
      }
    }

    describe("#close") {
      it("should invoke the close function immediately if 'now' is true") {
        val mockCloseFunc = mockFunction[Unit]
        val closeablePipeline = new CloseablePipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc
        )

        mockCloseFunc.expects().once()

        closeablePipeline.close(now = true)
      }

      it("should generate a new pipeline containing the close operation if 'now' is false") {
        val mockCloseFunc = mockFunction[Unit]
        val closeablePipeline = new CloseablePipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc
        )

        mockCloseFunc.expects().never()

        closeablePipeline.close(now = false)

        closeablePipeline.children.head shouldBe a [CloseablePipeline[_, _]]
      }
    }


    describe("#newPipeline") {
      it("should create a new pipeline with a no-op based on the class type") {
        import scala.language.existentials

        def getErasure[A, B](
          operation: Operation[A, B]
        )(implicit aClassTag: ClassTag[A], bClassTag: ClassTag[B]) = {
          (aClassTag.runtimeClass, bClassTag.runtimeClass)
        }

        val operation = CloseablePipeline.newPipeline(
          classOf[AnyRef], mockFunction[Unit]
        ).operation

        val (inputClass, outputClass) = getErasure(operation)

        operation shouldBe a [NoOperation[_]]
        inputClass should be (classOf[AnyRef])
        outputClass should be (classOf[AnyRef])
      }

      it("should create a closeable pipeline using the provided close function") {
        val mockCloseFunction = mockFunction[Unit]

        val closeablePipeline = CloseablePipeline.newPipeline(
          classOf[AnyRef], mockCloseFunction
        )

        mockCloseFunction.expects().once()

        closeablePipeline.close()
      }
    }
  }
}
