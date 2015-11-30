package org.senkbeil.debugger.api.pipelines

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

import scala.collection.GenTraversableOnce
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

class PipelineSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("Pipeline") {
    describe("#process") {
      it("should perform the pipeline's operation on the provided data") {
        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)
        val data = Seq(1, 2, 3)

        (mockOperation.process _).expects(data).once()

        pipeline.process(data: _*)
      }

      it("should recursively call children pipelines based on the process results") {
        val mockOperation = mock[Operation[Int, Int]]
        val mockChildOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)

        val data = Seq(1, 2, 3)
        val result = Seq(7, 8, 9, 10)

        (mockOperation.process _).expects(data).returning(result).once()
        (mockChildOperation.process _).expects(result).once()

        // Add a child pipeline
        pipeline.transform(mockChildOperation)

        pipeline.process(data: _*)
      }

      it("should return the transformed data at this point in the pipeline") {
        val expected = Success(Seq(1, 2, 3))

        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)
        val data = expected.get.map(_ - 1)

        (mockOperation.process _).expects(data).returning(expected.get).once()

        val actual = pipeline.process(data: _*)
        actual should be (expected)
      }

      it("should recursively call the failed pipeline if the process fails") {
        val throwable = new Throwable
        val expected = Failure(throwable)

        val mockFailedOperation = mock[Operation[Throwable, Throwable]]
        val failedPipeline = new Pipeline(mockFailedOperation)

        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation) {
          override def failed: Pipeline[Throwable, Throwable] = failedPipeline
        }

        val data = Seq(1, 2, 3)

        (mockOperation.process _).expects(data).throwing(throwable).once()
        (mockFailedOperation.process _).expects(Seq(throwable)).once()

        val actual = pipeline.process(data: _*)
        actual should be (expected)
      }
    }

    describe("#transform") {
      it("should create a new child pipeline using the provided operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val mockOperation = mock[Operation[Int, Int]]
        val childPipeline = pipeline.transform(mockOperation)

        childPipeline.operation should be (mockOperation)
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val mockOperation = mock[Operation[Int, Int]]
        val childPipeline = pipeline.transform(mockOperation)

        pipeline.children should contain (childPipeline)
      }
    }

    describe("#map") {
      it("should create a new child pipeline using the map operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.map(mockFunction[Int, Int])

        childPipeline.operation shouldBe a [MapOperation[_, _]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.map(mockFunction[Int, Int])

        pipeline.children should contain (childPipeline)
      }
    }

    describe("#flatMap") {
      it("should create a new child pipeline using the flatMap operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.flatMap(
          mockFunction[Int, GenTraversableOnce[Int]]
        )

        childPipeline.operation shouldBe a [FlatMapOperation[_, _]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.flatMap(
          mockFunction[Int, GenTraversableOnce[Int]]
        )

        pipeline.children should contain (childPipeline)
      }
    }

    describe("#filter") {
      it("should create a new child pipeline using the filter operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.filter(mockFunction[Int, Boolean])

        childPipeline.operation shouldBe a [FilterOperation[_]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.filter(mockFunction[Int, Boolean])

        pipeline.children should contain (childPipeline)
      }
    }

    describe("#filterNot") {
      it("should create a new child pipeline using the filterNot operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.filterNot(mockFunction[Int, Boolean])

        childPipeline.operation shouldBe a [FilterNotOperation[_]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.filterNot(mockFunction[Int, Boolean])

        pipeline.children should contain (childPipeline)
      }
    }

    describe("#foreach") {
      it("should create a new child pipeline using the foreach operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        pipeline.foreach(mockFunction[Int, Unit])

        pipeline.children.head.operation shouldBe a [ForeachOperation[_]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        pipeline.foreach(mockFunction[Int, Unit])

        pipeline.children.head.operation shouldBe a [ForeachOperation[_]]
      }
    }

    describe("#unionInput") {
      it("should union with another pipeline such that processing the union processes both pipelines") {
        val pipeline1 = new Pipeline(mock[Operation[Int, Int]])
        val pipeline2 = new Pipeline(mock[Operation[Int, Int]])

        val unionPipeline = pipeline1.unionInput(pipeline2)
        val data = Seq(1, 2, 3)

        // NOTE: Checking operations of pipelines as it is easier to test
        (pipeline1.operation.process _).expects(data).once()
        (pipeline2.operation.process _).expects(data).once()

        unionPipeline.process(data: _*)
      }

      it("should create a pipeline whose close invokes neither unioned pipelines' close methods") {
        val mockCloseFunc1 = mockFunction[Unit]
        val pipeline1 = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc1
        )

        val mockCloseFunc2 = mockFunction[Unit]
        val pipeline2 = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc2
        )

        val unionInputPipeline = pipeline1.unionInput(pipeline2)

        mockCloseFunc1.expects().never()
        mockCloseFunc2.expects().never()

        unionInputPipeline.close(now = true)
      }
    }

    describe("#unionOutput") {
      it("should union with another pipeline such that processing the either pipeline processes the union") {
        val mockOperation1 = mock[Operation[Int, Int]]
        val pipeline1 = new Pipeline(mockOperation1)

        val mockOperation2 = mock[Operation[Int, Int]]
        val pipeline2 = new Pipeline(mockOperation2)

        val unionPipeline = pipeline1.unionOutput(pipeline2)
        val data = Seq(1, 2, 3)

        // NOTE: Adding mock operation as easier to test
        val mockAfterUnionOperation = mock[Operation[Int, Int]]
        unionPipeline.transform(mockAfterUnionOperation)

        (mockOperation1.process _).expects(data).returning(data).once()
        (mockOperation2.process _).expects(data).returning(data).once()
        (mockAfterUnionOperation.process _).expects(data).twice()

        pipeline1.process(data: _*)
        pipeline2.process(data: _*)
      }

      it("should create a pipeline whose close invokes both unioned pipelines' close methods") {
        val mockCloseFunc1 = mockFunction[Unit]
        val pipeline1 = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc1
        )

        val mockCloseFunc2 = mockFunction[Unit]
        val pipeline2 = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc2
        )

        val unionOutputPipeline = pipeline1.unionOutput(pipeline2)

        mockCloseFunc1.expects().once()
        mockCloseFunc2.expects().once()

        unionOutputPipeline.close(now = true)
      }
    }

    describe("#close") {
      it("should invoke the close function immediately if 'now' is true") {
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc
        )

        mockCloseFunc.expects().once()

        pipeline.close(now = true)
      }

      it("should generate a new pipeline containing the close operation if 'now' is false") {
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc
        )

        mockCloseFunc.expects().never()

        pipeline.close(now = false)

        pipeline.children.head shouldBe a [Pipeline[_, _]]
      }

      it("should invoke the close function immediately if no flag given") {
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc
        )

        mockCloseFunc.expects().once()

        pipeline.close()
      }
    }

    describe("#toFuture") {
      it("should return a future that evaluates the next time the pipeline is evaluated") {
        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)

        val pipelineFuture = pipeline.toFuture

        (mockOperation.process _).expects(Seq(1, 2, 3))
          .returning(Seq(1, 2, 3)).once()

        pipeline.process(1, 2, 3)
        pipelineFuture.value.get.isSuccess should be (true)
      }

      it("should return a future that closes the pipeline upon success") {
        val mockOperation = mock[Operation[Int, Int]]
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = new Pipeline(mockOperation, mockCloseFunc)

        val pipelineFuture = pipeline.toFuture

        (mockOperation.process _).expects(Seq(1, 2, 3))
          .returning(Seq(1, 2, 3)).once()
        mockCloseFunc.expects().once()

        pipeline.process(1, 2, 3)
        pipelineFuture.value.get.isSuccess should be (true)
      }

      it("should return a future that evaluates to a failure when the pipeline fails") {
        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)

        val pipelineFuture = pipeline.toFuture

        (mockOperation.process _).expects(Seq(1, 2, 3))
          .throwing(new Throwable).once()

        pipeline.process(1, 2, 3)
        pipelineFuture.value.get.isFailure should be (true)
      }

      it("should return a future that closes the pipeline upon failure") {
        val mockOperation = mock[Operation[Int, Int]]
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = new Pipeline(mockOperation, mockCloseFunc)

        val pipelineFuture = pipeline.toFuture

        (mockOperation.process _).expects(Seq(1, 2, 3))
          .throwing(new Throwable).once()
        mockCloseFunc.expects().once()

        pipeline.process(1, 2, 3)
        pipelineFuture.value.get.isFailure should be (true)
      }
    }

    describe("#noop") {
      it("should create a new child pipeline using a no-op") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.noop()

        childPipeline.operation shouldBe a [NoOperation[_]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.noop()

        pipeline.children should contain (childPipeline)
      }
    }

    describe("#children") {
      it("should be empty when the pipeline is first created") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        pipeline.children should be (empty)
      }

      it("should return the current children contained by the pipeline") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        pipeline.transform(mock[Operation[Int, Int]])

        pipeline.children should have length (1)
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

        val operation = Pipeline.newPipeline(classOf[AnyRef]).operation

        val (inputClass, outputClass) = getErasure(operation)

        operation shouldBe a [NoOperation[_]]
        inputClass should be (classOf[AnyRef])
        outputClass should be (classOf[AnyRef])
      }

      it("should create a new pipeline with the specified close function") {
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = Pipeline.newPipeline(classOf[AnyRef], mockCloseFunc)

        mockCloseFunc.expects().once()

        pipeline.close()
      }
    }
  }
}
