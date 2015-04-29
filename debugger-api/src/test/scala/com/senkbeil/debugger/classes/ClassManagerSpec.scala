package com.senkbeil.debugger.classes

import com.sun.jdi.{ReferenceType, VirtualMachine}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers, OneInstancePerTest}

import scala.collection.JavaConverters._

class ClassManagerSpec extends FunSpec with Matchers with BeforeAndAfter
  with MockFactory with OneInstancePerTest
{
  private val TotalMockReferenceTypes = 5

  // Create N reference types with unique names and paths
  // (mixed Scala/Java/unknown) and
  private val stubReferenceTypes =
    (1 to TotalMockReferenceTypes).map(_ => stub[ReferenceType])
  stubReferenceTypes.foreach(r =>
    (r.name _).when().returns(java.util.UUID.randomUUID().toString))
  stubReferenceTypes.zipWithIndex.foreach { case (r, i) =>
    val extension =
      if (i % 3 == 0) "scala"
      else if (i % 3 == 1) "java"
      else "unknown"

    (r.sourcePaths _).when(*).returns(
      Seq(java.util.UUID.randomUUID().toString + "." + extension).asJava
    )
  }

  // Create virtual machine with stubbed reference types for classes
  private val stubVirtualMachine = stub[VirtualMachine]
  (stubVirtualMachine.allClasses _).when().returns(stubReferenceTypes.asJava)

  private val classManager =
    new ClassManager(stubVirtualMachine, loadClasses = true)

  describe("ClassManager") {
    describe("constructor") {
      it("should refresh the class listing if told to load classes") {
        classManager.allFileNames should contain theSameElementsAs
          stubReferenceTypes.map(_.sourcePaths("").asScala.head)
      }

      it("should not refresh the class listing if flag is set to false") {
        val classManager =
          new ClassManager(stubVirtualMachine, loadClasses = false)

        classManager.allFileNames shouldBe empty
      }
    }

    describe("#linesAndLocationsForFile") {
      it("should return a map whose keys represent available lines") {
        fail()
      }

      it("should return a map whose values correspond to available locations per line") {
        fail()
      }

      it("should throw an exception if the file is not found in the cache") {
        fail()
      }
    }

    describe("#underlyingReferencesFor") {
      it("should return a collection of reference types matching the class found in the cache") {
        fail()
      }

      it("should throw an exception if the class is not found in the cache") {
        fail()
      }
    }

    describe("#allClassNames") {
      it("should refresh the classes and references") {
        fail()
      }

      it("should not refresh the classes and references if refresh == false") {
        fail()
      }

      it("should retrieve only Scala classes") {
        fail()
      }

      it("should return a collection of Scala class names") {
        fail()
      }
    }
  }
}
