package com.senkbeil.debugger.classes

import com.sun.jdi.{ReferenceType, VirtualMachine}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers, OneInstancePerTest}
import test.JDIMockHelpers

import scala.collection.JavaConverters._

class ClassManagerSpec extends FunSpec with Matchers with BeforeAndAfter
  with MockFactory with OneInstancePerTest with JDIMockHelpers
{
  private val TotalMockReferenceTypes = 5

  private def indexToExtension(index: Int) =
    if (index % 3 == 0)       "scala"
    else if (index % 3 == 1)  "java"
    else                      "unknown"

  private val stubReferenceTypes = (1 to TotalMockReferenceTypes).map { i =>
    createRandomReferenceTypeStub(indexToExtension(i))
  }

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
      it("should return a map containing lines and associated locations for the file") {
        val referenceType = stubReferenceTypes.head
        val fileName = referenceType.sourcePaths("STUBBED").asScala.head

        val expected =
          referenceType.allLineLocations().asScala.groupBy(_.lineNumber())
        val actual = classManager.linesAndLocationsForFile(fileName)

        actual should contain theSameElementsAs expected
      }

      it("should not include locations whose line numbers cannot be retrieved") {
        // TODO: Should use static data like line 1, 2, 3, 4 and file1, file2,
        //       etc to test without doing things like groupBy above
        fail()
      }

      it("should throw an exception if the file is not found in the cache") {
        intercept[IllegalArgumentException] {
          classManager.linesAndLocationsForFile("asdf")
        }
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
