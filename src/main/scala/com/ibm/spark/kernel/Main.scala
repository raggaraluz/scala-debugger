package com.ibm.spark.kernel

import com.ibm.spark.kernel.debugger.Debugger
import com.sun.jdi._
import collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

object Main extends App {
  val debugger = new Debugger("127.0.0.1", 9877)

  debugger.start()

  println("Options to give other JVMs: " + debugger.RemoteJvmOptions)

  while (true) {
    println("Total connected JVMs: " + debugger.getVirtualMachines.size)
    debugger.getVirtualMachines.foreach(virtualMachine => {
      println("Virtual Machine: " + virtualMachine.name())
      val eventRequestManager = virtualMachine.eventRequestManager()

      val mapOfReferenceTypes = virtualMachine.allClasses().asScala.groupBy(
        referenceType => Try(referenceType.sourceName()).getOrElse("UNKNOWN"))

      //println("<KEYS>")
      //mapOfReferenceTypes.keys.foreach(println)

      //println("<UNKNOWN>")
      //mapOfReferenceTypes("UNKNOWN").foreach { referenceType =>
      //  Try(println(referenceType.name()))
      //  Try(println("comes from " + referenceType.sourceName()))
      //}
      //val dummyMainClassRef = virtualMachine.allClasses().asScala
        //.filter(_.name().contains("DummyMain"))
        //.find(_.name().contains("DummyMain"))
      println("<DummyMain.scala LOCATIONS>")
      val line = 10
      val locations = Helpers.locations("DummyMain.scala", line, mapOfReferenceTypes)
      println("Locations for line " + line)
      locations.foreach(println)

      println("<DummyMain.scala>")
      mapOfReferenceTypes("DummyMain.scala").foreach { referenceType =>
        println(referenceType.name())
        println("comes from " + referenceType.sourceName())
        println("source debug extension: " + Try(referenceType.sourceDebugExtension()).getOrElse("NONE"))
        /*println("Available Strata: " +
          referenceType.availableStrata().asScala.mkString(","))

        println("Fields: " + Try(referenceType.fields().asScala.mkString(",")).getOrElse(""))
        println("Methods: " + Try(referenceType.methods().asScala.mkString(",")).getOrElse(""))
        println("Source Name: " + Try(referenceType.sourceName()).getOrElse(""))
        Try(referenceType.allLineLocations().asScala.foreach(location => {
          if (location.lineNumber() == 8) {
            val breakpoint = eventRequestManager.createBreakpointRequest(location)
            breakpoint.setEnabled(true)
          }
          println(location.lineNumber() + ": " + location.declaringType().name())
          println("--> Code Index: " + location.codeIndex())
          println("--> Method Name: " + location.method().name())
        }))
        virtualMachine.allThreads().asScala.foreach(thread => {
          //println("<THREAD " + thread.uniqueID() + ">")
          thread.suspend()
          thread.frames().asScala.foreach(stackFrame => {
            /*println("Arguments: " +
              Try(stackFrame.getArgumentValues.asScala
                .map(a => s"${a.toString}: ${a.`type`()}")
                .mkString("\n")).getOrElse(""))*/
            /*println("Local Variables: \n" +
              Try(stackFrame.visibleVariables().asScala
                .map(v => s"${v.name()} = ${Helpers.valueSummary(stackFrame.getValue(v))}")
                .mkString("\n")).getOrElse(""))*/
          })
          thread.resume()
        })*/
      }
    })

    Thread.sleep(5000)
  }
}

object Helpers {
  def locations(fileName: String, line: Int,
    fileToUnits: Map[String, mutable.Buffer[ReferenceType]]): Set[Location] =
  {
    // Group locations by file and line
    case class LocationClass(loc: Location) {
      override def equals(that: Any): Boolean = that match {
        case that: Location =>
          loc.sourcePath == that.sourcePath &&
            loc.sourceName == that.sourceName &&
            loc.lineNumber == that.lineNumber
        case _ => false
      }
      override def hashCode: Int = loc.lineNumber.hashCode ^ loc.sourceName.hashCode
    }

    val buf = mutable.HashSet[LocationClass]()
    val key = fileName
    for (types <- fileToUnits.get(key)) {
      for (t <- types) {
        for (m <- t.methods().asScala) {
          try { buf ++= m.locationsOfLine(line).asScala.map(LocationClass.apply) } catch {
            case e: AbsentInformationException =>
          }
        }
        try { buf ++= t.locationsOfLine(line).asScala.map(LocationClass.apply) } catch {
          case e: AbsentInformationException =>
        }
      }
    }
    buf.map(_.loc).toSet
  }

  private def lastNameComponent(s: String): String = {
    "^.*?\\.([^\\.]+)$".r.findFirstMatchIn(s) match {
      case Some(m) => m.group(1)
      case None => s
    }
  }
  def valueSummary(value: Value): String = {
    value match {
      case v: BooleanValue => v.value().toString
      case v: ByteValue => v.value().toString
      case v: CharValue => "'" + v.value().toString + "'"
      case v: DoubleValue => v.value().toString
      case v: FloatValue => v.value().toString
      case v: IntegerValue => v.value().toString
      case v: LongValue => v.value().toString
      case v: ShortValue => v.value().toString
      case v: VoidValue => "void"
      case v: StringReference => "\"" + v.value() + "\""
      case v: ArrayReference =>
        val length = v.length()
        if (length > 3)
          "Array[" + v.getValues(0, 3).asScala.map(valueSummary).mkString(", ") + ",...]"
        else
          "Array[" + v.getValues.asScala.map(valueSummary).mkString(", ") + "]"
      case v: ObjectReference =>
        val tpe = v.referenceType()
        if (tpe.name().matches("^scala\\.runtime\\.[A-Z][a-z]+Ref$")) {
          val elemField = tpe.fieldByName("elem")
          valueSummary(v.getValue(elemField))
        } else "Instance of " + lastNameComponent(v.referenceType().name())
      case _ => "NA"
    }
  }
}
