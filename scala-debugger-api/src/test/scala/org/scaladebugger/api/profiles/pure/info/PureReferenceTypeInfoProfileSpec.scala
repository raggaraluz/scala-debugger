package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureReferenceTypeInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockNewFieldProfile = mockFunction[Field, VariableInfoProfile]
  private val mockNewMethodProfile = mockFunction[Method, MethodInfoProfile]
  private val mockNewLocationProfile = mockFunction[Location, LocationInfoProfile]
  private val mockNewObjectProfile = mockFunction[ObjectReference, ObjectInfoProfile]
  private val mockNewClassLoaderProfile = mockFunction[ClassLoaderReference, ClassLoaderInfoProfile]
  private val mockNewClassObjectProfile = mockFunction[ClassObjectReference, ClassObjectInfoProfile]
  private val mockNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val pureReferenceTypeInfoProfile = new PureReferenceTypeInfoProfile(
    scalaVirtualMachine = mockScalaVirtualMachine,
    referenceType = mockReferenceType
  ) {
    override protected def newFieldProfile(
      field: Field
    ): VariableInfoProfile = mockNewFieldProfile(field)

    override protected def newMethodProfile(
      method: Method
    ): MethodInfoProfile = mockNewMethodProfile(method)

    override protected def newObjectProfile(
      objectReference: ObjectReference
    ): ObjectInfoProfile = mockNewObjectProfile(objectReference)

    override protected def newLocationProfile(
      location: Location
    ): LocationInfoProfile = mockNewLocationProfile(location)

    override protected def newClassObjectProfile(
      classObjectReference: ClassObjectReference
    ): ClassObjectInfoProfile = mockNewClassObjectProfile(classObjectReference)

    override protected def newClassLoaderProfile(
      classLoaderReference: ClassLoaderReference
    ): ClassLoaderInfoProfile = mockNewClassLoaderProfile(classLoaderReference)

    override protected def newReferenceTypeProfile(
      referenceType: ReferenceType
    ): ReferenceTypeInfoProfile = mockNewReferenceTypeProfile(referenceType)
  }

  describe("PureReferenceTypeInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockReferenceType

        val actual = pureReferenceTypeInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#getAllFields") {
      it("should return a collection of profiles wrapping all fields in the underlying reference type") {
        val expected = Seq(mock[VariableInfoProfile])
        val fields = Seq(mock[Field])

        import scala.collection.JavaConverters._
        (mockReferenceType.allFields _).expects()
          .returning(fields.asJava).once()

        expected.zip(fields).foreach { case (e, f) =>
          mockNewFieldProfile.expects(f).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.getAllFields

        actual should be (expected)
      }
    }

    describe("#getVisibleFields") {
      it("should return a collection of profiles wrapping visible fields in the underlying reference type") {
        val expected = Seq(mock[VariableInfoProfile])
        val fields = Seq(mock[Field])

        import scala.collection.JavaConverters._
        (mockReferenceType.visibleFields _).expects()
          .returning(fields.asJava).once()

        expected.zip(fields).foreach { case (e, f) =>
          mockNewFieldProfile.expects(f).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.getVisibleFields

        actual should be (expected)
      }
    }

    describe("#getField") {
      it("should throw a NoSuchElement exception if no field with matching name is found") {
        val name = "someName"

        // Lookup the field and return null indicating no field found
        (mockReferenceType.fieldByName _).expects(name)
          .returning(null).once()

        intercept[NoSuchElementException] {
          pureReferenceTypeInfoProfile.getField(name)
        }
      }

      it("should return a profile wrapping the associated field if found") {
        val expected = mock[VariableInfoProfile]
        val name = "someName"

        // Lookup the field
        val mockField = mock[Field]
        (mockReferenceType.fieldByName _).expects(name)
          .returning(mockField).once()

        // Create the new profile
        mockNewFieldProfile.expects(mockField).returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.getField(name)

        actual should be (expected)
      }
    }

    describe("#getAllMethods") {
      it("should return a collection of profiles wrapping all methods in the underlying reference type") {
        val expected = Seq(mock[MethodInfoProfile])
        val methods = Seq(mock[Method])

        import scala.collection.JavaConverters._
        (mockReferenceType.allMethods _).expects()
          .returning(methods.asJava).once()

        expected.zip(methods).foreach { case (e, m) =>
          mockNewMethodProfile.expects(m).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.getAllMethods

        actual should be (expected)
      }
    }

    describe("#getVisibleMethods") {
      it("should return a collection of profiles wrapping visible methods in the underlying reference type") {
        val expected = Seq(mock[MethodInfoProfile])
        val methods = Seq(mock[Method])

        import scala.collection.JavaConverters._
        (mockReferenceType.visibleMethods _).expects()
          .returning(methods.asJava).once()

        expected.zip(methods).foreach { case (e, m) =>
          mockNewMethodProfile.expects(m).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.getVisibleMethods

        actual should be (expected)
      }
    }

    describe("#getMethods") {
      it("should return a collection of profiles wrapping methods with matching names in the underlying reference type") {
        val expected = Seq(mock[MethodInfoProfile])
        val methods = Seq(mock[Method])
        val name = "someName"

        import scala.collection.JavaConverters._
        (mockReferenceType.methodsByName(_: String)).expects(name)
          .returning(methods.asJava).once()

        expected.zip(methods).foreach { case (e, m) =>
          mockNewMethodProfile.expects(m).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.getMethods(name)

        actual should be (expected)
      }
    }

    describe("#getClassLoader") {
      it("should return a profile wrapping the class loader of the reference type") {
        val expected = mock[ClassLoaderInfoProfile]
        val classLoader = mock[ClassLoaderReference]

        (mockReferenceType.classLoader _).expects()
          .returning(classLoader).once()

        mockNewClassLoaderProfile.expects(classLoader)
          .returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.getClassLoader

        actual should be (expected)
      }
    }

    describe("#getClassObject") {
      it("should return a profile wrapping the class object of the reference type") {
        val expected = mock[ClassObjectInfoProfile]
        val classObject = mock[ClassObjectReference]

        (mockReferenceType.classObject _).expects()
          .returning(classObject).once()

        mockNewClassObjectProfile.expects(classObject)
          .returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.getClassObject

        actual should be (expected)
      }
    }

    describe("#getGenericSignature") {
      it("should return Some(signature) if it exists") {
        val expected = Some("signature")

        (mockReferenceType.genericSignature _).expects()
          .returning(expected.get).once()

        val actual = pureReferenceTypeInfoProfile.getGenericSignature

        actual should be (expected)
      }

      it("should return None if no signature exists") {
        val expected = None

        (mockReferenceType.genericSignature _).expects()
          .returning(null).once()

        val actual = pureReferenceTypeInfoProfile.getGenericSignature

        actual should be (expected)
      }
    }

    describe("#getInstances") {
      it("should throw an exception if a negative value is provided") {
        intercept[IllegalArgumentException] {
          pureReferenceTypeInfoProfile.getInstances(-1)
        }
      }

      it("should return a collection of profiles wrapping all instances returned from the underlying reference type") {
        val expected = Seq(mock[ObjectInfoProfile])
        val objectReferences = Seq(mock[ObjectReference])

        import scala.collection.JavaConverters._
        (mockReferenceType.instances _).expects(0L)
          .returning(objectReferences.asJava).once()

        expected.zip(objectReferences).foreach { case (e, o) =>
          mockNewObjectProfile.expects(o).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.getInstances(0)

        actual should be (expected)
      }
    }

    describe("#isAbstract") {
      it("should return the state of the underlying reference type") {
        val expected = true

        (mockReferenceType.isAbstract _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.isAbstract

        actual should be (expected)
      }
    }

    describe("#isFinal") {
      it("should return the state of the underlying reference type") {
        val expected = true

        (mockReferenceType.isFinal _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.isFinal

        actual should be (expected)
      }
    }

    describe("#isInitialized") {
      it("should return the state of the underlying reference type") {
        val expected = true

        (mockReferenceType.isInitialized _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.isInitialized

        actual should be (expected)
      }
    }

    describe("#isPrepared") {
      it("should return the state of the underlying reference type") {
        val expected = true

        (mockReferenceType.isPrepared _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.isPrepared

        actual should be (expected)
      }
    }

    describe("#isVerified") {
      it("should return the state of the underlying reference type") {
        val expected = true

        (mockReferenceType.isVerified _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.isVerified

        actual should be (expected)
      }
    }

    describe("#getAllLineLocations") {
      it("should return a collection of profiles wrapping all line locations in the underlying reference type") {
        val expected = Seq(mock[LocationInfoProfile])
        val locations = Seq(mock[Location])

        import scala.collection.JavaConverters._
        (mockReferenceType.allLineLocations: Function0[java.util.List[Location]])
          .expects()
          .returning(locations.asJava)
          .once()

        expected.zip(locations).foreach { case (e, l) =>
          mockNewLocationProfile.expects(l).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.getAllLineLocations

        actual should be (expected)
      }
    }

    describe("#getLocationsOfLine") {
      it("should return a collection of profiles wrapping all locations for the specified line in the underlying reference type") {
        val expected = Seq(mock[LocationInfoProfile])
        val locations = Seq(mock[Location])
        val line = 999

        import scala.collection.JavaConverters._
        (mockReferenceType.locationsOfLine(_: Int)).expects(line)
          .returning(locations.asJava).once()

        expected.zip(locations).foreach { case (e, l) =>
          mockNewLocationProfile.expects(l).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.getLocationsOfLine(line)

        actual should be (expected)
      }
    }

    describe("#getMajorVersion") {
      it("should return the major version of the underlying reference type") {
        val expected = 999

        (mockReferenceType.majorVersion _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.getMajorVersion

        actual should be (expected)
      }
    }

    describe("#getMinorVersion") {
      it("should return the minor version of the underlying reference type") {
        val expected = 999

        (mockReferenceType.minorVersion _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.getMinorVersion

        actual should be (expected)
      }
    }

    describe("#getName") {
      it("should return the name of the underlying reference type") {
        val expected = "some.class.name"

        (mockReferenceType.name _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.getName

        actual should be (expected)
      }
    }

    describe("#getNestedTypes") {
      it("should return a collection of profiles wrapping all nested types in the underlying reference type") {
        val expected = Seq(mock[ReferenceTypeInfoProfile])
        val referenceTypes = Seq(mock[ReferenceType])

        import scala.collection.JavaConverters._
        (mockReferenceType.nestedTypes _).expects()
          .returning(referenceTypes.asJava).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          mockNewReferenceTypeProfile.expects(r).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.getNestedTypes

        actual should be (expected)
      }
    }

    describe("#getSourceDebugExtension") {
      it("should return the source debug extension of the underlying reference type") {
        val expected = "debug"

        (mockReferenceType.sourceDebugExtension _).expects()
          .returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.getSourceDebugExtension

        actual should be (expected)
      }
    }

    describe("#getSourceNames") {
      it("should return all source names for the underlying reference type") {
        val expected = Seq("file1.scala", "file2.scala")
        val stratumName = "some name"

        (mockReferenceType.defaultStratum _).expects()
          .returning(stratumName).once()

        import scala.collection.JavaConverters._
        (mockReferenceType.sourceNames _).expects(stratumName)
          .returning(expected.asJava).once()

        val actual = pureReferenceTypeInfoProfile.getSourceNames

        actual should be (expected)
      }
    }

    describe("#getSourcePaths") {
      it("should return all source paths for the underlying reference type") {
        val expected = Seq("path/to/file1.scala", "path/to/file2.scala")
        val stratumName = "some name"

        (mockReferenceType.defaultStratum _).expects()
          .returning(stratumName).once()

        import scala.collection.JavaConverters._
        (mockReferenceType.sourcePaths _).expects(stratumName)
          .returning(expected.asJava).once()

        val actual = pureReferenceTypeInfoProfile.getSourcePaths

        actual should be (expected)
      }
    }
  }
}
