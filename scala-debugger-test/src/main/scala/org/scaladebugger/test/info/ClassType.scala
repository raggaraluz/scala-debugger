package org.scaladebugger.test.info

import org.scaladebugger.test.java.{JavaEnumerations, JavaStaticMethods}

/**
 * Provides tests for examining class type information.
 *
 * @note Should have a class name of org.scaladebugger.test.info.ClassType
 */
object ClassType {
  private trait BaseInterface
  private trait InterfaceFromBaseInterface extends BaseInterface
  private class ClassFromInterfaceFromBaseInterface extends InterfaceFromBaseInterface

  private class BaseClass
  private class ClassFromBaseClass extends BaseClass
  private class ClassFromIndirectBaseClass extends ClassFromBaseClass

  private class ClassFromBaseInterface extends BaseInterface
  private class ClassFromIndirectBaseInterface extends ClassFromBaseInterface

  private class NotEnumerationClass
  private class EnumerationClass extends Enumeration

  private class ClassWithMethods {
    def method1(): Unit = {}
    def method2(x: Int): Int = x
    def method3(x: Int): Int = x
    def method3(x: Int, y: String): String = x + y
  }

  private object ClassWithStaticMethods {
    def staticMethod1(): Unit = {}
    def staticMethod2(x: Int): Int = x
    def staticMethod3(x: Int): Int = x
    def staticMethod3(x: Int, y: String): String = x + y
  }

  private class ClassWithConstructor(x: Int, y: String) {
    def this() = this(0, "")
  }

  private class ClassWithFields {
    var baseClass: BaseClass = new BaseClass
    var i: Int = 999
  }

  def main(args: Array[String]): Unit = {
    // Must reference classes for them to be properly loaded
    val classFromInterfaceFromBaseInterface =
      new ClassFromInterfaceFromBaseInterface
    val baseClass = new BaseClass
    val classFromBaseClass = new ClassFromBaseClass
    val classFromIndirectBaseClass = new ClassFromIndirectBaseClass
    val classFromBaseInterface = new ClassFromBaseInterface
    val classFromIndirectBaseInterface = new ClassFromIndirectBaseInterface
    val notEnumerationClass = new NotEnumerationClass
    val enumerationClass = new EnumerationClass
    val javaEnumeration = JavaEnumerations.ONE
    val classWithMethods = new ClassWithMethods
    ClassWithStaticMethods.staticMethod1()
    val classWithConstructor = new ClassWithConstructor()
    val classWithFields = new ClassWithFields
    val classFromBaseClassUsingBaseClassDefinition: BaseClass =
      new ClassFromBaseClass
    val javaStaticMethodsClass = new JavaStaticMethods

    val x = 999 // Insert a breakpoint here
    println(x)
  }
}
