package org.senkbeil.debugger.api.pipelines

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

import scala.collection.GenTraversableOnce
import scala.reflect.ClassTag

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

    describe("#union") {
      it("should union with another pipeline such that processing the union processes both pipelines") {
        val pipeline1 = new Pipeline(mock[Operation[Int, Int]])
        val pipeline2 = new Pipeline(mock[Operation[Int, Int]])

        val unionPipeline = pipeline1.union(pipeline2)
        val data = Seq(1, 2, 3)

        // NOTE: Checking operations of pipelines as it is easier to test
        (pipeline1.operation.process _).expects(data).once()
        (pipeline2.operation.process _).expects(data).once()

        unionPipeline.process(data: _*)
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
    }
  }
}
